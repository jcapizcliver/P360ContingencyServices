package mx.com.liverpool.p360.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.PropertiesManager;

/**
 * Servlet implementation class CreateProposal
 */
@WebServlet("/public/rt/CreateProposal")
public class CreateProposal extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final java.util.concurrent.atomic.AtomicLong SEQ = new java.util.concurrent.atomic.AtomicLong();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateProposal() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String baseUrl = PropertiesManager.get( "p360.contingency.base_url" );
		String encoded = PropertiesManager.get( "p360.contingency.basic_token_auth" );
//		String basePath = PropertiesManager.get("p360.contingency.base_directory") + "/p360_temp/"; // "/u01/stage/p360_temp/";
		String templatesCacheDirectory = PropertiesManager.get("p360.contingency.templates_cache_directory");
		String doDeleteInputFile = PropertiesManager.get("p360.contingency.do_delete_input_file");
		Boolean x = Boolean.parseBoolean( request.getParameter("x") );
		request.setCharacterEncoding("UTF-8");
		java.io.BufferedReader br = request.getReader();
		String line = null;
//		String randomFileName = "BeibiGP_" + System.currentTimeMillis() + ".json";
		StringBuilder sb = new StringBuilder();
		String rawResponse = null;
		while((line = br.readLine()) != null) {
			sb.append(line);
		}
		org.json.JSONObject rootRequest = new org.json.JSONObject(sb.toString());
//		try{
//			try(java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(basePath + randomFileName), java.nio.charset.Charset.forName("UTF-8")))){
//				pw.println(rootRequest.getString("input"));
//			}catch(java.io.IOException e) {
//				e.printStackTrace();
//			}
//		}catch(org.json.JSONException e) {
//			rawResponse = new org.json.JSONObject().put("Error", "Input was not a json object.").toString();
//			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//		}
		mx.com.liverpool.p360.services.core.CreateProposal cp = new mx.com.liverpool.p360.services.core.CreateProposal(baseUrl, encoded, SEQ.incrementAndGet());
		rawResponse = cp.doIt(new String[] {rootRequest.getString("input"), templatesCacheDirectory, doDeleteInputFile}, x);
//		try {
//			org.json.JSONObject resp = new org.json.JSONObject(rawResponse);
//			if(resp.has("Error")) {
//				if("Servidor no tardó en responder.".equals(resp.getString("Error"))) {
//					throw new ServiceUnavailableException(resp.getString("Error"));
//				}
//			}
//		}catch(org.json.JSONException e) {
//			
//		}
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(rawResponse);
	}

}
