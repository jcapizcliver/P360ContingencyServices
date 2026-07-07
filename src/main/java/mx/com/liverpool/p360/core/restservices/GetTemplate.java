package mx.com.liverpool.p360.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.GetTemplateInformation;
import mx.com.liverpool.p360.services.core.PropertiesManager;

/**
 * Servlet implementation class GetTemplate
 */
@WebServlet("/public/rt/GetTemplate")
public class GetTemplate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * Default constructor. 
     */
    public GetTemplate() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String baseUrl = PropertiesManager.get( "p360.contingency.base_url" );
		String encoded = PropertiesManager.get( "p360.contingency.basic_token_auth" );
		
		String template = request.getParameter("template");
		String business = request.getParameter("business");
		String creationType = request.getParameter( "creationType" );
		String externalInformation = request.getParameter("externalInformation");
		String asi = request.getParameter("aSAPInt");
		
		GetTemplateInformation gti = new GetTemplateInformation();
		String rawResponse =  gti.processRequest(template, business, externalInformation, baseUrl, encoded, creationType); // gti.handleStart(new String[] {template, business, externalInformation});
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(rawResponse);

//		response.setStatus(HttpServletResponse.SC_GONE);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
//	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		// TODO Auto-generated method stub
//		doGet(request, response);
//	}

}
