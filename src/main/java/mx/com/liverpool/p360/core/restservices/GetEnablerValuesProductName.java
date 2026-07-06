package mx.com.liverpool.p360.core.restservices;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.PropertiesManager;
import mx.com.liverpool.p360.services.core.RESTWorkshop;
import mx.com.liverpool.p360.services.core.net.DataRequestor;

/**
 * Servlet implementation class GetEnablerValuesProductName
 */
@WebServlet("/public/rt/GetEnablerValuesProductName")
public class GetEnablerValuesProductName extends HttpServlet {

	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetEnablerValuesProductName() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String baseUrl = PropertiesManager.get( "p360.contingency.base_url" );
		String encoded = PropertiesManager.get( "p360.contingency.basic_token_auth" );
		
		String template = request.getParameter("template");
		String characteristic = request.getParameter("characteristic");
		String includeAlternative = request.getParameter("includeAlternative");
		
		String validValues = null;
		String lookup = null;
		String dependentAttribute = null;
		
		RESTWorkshop workshop = new RESTWorkshop();
		workshop.setBaseUrl( baseUrl );
		workshop.getRc().getHeader().put("Authorization", "Basic: " + encoded);
		
		org.json.JSONObject resp = null;
		org.json.JSONArray rows = null;
		org.json.JSONArray values = null;
		
		int currentIndex = 0;
		int totalSize = 0;
		
		org.json.JSONArray lookupValues = new org.json.JSONArray();
		java.util.TreeMap<String, String> qp = new java.util.TreeMap<>();
		
		String rawResponse = null;
		
		String stamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
		String logEntry = "[" + stamp + "] (" + template + ", " + characteristic + ") ";
		
		DataRequestor dr = new DataRequestor();
		String r = null;
		r = dr.getTemplateCharacteristicMetaDataByTemplateCharacteristicProperty(new org.json.JSONArray().put(new org.json.JSONObject().put("template", template).put("characteristic", characteristic).put("property", "dependentValues")));
		try {
			org.json.JSONObject jr = new org.json.JSONObject(r);
			org.json.JSONArray items = jr.getJSONArray("items");
			org.json.JSONObject item = items.getJSONObject(0);
			validValues = item.has(characteristic) && item.getJSONObject(characteristic).has("dependentValues") ? item.getJSONObject(characteristic).getString("dependentValues") : null;
		}catch(org.json.JSONException e) {
			e.printStackTrace();
		}
		
//		qp.put("dictionaryProxy", "'ExtensionDeMetadatos_ ValoresPredeterminadosPorPlantilla'");
//		qp.put("query", 
//				"StandardizationValue.Dictionary->StandardizationDictionary.Identifier equals \"ExtensionDeMetadatos_ ValoresPredeterminadosPorPlantilla\""
//				+ " and StandardizationValue.StructureGroup->LookupValue.Code equals \"" + template + "\""
//				+ " and StandardizationValue.Characteristic->Characteristic.Identifier equals \""+ characteristic + "\""
//				+ " and StandardizationValue.CreationType->LookupValue.Code equals \"CreateProposal\""
//				+ " and StandardizationValue.Property->LookupValue.Code equals \"DependentValues\""
//			);
//		qp.put("fields", "StandardizationValue.PropertyValue");
//		
//		resp = workshop.makeRequest("GET", "/list/StandardizationValue/bySearch", qp, null);
//		if(resp == null) {
//			System.out.println("ERROR: " + workshop.getRawResponse());
//			System.out.println("ERROR: " + workshop.getBaseUrl() + "/list/StandardizationValue/bySearch");
//		}else {
//			rows = resp.getJSONArray("rows");
//			
//			System.out.println(logEntry + "(DependentValues) Response from metadata plantillas: " + rows);
//			values = rows.length() > 0 ? rows.getJSONObject(0).getJSONArray("values") : null;
//			validValues = values != null ? values.getString(0) : null;
//		}
		StringBuilder sb0 = new StringBuilder();
		
