package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.json.JSONObject;

import mx.com.liverpool.p360.services.core.QuickJdbcConnectionManager;
import mx.com.liverpool.p360.services.core.SimpleDelimitedFileParser;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 50 * 1024 * 1024,
    maxRequestSize = 55 * 1024 * 1024
)
@WebServlet("/public/rt/CsvForFamiliaConVariantes")
public class CsvForFamiliaConVariantes extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int BATCH_SIZE = 2000;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        JSONObject responseJson = new JSONObject();

        Connection con = null;
        String tempTableName = "TMP_FAMILIA_" + (System.currentTimeMillis() % 10000000L);

        try {
            Part filePart = req.getPart("csvFile");

            if (filePart == null || filePart.getSize() == 0) {
                responseJson.put("success", false);
                responseJson.put("message", "No se recibió ningún archivo");
                out.print(responseJson.toString());
                return;
            }

            String fileName = filePart.getSubmittedFileName();

            QuickJdbcConnectionManager manager = new QuickJdbcConnectionManager();
            con = manager.openConnection(false);

            try (PreparedStatement ps = con.prepareStatement(
                "CREATE GLOBAL TEMPORARY TABLE " + tempTableName + 
                " (ID_PROPUESTA VARCHAR2(50), ID_VARIANTE VARCHAR2(50)) ON COMMIT PRESERVE ROWS")) {
                ps.execute();
            }

            String insertSQL = "INSERT INTO " + tempTableName + " (ID_PROPUESTA, ID_VARIANTE) VALUES (?, ?)";
            PreparedStatement insertPs = con.prepareStatement(insertSQL);

            AtomicInteger counter = new AtomicInteger(0);
            AtomicInteger totalRows = new AtomicInteger(0);

            final Connection finalCon = con;

            SimpleDelimitedFileParser parser = new SimpleDelimitedFileParser(
                '"', ',', null, "\n", java.nio.charset.StandardCharsets.UTF_8, row -> {

                    if (row == null || row.length < 2) return;

                    String idPropuesta = row[0] != null ? row[0].trim() : "";
                    String idVariante  = row[1] != null ? row[1].trim() : "";

                    if ("ID Propuesta".equalsIgnoreCase(idPropuesta) || "ID_PROPUESTA".equalsIgnoreCase(idPropuesta)) {
                        return;
                    }

                    try {
                        insertPs.setString(1, idPropuesta);
                        insertPs.setString(2, idVariante);
                        insertPs.addBatch();

                        if (counter.incrementAndGet() >= BATCH_SIZE) {
                            insertPs.executeBatch();
                            finalCon.commit();
                            counter.set(0);
                        }
                        totalRows.incrementAndGet();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

            parser.parse(filePart.getInputStream());

            // Batch restante
            if (counter.get() > 0) {
                insertPs.executeBatch();
                finalCon.commit();
            }

            responseJson.put("success", true);
            responseJson.put("fileName", fileName);
            responseJson.put("tempTable", tempTableName);
            responseJson.put("totalRows", totalRows.get());
            responseJson.put("message", "Archivo procesado y cargado correctamente");

        } catch (Exception e) {
            responseJson.put("success", false);
            responseJson.put("message", "Error: " + e.getMessage());
            e.printStackTrace();
            if (con != null) {
                try { con.rollback(); } catch (Exception ignore) {}
            }
        } finally {
            if (con != null) {
                try {
                    try (PreparedStatement ps = con.prepareStatement("DROP TABLE " + tempTableName)) {
                        ps.execute();
                    }
                } catch (Exception ignore) {}
                try { con.close(); } catch (Exception ignore) {}
            }
        }

        out.print(responseJson.toString());
    }
}