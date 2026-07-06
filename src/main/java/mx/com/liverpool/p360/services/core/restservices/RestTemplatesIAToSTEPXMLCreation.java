package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import mx.com.liverpool.p360.services.core.AITemplatesCreation;

/**
 * Servlet implementation class RestTemplatesIAToSTEPXMLCreation
 */
@WebServlet("/public/rt/RestTemplatesIAToSTEPXMLCreation")
public class RestTemplatesIAToSTEPXMLCreation extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RestTemplatesIAToSTEPXMLCreation() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String line = null;
		StringBuilder sb = new StringBuilder();
		java.io.BufferedReader br = request.getReader();
		try{
			while( (line = br.readLine()) != null ) {
				sb.append(line);
				sb.append("\n");
			}
			AITemplatesCreation ait = new AITemplatesCreation();
			try(java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream( java.nio.file.Files.createTempFile("iaRequest", ".xml").toFile() )))){
				pw.println(sb.toString());
			}catch(java.io.IOException e) {
				e.printStackTrace();
			}
			ait.processData( new java.io.ByteArrayInputStream(sb.toString().getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)) );
		}catch(java.io.IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}


}
