package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.PropertiesManager;
import mx.com.liverpool.p360.services.core.RESTWrapper;
import mx.com.liverpool.p360.services.core.RequestHandler;

/**
 * Servlet implementation class CharacteristicTemplateValues
 */
@WebServlet("/public/rt/CharacteristicTemplateValues")
public class CharacteristicTemplateValues extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CharacteristicTemplateValues() {
        super();
        // TODO Auto-generated constructor stub
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long init = System.currentTimeMillis();
		RESTWrapper rw = new RESTWrapper();
		String batchSizeString = PropertiesManager.get("p360.contingency.ia.characteristic_template_values.batch_size", "1000");
		Integer batchSize = Integer.parseInt(batchSizeString);
		request.setCharacterEncoding("UTF-8");
		java.io.BufferedReader br = request.getReader();
		String line = null;
		StringBuilder sb = new StringBuilder();
		Object rawResponse = null;
		String key = null;
		java.util.LinkedList<String> responses = new java.util.LinkedList<>();
		java.util.Map<String, String> qp = new java.util.TreeMap<>();
		while((line = br.readLine()) != null) {
			sb.append(line);
		}
		qp.put("includeObjectsInProtocol", "false");
		RequestHandler rh = new RequestHandler(
				new org.json.JSONArray()
					.put(new org.json.JSONObject().put("identifier", "StandardizationValue.StructureGroup"))
					.put(new org.json.JSONObject().put("identifier", "StandardizationValue.Characteristic"))
					.put(new org.json.JSONObject().put("identifier", "StandardizationValue.CreationType"))
					.put(new org.json.JSONObject().put("identifier", "StandardizationValue.Property"))
					.put(new org.json.JSONObject().put("identifier", "StandardizationValue.PropertyValue"))
				, batchSize, req -> rw.writeData("list", "StandardizationValue", null, qp, req, responses::addLast));
		org.json.JSONObject json;
		try {
			org.json.JSONObject rootRequest = new org.json.JSONObject(sb.toString());
			org.json.JSONArray records = rootRequest.getJSONArray("records");
			for(int i=0; i<records.length(); i++) {
				json = records.getJSONObject(i);
				key = json.getString("template") + "<::>" + json.getString("characteristic") + "<::>CreateProposal<::>" + json.getString("property");
				rh.addRow(new org.json.JSONObject().put("object", new org.json.JSONObject().put("id", "'" + key + "'@'ExtensionDeMetadatos_ ValoresPredeterminadosPorPlantilla'")).put("values", new org.json.JSONArray().put(json.getString("template")).put(json.getString("characteristic")).put("CreateProposal").put(json.getString("property")).put(json.getString("propertyValue"))));
			}
			rh.sendData();
		}catch(org.json.JSONException e) {
			e.printStackTrace();
		}
		org.json.JSONObject jc = null;
		org.json.JSONArray je = null;
		org.json.JSONObject counters = new org.json.JSONObject();
		counters.put("objectsWithErrors", 0);
		counters.put("warnings", 0);
		counters.put("createdObjects", 0);
		counters.put("updatedObjects", 0);
		counters.put("objectsWithWarnings", 0);
		counters.put("errors", 0);
		org.json.JSONArray entries = new org.json.JSONArray();
		for(String r : responses) {
			try {
				json = new org.json.JSONObject(r);
				jc = json.getJSONObject("counters");
				counters.put("objectsWithErrors", counters.getInt("objectsWithErrors") + jc.getInt("objectsWithErrors"));
				counters.put("warnings", counters.getInt("warnings") + jc.getInt("warnings"));
				counters.put("createdObjects", counters.getInt("createdObjects") + jc.getInt("createdObjects"));
				counters.put("updatedObjects", counters.getInt("updatedObjects") + jc.getInt("updatedObjects"));
				counters.put("objectsWithWarnings", counters.getInt("objectsWithWarnings") + jc.getInt("objectsWithWarnings"));
				counters.put("errors", counters.getInt("errors") + jc.getInt("errors"));
				je = json.getJSONArray("entries");
				for(int i=0; i<je.length(); i++) {
					entries.put(je.getJSONObject(i));
				}
			}catch(org.json.JSONException e) {
				logE(e);
			}
		}
		rawResponse = new org.json.JSONObject().put("counters", counters).put("entries", entries);
		logM(String.valueOf( rawResponse ));
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(rawResponse);
		logM( rw.getRw().formatTime(System.currentTimeMillis() - init) );
	}


	private void logM(String message){
		try(java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream("../logs/characteristic_template_values.log", true)))){
		  pw.println("[" + (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())) + "]  " + message);
		}catch(java.io.IOException e){}
	}

	private void logE(Exception ex){
		try(java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream("../logs/characteristic_template_values.log", true)))){
		  ex.printStackTrace(pw);
		}catch(java.io.IOException e){}
	}
}
