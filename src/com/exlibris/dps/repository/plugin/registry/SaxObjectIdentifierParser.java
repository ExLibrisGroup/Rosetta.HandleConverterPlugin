package com.exlibris.dps.repository.plugin.registry;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class SaxObjectIdentifierParser extends DefaultHandler {
	private StringBuilder xParams = new StringBuilder("<xb:x_params xmlns:xb=\"http://com/exlibris/digitool/common/forms/xmlbeans\">");
	private StringBuilder characters = new StringBuilder();

	private String objectIdentifierType;
	private boolean ieLevel;
	private boolean repLevel;
	private boolean fileLevel;
	private String deliveryUrl;

	private boolean isIe=false;
	private boolean isRep=false;
	private boolean isFile=false;
	private boolean isObjectIdentifier = false;
	private boolean writeContent = false;

	private static final String KEY_TAG = "key";
	private static final String ID_ATTR = "id";
	private static final String AMD_TAG = "mets:amdSec";
	private static final String FILE_ID_PREFIX = "FL";
	private static final String REP_ID_PREFIX = "REP";
	private static final String IE_ID_PREFIX = "ie";
	private static final String AMD_SUFFIX = "-amd";
	private static final String OBJECT_IDENTIFIER_TYPE = "objectIdentifierType";
	private static final String PARAM_START = "<x_param>";
	private static final String PARAM_CLOSE = "</x_param>";
	private static final String X_PARAMS_KEY_START = "<x_param_key>";
	private static final String X_PARAMS_KEY_CLOSE = "</x_param_key>";
	private static final String X_PARAMS_VALUE_START = "<x_param_value>";
	private static final String X_PARAMS_VALUE_CLOSE = "</x_param_value>";
	private static final String PARAMS_CLOSE = "</xb:x_params>";
	private String iePid;
	private String repPid;
	private String filePid;

	public SaxObjectIdentifierParser(String iePid, String objectIdentifierType, boolean ieLevel, boolean repLevel, boolean fileLevel, String deliveryUrl) {
		super();
		this.objectIdentifierType = objectIdentifierType;
		this.ieLevel = ieLevel;
		this.repLevel = repLevel;
		this.fileLevel = fileLevel;
		this.deliveryUrl = deliveryUrl;
		this.iePid = iePid;
	}

	/**
	 * @return the xParams
	 */
	public String getxParams() {
		return xParams.toString() + PARAMS_CLOSE;
	}

	@Override
	public void error(org.xml.sax.SAXParseException e) throws SAXException {
		throw e;
	};

	@Override
	public void fatalError(org.xml.sax.SAXParseException e) throws SAXException {
		throw e;
	};

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (attributes != null && attributes.getLength() > 0) {
			for (int i = 0; i < attributes.getLength(); i++) {
				if ( AMD_TAG.equals(qName) && ID_ATTR.equalsIgnoreCase(attributes.getQName(i))){
					// if enters a file amd sec
					if (attributes.getValue(i).startsWith(FILE_ID_PREFIX)) {
						filePid = attributes.getValue(i).replace(AMD_SUFFIX, "");
						isIe = false;
						isRep = false;
						isFile = true;
					}
					// if enters a rep amd sec
					if (attributes.getValue(i).startsWith(REP_ID_PREFIX)) {
						repPid = attributes.getValue(i).replace(AMD_SUFFIX, "");
						isIe = false;
						isRep = true;
						isFile = false;
					}
					if (attributes.getValue(i).startsWith(IE_ID_PREFIX)) {
						isIe = true;
						isRep = false;
						isFile = false;
					}
					// if enters the object identifier section
				} else if (KEY_TAG.equals(qName) &&
						ID_ATTR.equalsIgnoreCase(attributes.getQName(i)) &&
						OBJECT_IDENTIFIER_TYPE.equalsIgnoreCase(attributes.getValue(i))){
					isObjectIdentifier = true;
				}
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException{

		if (isObjectIdentifier){
			if (!writeContent) {
				// identifier type is placed in the value so we need to access it when ending the element
				String value = getCurrentText();
				// check if it's the right type and if we actually want to publish it
				if(value.equals(objectIdentifierType) && (isIe && ieLevel || isRep && repLevel || isFile && fileLevel)) {
					xParams.append(PARAM_START);
					xParams.append(X_PARAMS_KEY_START);
					writeContent = true;
					characters.setLength(0);
				}
				else {
					isObjectIdentifier = false;
					characters.setLength(0);
				}
			}
			else {
				appendCurrentText();
				xParams.append(X_PARAMS_KEY_CLOSE);
				xParams.append(X_PARAMS_VALUE_START);
				xParams.append(deliveryUrl);
				xParams.append(deliveryUrl.endsWith("/") ? "delivery/DeliveryManagerServlet?dps_pid=" : "/delivery/DeliveryManagerServlet?dps_pid=");
				xParams.append(isIe ? iePid : isRep? repPid : filePid);
				xParams.append(X_PARAMS_VALUE_CLOSE);
				xParams.append(PARAM_CLOSE);
				isObjectIdentifier = false;
				writeContent = false;
			}
		}
		else {
			characters.setLength(0);
		}

	}

	// Save the current element value
	@Override
	public void characters(char[] ch, int start, int length) {
		characters.append(ch, start, length);
	}



	private String getCurrentText() {
		return characters.toString().trim();
	}

	private void appendCurrentText() {
		xParams.append(characters.toString().trim());
		characters.setLength(0);
	}

	public void parse(String ieXml) throws SAXException, IOException, ParserConfigurationException {
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(this);
		//Attach content handler, etc...
		InputSource is = new InputSource(new StringReader(ieXml));
		xmlReader.parse(is);
	}
}


