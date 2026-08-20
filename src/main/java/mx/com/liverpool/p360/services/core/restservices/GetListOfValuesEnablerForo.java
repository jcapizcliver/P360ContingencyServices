package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.DBAccessDataStub;
import mx.com.liverpool.p360.services.core.ELog;
import mx.com.liverpool.p360.services.core.PropertiesManager;
import mx.com.liverpool.p360.services.core.RESTWorkshop;
import mx.com.liverpool.p360.services.core.net.DataRequestor;

/**
 * Servlet implementation class GetListOfValuesEnablerForo
 */
@WebServlet("/public/rt/GetListOfValuesEnabler")
public class GetListOfValuesEnablerForo extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetListOfValuesEnablerForo() {
        super();
        // TODO Auto-generated constructor stub
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
		workshop.setBaseUrl(baseUrl);
		workshop.getRc().getHeader().put("Authorization", "Basic: " + encoded);
		
		org.json.JSONObject resp = null;
		org.json.JSONArray rows = null;
		org.json.JSONArray values = null;
		
		int currentIndex = 0;
		int totalSize = 0;
		
		org.json.JSONArray lookupValues = new org.json.JSONArray();
		java.util.TreeMap<String, String> qp = new java.util.TreeMap<>();
		
		String rawResponse = null;
		try(DBAccessDataStub dastub = new DBAccessDataStub( new ELog() {
			
			@Override
			public void logE(Exception e) {
			}
			
			@Override
			public void log(String message) {
				logMe(message);
			}
		} )){
			DataRequestor dr = new DataRequestor(dastub);
			String r = null;
			r = dr.getTemplateCharacteristicMetaDataByTemplateCharacteristicProperty(new org.json.JSONArray().put(new org.json.JSONObject().put("template", template).put("characteristic", characteristic).put("property", "dependentValues")));
			logMe( "(getTemplateCharacteristicMetaDataByTemplateCharacteristicProperty) On template: " + template + ", char: " + characteristic + ": " + r );
			try {
				org.json.JSONObject jr = new org.json.JSONObject(r);
				org.json.JSONArray items = jr.getJSONArray("items");
				org.json.JSONObject item = items.getJSONObject(0);
				validValues = item.getJSONObject(characteristic).getString("dependentValues");
				logMe("Valid values: " + validValues);
			}catch(org.json.JSONException e) {
				e.printStackTrace();
				logMe("Err: " + e.getMessage());
			}
			r = dr.getTemplateCharacteristicMetaDataByTemplateCharacteristicProperty(new org.json.JSONArray().put(new org.json.JSONObject().put("template", template).put("characteristic", characteristic).put("property", "dependentAttribute")));
			logMe( "(*getTemplateCharacteristicMetaDataByTemplateCharacteristicProperty) On template: " + template + ", char: " + characteristic + ": " + r );
			try {
				org.json.JSONObject jr = new org.json.JSONObject(r);
				org.json.JSONArray items = jr.getJSONArray("items");
				org.json.JSONObject item = items.getJSONObject(0);
				dependentAttribute = item.getJSONObject(characteristic).getString("dependentAttribute");
				logMe("DependentAttribute: " + dependentAttribute);
			}catch(org.json.JSONException e) {
				e.printStackTrace();
			}
			if(dependentAttribute != null) {
				r = dr.getCharacteristicData(new org.json.JSONArray().put(dependentAttribute));
				logMe("After DependentAttribute: " + r);
				try {
					org.json.JSONObject jr = new org.json.JSONObject(r);
					org.json.JSONArray items = jr.getJSONArray("items");
					org.json.JSONObject item = items.getJSONObject(0);
					lookup = !"".equals(item.getString("lookup")) ? item.getString("lookup") : null;
					logMe("Res: " + lookup);
				}catch(org.json.JSONException e) {
					e.printStackTrace();
				}
				if(validValues != null && lookup != null) {
					String[] pieces = workshop.parseLine(validValues, "\"", ",", "\\");
					StringBuilder sb = new StringBuilder();
					for(int i=0; i<pieces.length; i++) {
						sb.append(i == 0 ? "" : ",").append("\"").append(pieces[i]).append("\"");
					}
					qp.clear();
					qp.put("lookup", "'" + lookup + "'");
					qp.put("fields", "LookupValue.Code,LookupValueLang.Name(es),LookupValueIdentifier.Code(ATG)");
					qp.put("query", "LookupValue.IsActive equals true and LookupValue.Code in (" + sb.toString() + ")");
					qp.put("pageSize", "900");
					logMe("Res2: " + qp);
					do {
						qp.put("startIndex", String.valueOf(currentIndex) );
						resp = workshop.makeRequest("GET", "/list/LookupValue/bySearch", qp, null);
						if(resp != null && resp.has("totalSize")) {
							totalSize = resp.getInt("totalSize");
							rows = resp.getJSONArray("rows");
							for(int i=0; i<rows.length();i++) {
								currentIndex++;
								values = rows.getJSONObject(i).getJSONArray("values");
								lookupValues.put(new org.json.JSONObject().put("code", values.getString(0)).put("name", values.getString(1)).put("altCode", values.getString(2)));
							}
						}else {
							logMe("Problem: " + workshop.getRawResponse());
						}
					}while(currentIndex < totalSize);
					currentIndex = 0;
					
				}
			}
		}
		logMe("Res: " + lookupValues);
		org.json.JSONArray vls = new org.json.JSONArray();
		boolean alternatives = Boolean.parseBoolean(includeAlternative != null ? includeAlternative.toLowerCase() : "false");
		for(int i=0; i<lookupValues.length(); i++) {
			vls.put( alternatives ? new org.json.JSONObject().put("label", lookupValues.getJSONObject(i).getString("name")).put("hex", lookupValues.getJSONObject(i).getString("altCode")) : lookupValues.getJSONObject(i).getString("name") );
		}
		logMe("Res: " + vls);
		rawResponse = new org.json.JSONObject().put("values", vls).toString();
		
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(rawResponse);
		
	}
	
	private static final Logger LOGGER = Logger.getLogger(GetListOfValuesEnablerForo.class.getName());

    static {
        try {
            LOGGER.setUseParentHandlers(false);

            FileHandler fileHandler = new FileHandler("../logs/sftp/ecc/getEnablerValuesForo-%g.log", 25 * 1024 * 1024, 10, true);
            fileHandler.setEncoding(StandardCharsets.UTF_8.name());
            fileHandler.setLevel(Level.ALL);

            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    java.time.LocalDateTime dateTime =
                        java.time.Instant.ofEpochMilli(record.getMillis())
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime();

                    String timestamp = dateTime.format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    );

                    return "[" + timestamp + "] [" + record.getLevel() + "] " + formatMessage(record) + System.lineSeparator();
                }
            });

            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);

        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el logger", e);
        }
    }

	private void logMe(String message) {
		LOGGER.info(message);
    }

}
