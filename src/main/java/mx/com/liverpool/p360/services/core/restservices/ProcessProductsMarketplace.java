package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import mx.com.liverpool.p360.services.core.temp.dataloader.LoadEUMarketplace;

/**
 * Servlet implementation class ProcessProductsMarketplace
 */
@WebServlet("/public/rt/ProcessProductsMarketplace")
public class ProcessProductsMarketplace extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ProcessProductsMarketplace() {
        super();
        // TODO Auto-generated constructor stub
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String line = null;
		StringBuilder sb = new StringBuilder();
		java.io.BufferedReader br = request.getReader();
		String fileName = "/u01/stage/EUMkt/MktPC_" + new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS").format(new java.util.Date());
		response.setContentType("application/json");
		try(java.io.StringWriter sw0 = new java.io.StringWriter(); 
				java.io.PrintWriter sw = new java.io.PrintWriter( sw0 );
				java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter( new java.io.FileOutputStream((fileName))))){
			while( (line = br.readLine()) != null ) {
				sw.println(line);
				pw.println(line);
				sb.append(line);
			}
			response.getWriter().println(new org.json.JSONObject().put("message", fileName).put("status", "received"));
			try {
				new LoadEUMarketplace().readProducts(true, true, new java.io.ByteArrayInputStream(sw0.getBuffer().toString().getBytes()) );
			} catch (SAXException | ParserConfigurationException e) {
				e.printStackTrace();
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println(new org.json.JSONObject().put("status", "error").put("message", "problem processing request"));
			}
		}catch(java.io.IOException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(new org.json.JSONObject().put("status", "error").put("message", "problem processing request"));
		}
	}

}
