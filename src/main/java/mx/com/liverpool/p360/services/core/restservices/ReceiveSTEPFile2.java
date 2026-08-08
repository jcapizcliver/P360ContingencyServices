package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.RESTWrapper;
import mx.com.liverpool.p360.services.core.temp.xml.local.LoadProductDataPipeline;

/**
 * Servlet implementation class ReceiveSTEPFile2
 */
@WebServlet("/public/rt/ReceiveSTEPFile2")
public class ReceiveSTEPFile2 extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ReceiveSTEPFile2() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    private static final RESTWrapper rw = new RESTWrapper();

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long init = System.currentTimeMillis();
		logMe("Processing request...");
		String line = null;
		StringBuilder sb = new StringBuilder();
		java.io.BufferedReader br = request.getReader();
		String charset = request.getHeader("charset");
		String fn = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
		java.nio.file.Path p = java.nio.file.Paths.get("/", "u01", "stage", "STEP", "m2");
		if(!java.nio.file.Files.exists(p)) {
			java.nio.file.Files.createDirectories(p);
		}
		if(!java.nio.file.Files.exists(java.nio.file.Paths.get( p.toString(), fn))) {
			java.nio.file.Files.createDirectories( java.nio.file.Paths.get(p.toString(), fn));
		}
		while( (line = br.readLine()) != null ) {
			sb.append(line).append(System.lineSeparator());
		}
		java.nio.file.Path fp = java.nio.file.Paths.get(p.toString(), fn, "Gral2_" + new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS").format(new java.util.Date()) + ".xml");
		try(java.io.PrintWriter pw = 
				new java.io.PrintWriter(
						new java.io.OutputStreamWriter(
								new java.io.FileOutputStream(
										fp.toFile()
										), charset == null ? java.nio.charset.StandardCharsets.UTF_8 : java.nio.charset.Charset.forName(charset)))){
			pw.println(sb.toString());
		}catch(java.io.IOException e) {
			e.printStackTrace();
		}
		LoadProductDataPipeline.processContent(sb.toString(), fp.toString());
		response.getWriter().println(sb.toString());
		logMe("done Processing request: " + rw.getRw().formatTime( System.currentTimeMillis() - init ));
	}
	
	private void logMe(String message) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream("../logs/receiveFile2.log", true)))) {
            pw.println("[" + (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()))
                    + "]  " + message);
        } catch (java.io.IOException e) {
        }
    }

}
