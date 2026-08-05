package mx.com.liverpool.p360.core.restservices;

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

import mx.com.liverpool.p360.services.core.PropertiesManager;
import mx.com.liverpool.p360.services.core.RESTWorkshop;
import mx.com.liverpool.p360.services.core.WriteAttributesForo;
import mx.com.liverpool.p360.services.core.net.DataRequestor;

/**
 * Servlet implementation class GetListOfValues
 */
@WebServlet("/public/rt/proc_ws_list_valid_values_for_template_characteristic_lov")
public class GetListOfValues extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String creationType = "CreateProposal";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetListOfValues() {
        super();
    }
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String baseUrl = PropertiesManager.get( "p360.contingency.base_url" );
		String encoded = PropertiesManager.get( "p360.contingency.basic_token_auth" );
		
		String template = request.getParameter("template");
		String characteristic = request.getParameter("characteristic");
		String includeAlternative = request.getParameter("includeAlternative");
		String creationType = request.getParameter("creationType");
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
		if (creationType == null) {
	        creationType = this.creationType;
	    }
/*		org.json.JSONArray partes = null;
		qp.put("dictionaryProxy", "'ExtensionDeMetadatos_ ValoresPredeterminadosPorPlantilla'");
		qp.put("query", ""
				+ "StandardizationValue.Dictionary->StandardizationDictionary.Identifier equals \"ExtensionDeMetadatos_ ValoresPredeterminadosPorPlantilla\""
				+ " and StandardizationValue.StructureGroup->LookupValue.Code equals \"" + template + "\""
				+ " and StandardizationValue.Characteristic->Characteristic.Identifier equals \""+ characteristic + "\""
				+ " and StandardizationValue.CreationType equals CreateProposal"
				+ " and StandardizationValue.Property equals ListOfValuesFilter");
		qp.put("fields", "StandardizationValue.PropertyValue,StandardizationValue.Characteristic->Characteristic.Lookup->Lookup.Identifier");
		resp = workshop.makeRequest("GET", "/list/StandardizationValue/bySearch", qp, null);
		rows = resp.getJSONArray("rows");
		if(rows.length() == 0) {
			qp.put("dictionaryProxy", "'GlobalTemplateAttributeConfiguration'");
			qp.put("query", ""
					+ "StandardizationValue.Dictionary->StandardizationDictionary.Identifier equals \"GlobalTemplateAttributeConfiguration\""
					+ " and StandardizationValue.Characteristic->Characteristic.Identifier equals \""+ characteristic + "\""
					+ " and StandardizationValue.CreationType equals CreateProposal"
					+ " and StandardizationValue.Property equals ListOfValuesFilter");
			qp.put("fields", "StandardizationValue.PropertyValue,StandardizationValue.Characteristic->Characteristic.Lookup->Lookup.Identifier");
			
			resp = workshop.makeRequest("GET", "/list/StandardizationValue/bySearch", qp, null);
			rows = resp.getJSONArray("rows");
		}
		values = rows.length() > 0 ? rows.getJSONObject(0).getJSONArray("values") : null;
		validValues = values != null ? values.getString(0) : null;
*/
		DataRequestor dr = new DataRequestor();
		String r = null;
		r = dr.getCharacteristicData(new org.json.JSONArray().put(characteristic));
		try {
			org.json.JSONObject jr = new org.json.JSONObject(r);
			org.json.JSONArray items = jr.getJSONArray("items");
			org.json.JSONObject item = items.getJSONObject(0);
			lookup = !"".equals(item.getString("lookup")) ? item.getString("lookup") : null;
			logMe("For characteristic: " + characteristic + ", got lov: " + lookup + " on template: " + template);
		}catch(org.json.JSONException e) {
			logMe("Problem getting lov for: " + characteristic);
			logE(e);
		}
		if(lookup != null) {
			r = dr.getTemplateCharacteristicMetaDataByTemplateCharacteristicProperty(new org.json.JSONArray().put(new org.json.JSONObject().put("template", template).put("characteristic", characteristic).put("property", "listofValuesValidValues").put("creationType", creationType)));
			logMe("rtcm (" + template + "," + characteristic + "," + lookup + ") " + r);
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
//		qp.clear();
//		qp.put("fields", "Characteristic.Lookup->Lookup.Identifier");
//		qp.put("query", "Characteristic.Identifier equals \"" + characteristic + "\"");
//		resp = workshop.makeRequest("GET", "/list/Characteristic/bySearch", qp, null);
//		if(resp == null) {
//			logMe("\n\n\tError querying the catalog for metadata, got response (LOV) --->" + workshop.getRawResponse());
//		}
//		rows = resp.getJSONArray("rows");
//		
//		values = rows.length() > 0 ? rows.getJSONObject(0).getJSONArray("values") : new org.json.JSONArray();
//		lookup = values.length() > 0 ? values.getString(0) : null;
//		logMe("Got asked for: " + characteristic);
		if(lookup == null) {
			
		}else {
			/*
			if(itemGroup != null && !"".equals(itemGroup)) {
				String sapAttributeName = null;
				logMe("Grabbing values for " + characteristic + "...");
				if("TamanoUnico".equals(characteristic)) {
					sapAttributeName = getAtributoSapLatalla(itemGroup, workshop);
					logMe("Now preguntando por ladesa... " + sapAttributeName);
					sapAttributeName = seleccionaLaDesa(sapAttributeName, workshop);
					logMe("Ladesa: " + sapAttributeName);
				}else {
					sapAttributeName = lookupInSAP(characteristic, workshop);
				}
				if(sapAttributeName != null) {
					entries = getBoys(sapAttributeName, itemGroup, workshop, "TamanoUnico".equals(characteristic));
					logMe("Boys: " + entries.toString());
				}
			}
			*/
			logMe("About valid values... ->" + validValues + "<- " + lookup);
			if(validValues == null) {
				if(entries == null || entries.isEmpty()) {
					qp.clear();
					qp.put("lookup", "'" + lookup + "'");
					qp.put("fields", "LookupValue.Code,LookupValueLang.Name(es),LookupValueIdentifier.Code(ATG),LookupValueLang.Name(en),LookupValue.IsActive");
					qp.put("query", "LookupValue.IsActive = true");
					qp.put("pageSize", "10000");
					do {
						qp.put("startIndex", String.valueOf(currentIndex) );
						resp = workshop.makeRequest("GET", "/list/LookupValue/bySearch", qp, null);
						logMe("Queried here!");
						totalSize = resp.getInt("totalSize");
						rows = resp.getJSONArray("rows");
						for(int i=0; i<rows.length();i++) {
							currentIndex++;
							values = rows.getJSONObject(i).getJSONArray("values");
							logMe("--" + values + "..");
							if(Boolean.parseBoolean(values.getString(4)))
								lookupValues.put(new org.json.JSONObject().put("code", values.getString(0)).put("name", values.getString(1)).put("altCode", values.getString(2)));
						}
					}while(currentIndex < totalSize);
					currentIndex = 0;

				}
				/*
				 else {
					for(java.util.Map.Entry<String, org.json.JSONArray> entry : entries) {
						partes = entry.getValue();
						lookupValues.put(new org.json.JSONObject().put("code", entry.getKey()).put("name", partes.getString(0)).put("altCode", partes.getString(1)));
					}
				}
				*/
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
					qp.put("query", "LookupValue.Code in (" + sb.toString() + ") and LookupValue.IsActive = true");
					qp.put("fields", "LookupValue.Code,LookupValueLang.Name(es),LookupValueIdentifier.Code(ATG)");
					qp.put("pageSize", "10000");
					java.util.Set<String> holas = new java.util.TreeSet<>();
//					if(entries != null)
//						entries.forEach(en -> holas.add(en.getKey()));
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
						if(!holas.isEmpty() && lookupValues.length() == 0) {
							/*
							for(java.util.Map.Entry<String, org.json.JSONArray> entry : entries) {
								partes = entry.getValue();
								lookupValues.put(new org.json.JSONObject().put("code", entry.getKey()).put("name", partes.getString(0)).put("altCode", partes.getString(1)));
							}
							*/
						}
					}else {
						logMe("<::::>" + workshop.getRawResponse() + "<::::>");
					}
				}
			}
		}
		
		org.json.JSONArray vls = new org.json.JSONArray();
		java.util.LinkedList<Object> lst = new java.util.LinkedList<>();
		boolean alternatives = Boolean.parseBoolean(includeAlternative != null ? includeAlternative.toLowerCase() : "false");
		for(int i=0; i<lookupValues.length(); i++) {
			lst.addLast( alternatives ? new org.json.JSONObject().put("label", lookupValues.getJSONObject(i).getString("name")).put("hex", lookupValues.getJSONObject(i).getString("altCode")) : lookupValues.getJSONObject(i).getString("name") );
		}
		java.util.Collections.sort( lst, (o1,o2) -> {
			return o1 instanceof org.json.JSONObject ? ((org.json.JSONObject)o1).getString("label").compareTo(((org.json.JSONObject)o2).getString("label")) : ((String)o1).compareTo((String)o2);
		} );
		for(Object o : lst) {
			vls.put( o );
		}
		rawResponse = new org.json.JSONObject().put("values", vls).toString();
		
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(rawResponse);
	}
	
	/*
	
	private org.json.JSONArray getLookupContent(String lookup, RESTWorkshop workshop){
		org.json.JSONArray entries = new org.json.JSONArray();
		int currentIndex = 0;
		int totalSize = 0;
		org.json.JSONObject resp = null;
		org.json.JSONArray rows = null;
		org.json.JSONArray values = null;
		java.util.Map<String, String> qp = new java.util.TreeMap<>();
		qp.put("lookup", "'" + lookup + "'");
		qp.put("fields", "LookupValue.Code,LookupValueLang.Name(es),LookupValueIdentifier.Code(ATG)");
		qp.put("query", "LookupValue.IsActive equals true");
		qp.put("pageSize", "900");
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
				entries.put(new org.json.JSONObject().put("code", values.getString(0)).put("name", values.getString(1)).put("altCode", values.getString(2)));
			}
		}while(currentIndex < totalSize);
		currentIndex = 0;
		
		return entries;
	}
	
	private String getAtributoSapLatalla(String itemGroup, RESTWorkshop rw) {
		String value = null;
		String dp = ("TallaUnicavsTallaS4H");
		rw.putParameter("dictionaryProxy", "'" + dp + "'");
		rw.putParameter("fields", "StandardizationValue.AlternativeValue");
		rw.putParameter("query", "StandardizationValue.Dictionary->StandardizationDictionary.Identifier equals \"" + dp + "\" and StandardizationValue.Value equals \"" + itemGroup + "\"");

		org.json.JSONObject response = rw.makeRequest("GET", "/list/StandardizationValue/bySearch");
		if(response != null) {
			org.json.JSONArray rows = response.getJSONArray("rows");
			if(rows.length() > 0) {
				value = rows.getJSONObject(0).getJSONArray("values").getString(0);
			}
		}else {
			System.out.println("###$$ ERROR: " + rw.getRawResponse());
		}
		if(value == null || "".equals(value)) {
			dp = ("ItemGroupSAPSizeAttribute");
			rw.putParameter("dictionaryProxy", "'" + dp + "'");
			rw.putParameter("fields", "StandardizationValue.AlternativeValue");
			rw.putParameter("query", "StandardizationValue.Dictionary->StandardizationDictionary.Identifier equals \"" + dp + "\" and StandardizationValue.Value equals \"" + itemGroup + "\"");

			response = rw.makeRequest("GET", "/list/StandardizationValue/bySearch");
			if(response != null) {
				org.json.JSONArray rows = response.getJSONArray("rows");
				if(rows.length() > 0) {
					value = rows.getJSONObject(0).getJSONArray("values").getString(0);
				}
			}else {
				System.out.println("###$$ ERROR: " + rw.getRawResponse());
			}
		}
		return value;
	}

	private String seleccionaLaDesa(String attribute, RESTWorkshop workshop){
		org.json.JSONObject response = null;
		java.util.Map<String, String> qp = new java.util.TreeMap<>();
		qp.put("lookup", "Characteristics");
		qp.put("query",  "LookupValue.Code equals \"" + attribute + "\"");
		qp.put("fields", "LookupValue.Code,LookupValueIdentifier.Code(S4HANA),LookupValueIdentifier.Code(ECC)");
		logMe("Checking: " + attribute);
		response = workshop.makeRequest("GET", "/list/LookupValue/bySearch", qp, null);
		logMe("GOT as response: " + (response != null ? response : workshop.getRawResponse()));
		return response != null && response.getJSONArray("rows").length() > 0 ? 
						"".equals( response.getJSONArray("rows").getJSONObject(0).getJSONArray("values").getString(1) ) ? 
								response.getJSONArray("rows").getJSONObject(0).getJSONArray("values").getString(2) : 
							response.getJSONArray("rows").getJSONObject(0).getJSONArray("values").getString(1) : "";

	}
	
	private String lookupInSAP(String field, RESTWorkshop rw) {
		
		java.util.Map<String, String> qp = new java.util.TreeMap<>();
		qp.put("fields", "LookupValue.Code,LookupValueIdentifier.Code(ECC),LookupValueIdentifier.Code(S4HANA)");
		qp.put("query", "LookupValue.Code equals \"" + field + "\" and ((not LookupValueIdentifier.Code(S4HANA) is empty) or (not LookupValueIdentifier.Code(ECC) is empty))");
		qp.put("lookup", "'Characteristics'");
		org.json.JSONObject response = null;
		org.json.JSONArray values = null;
		response = rw.makeRequest("GET", "/list/LookupValue/bySearch", qp, null);
		if(response != null) {
			if(response.getJSONArray("rows").length() > 0) {
				values = response.getJSONArray("rows").getJSONObject(0).getJSONArray("values");
				return "".equals(values.getString(1)) ? values.getString(2) : values.getString(1);
			}
		}
		return null;
		
	}
	private java.util.LinkedList<java.util.Map.Entry<String, org.json.JSONArray>> getBoys(String field, String itemGroup, RESTWorkshop rw, boolean isTamanoUnico){
		logMe("Getting boys given itemGroup: " + itemGroup + " for field: " + field);
		java.util.LinkedList<java.util.Map.Entry<String, org.json.JSONArray>> entries = new java.util.LinkedList<>();
		String lookup = null;
		lookup = field + "LOV";
		logMe("Got: " + lookup + "...");
		java.util.Map<String, String> qp = new java.util.TreeMap<>();
		qp.put("fields", "LookupValueReference.LookupValues('" + lookup + "')->LookupValue.Code,LookupValueReference.LookupValues('" + lookup + "')->LookupValueLang.Name(es),LookupValueReference.LookupValues('" + lookup + "')->LookupValueIdentifier.Code(ATG)");
		qp.put("query", "LookupValue.Code equals \"" + itemGroup + "\"");
		qp.put("lookup", "'MATKLLOV'");
		org.json.JSONObject response = null;
		org.json.JSONArray values = null;
		org.json.JSONArray allowedValueCodes = null;
		org.json.JSONArray allowedValueNames = null;
		org.json.JSONArray allowedValueHexes = null;
		logMe("Looking for: " + itemGroup);
		response = rw.makeRequest("GET", "/list/LookupValue/bySearch", qp, null);
		logMe("Raw got from Boys: " + (response == null ? "Irror: " + rw.getRawResponse() : response));
		if(response != null) {
			if(response.getJSONArray("rows").length() > 0) {
				values = response.getJSONArray("rows").getJSONObject(0).getJSONArray("values");
				allowedValueCodes = values.getJSONArray(0);
				allowedValueNames = values.getJSONArray(1);
				allowedValueHexes = values.getJSONArray(2);
				for(int i=0; i<allowedValueCodes.length(); i++) {
					if(!"".equals(allowedValueCodes.getString(0)))
						entries.addLast(new java.util.AbstractMap.SimpleEntry<>(allowedValueCodes.getString(i), new org.json.JSONArray().put(allowedValueNames.getString(i)).put(allowedValueHexes.getString(i))));
				}
			}else {
				qp.put("lookup", "'MATKLLOV_S4H'");
				response = rw.makeRequest("GET", "/list/LookupValue/bySearch", qp, null);
				if(response != null) {
					if(response.getJSONArray("rows").length() > 0) {
						values = response.getJSONArray("rows").getJSONObject(0).getJSONArray("values");
						allowedValueCodes = values.getJSONArray(0);
						allowedValueNames = values.getJSONArray(1);
						allowedValueHexes = values.getJSONArray(2);
						for(int i=0; i<allowedValueCodes.length(); i++) {
							if(!"".equals(allowedValueCodes.getString(i)))	
								entries.addLast(new java.util.AbstractMap.SimpleEntry<>(allowedValueCodes.getString(i), new org.json.JSONArray().put( allowedValueNames.getString(i) ).put(allowedValueHexes.getString(i))));
						}
					}
				}else {
					logMe(rw.getRawResponse());
				}
			}
		}else {
			logMe(rw.getRawResponse());
		}
		if(entries.isEmpty() && isTamanoUnico) {
			org.json.JSONArray ents = getLookupContent(lookup, rw);
			for(int i=0; i<ents.length(); i++) {
				entries.addLast(new java.util.AbstractMap.SimpleEntry<>(ents.getJSONObject(i).getString("code"), new org.json.JSONArray().put( ents.getJSONObject(i).getString("name") ).put(ents.getJSONObject(i).getString("altCode"))));
			}
			logMe("Done putting extra content...");
		}
		return entries;
	}
	
	private String fieldLookup(String field, RESTWorkshop rw) {
		java.util.Map<String, String> qp = new java.util.TreeMap<>();
		qp.put("fields", "Characteristic.Lookup->Lookup.Identifier");
		qp.put("query", "Characteristic.Identifier equals \"" + field + "\"");
		org.json.JSONObject response = null;
		org.json.JSONArray values = null;
		response = rw.makeRequest("GET", "/list/Characteristic/bySearch", qp, null);
		if(response != null) {
			if(response.getJSONArray("rows").length() > 0) {
				values = response.getJSONArray("rows").getJSONObject(0).getJSONArray("values");
				return "".equals(values.getString(1)) ? values.getString(2) : values.getString(1);
			}
		}
		return null;
	}
	
	*/
	
	private String encode(String a) {
		return java.net.URLEncoder.encode(a, java.nio.charset.StandardCharsets.UTF_8);
	}
	
	private void logMe(String message) {
		LOGGER.info(message);
//        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
//                new java.io.FileOutputStream("../logs/getLookupValues.log", true)))) {
//            pw.println("[" + (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()))
//                    + "]  " + message);
//        } catch (java.io.IOException e) {
//        }
    }

    private static void logE(Exception ex) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream("../logs/getLookupValues.log", true)))) {
            ex.printStackTrace(pw);
        } catch (java.io.IOException e) {
        }
    }

	
	private static final Logger LOGGER = Logger.getLogger(GetListOfValues.class.getName());

    static {
        try {
            LOGGER.setUseParentHandlers(false); // evita que también salga en consola con formato default

            FileHandler fileHandler = new FileHandler("../logs/getLookupValues.%g.log", 10 * 1024 * 1024, 10, true); // true = append
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

                    return "[" + timestamp + "]  " + formatMessage(record) + System.lineSeparator();
                }
            });

            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);

        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el logger", e);
        }
    }
    public static void main(String[] args) {
    	
    }
    
}
