package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.QuickJdbcConnectionManager;

/**
 * Servlet implementation class SurtidoYXDFastDataAccess
 */
@WebServlet("/public/rt/SurtidoYXDFastDataAccess")
public class SurtidoYXDFastDataAccess extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SurtidoYXDFastDataAccess() {
        super();
        // TODO Auto-generated constructor stub
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String skus = request.getParameter("skus");
		QuickJdbcConnectionManager cn = new QuickJdbcConnectionManager();
		org.json.JSONArray responses = new org.json.JSONArray();
		String[] skuArray = skus == null ? new String[] {} : skus.split(",");
		java.util.Set<String> currentSKUs = new java.util.TreeSet<>();
		try( 
			java.sql.Connection con = cn.openConnection(false)
		){
			for(String sku : skuArray) {
				if(currentSKUs.contains(sku))
					continue;
				try(java.sql.PreparedStatement pstmnt = con.prepareStatement(
					  "select /*+ leading(aa bb) use_nl(bb) */"
					+ "   bb.ID"
					+ " , bb.\"EntityID\""
					+ " , aa.\"EAN\""
					+ " , aa.\"Res_Text250_02\""
					+ " , bb.\"Identifier\""
					+ " , bb.\"ArticleID\""
					+ " from \"ArticleDetail\" aa"
					+ " inner join"
					+ " \"ArticleRevision\" bb"
					+ " on"
					+ "     aa.\"ArticleRevisionID\" = bb.ID"
					+ " and aa.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0'"
					+ " and bb.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0'"
					+ " and bb.\"RevisionID\" = 1 "
					+ " where aa.\"Res_Int_02\" = ?"
					+ " order by bb.\"EntityID\" asc"
				)){
					pstmnt.setInt(1, Integer.parseInt(sku));
					Integer id = null;
					Integer entityID = null;
					String identifier = null;
					Integer artID = null;
					Integer productInternalId = null;
					String ean = null;
					Integer parentSKU = null;
					String imageURL = null;
					String productName = null;
					String textoAdicional = null;
					String section = null;
					String itemGroup = null;
					String itemGroupS4H = null;
					String talla = null;
					String color = null;
					try(java.sql.ResultSet rs = pstmnt.executeQuery()){
						if(rs.next()) {
							id = rs.getInt(1);
							entityID = rs.getInt(2);
							ean = rs.getString(3);
							imageURL = rs.getString(4);
							identifier = rs.getString(5);
							artID = rs.getInt(6);
						}
					}
					if(id != null) {
						if( entityID == 1000 ) {
							try(java.sql.PreparedStatement pstmnt2 = con.prepareStatement(
									   "select /*+ leading(aa bb) use_nl(bb) */ "
									+ "   bb.\"ID\""
									+ " from"
									+ " \"ArticleReference\" aa"
									+ " inner join"
									+ " \"ArticleRevision\" bb"
									+ " on"
									+ "     aa.\"RefIntArtID\" = bb.\"ArticleID\""
									+ " and aa.\"RefExtArtIdentifier\" = bb.\"Identifier\""
									+ " where"
									+ " aa.\"ArticleRevisionID\" = ? and aa.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and bb.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0'"
							)){
								pstmnt2.setInt(1, id);
								try(java.sql.ResultSet rs = pstmnt2.executeQuery()){
									if(rs.next()) {
										productInternalId = rs.getInt(1);
									}
								}
							}
							
							try(java.sql.PreparedStatement pstmnt2 = con.prepareStatement(
									"select "
											+ "  aa.\"Res_Int_02\""
											+ " from"
											+ " \"ArticleDetail\" aa"
											+ " where"
											+ " aa.\"ArticleRevisionID\" = ? and aa.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0'"
									)){
								pstmnt2.setInt(1, productInternalId);
								try(java.sql.ResultSet rs = pstmnt2.executeQuery()){
									if(rs.next()) {
										parentSKU = rs.getInt(1);
									}
								}
							}
							
							try(java.sql.PreparedStatement pstmnt2 = con.prepareStatement(
									   "select "
									+ "   aa.\"Res_Text250_01\""
									+ " , aa.\"DescriptionShort\""
									+ " from"
									+ " \"ArticleLang\" aa"
									+ " where"
									+ " aa.\"ArticleRevisionID\" = ? and aa.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0'"
							)){
								pstmnt2.setInt(1, productInternalId);
								try(java.sql.ResultSet rs = pstmnt2.executeQuery()){
									if(rs.next()) {
										productName = rs.getString(1);
										textoAdicional = rs.getString(2);
									}
								}
							}
							
							try(java.sql.PreparedStatement pstmnt2 = con.prepareStatement(
									"select "
											+ "   (select (select \"Name\" from PIM_MAIN.\"LookupValueLang\" cc where cc.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and cc.\"LanguageID\" = 10 and cc.\"LookupValueRevisionID\" = aa.ID ) from PIM_MAIN.\"LookupValueRevision\" aa where aa.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and aa.\"RevisionID\" = 1 and aa.\"LookupValueID\" = ad.\"Res_Int_02\") \"Section\""
											+ " , (select (select \"Name\" from PIM_MAIN.\"LookupValueLang\" cc where cc.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and cc.\"LanguageID\" = 10 and cc.\"LookupValueRevisionID\" = aa.ID ) from PIM_MAIN.\"LookupValueRevision\" aa where aa.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and aa.\"RevisionID\" = 1 and aa.\"LookupValueID\" = ad.\"Res_Int_03\") \"ItemGroup\""
											+ " , (select (select \"Name\" from PIM_MAIN.\"LookupValueLang\" cc where cc.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and cc.\"LanguageID\" = 10 and cc.\"LookupValueRevisionID\" = aa.ID ) from PIM_MAIN.\"LookupValueRevision\" aa where aa.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and aa.\"RevisionID\" = 1 and aa.\"LookupValueID\" = ad.\"Res_Int_04\") \"ItemGroupS4H\""
											+ " from"
											+ " \"ArticleDomain\" ad"
											+ " where"
											+ " ad.\"ArticleRevisionID\" = ? and ad.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0'"
									)){
								pstmnt2.setInt(1, productInternalId);
								try(java.sql.ResultSet rs = pstmnt2.executeQuery()){
									if(rs.next()) {
										section = rs.getString(1);
										itemGroup = rs.getString(2);
										itemGroupS4H = rs.getString(3);
									}
								}
							}
							
							try(java.sql.PreparedStatement pstmnt2 = con.prepareStatement(
									   "select "
									+ "   (select (select \"Name\" from PIM_MAIN.\"LookupValueLang\" cc where cc.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and cc.\"LanguageID\" = 10 and cc.\"LookupValueRevisionID\" = aa.ID ) from PIM_MAIN.\"LookupValueRevision\" aa where aa.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and aa.\"RevisionID\" = 1 and aa.\"LookupValueID\" = ad.\"Res_Int_01\") \"Talla\""
									+ " , (select (select \"Name\" from PIM_MAIN.\"LookupValueLang\" cc where cc.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and cc.\"LanguageID\" = 10 and cc.\"LookupValueRevisionID\" = aa.ID ) from PIM_MAIN.\"LookupValueRevision\" aa where aa.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and aa.\"RevisionID\" = 1 and aa.\"LookupValueID\" = ad.\"Res_Int_02\") \"Color\""
									+ " from"
									+ " \"ArticleDomain\" ad"
									+ " where"
									+ " ad.\"ArticleRevisionID\" = ? and ad.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0'"
							)){
								pstmnt2.setInt(1, id);
								try(java.sql.ResultSet rs = pstmnt2.executeQuery()){
									if(rs.next()) {
										talla = rs.getString(1);
										color = rs.getString(2);
									}
								}
							}
							
							org.json.JSONObject jr = new org.json.JSONObject();
							jr
								.put("sku", sku)
								.put("ean", ean == null ? "" : ean)
								.put("parentSKU", parentSKU == null ? "" : parentSKU)
								.put("productName", productName == null ? "" : productName)
								.put("textoAdicional", textoAdicional == null ? "" : textoAdicional)
								.put("seccion", section == null ? "" : section)
								.put("itemGroup", itemGroup == null ? itemGroupS4H == null ? "" : itemGroupS4H : itemGroup)
								.put("imageURL", imageURL == null ? "" : imageURL)
								.put("tamanoUnico", talla == null ? "" : talla)
								.put("color", color == null ? "" : color)
							;
							responses.put(jr);
							currentSKUs.add(sku);
						}else {
							productInternalId = id;
							parentSKU = Integer.parseInt(sku);
							try(java.sql.PreparedStatement pstmnt2 = con.prepareStatement(
									   "select "
									+ "   aa.\"Res_Text250_01\""
									+ " , aa.\"DescriptionShort\""
									+ " from"
									+ " \"ArticleLang\" aa"
									+ " where"
									+ " aa.\"ArticleRevisionID\" = ? and aa.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0'"
							)){
								pstmnt2.setInt(1, productInternalId);
								try(java.sql.ResultSet rs = pstmnt2.executeQuery()){
									if(rs.next()) {
										productName = rs.getString(1);
										textoAdicional = rs.getString(2);
									}
								}
							}
							
							try(java.sql.PreparedStatement pstmnt2 = con.prepareStatement(
									"select "
											+ "   (select (select \"Name\" from PIM_MAIN.\"LookupValueLang\" cc where cc.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and cc.\"LanguageID\" = 10 and cc.\"LookupValueRevisionID\" = aa.ID ) from PIM_MAIN.\"LookupValueRevision\" aa where aa.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and aa.\"RevisionID\" = 1 and aa.\"LookupValueID\" = ad.\"Res_Int_02\") \"Section\""
											+ " , (select (select \"Name\" from PIM_MAIN.\"LookupValueLang\" cc where cc.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and cc.\"LanguageID\" = 10 and cc.\"LookupValueRevisionID\" = aa.ID ) from PIM_MAIN.\"LookupValueRevision\" aa where aa.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and aa.\"RevisionID\" = 1 and aa.\"LookupValueID\" = ad.\"Res_Int_03\") \"ItemGroup\""
											+ " , (select (select \"Name\" from PIM_MAIN.\"LookupValueLang\" cc where cc.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and cc.\"LanguageID\" = 10 and cc.\"LookupValueRevisionID\" = aa.ID ) from PIM_MAIN.\"LookupValueRevision\" aa where aa.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and aa.\"RevisionID\" = 1 and aa.\"LookupValueID\" = ad.\"Res_Int_04\") \"ItemGroupS4H\""
											+ " from"
											+ " \"ArticleDomain\" ad"
											+ " where"
											+ " ad.\"ArticleRevisionID\" = ? and ad.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0'"
									)){
								pstmnt2.setInt(1, productInternalId);
								try(java.sql.ResultSet rs = pstmnt2.executeQuery()){
									if(rs.next()) {
										section = rs.getString(1);
										itemGroup = rs.getString(2);
										itemGroupS4H = rs.getString(3);
									}
								}
							}
							
							try(java.sql.PreparedStatement pstmnt2 = con.prepareStatement(
									   "select "
									+ "   bb.\"ArticleRevisionID\""
									+ " from"
									+ " \"ArticleReference\" bb"
									+ " where"
									+ " bb.\"RefExtArtIdentifier\" = ? and bb.\"RefIntArtID\" = ? and bb.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0'"
							)){
								pstmnt2.setString(1, identifier);
								pstmnt2.setInt(2, artID);
								try(java.sql.ResultSet rs = pstmnt2.executeQuery()){
									while(rs.next()) {
										id = rs.getInt(1);
										
										try(java.sql.PreparedStatement pstmnt3 = con.prepareStatement(
												"select "
														+ "   aa.\"Res_Int_02\""
														+ "  ,aa.EAN"
														+ " from"
														+ " \"ArticleDetail\" aa"
														+ " where"
														+ " aa.\"ArticleRevisionID\" = ? and aa.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0'"
												)){
											pstmnt3.setInt(1, id);
											try(java.sql.ResultSet rs2 = pstmnt3.executeQuery()){
												if(rs2.next()) {
													sku = String.valueOf( rs2.getInt(1) );
												}
											}
										}
										
										if(currentSKUs.contains(sku))
											continue;
										
										try(java.sql.PreparedStatement pstmnt3 = con.prepareStatement(
												   "select "
												+ "   (select (select \"Name\" from PIM_MAIN.\"LookupValueLang\" cc where cc.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and cc.\"LanguageID\" = 10 and cc.\"LookupValueRevisionID\" = aa.ID ) from PIM_MAIN.\"LookupValueRevision\" aa where aa.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and aa.\"RevisionID\" = 1 and aa.\"LookupValueID\" = ad.\"Res_Int_01\") \"Talla\""
												+ " , (select (select \"Name\" from PIM_MAIN.\"LookupValueLang\" cc where cc.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and cc.\"LanguageID\" = 10 and cc.\"LookupValueRevisionID\" = aa.ID ) from PIM_MAIN.\"LookupValueRevision\" aa where aa.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0' and aa.\"RevisionID\" = 1 and aa.\"LookupValueID\" = ad.\"Res_Int_02\") \"Color\""
												+ " from"
												+ " \"ArticleDomain\" ad"
												+ " where"
												+ " ad.\"ArticleRevisionID\" = ? and ad.\"DeletionTimestamp\" = timestamp '9999-12-31 00:00:00.0'"
										)){
											pstmnt3.setInt(1, id);
											try(java.sql.ResultSet rs2 = pstmnt3.executeQuery()){
												if(rs2.next()) {
													talla = rs2.getString(1);
													color = rs2.getString(2);
												}
											}
										}
										
										org.json.JSONObject jr = new org.json.JSONObject();
										jr
											.put("sku", sku == null ? "" : sku)
											.put("ean", ean == null ? "" : ean)
											.put("parentSKU", parentSKU == null ? "" : parentSKU)
											.put("productName", productName == null ? "" : productName)
											.put("textoAdicional", textoAdicional == null ? "" : textoAdicional)
											.put("seccion", section == null ? "" : section)
											.put("itemGroup", itemGroup == null ? itemGroupS4H == null ? "" : itemGroupS4H : itemGroup)
											.put("imageURL", imageURL == null ? "" : imageURL)
											.put("tamanoUnico", talla == null ? "" : talla)
											.put("color", color == null ? "" : color)
										;
										responses.put(jr);
										currentSKUs.add(sku);
									}
								}
							}
						}
					}else {
						String r = "SKU not found.";
						org.json.JSONObject jr = new org.json.JSONObject();
						jr.put("sku", sku).put("error", r);
						responses.put(jr);
					}
				}catch(NumberFormatException e) {
					String r = "Invalid SKU, must be a numeric integer value.";
					org.json.JSONObject jr = new org.json.JSONObject();
					jr.put("sku", sku).put("error", r);
					responses.put(jr);
				}
			}
		}catch(java.sql.SQLException e) {
			String r= "Technical issues on retrieving data. DB access issue.";
			org.json.JSONObject jr = new org.json.JSONObject();
			jr.put("error", r + " " + e.getMessage());
			responses.put(jr);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Accept", "application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(responses.toString());
	}


	private void logMe(String message) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream("../logs/SurtidoYXD.log", true)))) {
            pw.println("[" + (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()))
                    + "]  " + message);
        } catch (java.io.IOException e) {
        }
    }
}
