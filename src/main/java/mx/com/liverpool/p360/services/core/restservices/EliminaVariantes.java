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
 * Servlet implementation class EliminaVariantes
 */
@WebServlet("/public/rt/EliminaVariantes")
public class EliminaVariantes extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EliminaVariantes() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String variantIds = request.getParameter("variantIds");
		if(variantIds != null && !"".equals(variantIds)) {
			StringBuilder sb = new StringBuilder();
			String[] pieces = variantIds.split(",");
			for(int i=0; i<pieces.length; i++) {
				sb.append(i == 0 ? "" : ",");
				sb.append("'").append(pieces[i].trim()).append("'@'MASTER'");
			}
			String baseUrl = PropertiesManager.get("p360.contingency.base_url");
			String encoded = PropertiesManager.get("p360.contingency.basic_token_auth");
			RESTWorkshop workshop = new RESTWorkshop();
			workshop.setBaseUrl(baseUrl);
			workshop.getRc().getHeader().put("Authorization", "Basic: " + encoded);
			workshop.putParameter("items", sb.toString());
			logMe("Deleting... " + sb.toString());
			workshop.getRc().getHeader().put("Content-Type", "application/x-www-form-urlencoded");
			org.json.JSONObject resp = workshop.makeRequest("DELETE", "/list/Article/byItems");
			response.getWriter().println(resp.toString());
		}else {
			response.getWriter().println(new org.json.JSONObject().put("message", "No variantIds to delete."));
		}
	}
	

	private void logMe(String message){
		try(java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream("../logs/eliminaVariantes.log", true)))){
		  pw.println("[" + (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())) + "] " + message);
		}catch(java.io.IOException e){}
	}

}
