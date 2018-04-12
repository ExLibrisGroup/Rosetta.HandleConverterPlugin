package com.exlibris.dps.repository.plugin.registry;

import java.util.Map;

import com.exlibris.core.infra.common.exceptions.logging.ExLogger;
import com.exlibris.dps.sdk.registry.ConverterRegistryPlugin;

public class HandleConverterPlugin implements ConverterRegistryPlugin {

	private static final ExLogger log = ExLogger.getExLogger(HandleConverterPlugin.class, ExLogger.PUBLISHING);

	private String iePid;
	private String objectType;
	private boolean ieLevel;
	private boolean repLevel;
	private boolean fileLevel;
	private String deliveryUrl;
	private static final String OBJECT_IDENTIFIER_TYPE = "objectIdentifierType";
	private static final String IE_LEVEL = "ieLevel";
	private static final String REP_LEVEL = "repLevel";
	private static final String FILE_LEVEL = "fileLevel";
	private static final String DELIVERY_URL = "deliveryUrl";
	private static final String PID = "PID";

	@Override
	public String convert(String ieXml) {
		try {
			SaxObjectIdentifierParser  parser = new SaxObjectIdentifierParser(iePid, objectType, ieLevel, repLevel, fileLevel, deliveryUrl);
			parser.parse(ieXml);
			return parser.getxParams();
		} catch (Exception e) {
			log.error("An error occured while parsing ie mets", e);
		}
		return null;
	}

	@Override
	public void initParam(Map<String, String> params) {
		iePid = params.get(PID);
		objectType = params.get(OBJECT_IDENTIFIER_TYPE);
		ieLevel = params.get(IE_LEVEL) != null;
		repLevel = params.get(REP_LEVEL) != null;
		fileLevel = params.get(FILE_LEVEL) != null;
		deliveryUrl = params.get(DELIVERY_URL);
	}

	@Override
	public String unPublish(String ieXml) {
		return null;
	}

}
