package mx.com.liverpool.p360.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.PropertiesManager;
import mx.com.liverpool.p360.services.core.RESTWorkshop;
import mx.com.liverpool.p360.services.core.RESTWrapper;

/**
 * Servlet implementation class DatosModernizacionCCL
 */
@WebServlet("/public/rt/DatosModernizacionCCL")
public class DatosModernizacionCCL extends HttpServlet {

	private static final RESTWrapper rw = new RESTWrapper();
	private static final RESTWorkshop workshop = rw.getRw();
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DatosModernizacionCCL() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RESTWrapper rw = new RESTWrapper();
		RESTWorkshop workshop = rw.getRw();
		org.json.JSONObject jr = new org.json.JSONObject();
		org.json.JSONArray objects = new org.json.JSONArray();
		jr.put("objects", objects);
		String skus = request.getParameter("skus");
		if(skus != null) {
			java.util.List<String> pieces = new java.util.ArrayList<>(java.util.Arrays.asList( skus.split(",") ) );
			java.util.List<String[]> index = loadIndex();
			for(String key : pieces) {
				String[] array = getData(key, index);
				if(array == null) {
					objects.put( 
							new org.json.JSONObject()
								.put( "sku", key )
								.put( "ProductType", "" )
								.put( "ColoursLiverpoolAtt", "" )
								.put( "SupplierPartNumber", "" )
								.put( "SupplierID", "" )
								.put( "Section", "" )
								.put( "Name", "" )
								.put( "ProductWidth", "" )
								.put( "ItemGroup", "" )
								.put( "NoSpot", "" )
								.put( "DescriptionLong", "")
								.put( "ImageURL", "" )
						);
				}else {
					// ProductType,ColoursLiverpoolAtt,SupplierPartNumber,SupplierID,Section,Name,ProductWidth,ItemGroup,NoSpot_AE416
					String[] pw = workshop.parseLine(array[11], "\"", ";", "\\");
					objects.put( 
							new org.json.JSONObject()
								.put( "sku", array[2] )
								.put( "ProductType", "".equals(array[5]) ? "" : workshop.parseLine( array[5], "\"", ";", "\\" )[1])
								.put( "ColoursLiverpoolAtt", "".equals(array[6]) ? "" : workshop.parseLine(array[6], "\"", ";", "\\")[1] )
								.put( "SupplierPartNumber", array[7] )
								.put( "SupplierID", array[8] )
								.put( "Section", "".equals(array[9]) ? "" : workshop.parseLine(array[9], "\"", ";", "\\")[1] )
								.put( "Name", array[10] )
								.put( "ProductWidth", !"".equals(array[11]) ? pw[1] : "" )
								.put( "ProductWidthUOM" , !"".equals(array[11]) ? pw[0] : "")
								.put( "ItemGroup", "".equals(array[12]) ? "" : workshop.parseLine( array[12], "\"", ";", "\\" )[1] )
								.put( "DescriptionLong", array[13])
								.put( "NoSpot", array[14] )
								.put( "ImageURL", array.length > 15 ? array[15] : getMeProductImage(array[2]) )
						);
				}
			}
		}

		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(jr.toString());
	}
	
	private String getMeProductImage(String sku) {
		java.util.Map<String, String> qp = new java.util.HashMap<>();
		qp.put("fields", "ArticleCharacteristicValueLang.Value('ProductImage',\"0000.0000.RK\",\"0000.0000.RK\",'ProductImage_URL',-1)");
		qp.put("query", "characteristic('SKU') = \"" + sku + "\"");
		String[] hola = new String[1];
		hola[0] = null;
		rw.collectData("list", "Article", null, "bySearch", qp, row ->{
			hola[0] = row.getJSONArray("values").getJSONArray(0).getString(0);
		} );
		return hola[0];
	}
	
	private java.util.List<String[]> loadIndex(){
		java.util.List<String[]> index = new java.util.ArrayList<>();
		try(java.io.BufferedReader br0 = java.nio.file.Files.newBufferedReader(java.nio.file.Paths.get( PropertiesManager.get("p360.contingency.modernizacion_ccl.range_dir"), "index.csv" ))){
			String ln = br0.readLine();
			String[] pcs = null;
			while((ln = br0.readLine()) != null) {
				pcs = workshop.parseLine(ln);
				index.add(pcs);
			}
		}catch(java.io.IOException e) {
			e.printStackTrace();
		}
		return index;
	}
	
	private String[] getData(String sku, java.util.List<String[]> elements) {
		String fileName = getFileName(sku, elements);
		if(fileName != null) {
			String ln = null;
			String[] pcs = null;
			System.out.println("Now reading...");
			try(java.io.BufferedReader br0 = java.nio.file.Files.newBufferedReader(java.nio.file.Paths.get( PropertiesManager.get("p360.contingency.modernizacion_ccl.range_dir"), fileName ))){
				while((ln = br0.readLine()) != null) {
					pcs = workshop.parseLine(ln);
					if(sku.equals(pcs[2])) {
						return pcs;
					}
				}
			}catch(java.io.IOException e) {
				e.printStackTrace();
			}
		}else {
			System.out.println("No file name found. " + sku);
		}
		return null;
	}

	private String getFileName(String sku, java.util.List<String[]> elements) {
		if(elements.isEmpty()) {
			return null;
		}
		int mid = elements.size()/2;
		int cmp = 0;
		String[] pieces = null;
		pieces = elements.get(mid);
		cmp = pieces[1].compareTo(sku);
		if(cmp > 0) {
			if(elements.size() == 1) {
				return null;
			}
			return getFileName(sku, java.util.Arrays.asList( java.util.Arrays.copyOfRange(elements.toArray(new String[][] {}), 0, mid) ));
		}else if(cmp < 0) {
			cmp = pieces[2].compareTo(sku);
			if(cmp >= 0) {
				return pieces[0];
			} else {
				if(elements.size() == 1) {
					return null;
				}
				return getFileName(sku, java.util.Arrays.asList( java.util.Arrays.copyOfRange(elements.toArray(new String[][] {}), mid, elements.size()) ));
			}
		}else {
			return pieces[0];
		}
	}
	

}
