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

import mx.com.liverpool.p360.services.core.DBAccessDataStub;
import mx.com.liverpool.p360.services.core.ELog;

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
    @Override
    protected void doGet(
    		HttpServletRequest request,
    		HttpServletResponse response)
    		throws ServletException, IOException {

    	String template =
    			trimToNull(
    				request.getParameter("template"));

    	String characteristic =
    			trimToNull(
    				request.getParameter("characteristic"));

    	String creationType =
    			trimToNull(
    				request.getParameter("creationType"));
    	if (creationType == null) {
    		creationType = this.creationType;
    	}

    	boolean alternatives =
    			Boolean.parseBoolean(
    				request.getParameter("includeAlternative"));

    	org.json.JSONArray responseValues =
    			new org.json.JSONArray();

    	final java.util.function.Consumer<String> dbMessageLogger =
    			this::logMe;

    	final java.util.function.Consumer<Exception> dbErrorLogger =
    			this::logE;

    	ELog dbLog = new ELog() {

    		@Override
    		public void log(String message) {
    			dbMessageLogger.accept(message);
    		}

    		@Override
    		public void logE(Exception e) {
    			dbErrorLogger.accept(e);
    		}
    	};
    			
    	try (DBAccessDataStub dastub =
    			new DBAccessDataStub(dbLog)) {

    		if (characteristic != null) {

    			org.json.JSONObject characteristicData =
    					dastub.getCharacteristicData(
    							characteristic);

    			String lookup =
    					trimToNull(
    						characteristicData.optString(
    								"lookup",
    								""));

    			logMe(
    				"For characteristic: "
    				+ characteristic
    				+ ", got lov: "
    				+ lookup
    				+ " on template: "
    				+ template);
    			
    			if (lookup != null) {
    				org.json.JSONArray jarr = dastub.getTemplateCharacteristicPropertyValue(template, characteristic, creationType, "listofValuesValidValues");
    				String validValues =
    						getValidValues(
    								dastub,
    								template,
    								characteristic,
    								creationType);

    				java.util.List<org.json.JSONObject> lookupRows =
    						dastub
    							.getLookupValueCodeNameExternalCodeRows(
    								lookup,
    								10,
    								"ATG",
    								true);
    				logMe(
        					"About valid values... ->"
        					+ validValues
        					+ "<- "
        					+ lookup + " <:> " + jarr);
//    				if(validValues == null && jarr != null && jarr.length() > 0) {
    				java.util.Set<String> allowedCodis = null;
    				if(jarr != null && jarr.length() > 0) {
    					org.json.JSONObject lachar = jarr.getJSONObject(0).getJSONObject(characteristic);
    					if(!lachar.has("listofValuesValidValues")) {
    						logMe("Maah: " + jarr);
    					}else {
	    					String vvs = lachar.getString("listofValuesValidValues");
	    					allowedCodis =
	        						parseValidValues(vvs);
	    					logMe("vvs: " + vvs + ". YEs: " + allowedCodis);
//	    					logMe("Catarinas: " + lookupRows);
    					}
    				}

    				java.util.Set<String> allowedCodes = allowedCodis == null ?
    						parseValidValues(validValues) : allowedCodis;

    				java.util.List<Object> sortedValues =
    						new java.util.ArrayList<>();

    				for (org.json.JSONObject lookupRow :
    						lookupRows) {

    					String code =
    							lookupRow.optString(
    									"code",
    									"");

    					if (allowedCodes != null
    							&& !allowedCodes.contains(code)) {

    						continue;
    					}

    					String name =
    							lookupRow.optString(
    									"name",
    									"");

    					if (alternatives) {

    						sortedValues.add(
    							new org.json.JSONObject()
    								.put(
    									"label",
    									name)
    								.put(
    									"hex",
    									lookupRow.optString(
    										"externalCode",
    										"")));

    					} else {
    						sortedValues.add(name);
    					}
    				}

    				sortedValues.sort(
    					(o1, o2) -> {

    						if (o1 instanceof org.json.JSONObject) {

    							return ((org.json.JSONObject) o1)
    									.optString("label", "")
    									.compareTo(
    										((org.json.JSONObject) o2)
    											.optString(
    												"label",
    												""));
    						}

    						return String.valueOf(o1)
    								.compareTo(
    									String.valueOf(o2));
    					});

    				for (Object value : sortedValues) {
    					responseValues.put(value);
    				}
    			}
    		}
    
    	} catch (RuntimeException e) {
    		logE(e);
    	}

    	org.json.JSONObject rawResponse =
    			new org.json.JSONObject()
    				.put(
    					"values",
    					responseValues);

    	response.setHeader(
    			"Content-Type",
    			"application/json");

    	response.setHeader(
    			"Accept",
    			"application/json");

    	response.setCharacterEncoding("UTF-8");

    	response.getWriter().println(
    			rawResponse.toString());
    }
    
    private String getValidValues(
    		DBAccessDataStub dastub,
    		String template,
    		String characteristic,
    		String creationType) {

    	String validValues = null;

    	if (template != null) {

    		java.util.Map<String, org.json.JSONObject>
    				templateProperties =
    					dastub.getTemplateCharacteristicProperties(
    							template,
    							creationType);

    		org.json.JSONObject characteristicProperties =
    				templateProperties.get(characteristic);

    		validValues =
    				getNonBlankProperty(
    						characteristicProperties,
    						"listofValuesValidValues");
    		if(validValues == null || "".equals(validValues)) {
    			validValues =
        				getNonBlankProperty(
        						characteristicProperties,
        						"ListOfValuesFilter");
    		}
    	}

    	if (validValues == null) {

    		org.json.JSONObject globalMetadata =
    				dastub.getGlobalMetadata(
    						"CreateProposal");

    		validValues =
    				getNonBlankProperty(
    						globalMetadata.optJSONObject(
    								characteristic),
    						"listofValuesValidValues");
    		if(validValues == null || "".equals(validValues)) {
    			validValues =
        				getNonBlankProperty(
        						globalMetadata,
        						"ListOfValuesFilter");
    		}
    	}

    	return validValues;
    }

    private String getNonBlankProperty(
    		org.json.JSONObject properties,
    		String property) {

    	if (properties == null
    			|| property == null
    			|| !properties.has(property)
    			|| properties.isNull(property)) {

    		return null;
    	}

    	return trimToNull(
    			properties.optString(
    					property,
    					null));
    }

    private java.util.Set<String> parseValidValues(
    		String validValues) {

    	if (validValues == null
    			|| validValues.isBlank()) {

    		return null;
    	}

    	java.util.Set<String> values =
    			new java.util.LinkedHashSet<>();

    	for (String piece :
    			validValues.split(",")) {

    		String value =
    				trimToNull(piece);

    		if (value != null) {
    			values.add(value);
    		}
    	}
    	
    	return values.isEmpty()
    			? null
    			: values;
    }

    private String trimToNull(String value) {

    	if (value == null) {
    		return null;
    	}

    	String trimmed =
    			value.trim();

    	return trimmed.isEmpty()
    			? null
    			: trimmed;
    }
	
	
	private void logMe(String message) {
		LOGGER.info(message);
    }

    private void logE(Exception ex) {
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
