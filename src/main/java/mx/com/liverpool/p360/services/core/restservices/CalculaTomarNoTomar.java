package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.AgarraloONo;
import mx.com.liverpool.p360.services.core.DBAccessDataStub;
import mx.com.liverpool.p360.services.core.ELog;
import mx.com.liverpool.p360.services.core.PropertiesManager;
import mx.com.liverpool.p360.services.core.RESTWorkshop;

/**
 * Servlet implementation class CalculaTomarNoTomar
 */
@WebServlet("/public/rt/CalculaTomarNoTomar")
public class CalculaTomarNoTomar extends HttpServlet {
	private static final long serialVersionUID = 1L;
//	private static final String BASE_URL = "http://172.18.237.162:1512/rest/V2.0";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CalculaTomarNoTomar() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String externalId = request.getParameter("proposalId");
		if(externalId != null && !"".equals(externalId)) {
			RESTWorkshop rw = new RESTWorkshop();
			rw.setBaseUrl( PropertiesManager.get("p360.contingency.base_url") );
			rw.getRc().getHeader().put("Authorization", "Basic: " + PropertiesManager.get("p360.contingency.basic_token_auth"));
			try(DBAccessDataStub dastub = new DBAccessDataStub( new ELog() {
				
				@Override
				public void logE(Exception e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void log(String message) {
					// TODO Auto-generated method stub
					
				}
			}
			)){
				AgarraloONo a = new AgarraloONo(dastub);
				a.checale(externalId, rw.getBaseUrl());
			}
		}
	}

}
