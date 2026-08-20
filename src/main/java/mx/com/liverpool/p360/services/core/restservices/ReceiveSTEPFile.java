package mx.com.liverpool.p360.services.core.restservices;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
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

@WebServlet("/public/rt/ReceiveSTEPFile")
public class ReceiveSTEPFile extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Path LANDING_DIRECTORY =
            Path.of("/", "u01", "stage", "STEP", "inbox");

    private static final Path LOG_PATH =
            Path.of("..", "logs", "receiveFile.log");

    private static final int COPY_BUFFER_SIZE = 64 * 1024;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	long totalInit = System.nanoTime();

    	String inbox = "/u01/stage/STEP/inbox";
    	Files.createDirectories(Path.of(inbox));

    	String fileName = "Gral_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + ".xml";

    	Path finalFile = Path.of(inbox, fileName);
    	Path partFile = Path.of(inbox, fileName + ".part");

    	long contentLength = request.getContentLengthLong();

    	logMe("Receiving STEP payload U01: part=" + partFile + " contentLength=" + contentLength);

    	long bytes = 0;
    	long readNs = 0;
    	long writeNs = 0;
    	long moveNs = 0;
    	long readCalls = 0;

    	try (
    			InputStream in = request.getInputStream();
    			OutputStream out = new BufferedOutputStream(new FileOutputStream(partFile.toFile()), 64 * 1024)
    	) {
    		byte[] buffer = new byte[64 * 1024];

    		while (true) {
    			long t = System.nanoTime();
    			int read = in.read(buffer);
    			readNs += System.nanoTime() - t;

    			if (read == -1) {
    				break;
    			}

    			readCalls++;

    			t = System.nanoTime();
    			out.write(buffer, 0, read);
    			writeNs += System.nanoTime() - t;

    			bytes += read;
    		}

    		long t = System.nanoTime();
    		out.flush();
    		writeNs += System.nanoTime() - t;
    	}

    	long t = System.nanoTime();

    	Files.move(
    			partFile,
    			finalFile,
    			StandardCopyOption.REPLACE_EXISTING,
    			StandardCopyOption.ATOMIC_MOVE
    	);

    	moveNs = System.nanoTime() - t;

    	long totalNs = System.nanoTime() - totalInit;

    	double totalMs = totalNs / 1_000_000.0;
    	double readMs = readNs / 1_000_000.0;
    	double writeMs = writeNs / 1_000_000.0;
    	double moveMs = moveNs / 1_000_000.0;
    	double mb = bytes / 1024.0 / 1024.0;
    	double mbps = totalNs > 0 ? mb / (totalNs / 1_000_000_000.0) : 0;

    	logMe(String.format(
    			"STEP U01 landed: file=%s bytes=%d totalMs=%.3f readMs=%.3f writeMs=%.3f moveMs=%.3f readCalls=%d MBps=%.3f",
    			finalFile, bytes, totalMs, readMs, writeMs, moveMs, readCalls, mbps
    	));

    	response.setStatus(HttpServletResponse.SC_OK);
    	response.setCharacterEncoding("UTF-8");
    	response.setContentType("text/plain");
    	response.getWriter().print("OK");
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
