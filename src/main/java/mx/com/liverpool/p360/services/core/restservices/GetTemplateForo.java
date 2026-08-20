package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.PropertiesManager;
import mx.com.liverpool.p360.services.core.TemplateAttributesForo;

/**
 * Servlet implementation class GetTemplateForo
 */
@WebServlet("/public/rt/GetTemplateForo")
public class GetTemplateForo extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetTemplateForo() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String baseUrl = PropertiesManager.get( "p360.contingency.base_url" );
		String encoded = PropertiesManager.get( "p360.contingency.basic_token_auth" );
		String externalInformation = request.getParameter("externalInformation");
		String template = request.getParameter("template");
		
		try(TemplateAttributesForo ga = new TemplateAttributesForo()){
			Object rawResponse =  ga.processRequest(new String[] {template, baseUrl
					, encoded, externalInformation});
			response.setHeader("Content-Type", "application/json");
			response.setHeader("Accept", "application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(rawResponse);
		}
		
		
	}


}
