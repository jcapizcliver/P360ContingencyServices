package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.gcp.placeholder.PlaceholderStatusBucketJob;
import mx.com.liverpool.p360.services.core.gcp.placeholder.PlaceholderStatusProcessResult;

/**
 * Manual HTTP trigger for the placeholder status bucket job.
 */
@WebServlet("/public/rt/ProcessPlaceholderStatusFile")
public class ProcessPlaceholderStatusFile extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        execute(response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        execute(response);
    }

    private void execute(HttpServletResponse response) throws IOException {
        response.setHeader("Content-Type", "application/json");
        response.setHeader("Accept", "application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            PlaceholderStatusProcessResult result = new PlaceholderStatusBucketJob().run();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(result.toJson());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(new org.json.JSONObject()
                    .put("status", "error")
                    .put("message", e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
        }
    }
}
