package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.PropertiesManager;

/**
 * Servlet implementation class ProveedoresMigrados
 */
@WebServlet("/public/rt/ProveedoresMigrados")
public class ProveedoresMigrados extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ProveedoresMigrados() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		org.json.JSONObject jr = new org.json.JSONObject();
		org.json.JSONArray objects = new org.json.JSONArray();
		jr.put("objects", objects);
		java.util.Set<String> proveedoresMigrados = cargaProveedoresMigrados();
		if(proveedoresMigrados != null) {
			for(String proveedorId : proveedoresMigrados) {
				if(!"".equals(proveedorId))
					objects.put(proveedorId);
			}
		}
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(jr.toString());
	}

	private java.util.Set<String> cargaProveedoresMigrados(){
		try {
			return java.nio.file.Files.lines(java.nio.file.Paths.get(PropertiesManager.get("p360.contingency.base_directory"), "cache", "proveedores_migrados"), java.nio.charset.StandardCharsets.UTF_8).collect(java.util.stream.Collectors.toSet());
		}catch(java.io.IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
