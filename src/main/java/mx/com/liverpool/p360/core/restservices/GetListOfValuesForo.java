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
 * Servlet implementation class GetListOfValuesForo
 */
@WebServlet("/public/rt/GetListOfValues")
public class GetListOfValuesForo extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetListOfValuesForo() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String baseUrl = PropertiesManager.get( "p360.contingency.base_url" );
		String encoded = PropertiesManager.get( "p360.contingency.basic_token_auth" );
		
		String template = request.getParameter("template");
		String characteristic = request.getParameter("characteristic");
		String includeAlternative = request.getParameter("includeAlternative");
//		String itemGroup = null; //request.getParameter("itemGroup");
		
		String validValues = null;
		String lookup = null;
		
		RESTWorkshop workshop = new RESTWorkshop();
		workshop.setBaseUrl(baseUrl);
		workshop.getRc().getHeader().put("Authorization", "Basic: " + encoded);
		
		org.json.JSONObject resp = null;
		org.json.JSONArray rows = null;
		org.json.JSONArray values = null;
		
		int currentIndex = 0;
		int totalSize = 0;
		
		org.json.JSONArray lookupValues = new org.json.JSONArray();
		java.util.TreeMap<String, String> qp = new java.util.TreeMap<>();
		java.util.LinkedList<java.util.Map.Entry<String, org.json.JSONArray>> entries = null;
		String rawResponse = null;
		DataRequestor dr = new DataRequestor();
		String r = null;
		r = dr.getCharacteristicData(new org.json.JSONArray().put(characteristic));
		try {
			org.json.JSONObject jr = new org.json.JSONObject(r);
			org.json.JSONArray items = jr.getJSONArray("items");
			org.json.JSONObject item = items.getJSONObject(0);
			lookup = !"".equals(item.getString("lookup")) ? item.getString("lookup") : null;
		}catch(org.json.JSONException e) {
			logE(e);
		}
		if(lookup != null) {
			r = dr.getTemplateCharacteristicMetaDataByTemplateCharacteristicProperty(new org.json.JSONArray().put(new org.json.JSONObject().put("template", template).put("characteristic", characteristic).put("property", "listofValuesValidValues")));
			log("rtcm");
			try {
				org.json.JSONObject jr = new org.json.JSONObject(r);
				org.json.JSONArray items = jr.getJSONArray("items");
				org.json.JSONObject item = items.getJSONObject(0);
				if(item.length() > 0)
					validValues = item.has(characteristic) && item.getJSONObject(characteristic).has("listofValuesValidValues") ? item.getJSONObject(characteristic).getString("listofValuesValidValues") : null;
			}catch(org.json.JSONException e) {
			}
			if(validValues == null) {
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
							if( properties.containsKey("listofValuesValidValues") && name.equals(characteristic) ) {
								validValues = properties.get("listofValuesValidValues");
								found = true;
								break;
							}
						}
					}
				}catch(org.json.JSONException e) {
					logE(e);
				}
			}
		}
		logMe("Got asked for: " + characteristic);
		if(lookup == null) {
			
		}else {
			logMe("About valid values... ->" + validValues + "<-");
			if(validValues == null) {
				if(entries == null || entries.isEmpty()) {
					qp.clear();
					qp.put("lookup", "'" + lookup + "'");
					qp.put("fields", "LookupValue.Code,LookupValueLang.Name(es),LookupValueIdentifier.Code(ATG)");
					qp.put("query", "LookupValue.IsActive equals true");
					qp.put("pageSize", "10000");
					do {
						StringBuilder sb = new StringBuilder();
						int times = 0;
						for(java.util.Map.Entry<String, String> entry : qp.entrySet()) {
							sb.append( times == 0 ? "?" : "&" ).append(entry.getKey()).append("=").append(encode(entry.getValue()));
							times++;
						}
						qp.put("startIndex", String.valueOf(currentIndex) );
						resp = workshop.makeRequest("GET", "/list/LookupValue/bySearch", qp, null);
						totalSize = resp.getInt("totalSize");
						rows = resp.getJSONArray("rows");
						for(int i=0; i<rows.length();i++) {
							currentIndex++;
							values = rows.getJSONObject(i).getJSONArray("values");
							lookupValues.put(new org.json.JSONObject().put("code", values.getString(0)).put("name", values.getString(1)).put("altCode", values.getString(2)));
						}
					}while(currentIndex < totalSize);
					currentIndex = 0;

				}
			}else {
				String[] pieces = validValues.split(",");
				StringBuilder sb = new StringBuilder();
				
				for(int i=0; i<pieces.length; i++) {
					if(!"".equals(pieces[i]))
						sb.append(i == 0 ? "" : ",").append("\"").append(pieces[i].trim()/*.replaceAll("\\\\", "\\\\\\\\")*/.replaceAll("\"", "\\\\\"")).append("\"");
				}
				
				qp.clear();
				if(lookup != null) {
					qp.put("lookup", "'" + lookup + "'");
					qp.put("query", "LookupValue.Code in (" + sb.toString() + ")");
					qp.put("fields", "LookupValue.Code,LookupValueLang.Name(es),LookupValueIdentifier.Code(ATG)");
					qp.put("pageSize", "10000");
					java.util.Set<String> holas = new java.util.TreeSet<>();
					resp = workshop.makeRequest("GET", "/list/LookupValue/bySearch", qp, null);
					if(resp != null) {
						totalSize = resp.getInt("totalSize");
						rows = resp.getJSONArray("rows");
						for(int i=0; i<rows.length(); i++) {
							values = rows.getJSONObject(i).getJSONArray("values");
							if(!holas.isEmpty() && holas.contains(values.getString(0)))
								lookupValues.put(new org.json.JSONObject().put("code", values.getString(0)).put("name", values.getString(1)).put("altCode", values.getString(2)));
							else if(holas.isEmpty())
								lookupValues.put(new org.json.JSONObject().put("code", values.getString(0)).put("name", values.getString(1)).put("altCode", values.getString(2)));
						}
					}else {
						logMe("<::::>" + workshop.getRawResponse() + "<::::>");
					}
				}
			}
		}
		
		org.json.JSONArray vls = new org.json.JSONArray();
		boolean alternatives = Boolean.parseBoolean(includeAlternative != null ? includeAlternative.toLowerCase() : "false");
		for(int i=0; i<lookupValues.length(); i++) {
			vls.put( alternatives ? new org.json.JSONObject().put("label", lookupValues.getJSONObject(i).getString("name")).put("hex", lookupValues.getJSONObject(i).getString("altCode")) : new org.json.JSONObject().put("label", lookupValues.getJSONObject(i).getString("name")).put("code", lookupValues.getJSONObject(i).getString("code")) );
		}
		
		rawResponse = new org.json.JSONObject().put("values", vls).toString();
		
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(rawResponse);
	}

	
	private String encode(String a) {
		return java.net.URLEncoder.encode(a, java.nio.charset.StandardCharsets.UTF_8);
	}
	
	private void logMe(String message) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream("../logs/getLookupValuesForo.log", true)))) {
            pw.println("[" + (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()))
                    + "]  " + message);
        } catch (java.io.IOException e) {
        }
    }

    private static void logE(Exception ex) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream("../logs/getLookupValuesForo.log", true)))) {
            ex.printStackTrace(pw);
        } catch (java.io.IOException e) {
        }
    }

}
