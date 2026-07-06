package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import mx.com.liverpool.p360.services.core.AITítulosYDescripciones;

/**
 * Servlet implementation class RestTitlesIAToSTEPXMLCreation
 */
@WebServlet("/public/rt/RestTitlesIAToSTEPXMLCreation")
public class RestTitlesIAToSTEPXMLCreation extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RestTitlesIAToSTEPXMLCreation() {
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
		try{
			while( (line = br.readLine()) != null ) {
				sb.append(line);
				sb.append("\n");
			}
			AITítulosYDescripciones ait = new AITítulosYDescripciones();
			ait.processData( new java.io.ByteArrayInputStream(sb.toString().getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)) );
		}catch(java.io.IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	
	}

}
