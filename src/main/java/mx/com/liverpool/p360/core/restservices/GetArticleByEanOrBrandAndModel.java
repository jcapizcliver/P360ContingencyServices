package mx.com.liverpool.p360.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.PropertiesManager;

/**
 * Servlet implementation class GetArticleByEanOrBrandAndModel
 */
@WebServlet("/public/rt/GetArticleByEanOrBrandAndModel")
public class GetArticleByEanOrBrandAndModel extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetArticleByEanOrBrandAndModel() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
   	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   	 */
   	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
   		
//		String basePath = PropertiesManager.get("p360.contingency.base_directory") + "/p360_temp/";// "/u01/stage/p360_temp/";
		String baseUrl = PropertiesManager.get("p360.contingency.base_url");
		String encoded = PropertiesManager.get("p360.contingency.basic_token_auth");
		request.setCharacterEncoding("UTF-8");
		java.io.BufferedReader br = request.getReader();
		String line = null;
//		String randomFileName = "GetArticleByEanOrBrandAndModel_" + System.currentTimeMillis() + ".json";
		StringBuilder sb = new StringBuilder();
		String rawResponse = null;
//		org.json.JSONObject rootRequest = null;
		while((line = br.readLine()) != null) {
			sb.append(line);
		}
		try{
//			rootRequest = new org.json.JSONObject(sb.toString());
			mx.com.liverpool.p360.services.core.GetVariantByEanOrBrandAndModel elese = 
					new mx.com.liverpool.p360.services.core.GetVariantByEanOrBrandAndModel(baseUrl);
			elese.loadInput(sb.toString());
			rawResponse = elese.processFile(encoded);
			
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
