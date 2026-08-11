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
 * Servlet implementation class GetListOfValuesEnabler
 */
@WebServlet("/public/rt/ws_list_valid_values_for_parent_template_characteristic_enabler")
public class GetListOfValuesEnabler extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String creationType = "CreateProposal";

    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetListOfValuesEnabler() {
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
		String creationType = request.getParameter("creationType");
		if(creationType == null || "".equals(creationType.trim())) {
			creationType = this.creationType;
		}else {
			creationType = creationType.trim();
		}
		
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
		org.json.JSONArray dependentValuesRequest = new org.json.JSONArray().put(new org.json.JSONObject().put("template", template).put("characteristic", characteristic).put("property", "dependentValues").put("creationType", creationType));
		r = dr.getTemplateCharacteristicMetaDataByTemplateCharacteristicProperty(dependentValuesRequest);
		logMe("For .." + dependentValuesRequest + ".. " + template + ", " + characteristic + ": " + r);
		try {
			org.json.JSONObject jr = new org.json.JSONObject(r);
			org.json.JSONArray items = jr.getJSONArray("items");
			org.json.JSONObject item = items.getJSONObject(0);
			validValues = item.has(characteristic) && item.getJSONObject(characteristic).has("dependentValues") ? item.getJSONObject(characteristic).getString("dependentValues") : null;
			logMe("For .. " + validValues);
		}catch(org.json.JSONException e) {
			logMe( "Problem for: " + template + " - " + characteristic + " - " + e.getMessage() );
		}
		
		StringBuilder sb0 = new StringBuilder();
		
		if(validValues != null) {
			String[] vvs = validValues.split(",");
			for(int i=0; i<vvs.length; i++) {
				sb0.append(sb0.length() == 0 ? "" : ",");
				sb0.append("\"");
				sb0.append(vvs[i].trim());
				sb0.append("\"");
			}
			r = dr.getTemplateCharacteristicMetaDataByTemplateCharacteristicProperty(new org.json.JSONArray().put(new org.json.JSONObject().put("template", template).put("characteristic", characteristic).put("property", "dependentAttribute").put("creationType", creationType)));
			try {
				org.json.JSONObject jr = new org.json.JSONObject(r);
				org.json.JSONArray items = jr.getJSONArray("items");
				org.json.JSONObject item = items.getJSONObject(0);
				dependentAttribute = item.has(characteristic) && item.getJSONObject(characteristic).has("dependentAttribute") ? item.getJSONObject(characteristic).getString("dependentAttribute") : null;
			}catch(org.json.JSONException e) {
			}
			logMe(logEntry + "(DependentAttribute) Response from metadata plantillas: " + rows);
			if(dependentAttribute != null) {
				r = dr.getCharacteristicData(new org.json.JSONArray().put(dependentAttribute));
				try {
					org.json.JSONObject jr = new org.json.JSONObject(r);
					org.json.JSONArray items = jr.getJSONArray("items");
					org.json.JSONObject item = items.getJSONObject(0);
					lookup = !"".equals(item.getString("lookup")) ? item.getString("lookup") : null;
				}catch(org.json.JSONException e) {
					e.printStackTrace();
				}
				logMe(logEntry + "Response from characteristic: " + rows);
				if(validValues != null && lookup != null) {
					
					qp.clear();
					qp.put("lookup", "'" + lookup + "'");
					qp.put("fields", "LookupValue.Code,LookupValueLang.Name(es),LookupValueIdentifier.Code(ATG)");
					qp.put("query", "LookupValue.IsActive equals true and LookupValue.Code in (" + sb0.toString() + ")");
					qp.put("pageSize", "900");
					
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
		}else {
			r = dr.getGlobalMetaData();
			try {
				boolean found = false;
				org.json.JSONObject jr = new org.json.JSONObject(r);
				org.json.JSONArray items = jr.getJSONArray("items");
				java.util.Map<String, java.util.Map<String, String>> characteristics = new java.util.HashMap<>();
				java.util.Map<String, String> properties = null;
				org.json.JSONObject item = null;
				org.json.JSONObject itemProperties = null;
				for(int i=0; i<items.length() && !found; i++) {
					item = items.getJSONObject(i);
					for(String name : org.json.JSONObject.getNames(item)) {
						properties = characteristics.get( name );
						if(properties == null) {
							properties = new java.util.HashMap<>();
							characteristics.put(name, properties);
						}
						itemProperties = item.getJSONObject(name);
						for(String sn : org.json.JSONObject.getNames(itemProperties)) {
							properties.put(sn, itemProperties.getString(sn));
						}
						if( properties.containsKey("dependentValues") && name.equals(characteristic) ) {
							validValues = properties.get("dependentValues");
							found = true;
							break;
						}
					}
				}
			}catch(org.json.JSONException e) {
				e.printStackTrace();
			}
			logMe(logEntry + "(DependentValues) Response from global metadata plantillas: " + rows);
			if(validValues != null) {
				String[] vvs = validValues.split(",");
				for(int i=0; i<vvs.length; i++) {
					sb0.append("\"");
					sb0.append(vvs[0].trim());
					sb0.append("\"");
				}
				try {
					boolean found = false;
					org.json.JSONObject jr = new org.json.JSONObject(r);
					org.json.JSONArray items = jr.getJSONArray("items");
					java.util.Map<String, java.util.Map<String, String>> characteristics = new java.util.HashMap<>();
					java.util.Map<String, String> properties = null;
					org.json.JSONObject item = null;
					org.json.JSONObject itemProperties = null;
					for(int i=0; i<items.length() && !found; i++) {
						item = items.getJSONObject(i);
						for(String name : org.json.JSONObject.getNames(item)) {
							properties = characteristics.get( name );
							if(properties == null) {
								properties = new java.util.HashMap<>();
								characteristics.put(name, properties);
							}
							itemProperties = item.getJSONObject(name);
							for(String sn : org.json.JSONObject.getNames(itemProperties)) {
								properties.put(sn, itemProperties.getString(sn));
							}
							if( properties.containsKey("dependentAttribute") && name.equals(characteristic) ) {
								dependentAttribute = properties.get("dependentAttribute");
								found = true;
								break;
							}
						}
					}
				}catch(org.json.JSONException e) {
					e.printStackTrace();
				}
					
					if(dependentAttribute != null) {
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
							qp.put("pageSize", "900");
							
							do {
								qp.put("startIndex", String.valueOf(currentIndex) );
								resp = workshop.makeRequest("GET", "/list/LookupValue/bySearch", qp, null);
								totalSize = resp.getInt("totalSize");
								rows = resp.getJSONArray("rows");
								logMe(logEntry + "values are: " + rows);
								
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
		}
		
		org.json.JSONArray vls = new org.json.JSONArray();
		boolean alternatives = Boolean.parseBoolean(includeAlternative != null ? includeAlternative.toLowerCase() : "false");
		for(int i=0; i<lookupValues.length(); i++) {
			vls.put( alternatives ? new org.json.JSONObject().put("label", lookupValues.getJSONObject(i).getString("name")).put("hex", lookupValues.getJSONObject(i).getString("altCode")) : lookupValues.getJSONObject(i).getString("name") );
		}
		
		rawResponse = new org.json.JSONObject().put("values", vls).toString();
		
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(rawResponse);
		
	}
	
	private void logMe(String message) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream("../logs/getEnablerValues.log", true)))) {
            pw.println("[" + (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()))
                    + "]  " + message);
        } catch (java.io.IOException e) {
        }
    }

    private static void logE(Exception ex) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream("../logs/getEnablerValues.log", true)))) {
            ex.printStackTrace(pw);
        } catch (java.io.IOException e) {
        }
    }

}
