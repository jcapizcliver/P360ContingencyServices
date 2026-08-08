package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.DBAccessDataStub;
import mx.com.liverpool.p360.services.core.ELog;

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
	String lookup = request.getParameter("lookup");
	if (lookup == null || "".equals(lookup)) {
		return;
	}
	org.json.JSONArray responseArray = new org.json.JSONArray();
	try (DBAccessDataStub dastub = new DBAccessDataStub( new ELog() {
		
		@Override
		public void logE(Exception e) {
		}
		
		@Override
		public void log(String message) {
		}
	} )) {
		java.util.Map<String, String> lookupValues = dastub.getLookupValueCodeNameMap( lookup, 10, true);
		for (java.util.Map.Entry<String, String> entry : lookupValues.entrySet()) {
			responseArray.put(new org.json.JSONObject().put("code", entry.getKey()).put("name", entry.getValue()));
		}
	}
	response.setHeader(
			"Content-Type",
			"application/json");
	response.setHeader(
			"Accept",
			"application/json");
	response.setCharacterEncoding("UTF-8");
	response.getWriter().println(new org.json.JSONObject().put("values", responseArray));	
	}

}
