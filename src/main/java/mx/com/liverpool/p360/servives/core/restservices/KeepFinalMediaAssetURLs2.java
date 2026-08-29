package mx.com.liverpool.p360.servives.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.ImageTrafficLimiter;
import mx.com.liverpool.p360.services.core.PropertiesManager;

@WebServlet("/public/rt/KeepFinalMediaAssetURLs2")
public class KeepFinalMediaAssetURLs2 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final java.util.concurrent.atomic.AtomicLong SEQ = new java.util.concurrent.atomic.AtomicLong();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader br = request.getReader()) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }

        try {
            org.json.JSONObject rootRequest = new org.json.JSONObject(sb.toString());
            String baseUrl = PropertiesManager.get("p360.contingency.base_url");
            String encoded = PropertiesManager.get("p360.contingency.basic_token_auth");
            String templatesCacheDirectory = PropertiesManager.get("p360.contingency.templates_cache_directory");
            String doDeleteInputFile = PropertiesManager.get("p360.contingency.do_delete_input_file");
            boolean x = Boolean.parseBoolean(request.getParameter("x"));

            mx.com.liverpool.p360.services.core.CreateProposalFrozenMediaURLs cp = new mx.com.liverpool.p360.services.core.CreateProposalFrozenMediaURLs(baseUrl, encoded, SEQ.incrementAndGet());
            String rawResponse = cp.doIt(new String[] {
                    rootRequest.getString("input"), templatesCacheDirectory, doDeleteInputFile
            }, x);
            if (ImageTrafficLimiter.isBusyResponse(rawResponse)) {
                response.setStatus(429);
                response.setHeader("Retry-After", "5");
            }
            response.getWriter().println(rawResponse);
        } catch (org.json.JSONException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"Error\":\"Invalid JSON input\"}");
        }
    }
}
