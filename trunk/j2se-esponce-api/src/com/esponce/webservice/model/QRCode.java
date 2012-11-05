package com.esponce.webservice.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class QRCode
{
	public String id = null;
	public String campaignId = null;
	public String name = null;
	public String shortUrl = null;
	public String content = null;
	public Integer scans = null;
	public String created = null;
	public String comment = null;
	public String urlinfo = null;
	public String imageUrl = null;
	
	public Boolean isAvailable = false;
	public Boolean isScheduleEnabled = null;
	
	public QRCodeLocation location = new QRCodeLocation();
	public QRCodeProperties properties = new QRCodeProperties();
	public ArrayList<QRCodeSchedule> schedules = new ArrayList<QRCodeSchedule>();
	
	public void parse(JSONObject json) throws Exception
	{
		this.id = json.optString("id");
		this.campaignId = json.optString("campaignId");
		this.name = json.optString("name");
		this.content = json.optString("content");
		this.comment = json.optString("comment");
		this.urlinfo = json.optString("urlinfo");
		this.shortUrl = json.optString("shortUrl");
		this.imageUrl = json.optString("imageUrl");
		this.scans = json.optInt("scans");
		this.created = json.optString("created");
		this.isAvailable = json.optBoolean("isAvailable");
		this.isScheduleEnabled = json.optBoolean("isScheduleEnabled");
		
		JSONObject properties = json.optJSONObject("properties");
		if (properties != null)
		{
			this.properties.size = properties.optInt("size");
			this.properties.version = properties.optInt("version");
			this.properties.padding = properties.optInt("padding");
			this.properties.em = properties.optString("em");
			this.properties.ec = properties.optString("ec");
			this.properties.foreground = properties.optString("foreground");
			this.properties.background = properties.optString("background");
		}
		
		JSONObject location = json.optJSONObject("location");
		if (location != null)
		{
			this.location.adress = location.optString("adress");
			this.location.latitude = location.optDouble("latitude");
			this.location.longitude = location.optDouble("longitude");
		}
		
		JSONArray schedules = json.optJSONArray("schedule");
		if (schedules != null)
		{
			for (int i = 0; i < schedules.length(); i++)
			{
				QRCodeSchedule qrCodeSchedule = new QRCodeSchedule();
				JSONObject schedule = schedules.getJSONObject(i);
				
				qrCodeSchedule.content = schedule.optString("content");
				qrCodeSchedule.date = schedule.optString("date");
				qrCodeSchedule.recurrence = schedule.optInt("recurrence");
				
				this.schedules.add(qrCodeSchedule);
			}
		}
	}

	public JSONObject toJSON() throws Exception
	{
		JSONObject json = new JSONObject();
		if (this.name != null)
		{
			json.put("name", this.name);
		}
		if (this.campaignId != null)
		{
			json.put("campaignId", this.campaignId);
		}
		if (this.content != null)
		{
			json.put("content", this.content);
		}
		if (this.comment != null)
		{
			json.put("comment", this.comment);
		}
		if (this.urlinfo != null)
		{
			json.put("urlinfo", this.urlinfo);
		}
		if (this.shortUrl != null)
		{
			json.put("shortUrl", this.shortUrl);
		}
		if (this.imageUrl != null)
		{
			json.put("imageUrl", this.imageUrl);
		}
		if (this.scans != null)
		{
			json.put("scans", this.scans);
		}
		if (this.isScheduleEnabled != null)
		{
			json.put("isScheduleEnabled", this.isScheduleEnabled);
		}
		
		if (this.properties != null)
		{
			JSONObject properties = new JSONObject();
			if (this.properties.size != null)
			{
				properties.put("size", this.properties.size);
			}
			if (this.properties.version != null)
			{
				properties.put("version", this.properties.version);
			}
			if (this.properties.padding != null)
			{
				properties.put("padding", this.properties.padding);
			}
			if (this.properties.em != null)
			{
				properties.put("em", this.properties.em);
			}
			if (this.properties.ec != null)
			{
				properties.put("ec", this.properties.ec);
			}
			if (this.properties.foreground != null)
			{
				properties.put("foreground", this.properties.foreground);
			}
			if (this.properties.background != null)
			{
				properties.put("background", this.properties.background);
			}
			
			json.put("properties", properties);
		}
		
		if (this.location != null)
		{
			JSONObject location = new JSONObject();
			if (this.location.adress != null)
			{
				location.put("adress", this.location.adress);
			}
			if (this.location.latitude != null)
			{
				location.put("latitude", this.location.latitude);
			}
			if (this.location.longitude != null)
			{
				location.put("longitude", this.location.longitude);
			}
			
			json.put("location", location);
		}
		
		if (this.schedules != null && !this.schedules.isEmpty())
		{
			JSONArray schedules = new JSONArray();
			for (int i = 0; i < this.schedules.size(); i++)
			{
				JSONObject schedule = new JSONObject();
				schedule.put("content", this.schedules.get(i).content);
				schedule.put("date", this.schedules.get(i).date);
				schedule.put("recurrence", this.schedules.get(i).recurrence);
				schedules.put(schedule);
			}
		}
		
		return json;
	}
}
