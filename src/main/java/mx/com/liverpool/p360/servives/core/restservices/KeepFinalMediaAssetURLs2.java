package mx.com.liverpool.p360.servives.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.PropertiesManager;

/**
 * Servlet implementation class KeepFinalMediaAssetURLs2
 */
@WebServlet("/public/rt/KeepFinalMediaAssetURLs2")
public class KeepFinalMediaAssetURLs2 extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final java.util.concurrent.atomic.AtomicLong SEQ = new java.util.concurrent.atomic.AtomicLong();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public KeepFinalMediaAssetURLs2() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String baseUrl = PropertiesManager.get( "p360.contingency.base_url" );
		String encoded = PropertiesManager.get( "p360.contingency.basic_token_auth" );
		String templatesCacheDirectory = PropertiesManager.get("p360.contingency.templates_cache_directory");
		String doDeleteInputFile = PropertiesManager.get("p360.contingency.do_delete_input_file");
		Boolean x = Boolean.parseBoolean( request.getParameter("x") );
		request.setCharacterEncoding("UTF-8");
		java.io.BufferedReader br = request.getReader();
		String line = null;
		StringBuilder sb = new StringBuilder();
		String rawResponse = null;
		while((line = br.readLine()) != null) {
			sb.append(line);
		}
		String fn = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
		java.nio.file.Path p = java.nio.file.Paths.get("/", "u01", "stage", "Imagenes", "KeepFinalMediaAssets");
		if(!java.nio.file.Files.exists(p)) {
			java.nio.file.Files.createDirectories(p);
		}
		if(!java.nio.file.Files.exists(java.nio.file.Paths.get( p.toString(), fn))) {
			java.nio.file.Files.createDirectories( java.nio.file.Paths.get(p.toString(), fn));
		}
		while( (line = br.readLine()) != null ) {
			sb.append(line);
		}
//		java.nio.file.Path fp = java.nio.file.Paths.get(p.toString(), fn, "KeepFinalMeta_" + new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS").format(new java.util.Date()) + ".xml");
//		try(java.io.PrintWriter pw = 
//				new java.io.PrintWriter(
//						new java.io.OutputStreamWriter(
//								new java.io.FileOutputStream(
//										fp.toFile()
//									), java.nio.charset.StandardCharsets.UTF_8 ))){
//				pw.println(sb.toString());
//		}catch(java.io.IOException e) {
//			e.printStackTrace();
//		}
		org.json.JSONObject rootRequest = new org.json.JSONObject(sb.toString());
		mx.com.liverpool.p360.services.core.CreateProposalFrozenMediaURLs cp = new mx.com.liverpool.p360.services.core.CreateProposalFrozenMediaURLs(baseUrl, encoded, SEQ.incrementAndGet());
		rawResponse = cp.doIt(new String[] {rootRequest.getString("input"), templatesCacheDirectory, doDeleteInputFile}, x);
		
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(rawResponse);
	}

}
