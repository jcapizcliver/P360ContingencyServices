/*package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;
import java.io.FileNotFoundException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.gcp.placeholder.PlaceholderStatusJobRunner;
import mx.com.liverpool.p360.services.core.gcp.placeholder.PlaceholderStatusProcessResult;

/**
 * Manual HTTP trigger for the placeholder status file job.
 */
/*
@WebServlet("/public/rt/ProcessPlaceholderStatusFile")
public class ProcessPlaceholderStatusFile extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        execute(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        execute(request, response);
    }

    private void execute(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Content-Type", "application/json");
        response.setHeader("Accept", "application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String path = request.getParameter("path");
            PlaceholderStatusProcessResult result = new PlaceholderStatusJobRunner().run(path);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(result.toJson());
        } catch (FileNotFoundException | IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(new org.json.JSONObject()
                    .put("status", "error")
                    .put("message", e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(new org.json.JSONObject()
                    .put("status", "error")
                    .put("message", e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
        }
    }
}
*/