		if(validValues != null) {
			String[] vvs = validValues.split(",");
			for(int i=0; i<vvs.length; i++) {
				sb0.append(sb0.length() == 0 ? "" : ",");
				sb0.append("\"");
				sb0.append(vvs[i].trim());
				sb0.append("\"");
			}
//			qp.put("query", 
//					"StandardizationValue.Dictionary->StandardizationDictionary.Identifier equals \"ExtensionDeMetadatos_ ValoresPredeterminadosPorPlantilla\""
//					+ " and StandardizationValue.StructureGroup->LookupValue.Code equals \"" + template + "\""
//					+ " and StandardizationValue.Characteristic->Characteristic.Identifier equals \"" + characteristic + "\""
//					+ " and StandardizationValue.Property->LookupValue.Code equals \"DependentAttribute\""
//					+ " and StandardizationValue.CreationType->LookupValue.Code equals \"CreateProposal\""
//				);
//			
//			resp = workshop.makeRequest("GET", "/list/StandardizationValue/bySearch", qp, null);
//			rows = resp.getJSONArray("rows");
//			System.out.println(logEntry + "(DependentAttribute) Response from metadata plantillas: " + rows);
//			
//			values = rows.length() > 0 ? rows.getJSONObject(0).getJSONArray("values") : null;
//			dependentAttribute = values != null ? values.getString(0) : null;
			r = dr.getTemplateCharacteristicMetaDataByTemplateCharacteristicProperty(new org.json.JSONArray().put(new org.json.JSONObject().put("template", template).put("characteristic", characteristic).put("property", "dependentAttribute")));
			try {
				org.json.JSONObject jr = new org.json.JSONObject(r);
				org.json.JSONArray items = jr.getJSONArray("items");
				org.json.JSONObject item = items.getJSONObject(0);
				dependentAttribute = item.has(characteristic) && item.getJSONObject(characteristic).has("dependentValues") ? item.getJSONObject(characteristic).getString("dependentValues") : null;
			}catch(org.json.JSONException e) {
				e.printStackTrace();
			}
			if(dependentAttribute != null) {
//				qp.clear();
//				qp.put("fields", "Characteristic.Lookup->Lookup.Identifier"); 
//				qp.put("query", "Characteristic.Identifier equals \"" + dependentAttribute + "\"");
//				
//				resp = workshop.makeRequest("GET", "/list/Characteristic/bySearch", qp, null);
//				rows = resp.getJSONArray("rows");
//				System.out.println(logEntry + "Response from characteristic: " + rows);
//				
//				values = rows.length() > 0 ? rows.getJSONObject(0).getJSONArray("values") : null;
//				lookup = values != null ? values.getString(0) : null;
				r = dr.getCharacteristicData(new org.json.JSONArray().put(dependentAttribute));
				try {
					org.json.JSONObject jr = new org.json.JSONObject(r);
					org.json.JSONArray items = jr.getJSONArray("items");
					org.json.JSONObject item = items.getJSONObject(0);
					lookup = !"".equals(item.getString("lookup")) ? item.getString("lookup") : null;
				}catch(org.json.JSONException e) {
					e.printStackTrace();
				}
				if(validValues != null && lookup != null) {
					
					qp.clear();
					qp.put("lookup", "'" + lookup + "'");
					qp.put("fields", "LookupValue.Code,LookupValueLang.Name(es),LookupValueIdentifier.Code(ATG)");
					qp.put("query", "LookupValue.IsActive equals true and LookupValue.Code in (" + sb0.toString() + ")");
					qp.put("pageSize", "10000");
					
					System.out.println(logEntry + "Query is: " + qp.get("query"));
					
					do {
						qp.put("startIndex", String.valueOf(currentIndex) );
						resp = workshop.makeRequest("GET", "/list/LookupValue/bySearch", qp, null);
						totalSize = resp.getInt("totalSize");
						rows = resp.getJSONArray("rows");
						System.out.println(logEntry + "Values are: " + rows);
						
						for(int i=0; i<rows.length();i++) {
							currentIndex++;
							values = rows.getJSONObject(i).getJSONArray("values");
							lookupValues.put(new org.json.JSONObject().put("code", values.getString(0)).put("name", values.getString(1)).put("altCode", values.getString(2)));
						}
					}while(currentIndex < totalSize);
					currentIndex = 0;
					
				}else {
					
				}
			}
		}
		
		org.json.JSONArray vls = new org.json.JSONArray();
		boolean alternatives = Boolean.parseBoolean(includeAlternative != null ? includeAlternative.toLowerCase() : "false");
		for(int i=0; i<lookupValues.length(); i++) {
			vls.put( alternatives ? new org.json.JSONObject().put("label", lookupValues.getJSONObject(i).getString("name")).put("hex", lookupValues.getJSONObject(i).getString("altCode")) : lookupValues.getJSONObject(i).getString("code") );
		}
		
		rawResponse = new org.json.JSONObject().put("values", vls).toString();
		
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(rawResponse);
		
	}

}
