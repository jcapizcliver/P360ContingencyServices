package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.PropertiesManager;
import mx.com.liverpool.p360.services.core.WriteAttributesForo;

/**
 * Servlet implementation class AutForoWrite
 */
@WebServlet("/public/rt/AutForoWrite")
public class AutForoWrite extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AutForoWrite() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String baseDirectory = PropertiesManager.get("p360.contingency.base_directory", "/u01/stage");
		if(baseDirectory == null || "".equals(baseDirectory)) {
			baseDirectory = "/u01/stage/";
		}
		String lkpCharsFile = PropertiesManager.get("p360.contingency.lookup_characteristics_file", "/u01/stage/cache/participantes");
		String baseUrl = PropertiesManager.get("p360.contingency.base_url", "http://172.18.237.162:1512/rest/V2.0");
		String authorization = PropertiesManager.get("p360.contingency.basic_token_auth", "cmVzdDpoZWlsZXI=");
		request.setCharacterEncoding("UTF-8");
		java.io.BufferedReader br = request.getReader();
		String line = null;
		String randomFileName = "BeibiGPF_" + System.currentTimeMillis() + ".json";
		StringBuilder sb = new StringBuilder();
		Object rawResponse = null;
		while((line = br.readLine()) != null) {
			sb.append(line);
		}
		try{
			
			org.json.JSONObject rootRequest = new org.json.JSONObject(sb.toString());

			try(java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(baseDirectory + randomFileName), java.nio.charset.Charset.forName("UTF-8")))){
					pw.println(rootRequest);
			}catch(java.io.IOException e) {
				e.printStackTrace();
			}
			WriteAttributesForo waf = new WriteAttributesForo();
			rawResponse = waf.processRequest(new String[] { baseDirectory + randomFileName, lkpCharsFile, baseUrl, authorization /* "cmVzdDpoZWlsZXI=" */ });
			java.nio.file.Files.delete(java.nio.file.Paths.get( baseDirectory + randomFileName));
			if(rawResponse instanceof org.json.JSONObject) {
				org.json.JSONObject rsp = (org.json.JSONObject) rawResponse;
				if(rsp.has("Error")){
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				}
			}
		}catch(org.json.JSONException | ClassCastException e) {
			rawResponse = new org.json.JSONObject().put("Error", "Input was not a json object in expected format.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(rawResponse);
		
	}

}
