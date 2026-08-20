package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.ImageTrafficLimiter;

@WebServlet("/public/rt/LasImagenes")
public class LasImagenes extends HttpServlet {
    private static final long serialVersionUID = 1L;

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
            String rawResponse = new mx.com.liverpool.p360.services.core.LasImagenes()
                    .doIt(rootRequest.getString("input"));
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
