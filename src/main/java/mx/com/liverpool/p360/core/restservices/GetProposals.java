package mx.com.liverpool.p360.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.PropertiesManager;

/**
 * Servlet implementation class GetProposals
 */
@WebServlet("/public/rt/GetProposals")
public class GetProposals extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final java.util.concurrent.atomic.AtomicLong SEQ = new java.util.concurrent.atomic.AtomicLong();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetProposals() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String baseUrl = PropertiesManager.get( "p360.contingency.base_url" );
		String encoded = PropertiesManager.get( "p360.contingency.basic_token_auth" );
		request.setCharacterEncoding("UTF-8");
		java.io.BufferedReader br = request.getReader();
		String line = null;
		StringBuilder sb = new StringBuilder();
		String rawResponse = null;
		while((line = br.readLine()) != null) {
			sb.append(line);
		}
		try{
			org.json.JSONObject rootRequest = new org.json.JSONObject(sb.toString());

			mx.com.liverpool.p360.services.core.GetProposals gp = new mx.com.liverpool.p360.services.core.GetProposals(baseUrl, encoded, SEQ.incrementAndGet());
			rawResponse = gp.run(new String[] { rootRequest.getString("input") } /*new String[] { basePath + randomFileName }*/);
			
		}catch(org.json.JSONException e) {
			rawResponse = new org.json.JSONObject().put("Error", "Input was not a json object.").toString();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(rawResponse);
		
	}

}
