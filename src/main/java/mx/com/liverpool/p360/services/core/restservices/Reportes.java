package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.EchamelosCompa;
import mx.com.liverpool.p360.services.core.PropertiesManager;

/**
 * Servlet implementation class Reportes
 */
@WebServlet("/public/rt/GetReportFieldValues")
public class Reportes extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Reportes() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String baseUrl = PropertiesManager.get( "p360.contingency.base_url" );
		String encoded = PropertiesManager.get( "p360.contingency.basic_token_auth" );
		
		String baseDirectory = PropertiesManager.get("p360.contingency.base_directory");
		if(baseDirectory == null || "".equals(baseDirectory)) {
			baseDirectory = "/u01/stage/";
		}
		request.setCharacterEncoding("UTF-8");
		java.io.BufferedReader br = request.getReader();
		String line = null;
//		String randomFileName = "BeibiGRFV_" + System.currentTimeMillis() + ".json";
		StringBuilder sb = new StringBuilder();
		org.json.JSONObject rawResponse = null;
		while((line = br.readLine()) != null) {
			sb.append(line);
		}
		try{
			org.json.JSONObject rootRequest = new org.json.JSONObject(sb.toString());
			EchamelosCompa ec = new EchamelosCompa(baseUrl, encoded);
			rawResponse = ec.processRequest(new String[] { rootRequest.getString("input") });
		}catch(org.json.JSONException e) {
			rawResponse = new org.json.JSONObject().put("Error", "Input was not a json object.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
//		response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(rawResponse);
	}

}
