package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.PropertiesManager;
import mx.com.liverpool.p360.services.core.RESTWorkshop;

/**
 * Servlet implementation class GetListOfValuesComplete
 */
@WebServlet("/public/rt/GetListOfValuesComplete")
public class GetListOfValuesComplete extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetListOfValuesComplete() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String baseUrl = PropertiesManager.get( "p360.contingency.base_url" );
		String encoded = PropertiesManager.get( "p360.contingency.basic_token_auth" );
		String lookup = request.getParameter("lookup");
		
		if(lookup != null && !"".equals(lookup)) {
			
			RESTWorkshop workshop = new RESTWorkshop();
			workshop.setBaseUrl( baseUrl );
			workshop.getRc().getHeader().put("Authorization", "Basic: " + encoded);
			java.util.Map<String, String> qp = new java.util.TreeMap<>();
			qp.put("lookup", lookup);
			qp.put("pageSize", "1200");
			qp.put("fields", "LookupValue.Code,LookupValueLang.Name(es)");
			
			org.json.JSONObject resp = null;
			org.json.JSONArray rows = null;
			org.json.JSONArray values = null;
			
			int currentIndex = 0;
			int totalSize = 0;
			
			org.json.JSONArray responseArray = new org.json.JSONArray();
			
			do{
				qp.put("startIndex", String.valueOf(currentIndex));
				resp = workshop.makeRequest("GET", "/list/LookupValue/byLookup", qp, null);
				totalSize = resp.getInt("totalSize");
				rows = resp.getJSONArray("rows");
				for(int i=0; i<rows.length(); i++) {
					currentIndex++;
					values = rows.getJSONObject(i).getJSONArray("values");
					responseArray.put(new org.json.JSONObject().put("code", values.getString(0)).put("name", values.getString(1)));
				}
			}while(currentIndex < totalSize);
			currentIndex = 0;
			
			response.setHeader("Content-Type", "application/json");
			response.setHeader("Accept", "application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(new org.json.JSONObject().put("values", responseArray));
		}
		
	}

}
