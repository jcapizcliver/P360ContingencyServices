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
 * Servlet implementation class GetLastSentTimeBySKU
 */
@WebServlet("/public/rt/GetLastSentTimeBySKU")
public class GetLastSentTimeBySKU extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetLastSentTimeBySKU() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String skus = request.getParameter("skus");
		org.json.JSONArray responses = new org.json.JSONArray();
		String[] skuArray = skus == null ? new String[] {} : skus.split(",");
		java.util.Set<String> currentSKUs = new java.util.TreeSet<>();
		try(DBAccessDataStub dastub = new DBAccessDataStub(new ELog() { @Override public void log(String message){ GetLastSentTimeBySKU.this.logMe(message); } @Override public void logE(Exception e) { GetLastSentTimeBySKU.this.logE(e); } })){
			for(String sku : skuArray) {
				if(currentSKUs.contains(sku))
					continue;
				String[] data = dastub.variantSentToEcommBySKU(sku);
				if(data != null) {
					responses.put( new org.json.JSONObject().put("sku", data[0]).put("articleIdentifier", data[1]).put("parentProductIdentifier", data[2]).put("parentProductSKU", data[3]).put("ultimoTiempoDeEnvioEcomm", data[4]) );
				}else {
					responses.put( new org.json.JSONObject().put("sku", sku).put("articleIdentifier", "").put("parentProductIdentifier", "").put("parentProductSKU", "").put("ultimoTiempoDeEnvioEcomm", "") );
				}
				currentSKUs.add(sku);
			}
		}
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(responses.toString());
	}

	private void logMe(String message) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream("../logs/GetLastSentTimeBySKU.log", true)))) {
            pw.println("[" + (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()))
                    + "]  " + message);
        } catch (java.io.IOException e) {
        }
    }
	
	private void logE(Exception e) {
		try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
				new java.io.FileOutputStream("../logs/GetLastSentTimeBySKU.log", true)))) {
			pw.println("[" + (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()))
					+ "] Exception. ");
			e.printStackTrace(pw);
		} catch (java.io.IOException ex) {
		}
	}

}
