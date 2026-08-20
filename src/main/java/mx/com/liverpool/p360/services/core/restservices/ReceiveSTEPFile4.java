package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/public/rt/ReceiveSTEPFile4")
public class ReceiveSTEPFile4 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Path LANDING_DIRECTORY =
            Path.of("/", "u01", "stage", "STEP", "inbox");

    private static final Path LOG_PATH =
            Path.of("..", "logs", "receiveFile4.log");

    private static final int COPY_BUFFER_SIZE = 64 * 1024;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();
        Files.createDirectories(LANDING_DIRECTORY);

        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS")
                .format(new Date());
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String baseName = "Gral_" + timestamp + "_" + unique;

        Path partFile = LANDING_DIRECTORY.resolve(baseName + ".xml.part");
        Path finalFile = LANDING_DIRECTORY.resolve(baseName + ".xml");

        long expectedBytes = request.getContentLengthLong();
        long receivedBytes = 0L;

        logMe("Receiving STEP payload: part=" + partFile
                + " contentLength=" + expectedBytes);

        try {
            try (InputStream in = request.getInputStream();
                 OutputStream out = Files.newOutputStream(
                         partFile,
                         StandardOpenOption.CREATE_NEW,
                         StandardOpenOption.WRITE)) {

                byte[] buffer = new byte[COPY_BUFFER_SIZE];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    receivedBytes += read;
                }
            }

            if (expectedBytes >= 0L && receivedBytes != expectedBytes) {
                throw new IOException(
                        "Incomplete STEP payload. Expected " + expectedBytes
                        + " bytes but received " + receivedBytes);
            }

            if (receivedBytes == 0L) {
                throw new IOException("Empty STEP payload received");
            }

            moveCompletedUpload(partFile, finalFile);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("text/plain");
            response.getWriter().print("OK");

            logMe("STEP payload landed: file=" + finalFile
                    + " bytes=" + receivedBytes
                    + " elapsedMs=" + (System.currentTimeMillis() - start));

        } catch (Exception e) {
            try {
                Files.deleteIfExists(partFile);
            } catch (IOException cleanupException) {
                e.addSuppressed(cleanupException);
            }

            logE(e);
            logMe("FAILED receiving STEP payload: part=" + partFile
                    + " bytes=" + receivedBytes
                    + " elapsedMs=" + (System.currentTimeMillis() - start)
                    + " error=" + e.getClass().getName()
                    + ": " + String.valueOf(e.getMessage()));

            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new ServletException("Error landing STEP payload", e);
        }
    }

    private static void moveCompletedUpload(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target);
        }
    }

    private static synchronized void logMe(String message) {
        String line = "["
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())
                + "]  " + message;

        try {
            Path parent = LOG_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (java.io.BufferedWriter writer = Files.newBufferedWriter(
                    LOG_PATH,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException ignored) {
        }
    }

    private static synchronized void logE(Exception ex) {
        try {
            Path parent = LOG_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (java.io.PrintWriter pw = new java.io.PrintWriter(
                    new java.io.OutputStreamWriter(
                            Files.newOutputStream(
                                    LOG_PATH,
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.APPEND),
                            StandardCharsets.UTF_8))) {
                ex.printStackTrace(pw);
            }
        } catch (IOException ignored) {
        }
    }
}
