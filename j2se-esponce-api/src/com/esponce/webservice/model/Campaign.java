package com.esponce.webservice.model;

import org.json.JSONObject;

public class Campaign
{
	public String id = null;
	public String name = null;
	public String description = null;
	public String created = null;
	
	public void parse(JSONObject json)
	{
		this.id = json.optString("id");
		this.name = json.optString("name");
		this.description = json.optString("description");
		this.created = json.optString("created");
	}

	public JSONObject toJSON() throws Exception
	{
		JSONObject json = new JSONObject();
		if (this.name != null)
		{
			json.put("name", this.name);
		}
		if (this.description != null)
		{
			json.put("description", this.description);
		}
		return json;
	}	
}
