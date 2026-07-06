package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.RESTWrapper;

/**
 * Servlet implementation class GetTemplates
 */
@WebServlet("/public/rt/GetTemplates")
public class GetTemplates extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetTemplates() {
        super();
        // TODO Auto-generated constructor stub
    }

    
    public static void main(String[] args) {
    	String structureSystemID = "CommercialECC";// request.getParameter("structureSystemID");
		RESTWrapper rw = new RESTWrapper();
		java.util.Map<String, String> qp = new java.util.HashMap<>();
		qp.put("structure", "'" + structureSystemID + "'");
		qp.put("pageSize", "4000");
		qp.put("fields", 
				   "StructureGroup.Identifier"
				+ ",StructureGroup.ParentIdentifier"
				+ ",StructureGroup.Level"
				+ ",StructureGroupLang.Name(es)"
				+ ",StructureGroupLang.Description(es)"
				+ ",StructureGroupLang.Synonym(es)"
			);
		java.util.List<org.json.JSONObject> root = new java.util.ArrayList<>();
		java.util.Map<String, org.json.JSONObject> data = new java.util.HashMap<>();
		rw.collectData("list", "StructureGroup", null, "byStructure", qp, row -> {
			org.json.JSONArray values = row.getJSONArray("values");
			org.json.JSONObject json = data.get(values.getString(0));
			if(json == null || !json.has("name")) {
				json = new org.json.JSONObject();
				json.put("id", values.getString(0));
				json.put("level", values.get(2));
				json.put("name", values.getString(3));
				json.put("description", values.getString(4));
				org.json.JSONArray keywords = values.getJSONArray(5);
				json.put("keywords",  keywords.length() == 1 && "".equals(keywords.getString(0)) ? new org.json.JSONArray() : keywords);
				if(json != null && !json.has("_children")) {
					json.put("_children", new org.json.JSONArray());
				}
				data.put(values.getString(0), json);
			}
			if("".equals(values.getString(1)) || "0".equals(values.getString(2))) {
				root.add(json);
			}
			org.json.JSONObject apá = data.get(values.getString(1));
			if(apá == null) {
				apá = new org.json.JSONObject();
				apá.put("id", values.getString(1));
				apá.put("level", Integer.parseInt(values.getString(2)) - 1);
				apá.put("_children", new org.json.JSONArray());
				data.put(values.getString(1), apá);
			}
			apá.getJSONArray("_children").put(json);
		});
		data.clear();
		System.out.println(root.get(0));
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String structureSystemID = request.getParameter("structureSystemID");
		String rawResponse = null;
		RESTWrapper rw = new RESTWrapper();
		java.util.Map<String, String> qp = new java.util.HashMap<>();
		qp.put("structure", "'" + structureSystemID + "'");
		qp.put("pageSize", "4000");
		qp.put("fields", 
				   "StructureGroup.Identifier"
				+ ",StructureGroup.ParentIdentifier"
				+ ",StructureGroup.Level"
				+ ",StructureGroupLang.Name(es)"
				+ ",StructureGroupLang.Description(es)"
				+ ",StructureGroupLang.Synonym(es)"
			);
		java.util.List<org.json.JSONObject> root = new java.util.ArrayList<>();
		java.util.Map<String, org.json.JSONObject> data = new java.util.HashMap<>();
		rw.collectData("list", "StructureGroup", null, "byStructure", qp, row -> {
			org.json.JSONArray values = row.getJSONArray("values");
			org.json.JSONObject json = data.get(values.getString(0));
			if(json == null || !json.has("name")) {
				json = new org.json.JSONObject();
				json.put("id", values.getString(0));
				json.put("level", values.get(2));
				json.put("name", values.getString(3));
				json.put("description", values.getString(4));
				org.json.JSONArray keywords = values.getJSONArray(5);
				json.put("keywords",  keywords.length() == 1 && "".equals(keywords.getString(0)) ? new org.json.JSONArray() : keywords);
				if(json != null && !json.has("_children")) {
					json.put("_children", new org.json.JSONArray());
				}
				data.put(values.getString(0), json);
			}
			if("".equals(values.getString(1)) || "0".equals(values.getString(2))) {
				root.add(json);
			}
			org.json.JSONObject apá = data.get(values.getString(1));
			if(apá == null) {
				apá = new org.json.JSONObject();
				apá.put("id", values.getString(1));
				apá.put("level", Integer.parseInt(values.getString(2)) - 1);
				apá.put("_children", new org.json.JSONArray());
				data.put(values.getString(1), apá);
			}
			apá.getJSONArray("_children").put(json);
		});
		data.clear();
		rawResponse = root.get(0).toString();
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(rawResponse);

//		response.setStatus(HttpServletResponse.SC_GONE);
	
	}


}
