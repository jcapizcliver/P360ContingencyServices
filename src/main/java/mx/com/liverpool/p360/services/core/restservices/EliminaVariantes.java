package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.RESTWrapper;

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
				sb.append("'").append(pieces[i].trim()).append("'@1");
			}
			RESTWrapper rw = new RESTWrapper();
			java.util.Map<String, String> qp = new java.util.HashMap<>();
			qp.put("items", sb.toString());
			logMe("Deleting... " + sb.toString());
			logMe("Change");
			String[] dr = new String[1];
			dr[0] = null;
			rw.deleteData("list", "Article", null, "byItems", qp, r -> dr[0] = r);
			logMe("RR: " + dr[0]);
			logMe("RR2: " + rw.getRw().getRawResponse());
			response.getWriter().println(dr[0] == null ? rw.getRw().getRawResponse() : dr[0]);
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
