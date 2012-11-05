package com.esponce.webservice;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.naming.AuthenticationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.WebServiceException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.esponce.webservice.model.Campaign;
import com.esponce.webservice.model.Item;
import com.esponce.webservice.model.ItemControl;
import com.esponce.webservice.model.QRCode;
import com.esponce.webservice.model.Record;
import com.esponce.webservice.model.TrackListResponse;

public class QRCodeClient
{
	private String baseUrl = "http://www.esponce.com/api/v3/";
	private String auth = null;
	public Boolean verbose = false;
	
	public QRCodeClient()
	{
	}
	
	public QRCodeClient(String apiKey)
	{
		this.auth = apiKey;
	}
	
	public void setApiKey(String apiKey)
	{
		this.auth = apiKey;
	}
	
	public String getApiKey()
	{
		return this.auth;
	}
	
	/**************************************************************************************//**
	 * Makes an HTTP GET request and returns JSON object.
	 ******************************************************************************************/
	protected JSONObject requestJSONObject(String url) throws Exception
	{
		if (verbose)
		{
			System.out.println("GET " + url);
		}
		
		//Make an HTTP GET request
		StringBuilder result = new StringBuilder();
		BufferedInputStream stream = new BufferedInputStream(new URL(url).openStream());
		
		//Read content as string
		int length = 0;
		byte[] buffer = new byte[1024];
		while ((length = stream.read(buffer, 0, 1024)) > 0)
		{
			result.append(new String(buffer, 0, length));
		}
		
		//Convert string to JSON object
		JSONObject json = new JSONObject(result.toString());
		return json;
	}
	
	/**************************************************************************************//**
	 * Checks if authentication key is set.
	 ******************************************************************************************/
	protected void authenticationRequired() throws Exception
	{
		if (auth == null || auth.length() == 0)
		{
			throw new AuthenticationException("Authentication key is required!");
		}
	}
	
	/**************************************************************************************//**
	 * Makes an HTTP request.
	 ******************************************************************************************/
	protected String request(String url, String method, String data) throws Exception
	{
		if (verbose)
		{
			System.out.println(method + " " + url);
		}
		
		if (method.equals("GET"))
		{
			//Make an HTTP GET request
			StringBuilder result = new StringBuilder();
			BufferedInputStream stream = new BufferedInputStream(new URL(url).openStream());
			
			//Read content as string
			int length = 0;
			byte[] buffer = new byte[1024];
			while ((length = stream.read(buffer, 0, 1024)) > 0)
			{
				result.append(new String(buffer, 0, length));
			}
			
			//Return result
			return result.toString();
		}
		
		if (method.equals("POST") || method.equals("PUT"))
		{
			URL uri = new URL(url);
			HttpURLConnection http = (HttpURLConnection) uri.openConnection();
			http.setDoOutput(true);
			http.setRequestMethod(method);
			
			OutputStreamWriter os = new OutputStreamWriter(http.getOutputStream());
			os.append(data);
			os.flush();
			
			//Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(http.getInputStream()));
			
			String line;
			StringBuilder builder = new StringBuilder();
			while ((line = rd.readLine()) != null)
			{
				builder.append(line);
			}
			os.close();
			rd.close();
			
			String error = http.getHeaderField("X-Api-Error");
			if (error != null)
			{
				throw new WebServiceException(error);
			}
			
			return builder.toString();
		}
		
		if (method.equals("DELETE"))
		{
			URL uri = new URL(url);
			HttpURLConnection http = (HttpURLConnection) uri.openConnection();
			http.setDoOutput(true);
			http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			http.setRequestMethod("DELETE");
			http.getInputStream();
			
			String error = http.getHeaderField("X-Api-Error");
			if (error != null)
			{
				throw new WebServiceException(error);
			}
			
			return null;
		}
		
		throw new UnsupportedOperationException("HTTP method not supported: " + method);
	}
	
	/**************************************************************************************//**
	 * Converts XML document to string.
	 ******************************************************************************************/
	protected String getStringFromDocument(Document doc) throws Exception
	{
		DOMSource domSource = new DOMSource(doc);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.transform(domSource, result);
		return writer.toString();
	}
	
