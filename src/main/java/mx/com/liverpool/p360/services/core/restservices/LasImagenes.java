package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class LasImagenes
 */
@WebServlet("/public/rt/LasImagenes")
public class LasImagenes extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LasImagenes() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		java.io.BufferedReader br = request.getReader();
		String line = null;
		StringBuilder sb = new StringBuilder();
		String rawResponse = null;
		while((line = br.readLine()) != null) {
			sb.append(line);
		}
		
		String fn = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
		java.nio.file.Path p = java.nio.file.Paths.get("/", "u01", "stage", "Imagenes", "LasImagenes");
		if(!java.nio.file.Files.exists(p)) {
			java.nio.file.Files.createDirectories(p);
		}
		if(!java.nio.file.Files.exists(java.nio.file.Paths.get( p.toString(), fn))) {
			java.nio.file.Files.createDirectories( java.nio.file.Paths.get(p.toString(), fn));
		}
		while( (line = br.readLine()) != null ) {
			sb.append(line);
		}
		
//		java.nio.file.Path fp = java.nio.file.Paths.get(p.toString(), fn, "LasImagenes_" + new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS").format(new java.util.Date()) + ".xml");
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
		mx.com.liverpool.p360.services.core.LasImagenes cp = new mx.com.liverpool.p360.services.core.LasImagenes();
		rawResponse = cp.doIt(rootRequest.getString("input"));
		
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(rawResponse);
	}

}
