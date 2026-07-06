package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ReceiveSTEPFileGral2
 */
@WebServlet("/public/rt/ReceiveSTEPFileGral2")
public class ReceiveSTEPFileGral2 extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ReceiveSTEPFileGral2() {
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
		String charset = request.getHeader("charset");
		try(java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream("/u01/stage/STEP/OLC/Gral_" + new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS").format(new java.util.Date())), charset == null ? java.nio.charset.StandardCharsets.UTF_8 : java.nio.charset.Charset.forName(charset)))){
			while( (line = br.readLine()) != null ) {
				pw.println(line);
				sb.append(line).append(System.lineSeparator());
			}
		}catch(java.io.IOException e) {
			e.printStackTrace();
		}
		response.getWriter().println(sb.toString());
	}

}
