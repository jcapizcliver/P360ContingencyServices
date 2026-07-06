package mx.com.liverpool.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.RESTWorkshop;

/**
 * Servlet implementation class TestRestrictedAccess
 */
@WebServlet("/rt/TestRestrictedAccess-1")
public class TestRestrictedAccess extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TestRestrictedAccess() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RESTWorkshop rw = new RESTWorkshop();
		rw.setBaseUrl("http://gcpcatqap04.liverpool.com.mx:7080/process-engine");
		String infaTokenId = request.getHeader("IDS-SESSION-ID");
		rw.getRc().getHeader().put("IDS-SESSION-ID", infaTokenId);
		rw.makeRequest("GET", "/rt/TestRestrictedAccess-1");
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(rw.getRawResponse());
	}


}
