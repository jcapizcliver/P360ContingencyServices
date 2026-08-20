package mx.com.liverpool.p360.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.PropertiesManager;
import mx.com.liverpool.p360.services.core.RESTWorkshop;

/**
 * Servlet implementation class BrandsGroupOfArticleByTemplateBusinessProvider
 */
@WebServlet("/public/rt/BrandsGroupOfArticleByTemplateBusinessProvider")
public class BrandsGroupOfArticleByTemplateBusinessProvider extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BrandsGroupOfArticleByTemplateBusinessProvider() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String idPlantilla = request.getParameter("idPlantilla");
		String negocio = request.getParameter("negocio");
		String proveedor = request.getParameter("proveedor");
		String baseUrl = PropertiesManager.get("p360.contingency.base_url");
		
		try(mx.com.liverpool.p360.services.core.BrandsGroupOfArticleByTemplateBusinessProvider runnablePiece = new mx.com.liverpool.p360.services.core.BrandsGroupOfArticleByTemplateBusinessProvider()){
			RESTWorkshop workshop = new RESTWorkshop();
			workshop.setBaseUrl(baseUrl);
			workshop.getRc().getHeader().put("Authorization", "Basic: " + PropertiesManager.get("p360.contingency.basic_token_auth"));
			String rawResponse = runnablePiece.otroRun(new String[] { proveedor, idPlantilla, negocio }, workshop);
			response.setHeader("Content-Type", "application/json");
			response.setHeader("Accept", "application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(rawResponse);
		}
	}

	
	public static void main(String[] args) {
		String proveedor = "20325";
		String idPlantilla = "EU4-113578";
		String negocio = "Liverpool";
		try(mx.com.liverpool.p360.services.core.BrandsGroupOfArticleByTemplateBusinessProvider runnablePiece = new mx.com.liverpool.p360.services.core.BrandsGroupOfArticleByTemplateBusinessProvider()){
			String rawResponse = runnablePiece.otroRun(new String[] { proveedor, idPlantilla, negocio }, new RESTWorkshop());
			System.out.println(rawResponse);
		}catch(java.io.IOException e) {
			e.printStackTrace();
		}
	}
	
}
