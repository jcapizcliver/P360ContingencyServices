package mx.com.liverpool.p360.core.restservices;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

@WebServlet("/public/rt/ws_list_valid_values_for_parent_template_characteristic_enabler")
public class GetListOfValuesEnabler extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_CREATION_TYPE = "CreateProposal";
	private static final int LANGUAGE_ID_ES = 10;
	private static final String EXTERNAL_SYSTEM_ATG = "ATG";

	@Override
	protected void doGet(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		String template = trimToNull(request.getParameter("template"));
		String characteristic = trimToNull(request.getParameter("characteristic"));
		boolean includeAlternative =
				Boolean.parseBoolean(request.getParameter("includeAlternative"));

		String creationType = trimToNull(request.getParameter("creationType"));
		if (creationType == null) {
			creationType = DEFAULT_CREATION_TYPE;
		}

		org.json.JSONArray responseValues = new org.json.JSONArray();

		if (characteristic == null) {
			writeResponse(response, responseValues);
			return;
		}

		final java.util.function.Consumer<String> dbMessageLogger = this::logMe;
		final java.util.function.Consumer<Exception> dbErrorLogger = this::logE;

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

		try (DBAccessDataStub dastub = new DBAccessDataStub(dbLog)) {

			String validValues = null;
			String dependentAttribute = null;

			if (template != null) {
				validValues =
						getTemplateProperty(
								dastub,
								template,
								characteristic,
								creationType,
								"dependentValues");

				if (validValues != null) {
					dependentAttribute =
							getTemplateProperty(
									dastub,
									template,
									characteristic,
									creationType,
									"dependentAttribute");
				}
			}

			if (validValues == null) {
				org.json.JSONObject globalMetadata =
						dastub.getGlobalMetadata(creationType);

				org.json.JSONObject characteristicMetadata =
						globalMetadata == null
								? null
								: globalMetadata.optJSONObject(characteristic);

				validValues =
						getNonBlankProperty(
								characteristicMetadata,
								"dependentValues");

				dependentAttribute =
						getNonBlankProperty(
								characteristicMetadata,
								"dependentAttribute");
			}

			if (validValues == null || dependentAttribute == null) {
				writeResponse(response, responseValues);
				return;
			}

			org.json.JSONObject dependentCharacteristicData =
					dastub.getCharacteristicData(dependentAttribute);

			String lookup =
					dependentCharacteristicData == null
							? null
							: trimToNull(
									dependentCharacteristicData.optString(
											"lookup",
											null));

			if (lookup == null) {
				writeResponse(response, responseValues);
				return;
			}

			Set<String> allowedCodes = parseValues(validValues);

			if (allowedCodes.isEmpty()) {
				writeResponse(response, responseValues);
				return;
			}

			List<org.json.JSONObject> lookupRows =
					dastub.getLookupValueCodeNameExternalCodeRows(
							lookup,
							allowedCodes,
							LANGUAGE_ID_ES,
							EXTERNAL_SYSTEM_ATG,
							true);

			for (org.json.JSONObject lookupRow : lookupRows) {

				String code =
						trimToNull(
								lookupRow.optString(
										"code",
										null));

				if (code == null || !allowedCodes.contains(code)) {
					continue;
				}

				String name =
						lookupRow.optString(
								"name",
								"");

				if (includeAlternative) {
					responseValues.put(
							new org.json.JSONObject()
									.put("label", name)
									.put(
											"hex",
											lookupRow.optString(
													"externalCode",
													"")));
				} else {
					responseValues.put(name);
				}
			}

		} catch (RuntimeException e) {
			logE(e);
		}

		writeResponse(response, responseValues);
	}

	private String getTemplateProperty(
			DBAccessDataStub dastub,
			String template,
			String characteristic,
			String creationType,
			String property) {

		org.json.JSONArray rows =
				dastub.getTemplateCharacteristicPropertyValue(
						template,
						characteristic,
						creationType,
						property);

		if (rows == null || rows.length() == 0) {
			return null;
		}

		org.json.JSONObject item =
				rows.optJSONObject(0);

		if (item == null) {
			return null;
		}

		org.json.JSONObject characteristicData =
				item.optJSONObject(characteristic);

		return getNonBlankProperty(
				characteristicData,
				property);
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

	private Set<String> parseValues(String values) {

		Set<String> result = new LinkedHashSet<>();

		if (values == null || values.isBlank()) {
			return result;
		}

		for (String piece : values.split(",")) {

			String value = trimToNull(piece);

			if (value != null) {
				result.add(value);
			}
		}

		return result;
	}

	private String trimToNull(String value) {

		if (value == null) {
			return null;
		}

		String trimmed = value.trim();

		return trimmed.isEmpty()
				? null
				: trimmed;
	}

	private void writeResponse(
			HttpServletResponse response,
			org.json.JSONArray values)
			throws IOException {

		org.json.JSONObject rawResponse =
				new org.json.JSONObject()
						.put(
								"values",
								values);

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

	private void logMe(String message) {
		LOGGER.info(message);
	}

	private void logE(Exception ex) {
		LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
	}

	private static final Logger LOGGER =
			Logger.getLogger(
					GetListOfValuesEnabler.class.getName());

	static {
		try {
			LOGGER.setUseParentHandlers(false);

			FileHandler fileHandler =
					new FileHandler(
							"../logs/getEnablerValues.%g.log",
							10 * 1024 * 1024,
							10,
							true);

			fileHandler.setEncoding(
					StandardCharsets.UTF_8.name());

			fileHandler.setLevel(
					Level.ALL);

			fileHandler.setFormatter(
					new Formatter() {
						@Override
						public String format(LogRecord record) {

							java.time.LocalDateTime dateTime =
									java.time.Instant
											.ofEpochMilli(
													record.getMillis())
											.atZone(
													java.time.ZoneId.systemDefault())
											.toLocalDateTime();

							String timestamp =
									dateTime.format(
											java.time.format.DateTimeFormatter
													.ofPattern(
															"yyyy-MM-dd HH:mm:ss"));

							return "["
									+ timestamp
									+ "] ["
									+ record.getLevel()
									+ "] "
									+ formatMessage(record)
									+ System.lineSeparator();
						}
					});

			LOGGER.addHandler(fileHandler);
			LOGGER.setLevel(Level.ALL);

		} catch (IOException e) {
			throw new RuntimeException(
					"No se pudo inicializar el logger",
					e);
		}
	}
}
