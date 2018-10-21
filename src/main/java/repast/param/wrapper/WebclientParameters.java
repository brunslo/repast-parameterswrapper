package repast.param.wrapper;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.jr.ob.JSON;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import repast.simphony.parameter.Parameters;
import repast.simphony.parameter.Schema;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;


public final class WebclientParameters implements Parameters {

	// TODO: Temporary for testing. This should be a list of a new objects that
	// fully maps each parameters metadata
	private final static Map<String, Object> parameterMap = new HashMap<>();

	public static  WebclientParameters transmitParam(String endPoint)  {
		final Map<String, Object>  initialParameters = new HashMap<>();
		try {
			populateParameters();

			String objectString = convertObjectAsString(parameterMap);
			HttpResponse<JsonNode> response = Unirest.post(endPoint)
					.header("Accept", "application/json")
					.header("Content-Type", "application/json")
					.body(objectString)
					.asJson();

//			    System.out.println("statusCode = " + response.getStatus());

		} catch (UnirestException | IOException e ) {
			e.printStackTrace();
		}

		return new WebclientParameters(initialParameters);

	}

	public static WebclientParameters receiveParam(String endPoint)  {
		final Map<String, Object> initialParameters = new HashMap<>();

		try {
			final String responseString = Unirest.get(endPoint).asString().getBody();

			final Map<String, Object> responseMap = JSON.std.mapFrom(responseString);

			initialParameters.putAll(responseMap);

		} catch (UnirestException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new WebclientParameters(initialParameters);
	}

	public static String convertObjectAsString(Map<String,Object> obj) throws IOException {
		return JSON.std.asString(obj);
	}

	private WebclientParameters(final Map<String, Object> initialParameters) {

		parameterMap.putAll(initialParameters);
	}

	private static void populateParameters() {
		//  Read Parameters from XML file

		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new File("./among.rs/parameters.xml"));
			doc.getDocumentElement().normalize();
//            System.out.println("Root element of the doc is " + doc.getDocumentElement().getNodeName());
			NodeList listOfParameters = doc.getElementsByTagName("parameter");
			int totalParameters = listOfParameters.getLength();
//            System.out.println("Total no of parameters : " + totalParameters);
			Map<String,Object> allParams = new HashMap();
			String mainParam="";
			for (int s = 0; s < listOfParameters.getLength(); s++) {
				Node firstParamNode = listOfParameters.item(s);
				NamedNodeMap attributes = firstParamNode.getAttributes();
				HashMap<String, String> params = new HashMap<String, String>();
				for (int t = 0; t < attributes.getLength(); t++) {
					Node theAttribute = attributes.item(t);
					params.put(theAttribute.getNodeName(), theAttribute.getNodeValue());
					if(theAttribute.getNodeName() == "name") {
						mainParam=theAttribute.getNodeValue();
					}
				}
				allParams.put(mainParam,params);

				parameterMap.putAll(allParams);
			}
		} catch (
				SAXParseException err) {
			System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
			System.out.println(" " + err.getMessage());
		} catch (
				SAXException e) {
			Exception x = e.getException();
			((x == null) ? e : x).printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Schema getSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue(String paramName) {
		// TODO: Testing code, get rid of it ASAP
		return parameterMap.get(paramName);
	}

	@Override
	public Double getDouble(String paramName) {
		// TODO: Testing code, get rid of it ASAP
		return (Double) parameterMap.get(paramName);
	}

	@Override
	public Integer getInteger(String paramName) {
		// TODO: Testing code, get rid of it ASAP
		return (Integer) parameterMap.get(paramName);
	}

	@Override
	public Boolean getBoolean(String paramName) {
		// TODO: Testing code, get rid of it ASAP
		return (Boolean) parameterMap.get(paramName);
	}

	@Override
	public String getString(String paramName) {
		// TODO: Testing code, get rid of it ASAP
		return (String) parameterMap.get(paramName);
	}

	@Override
	public Long getLong(String paramName) {
		// TODO: Testing code, get rid of it ASAP
		return (Long) parameterMap.get(paramName);
	}

	@Override
	public Float getFloat(String paramName) {
		// TODO: Testing code, get rid of it ASAP
		return (Float) parameterMap.get(paramName);
	}

	@Override
	public String getValueAsString(String paramName) {
		// TODO: Testing code, get rid of it ASAP
		return String.valueOf(parameterMap.get(paramName));
	}

	@Override
	public void setValue(String paramName, Object val) {
		// TODO: Testing code, get rid of it ASAP
		throw new IllegalStateException("setValue not implemented yet; stop calling me!");
	}

	@Override
	public boolean isReadOnly(String paramName) {
		// TODO: Testing code, get rid of it ASAP
		throw new IllegalStateException("isReadOnly not implemented yet; stop calling me!");
	}

	@Override
	public String getDisplayName(String paramName) {
		// TODO: Testing code, get rid of it ASAP
		return paramName;
	}

	@Override
	public Parameters clone() {
		try {
			return (Parameters) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
	
	public void graphStuff() {
		// I'M A STUB!
	}

}