package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.PropertiesManager;
import mx.com.liverpool.p360.services.core.net.DataRequestor;

/**
 * Servlet implementation class GetProductSystemOwner
 */
@WebServlet("/public/rt/GetProductSystemOwner")
public class GetProductSystemOwner extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetProductSystemOwner() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		org.json.JSONObject jr = new org.json.JSONObject();
		org.json.JSONArray objects = new org.json.JSONArray();
		org.json.JSONArray objectsGeneric = new org.json.JSONArray();
		jr.put("objects", objects);
		jr.put("objectsGeneric", objectsGeneric);
		String skus = request.getParameter("skus");
		String skusGeneric = request.getParameter("skusGeneric");
		DataRequestor dr = new DataRequestor();
		String pn = null;
		String supplierID = null;
		java.util.Set<String> proveedoresMigrados = cargaProveedoresMigrados();
		java.util.Map<String, String> map = new java.util.HashMap<>();
		java.util.Map<String, String> statusMap = getStatusMap();
		if(skus != null) {
			String[] pieces = skus.split(",");
			java.util.Map<String, String> skusAProductNo = new java.util.TreeMap<>();
			java.util.Set<String> productNos = new java.util.TreeSet<>();
			java.util.Map<String, String> productNoASupplierID = new java.util.TreeMap<>();
			if(pieces != null) {
				org.json.JSONArray skusArray = new org.json.JSONArray();
				for(int i=0; i<pieces.length; i++) {
					skusArray.put(pieces[i]);
				}
				String resp = dr.articleBySKU(skusArray);
				if(resp != null) {
					logMe("Resp is: " + resp);
					try{
						org.json.JSONObject jsonResponse = new org.json.JSONObject(resp);
						org.json.JSONArray items = jsonResponse.getJSONArray("items");
						for(int i=0; i<items.length(); i++) {
							skusAProductNo.put(items.getJSONObject(i).getString("article_sku"), items.getJSONObject(i).getString("product"));
							productNos.add(items.getJSONObject(i).getString("product"));
						}
						logMe("." + pieces.length + "." + items.length() + "." + productNos.size());
						org.json.JSONArray productArray = new org.json.JSONArray();
						productNos.forEach(productArray::put);
						resp = dr.getProductData(productArray);
						jsonResponse = new org.json.JSONObject(resp);
						items = jsonResponse.getJSONArray("items");
						for(int i=0; i<items.length(); i++) {
							logMe( productArray.getString(i) + " - " + items.getJSONObject(i));
							productNoASupplierID.put(productArray.getString(i), items.getJSONObject(i).getString("SupplierID"));
							map.put(productArray.getString(i), items.getJSONObject(i).getString("CurrentStatus"));
						}
						logMe("." + pieces.length + "." + items.length());
						for(int i=0; i<pieces.length; i++) {
//							objects.put(new org.json.JSONObject().put("sku", pieces[i]).put("owner", proveedoresMigrados.contains( productNoASupplierID.get( skusAProductNo.get( pieces[i] ) )  ) || skusAProductNo.get(pieces[i]).length() >= 15 ? "P360" : "STEP" ));
							pn = skusAProductNo.get(pieces[i]);
							if(pn != null) {
								if(pn.length() >= 15) {
									objects.put(new org.json.JSONObject().put("sku", pieces[i]).put("owner", "P360" ).put("status", nvl(statusMap.get( map.get(pn) ))));
								}else {
									supplierID = productNoASupplierID.get(pn);
									if(supplierID != null) {
										objects.put(new org.json.JSONObject().put("sku", pieces[i]).put("owner", proveedoresMigrados.contains( supplierID  ) ? "P360" : "STEP" ).put("status", nvl( statusMap.get( map.get(pn) ))));
									}else {
										objects.put(new org.json.JSONObject().put("sku", pieces[i]).put("owner", "STEP" ).put("status", nvl( statusMap.get(map.get(pn)))));
									}
								}
							}else {
								objects.put(new org.json.JSONObject().put("sku", pieces[i]).put("owner", "STEP" ).put("status", ""));
							}
						}
					}catch(org.json.JSONException e) {
						e.printStackTrace();
					}
				}else {
					logMe("Error.");
				}
			}
		}
		if(skusGeneric != null) {
			String[] pieces = skusGeneric.split(",");
			java.util.Map<String, String> skusAProductNo = new java.util.TreeMap<>();
			java.util.Map<String, String> productNoASupplierID = new java.util.TreeMap<>();
			if(pieces != null) {
				org.json.JSONArray skusArray = new org.json.JSONArray();
				for(int i=0; i<pieces.length; i++) {
					skusArray.put(pieces[i]);
				}
//				System.out.println("Requesting: " + skusArray);
				String resp = dr.productBySKU(skusArray);
//				System.out.println("Got: " + resp);
				if(resp != null) {
					try{
						org.json.JSONObject jsonResponse = new org.json.JSONObject(resp);
						org.json.JSONArray items = jsonResponse.getJSONArray("items");
						org.json.JSONArray productArray = new org.json.JSONArray();
						for(int i=0; i<items.length(); i++) {
							if(!"".equals(items.getString(i))) {
								productArray.put(items.getString(i));
								skusAProductNo.put(pieces[i], items.getString(i));
							}
						}
//						System.out.println("Sending now: " + productArray);
						resp = dr.getProductData(productArray);
//						System.out.println("Got now: " + resp);
						jsonResponse = new org.json.JSONObject(resp);
						items = jsonResponse.getJSONArray("items");
						for(int i=0; i<items.length(); i++) {
//							System.out.println( productArray.getString(i) + " - " + items.getJSONObject(i));
							productNoASupplierID.put(productArray.getString(i), items.getJSONObject(i).getString("SupplierID"));
							map.put(productArray.getString(i), items.getJSONObject(i).getString("CurrentStatus"));
						}
						logMe(".-. lst .-.");
						for(int i=0; i<pieces.length; i++) {
							pn = skusAProductNo.get(pieces[i]);
							if(pn != null) {
								if(pn.length() >= 15) {
									objectsGeneric.put(new org.json.JSONObject().put("sku", pieces[i]).put("owner", "P360" ).put("status", nvl( statusMap.get( map.get(pn) ))));
								}else {
									supplierID = productNoASupplierID.get(pn);
									if(supplierID != null) {
										objectsGeneric.put(new org.json.JSONObject().put("sku", pieces[i]).put("owner", proveedoresMigrados.contains( supplierID  ) ? "P360" : "STEP" ).put("status", nvl( statusMap.get( map.get(pn) ))));
									}else {
										objectsGeneric.put(new org.json.JSONObject().put("sku", pieces[i]).put("owner", "STEP" ).put("status", nvl( statusMap.get( map.get(pn) ))));
									}
								}
							}else {
//								System.out.println("No lo tuve: " + pieces[i]);
								objectsGeneric.put(new org.json.JSONObject().put("sku", pieces[i]).put("owner", "STEP" ).put("status", ""));
							}
						}
					}catch(org.json.JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}

		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(jr.toString());
	}

	private String nvl(String val) {
		return val == null ? "" : val;
	}
	
	private java.util.Set<String> cargaProveedoresMigrados(){
		try {
			return java.nio.file.Files.lines(java.nio.file.Paths.get(PropertiesManager.get("p360.contingency.base_directory"), "cache", "proveedores_migrados"), java.nio.charset.StandardCharsets.UTF_8).collect(java.util.stream.Collectors.toSet());
		}catch(java.io.IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	private void logMe(String message) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream("../logs/getOwner.log", true)))) {
            pw.println("[" + (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()))
                    + "]  " + message);
        } catch (java.io.IOException e) {
        }
    }

	private final java.util.Map<String, String> getStatusMap() {
		java.util.Map<String, String> data = new java.util.HashMap<>();
		java.util.List<String> pieces = java.util.Arrays.asList((""
				+ "Proposal Generated	1001\r\n"
				+ "Pending Enrichment	1002\r\n"
				+ "Purchase Revision	1003\r\n"
				+ "Image Load	1004\r\n"
				+ "Rejected	1005\r\n"
				+ "To Be Updated	1006\r\n"
				+ "Approved	1007\r\n"
				+ "Modified	1008\r\n"
				+ "Canceled	1009\r\n"
				+ "Liverpool in progress	1010\r\n"
				+ "Sending in progress	1011\r\n"
				+ "SKU Creation	1020\r\n"
				+ "Data Gobernance	1021\r\n"
				+ "QA Revision	1022\r\n"
				+ "Category	1023\r\n"
				+ "Publish Rejected	1024\r\n"
				+ "Deleted	1025\r\n"
				+ "In Foro Process	1026\r\n"
				+ "Draft	10031\r\n"
				+ "Purchase Rejected	1027\r\n"
				+ "QA Rejected	1028\r\n"
				+ "Governance Rejected	1029\r\n"
				+ "Category Rejected	1030\r\n"
				+ "Repopulation	1031\r\n"
				+ "Cataloguing Exception	1032").split("\\r\\n"));
		String[] parts = null;
		for(String line : pieces) {
			parts = line.split("\t");
			data.put(parts[1], parts[0]);
		}
		return data;
	}
	
}