	/**************************************************************************************//**
	 * Generates a QR Code image from the content.
	 * @param content     Content to be encoded in QR Code, e.g. plain text, URL, VCard, etc.
	 ******************************************************************************************/
	public BufferedInputStream generate(String content) throws Exception
	{
		return generate(content, null, null, null, null, null, null, null, null, null);
	}
	
	/**************************************************************************************//**
	 * Generates a QR Code image from the content.
	 * @param content     Content to be encoded in QR Code, e.g. plain text, URL, VCard, etc.
	 * @param format      Output image format
	 * @param version     Version number, set null to auto detect or set value 1-40
	 * @param size        Module size in pixels
	 * @param padding     Padding from the edge, value in number of modules
	 * @param em          Encode mode
	 * @param ec          Error correction level
	 * @param foreground  Foreground color name or #AARRGGBB hex code, e.g. black or #FF000000
	 * @param background  Background color name or #AARRGGBB hex code, e.g. transparent or #00FFFFFF
	 * @param shorten     Name of URL shortener service or null for none
	 ******************************************************************************************/
	public BufferedInputStream generate(String content, String format, Integer version, Integer size, Integer padding, String em, String ec, String foreground, String background, String shorten) throws Exception
	{
		if (content == null || content.length() == 0)
		{
			throw new IllegalArgumentException("Content is missing!");
		}
		
		//Build a URI with parameters
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "generate");
		
		urlBuilder.append("?content=").append(URLEncoder.encode(content, "UTF-8"));
		
		if (format != null)
		{
			urlBuilder.append("&format=").append(URLEncoder.encode(format, "UTF-8"));
		}
		if (version != null)
		{
			urlBuilder.append("&version=").append(version);
		}
		if (size != null)
		{
			urlBuilder.append("&size=").append(size);
		}
		if (padding != null)
		{
			urlBuilder.append("&padding=").append(padding);
		}
		if (em != null)
		{
			em = URLEncoder.encode(em, "UTF-8");
			urlBuilder.append("&em=").append(em);
		}
		if (ec != null)
		{
			ec = URLEncoder.encode(ec, "UTF-8");
			urlBuilder.append("&ec=").append(ec);
		}
		if (foreground != null)
		{
			foreground = URLEncoder.encode(foreground, "UTF-8");
			urlBuilder.append("&foreground=").append(foreground);
		}
		if (background != null)
		{
			background = URLEncoder.encode(background, "UTF-8");
			urlBuilder.append("&background=").append(background);
		}
		if (shorten != null)
		{
			shorten = URLEncoder.encode(shorten, "UTF-8");
			urlBuilder.append("&shorten=").append(shorten);
		}
		
		//Make an HTTP GET request
		BufferedInputStream ins = new BufferedInputStream(new URL(urlBuilder.toString()).openStream());
		
