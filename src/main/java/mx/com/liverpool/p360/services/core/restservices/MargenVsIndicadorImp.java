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
 * Servlet implementation class MargenVsIndicadorImp
 */
@WebServlet("/public/rt/MargenVsIndicadorImp")
public class MargenVsIndicadorImp extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MargenVsIndicadorImp() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String baseUrl = PropertiesManager.get( "p360.contingency.base_url" );
		String encoded = PropertiesManager.get( "p360.contingency.basic_token_auth" );
		
		String pageSize = request.getParameter("pageSize");
		String currentIndex = request.getParameter("startIndex");
		RESTWorkshop workshop = new RESTWorkshop();
		workshop.setBaseUrl(baseUrl);
		workshop.getRc().getHeader().put("Authorization", "Basic: " + encoded);
		
		java.util.TreeMap<String, String> qp = new java.util.TreeMap<>();
		
		if(pageSize != null && !"".equals(pageSize)) {
			try {
				qp.put("pageSize", String.valueOf(Integer.parseInt(pageSize)));
			}catch(NumberFormatException e) {
				
			}
		}
		if(currentIndex != null && !"".equals(currentIndex)) {
			try {
				qp.put("startIndex", String.valueOf( Integer.parseInt(currentIndex) ));
			}catch(NumberFormatException e) {
				
			}
		}
		
		org.json.JSONObject jsonResponse = null;
		org.json.JSONArray objects = new org.json.JSONArray();
		qp.put("fields", "StandardizationValue.Value,StandardizationValue.AlternativeValue");
		qp.put("dictionary", "MargenVsIndicadorImp");
		org.json.JSONArray rows = null;
		org.json.JSONArray values = null;
		
		jsonResponse = workshop.makeRequest("GET", "/list/StandardizationValue/byDictionary", qp, null);
		if(jsonResponse != null) {
			rows = jsonResponse.getJSONArray("rows");
			for(int i=0; i<rows.length(); i++) {
				values = rows.getJSONObject(i).getJSONArray("values");
				objects.put(new org.json.JSONObject().put("code", values.getString(0)).put("value", values.getString(1)));
			}
		}else {
			System.out.println("Error en petición de características VaD: " + workshop.getRawResponse());
		}
		response.getWriter().println( new org.json.JSONObject().put("values", objects).toString() );
	}


}