		return ins;
	}
	
	/**************************************************************************************//**
	 * Decodes QR Code image and returns the content.
	 ******************************************************************************************/
	public String decode(BufferedInputStream imageBuffer) throws Exception
	{
		String format = "png";
		StringBuilder result = new StringBuilder();
		
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "decode");
		if (format != null)
		{
			format = URLEncoder.encode(format, "UTF-8");
			urlBuilder.append("?format=").append(format);
		}
		
		String url = urlBuilder.toString();
		URL uri = new URL(url);
		URLConnection conn = uri.openConnection();
		conn.setDoOutput(true);
		OutputStream os = conn.getOutputStream();
		
		int length = 0;
		byte[] contents = new byte[1024];
		while ((length = imageBuffer.read(contents, 0, 1024)) > 0)
		{
			os.write(contents, 0, length);
		}
		os.flush();
		
		//Get the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		String line;
		while ((line = rd.readLine()) != null)
		{
			result.append(line);
		}
		os.close();
		rd.close();
		
		JSONObject json = new JSONObject(result.toString());
		result = new StringBuilder();
		result.append(json.getString("content"));
		
		return result.toString();
	}
	
	/**************************************************************************************//**
	 * Gets a list of campaigns and trackable QR Codes.
	 ******************************************************************************************/
	public TrackListResponse getList() throws Exception
	{
		authenticationRequired();
		
		//Build URI
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "track/list");
		urlBuilder.append("?auth=").append(auth);
		String url = urlBuilder.toString();
		
		//Make an HTTP request
		JSONObject jsonObject = requestJSONObject(url);
		TrackListResponse response = new TrackListResponse();
		
		//Parse campaigns
		JSONArray jsonArrayCampaigns = jsonObject.getJSONArray("campaigns");
		for (int i = 0; i < jsonArrayCampaigns.length(); i++)
		{
			JSONObject jsonCampaign = jsonArrayCampaigns.getJSONObject(i);
			
			Campaign campaign = new Campaign();
			campaign.parse(jsonCampaign);
			response.campaigns.add(campaign);
		}
		
		//Parse QR Codes
		JSONArray jsonArrayQRCodes = jsonObject.getJSONArray("qrcodes");
		for (int i = 0; i < jsonArrayQRCodes.length(); i++)
		{
			JSONObject jsonQRCode = jsonArrayQRCodes.getJSONObject(i);
			
			QRCode qrcode = new QRCode();
			qrcode.parse(jsonQRCode);
			response.qrcodes.add(qrcode);
		}
		
		return response;
	}
	
	/**************************************************************************************//**
	 * Gets a campaign by id.
	 * @return Returns a campign model.
	 ******************************************************************************************/
	public Campaign getCampaign(String id) throws Exception
	{
		authenticationRequired();
		
		//Check required parameter
		if (id == null || id.length() == 0)
		{
			throw new IllegalArgumentException("Campaign id is missing!");
		}
		
		//Build URI
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "track/campaign/");
		urlBuilder.append(URLEncoder.encode(id, "UTF-8"));
		urlBuilder.append("?auth=").append(auth);
		
		//Make a HTTP request
		JSONObject jsonObject = requestJSONObject(urlBuilder.toString());
		Campaign campaign = new Campaign();
		campaign.parse(jsonObject);
		
		return campaign;
	}
	
	/**************************************************************************************//**
	 * Creates a new campaign.
	 * @param name         Campaign name, required value.
	 * @param description  Campaign description, optional.
	 * @return Returns the created campign model with id.
	 ******************************************************************************************/
	public Campaign insertCampaign(String name, String description) throws Exception
	{
		Campaign campaign = new Campaign();
		campaign.name = name;
		campaign.description = description;
		return insertCampaign(campaign);
	}
	
	/**************************************************************************************//**
	 * Creates a new campaign.
	 * @param campaign  Campaign model, name field is required.
	 * @return Returns the created campign model with id.
	 ******************************************************************************************/
	public Campaign insertCampaign(Campaign campaign) throws Exception
	{
		authenticationRequired();
		
		//Check required parameters
		if (campaign.name == null || campaign.name.length() == 0)
		{
			throw new IllegalArgumentException("Campaign name is missing!");
		}
		
		//Build URI
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "track/campaign");
		urlBuilder.append("?auth=").append(auth);
		
		//Make an HTTP request
		String url = urlBuilder.toString();
		JSONObject jsonObject = campaign.toJSON();
		String jsonString = request(url, "POST", jsonObject.toString());
		
		//Parse result
		JSONObject json = new JSONObject(jsonString.toString());
		Campaign result = new Campaign();
		result.parse(json);
		
		return result;
	}
	
	/**************************************************************************************//**
	 * Updates an existing campaign.
	 ******************************************************************************************/
	public void updateCampaign(String id, String name, String description) throws Exception
	{
		Campaign campaign = new Campaign();
		campaign.id = id;
		campaign.name = name;
		campaign.description = description;
		updateCampaign(campaign);
	}
	
	/**************************************************************************************//**
	 * Updates an existing campaign.
	 ******************************************************************************************/
	public void updateCampaign(Campaign campaign) throws Exception
	{
		authenticationRequired();
		
		//Check required parameters
		if (campaign.id == null || campaign.id.length() == 0)
		{
			throw new IllegalArgumentException("Campaign id is missing!");
		}
		
		//Build URI
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "track/campaign/");
		urlBuilder.append(URLEncoder.encode(campaign.id, "UTF-8"));
		urlBuilder.append("?auth=").append(auth);
		
		//Make an HTTP request
		String url = urlBuilder.toString();
		String data = campaign.toJSON().toString();
		request(url, "PUT", data);
	}
	
	/**************************************************************************************//**
	 * Deletes a campaign including its associated QR Codes.
	 ******************************************************************************************/
	public void deleteCampaign(String id) throws Exception
	{
		deleteCampaign(id, false);
	}
	
	/**************************************************************************************//**
	 * Deletes a campaign by id.
	 * @param id    Campaign id
	 * @param keep  A value indicating whether to keep associated QR Codes (moves out of campaign before delete)
	 ******************************************************************************************/
	public void deleteCampaign(String id, Boolean keep) throws Exception
	{
		authenticationRequired();
		
		//Check required parameters
		if (id == null || id.length() == 0)
		{
			throw new IllegalArgumentException("Campaign id is missing!");
		}
		
		//Build URI
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "track/campaign/");
		urlBuilder.append(URLEncoder.encode(id, "UTF-8"));
		urlBuilder.append("?auth=").append(auth);
		if (keep != null)
		{
			urlBuilder.append("&keep=").append(keep);
		}
		
		//Make an HTTP request
		String url = urlBuilder.toString();
		request(url, "DELETE", null);
	}
	
	/**************************************************************************************//**
	 * Gets a trackable QR code by id.
	 ******************************************************************************************/
	public QRCode getQRCode(String id) throws Exception
	{
		authenticationRequired();
		
		//Build URI
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "track/qrcode/");
		urlBuilder.append(URLEncoder.encode(id, "UTF-8"));
		urlBuilder.append("?auth=").append(auth);
		
		//Make a HTTP request
		String url = urlBuilder.toString();
		JSONObject jsonObject = requestJSONObject(url);
		
		//Parse JSON object
		QRCode qrcode = new QRCode();
		qrcode.parse(jsonObject);
		return qrcode;
	}
	
	/**************************************************************************************//**
	 * Creates a new trackable QR Code.
	 ******************************************************************************************/
	public QRCode insertQRCode(QRCode qrcode) throws Exception
	{
		authenticationRequired();
		
		//Check required parameters
		if (qrcode.content == null || qrcode.content.length() == 0)
		{
			throw new IllegalArgumentException("QR Code content is missing!");
		}
		
		if (qrcode.name == null || qrcode.name.length() == 0)
		{
			//Try to create a friendly QR Code name when the name is not specified
			qrcode.name = qrcode.content.replace("\r", "").replace('\n', ' ');
		}
		
		//Build URI
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "track/qrcode");
		urlBuilder.append("?auth=").append(auth);
		
		//Make an HTTP request
		String url = urlBuilder.toString();
		String data = qrcode.toJSON().toString();
		String jsonString = request(url, "POST", data);
		
		//Parse response
		QRCode result = new QRCode();
		JSONObject json = new JSONObject(jsonString.toString());
		result.created = json.getString("created");
		result.id = json.getString("id");
		
		return result;
	}
	
	/**************************************************************************************//**
	 * Updates an existing QR Code.
	 * @param qrcode  QR Code model, id field is required.
	 ******************************************************************************************/
	public void updateQRCode(QRCode qrcode) throws Exception
	{
		authenticationRequired();
		
		//Check required parameters
		if (qrcode.id == null || qrcode.id.length() == 0)
		{
			throw new IllegalArgumentException("QR Code id is missing!");
		}
		
		//Build URI
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "track/qrcode/");
		urlBuilder.append(URLEncoder.encode(qrcode.id, "UTF-8"));
		urlBuilder.append("?auth=").append(auth);
		
		//Make an HTTP request
		String url = urlBuilder.toString();
		String data = qrcode.toJSON().toString();
		request(url, "PUT", data);
	}
	
	/**************************************************************************************//**
	 * Deletes a QR code by id.
	 * @param id  QR Code id.
	 ******************************************************************************************/
	public void deleteQRCode(String id) throws Exception
	{
		authenticationRequired();
		
		//Check required parameters
		if (id == null || id.length() == 0)
		{
			throw new IllegalArgumentException("QR code id is missing!");
		}
		
		//Build URI
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "track/qrcode/");
		urlBuilder.append(URLEncoder.encode(id, "UTF-8"));
		urlBuilder.append("?auth=").append(auth);
		
		//Make an HTTP request
		String url = urlBuilder.toString();
		request(url, "DELETE", null);
	}
	
	/**************************************************************************************//**
	 * Gets statistics for the specified QR Code.
	 ******************************************************************************************/
	public ArrayList<Record> getStatistics(String id) throws Exception
	{
		authenticationRequired();
		
		//Check required parameter
		if (id == null || id.length() == 0)
		{
			throw new IllegalArgumentException("QR code id is missing!");
		}
		
		//Build a URI
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "track/statistics/");
		urlBuilder.append(URLEncoder.encode(id, "UTF-8"));
		urlBuilder.append(".json"); //Lightweight and easy to parse
		urlBuilder.append("?auth=").append(auth);
		
		String url = urlBuilder.toString();
		if (verbose)
		{
			System.out.println(url);
		}
		
		BufferedInputStream ins = new BufferedInputStream(new URL(url).openStream());
		
		ArrayList<Record> arrayList = new ArrayList<Record>();
		StringBuilder result = new StringBuilder();
		
		int length = 0;
		byte[] contents = new byte[1024];
		while ((length = ins.read(contents, 0, 1024)) > 0)
		{
			result.append(new String(contents, 0, length));
		}
		
		String jsonString = result.toString();
		JSONArray array = new JSONArray(jsonString);
		
		for (int i = 0; i < array.length(); i++)
		{
			JSONObject json = array.getJSONObject(i);
			
			Record record = new Record();
			record.campaign = json.optString("campaign");
			record.systemName = json.optString("systemName");
			record.systemVersion = json.optString("systemVersion");
			record.browserName = json.optString("browserName");
			record.browserVersion = json.optString("browserVersion");
			record.manufacturer = json.optString("manufacturer");
			record.deviceModel = json.optString("deviceModel");
			record.isMobileDevice = json.optBoolean("isMobileDevice");
			record.isTablet = json.optBoolean("isTablet");
			record.countryCode = json.optString("countryCode");
			record.region = json.optString("region");
			record.city = json.optString("city");
			record.latitude = json.optDouble("latitude");
			record.longitude = json.optDouble("longitude");
			record.created = json.optString("created");
			arrayList.add(record);
		}
		
		return arrayList;
	}
	
	/**************************************************************************************//**
	 * Gets statistics as data stream.
	 * @param id      QR Code id
	 * @param format  Output content format, must be "csv", "xls" or "xml"
	 ******************************************************************************************/
	public BufferedInputStream getStatistics(String id, String format) throws Exception
	{
		authenticationRequired();
		
		//Build a URI
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "track/statistics/");
		urlBuilder.append(URLEncoder.encode(id, "UTF-8"));
		urlBuilder.append(".").append(URLEncoder.encode(format, "UTF-8"));
		urlBuilder.append("?auth=").append(auth);

		//Make an HTTP request
		String url = urlBuilder.toString();
		BufferedInputStream ins = new BufferedInputStream(new URL(url).openStream());
		return ins;
	}
	
	/**************************************************************************************//**
	 * Gets exported campaigns and trackable QR Codes.
	 ******************************************************************************************/
	public ArrayList<Item> exportData() throws Exception
	{
		authenticationRequired();
		
		ArrayList<Item> arrayList = new ArrayList<Item>();
		BufferedInputStream ins = null;
		StringBuilder result = new StringBuilder();
		
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "track/export");
		urlBuilder.append(".xml");
		urlBuilder.append("?auth=").append(auth);
		
		ins = new BufferedInputStream(new URL(urlBuilder.toString()).openStream());
		
		int length = 0;
		byte[] contents = new byte[1024];
		
		while ((length = ins.read(contents, 0, 1024)) > 0)
		{
			result.append(new String(contents, 0, length));
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream isResult = new ByteArrayInputStream(result.toString().getBytes());
		Document doc = builder.parse(isResult);
		
		doc.getDocumentElement().normalize();
		
		NodeList records = doc.getElementsByTagName("item");
		for (int i = 0; i < records.getLength(); i++)
		{
			Node firstRecordNode = records.item(i);
			if (firstRecordNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Item item = new Item();
				
				Element firstRecordElement = (Element) firstRecordNode;
				
				// NAME
				NodeList nameList = firstRecordElement.getElementsByTagName("name");
				Element nameElement = (Element) nameList.item(0);
				
				if (nameElement != null)
				{
					NodeList textnameList = nameElement.getChildNodes();
					item.name = ((Node) textnameList.item(0)).getNodeValue().trim();
				}
				
				// CONTENT
				NodeList contentList = firstRecordElement.getElementsByTagName("content");
				Element contentElement = (Element) contentList.item(0);
				
				if (contentElement != null)
				{
					NodeList textcontentList = contentElement.getChildNodes();
					item.content = ((Node) textcontentList.item(0)).getNodeValue().trim();
				}
				
				// TRACK-URL
				NodeList trackUrlList = firstRecordElement.getElementsByTagName("trackUrl");
				Element trackUrlElement = (Element) trackUrlList.item(0);
				
				if (trackUrlElement != null)
				{
					NodeList texttrackUrlList = trackUrlElement.getChildNodes();
					item.trackUrl = ((Node) texttrackUrlList.item(0)).getNodeValue().trim();
				}
				
				// CREATED
				NodeList createdList = firstRecordElement.getElementsByTagName("created");
				Element createdElement = (Element) createdList.item(0);
				
				if (createdElement != null)
				{
					NodeList textcreatedList = createdElement.getChildNodes();
					item.created = ((Node) textcreatedList.item(0)).getNodeValue().trim();
				}
				
				// SCANS
				NodeList scansList = firstRecordElement.getElementsByTagName("scans");
				Element scansElement = (Element) scansList.item(0);
				
				if (scansElement != null)
				{
					NodeList textscansList = scansElement.getChildNodes();
					item.scans = Integer.parseInt(((Node) textscansList.item(0)).getNodeValue().trim());
				}
				
				// COMMENTS
				NodeList commentsList = firstRecordElement.getElementsByTagName("comments");
				Element commentsElement = (Element) commentsList.item(0);
				
				if (commentsElement != null)
				{
					NodeList textcommentsList = commentsElement.getChildNodes();
					item.comments = ((Node) textcommentsList.item(0)).getNodeValue().trim();
				}
				
				// SIZE
				NodeList sizeList = firstRecordElement.getElementsByTagName("size");
				Element sizeElement = (Element) sizeList.item(0);
				
				if (sizeElement != null)
				{
					NodeList textsizeList = sizeElement.getChildNodes();
					item.size = Integer.parseInt(((Node) textsizeList.item(0)).getNodeValue().trim());
				}
				
				// PADDING
				NodeList paddingList = firstRecordElement.getElementsByTagName("padding");
				Element paddingElement = (Element) paddingList.item(0);
				
				if (paddingElement != null)
				{
					NodeList textpaddingList = paddingElement.getChildNodes();
					item.padding = Integer.parseInt(((Node) textpaddingList.item(0)).getNodeValue().trim());
				}
				
				// VERSION
				NodeList versionList = firstRecordElement.getElementsByTagName("version");
				Element versionElement = (Element) versionList.item(0);
				
				if (versionElement != null)
				{
					NodeList textversionList = versionElement.getChildNodes();
					item.version = Double.parseDouble(((Node) textversionList.item(0)).getNodeValue().trim());
				}
				
				// ENCODEMODE
				NodeList encodeModeList = firstRecordElement.getElementsByTagName("encodeMode");
				Element encodeModeElement = (Element) encodeModeList.item(0);
				
				if (encodeModeElement != null)
				{
					NodeList textencodeModeList = encodeModeElement.getChildNodes();
					item.encodeMode = ((Node) textencodeModeList.item(0)).getNodeValue().trim();
				}
				
				// ERRORCORRECTION
				NodeList errorCorrectionList = firstRecordElement.getElementsByTagName("errorCorrection");
				Element errorCorrectionElement = (Element) errorCorrectionList.item(0);
				
				if (errorCorrectionElement != null)
				{
					NodeList texterrorCorrectionList = errorCorrectionElement.getChildNodes();
					item.errorCorrection = ((Node) texterrorCorrectionList.item(0)).getNodeValue().trim();
				}
				
				// CAMPAIGN
				NodeList campaignList = firstRecordElement.getElementsByTagName("campaign");
				Element campaignElement = (Element) campaignList.item(0);
				
				if (campaignElement != null)
				{
					NodeList textcampaignList = campaignElement.getChildNodes();
					item.campaign = ((Node) textcampaignList.item(0)).getNodeValue().trim();
				}
				
				arrayList.add(item);
			}
		}
		
		return arrayList;
	}
	
	/**************************************************************************************//**
	 * Gets exported campaigns and trackable QR Codes in raw format.
	 * @param format  Output format, must be "csv", "xls", "xml", "json" or "zip".
	 * @param imageFormat  Output image format in "zip" file, can be "png", "eps" or "svg"
	 ******************************************************************************************/
	public BufferedInputStream exportData(String format, String imageFormat) throws Exception
	{
		authenticationRequired();
		
		if (imageFormat != null)
		{
			if (imageFormat.equals("png") || imageFormat.equals("eps") || imageFormat.equals("svg"))
			{
				imageFormat = (format.equals("zip")) ? (imageFormat) : null;
			}
			else
			{
				imageFormat = null;
			}
		}
		
		//Build a URI
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "track/export");
		urlBuilder.append(".").append(format);
		urlBuilder.append("?auth=").append(auth);
		if (imageFormat != null)
		{
			urlBuilder.append("&format=").append(imageFormat);
		}
		
		//Make an HTTP request
		BufferedInputStream ins = new BufferedInputStream(new URL(urlBuilder.toString()).openStream());
		
		return ins;
	}
	
	/**************************************************************************************//**
	 * Imports campaigns and trackable QR Codes.
	 ******************************************************************************************/
	public ArrayList<ItemControl> importData(ArrayList<Item> items) throws Exception
	{
		authenticationRequired();
		
		ArrayList<ItemControl> arrayList = new ArrayList<ItemControl>();
	
		int no = items.size();
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = docFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		Element rootElement = document.createElement("tracking");
		document.appendChild(rootElement);
		
		for (int i = 0; i < no; i++)
		{
			Element item = document.createElement("item");
			rootElement.appendChild(item);
			
			if (items.get(i).name != null)
			{
				Element name = document.createElement("name");
				name.appendChild(document.createTextNode(items.get(i).name));
				item.appendChild(name);
			}
			
			if (items.get(i).content != null)
			{
				Element content = document.createElement("content");
				content.appendChild(document.createTextNode(items.get(i).content));
				item.appendChild(content);
			}
			
			if (items.get(i).trackUrl != null)
			{
				Element trackUrl = document.createElement("trackUrl");
				trackUrl.appendChild(document.createTextNode(items.get(i).trackUrl));
				item.appendChild(trackUrl);
			}
			
			if (items.get(i).created != null)
			{
				Element created = document.createElement("created");
				created.appendChild(document.createTextNode(items.get(i).created));
				item.appendChild(created);
			}
			
			if (items.get(i).scans != null)
			{
				Element scans = document.createElement("scans");
				scans.appendChild(document.createTextNode("" + items.get(i).scans));
				item.appendChild(scans);
			}
			
			if (items.get(i).comments != null)
			{
				Element comments = document.createElement("comments");
				comments.appendChild(document.createTextNode(items.get(i).comments));
				item.appendChild(comments);
			}
			
			if (items.get(i).size != null)
			{
				Element size = document.createElement("size");
				size.appendChild(document.createTextNode("" + items.get(i).size));
				item.appendChild(size);
			}
			
			if (items.get(i).padding != null)
			{
				Element padding = document.createElement("padding");
				padding.appendChild(document.createTextNode("" + items.get(i).padding));
				item.appendChild(padding);
			}
			
			if (items.get(i).version != null)
			{
				Element version = document.createElement("version");
				version.appendChild(document.createTextNode("" + items.get(i).version));
				item.appendChild(version);
			}
			
			if (items.get(i).encodeMode != null)
			{
				Element encodeMode = document.createElement("encodeMode");
				encodeMode.appendChild(document.createTextNode(items.get(i).encodeMode));
				item.appendChild(encodeMode);
			}
			
			if (items.get(i).errorCorrection != null)
			{
				Element errorCorrection = document.createElement("errorCorrection");
				errorCorrection.appendChild(document.createTextNode(items.get(i).errorCorrection));
				item.appendChild(errorCorrection);
			}
			
			if (items.get(i).campaign != null)
			{
				Element campaign = document.createElement("campaign");
				campaign.appendChild(document.createTextNode(items.get(i).campaign));
				item.appendChild(campaign);
			}
		}
		
		String xml = getStringFromDocument(document);
		xml = xml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "");
		//System.out.println(xml);
		
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "track/import");
		urlBuilder.append("?auth=").append(auth);
		urlBuilder.append("&format=").append("xml");
		
		URL url = new URL(urlBuilder.toString());
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		
		OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
		os.append(xml);
		os.flush();
		
		//Get the response
		StringBuilder result = new StringBuilder();
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		String line;
		while ((line = rd.readLine()) != null)
		{
			result.append(line);
		}
		os.close();
		rd.close();
		
		if (result.length() != 0)
		{
			JSONArray jsonArray = new JSONArray(result.toString());
			
			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				
				ItemControl ignoredItem = new ItemControl();
				ignoredItem.campaign = jsonObject.optString("campaign");
				ignoredItem.content = jsonObject.optString("content");
				ignoredItem.error = jsonObject.optString("error");
				ignoredItem.name = jsonObject.optString("name");
				
				arrayList.add(ignoredItem);
			}
		}
		
		return arrayList;
	}
	
	/**************************************************************************************//**
	 * Imports campaigns and trackable QR Codes.
	 ******************************************************************************************/
	public ArrayList<ItemControl> importData(BufferedInputStream items, String format) throws Exception
	{
		authenticationRequired();
		
		ArrayList<ItemControl> arrayList = new ArrayList<ItemControl>();
		
		StringBuilder urlBuilder = new StringBuilder(baseUrl + "track/import");
		urlBuilder.append("?auth=").append(auth);
		urlBuilder.append("&format=").append(URLEncoder.encode(format, "UTF-8"));
		
		URL url = new URL(urlBuilder.toString());
		URLConnection conn = url.openConnection();
		//conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
		conn.setDoOutput(true);
		
		OutputStream os = conn.getOutputStream();
		
		int length = 0;
		byte[] contents = new byte[1024];
		while ((length = items.read(contents, 0, 1024)) > 0)
		{
			os.write(contents, 0, length);
		}
		
		os.flush();
		
		//Get the response
		StringBuilder result = new StringBuilder();
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		String line;
		while ((line = rd.readLine()) != null)
		{
			result.append(line);
		}
		os.close();
		rd.close();
		
		if (result.length() != 0)
		{
			JSONArray jsonArray = new JSONArray(result.toString());
			
			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				
				ItemControl ignoredItem = new ItemControl();
				ignoredItem.campaign = jsonObject.optString("campaign");
				ignoredItem.content = jsonObject.optString("content");
				ignoredItem.error = jsonObject.optString("error");
				ignoredItem.name = jsonObject.optString("name");
				
				arrayList.add(ignoredItem);
			}
		}
		
		return arrayList;
	}
}
