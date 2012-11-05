#!/usr/bin/env python
# -*- coding: UTF-8 -*-

# 2012 (c) Esponce
# Last mod: 2012-11-05
# Uses: Python 2.7.2
# Description: Esponce QR Code API 3.0

import json
import urllib
import httplib
import platform
import time
from StringIO import StringIO
from xml.etree.ElementTree import ElementTree
from helper import json_deserialize, json_dump, sha256

#******************************************************************************************
# QR Code API
#******************************************************************************************
class QRCodeAPI():
    
    verbose = True
    
    domain = "www.esponce.com"
    basepath = "/api/v3"
    baseurl = "http://" + domain + basepath
    headers = {
        "Content-type": "application/json",
        "Accept": "application/json",
        "User-Agent": "Esponce/3.0 (%s %s; Python %s; U; en)" % (platform.system(), platform.release(), platform.python_version())
    }
    auth = None
    
    
    #******************************************************************************************
    # Makes a HTTP request to the server.
    #******************************************************************************************
    def request(self, path, method = "GET", data = None):
        
        if self.verbose:
            print method + ": ", path
            
        raw = data
        if type(raw) is dict:
            raw = json.dumps(raw)
        
        # Make a request
        con = httplib.HTTPConnection(self.domain)
        con.request(method, self.basepath + path, raw, self.headers)
        
        # Get response status
        response = con.getresponse()
        if self.verbose:
            print "Status: ", response.status, response.reason
            
        # Read the response body
        result = ""
        if response.status == 200:
            result = response.read()
        
        # Close the connection    
        con.close()
        
        # TODO: Get X-Api-Error parameter from header

        # Print nicely formatted output
        if self.verbose:
            print "--------------------------------------------------------------------------------"

        return result

    
    
    #**************************************************************************************//**
    # Generates a QR Code image using 'generate' API method (HTTP GET).
    # @param content       Content to be encoded in QR Code. This parameter is required.
    # @param format        Target image format: png (default), jpg, bmp, tif, xaml, svg, eps, txt, html, zip
    # @param size          Size of a single QR code "pixel" (module) in real pixels; values 1..20, default: 8
    # @param padding       Border thickness in QR code "pixels" (modules); values 0..20, default: 4
    # @param version       Defines capacity and overall image size; values 1..40, unspecified or empty value for auto (default value)
    # @param em            Encoding mode, defines what kind of characters can encode and affects total image size; values:
    #                      * byte - can store any data, default value
    #                      * numeric - only numbers are allowed 0-9
    #                      * alphanumeric - numbers 0-9, letters A-Z, subset of punctuation .$%/*:-
    # @param ec            Error Correction level, defines how much code can be damaged but still recoverable, affects capacity and image size; values: M (default), H, L, Q
    # @param attachment    Flag indicating whether to return response as downloadable file, values "true" or "false", default: false 
    # @return Returns image of the specified format or PNG image by default.
    #*****************************************************************************************/
    def generate(self, content, format = "png", size = None, padding = None, version = None, em = None, ec = None, foreground = None, background = None, attachment = False):
        
        d = { "content" : content, "format": format }
        
        if version > 0 and version <= 40:
            d["version"] = version
            
        if size >= 1 and size <= 20:
            d["size"] = size
            
        if padding >= 0 and padding <= 20:
            d["padding"] = padding
            
        if em != None:
            d["em"] = em
            
        if ec != None:
            d["ec"] = ec
            
        if foreground != None:
            d["foreground"] = foreground
            
        if background != None:
            d["background"] = background
            
        if attachment:
            d["attachment"] = "true"
            
        if self.auth != None:
            d["auth"] = self.auth;
            
        query = urllib.urlencode(d)
        
        result = self.request("/generate?" + query)
        return result;
    
    #**************************************************************************************//**
    # Generates a QR Code image using 'encode' API method (HTTP POST).
    # @param content       Content to be encoded in QR Code. This parameter is required.
    # @param format        Target image format: png (default), jpg, bmp, tif, xaml, svg, eps, txt, html, zip
    # @param size          Size of a single QR code "pixel" (module) in real pixels; values 1..20, default: 8
    # @param padding       Border thickness in QR code "pixels" (modules); values 0..20, default: 4
    # @param version       Defines capacity and overall image size; values 1..40, unspecified or empty value for auto (default value)
    # @param em            Encoding mode, defines what kind of characters can encode and affects total image size; values:
    #                      * byte - can store any data, default value
    #                      * numeric - only numbers are allowed 0-9
    #                      * alphanumeric - numbers 0-9, letters A-Z, subset of punctuation .$%/*:-
    # @param ec            Error Correction level, defines how much code can be damaged but still recoverable, affects capacity and image size; values: M (default), H, L, Q
    # @param attachment    Flag indicating whether to return response as downloadable file, values "true" or "false", default: false 
    # @return Returns image of the specified format or PNG image by default.
    #*****************************************************************************************/
    def encode(self, content, format = "png", size = None, padding = None, version = None, em = None, ec = None, foreground = None, background = None):
        
        d = { "content": content, "format": format }
        
        if version > 0 and version <= 40:
            d["version"] = version
            
        if size >= 1 and size <= 20:
            d["size"] = size
            
        if padding >= 0 and padding <= 20:
            d["padding"] = padding
            
        if em != None:
            d["em"] = em
            
        if ec != None:
            d["ec"] = ec
            
        if foreground != None:
            d["foreground"] = foreground
            
        if background != None:
            d["background"] = background
            
        query = ""
        if self.auth != None:
            d["auth"] = self.auth;
            query = "?" + urllib.urlencode(d)
        
        result = self.request("/encode" + query, "POST", d)
        return result;
    
    
    #**************************************************************************************//**
    # Decodes a QR Code image using 'decode' API method (HTTP POST).
    # @param file    Path to local QR Code image file.
    # @return Returns a content decoded from a QR Code image. 
    #*****************************************************************************************/
    def decode(self, image, format = "png"):
        d = { "format": format }
        if self.auth != None:
            d["auth"] = self.auth;
        if format == "pdf":
            d["hint"] = "pdf1";
        query = urllib.urlencode(d)
        result = self.request("/decode?" + query, "POST", image)
        return result;
    
    
    #**************************************************************************************//**
    # Gets a list of campaigns and QR Codes for tracking.
    #*****************************************************************************************/
    def get_track_list(self):
        d = { "auth" : self.auth }
        query = urllib.urlencode(d)
        result = self.request("/track/list?" + query, "GET")
        return json_deserialize(result)
    
    #**************************************************************************************//**
    # Gets a campaign data.
    #*****************************************************************************************/
    def get_track_campaign(self, id):
        d = { "auth" : self.auth }
        query = urllib.urlencode(d)
        result = self.request("/track/campaign/" + id + "?" + query, "GET")
        return json_deserialize(result)
    
    #**************************************************************************************//**
    # Inserts a new campaign.
    #*****************************************************************************************/
    def insert_track_campaign(self, model):
        d = { "auth" : self.auth }
        query = urllib.urlencode(d)
        data = json.dumps(model)
        result = self.request("/track/campaign?" + query, "POST", data)
        return json_deserialize(result)
    
    #**************************************************************************************//**
    # Updates an existing campaign.
    #*****************************************************************************************/
    def update_track_campaign(self, id, model):
        d = { "auth" : self.auth }
        query = urllib.urlencode(d)
        data = json.dumps(model)
        result = self.request("/track/campaign/" + id + "?" + query, "PUT", data)
    
    #**************************************************************************************//**
    # Deletes a campaign.
    #*****************************************************************************************/
    def delete_track_campaign(self, id, keep = False):
        d = { "auth" : self.auth, "keep" : keep }
        query = urllib.urlencode(d)
        result = self.request("/track/campaign/" + id + "?" + query, "DELETE")
    
    #**************************************************************************************//**
    # Gets a QR Code for tracking.
    #*****************************************************************************************/
    def get_track_qrcode(self, id):
        d = { "auth" : self.auth }
        query = urllib.urlencode(d)
        result = self.request("/track/qrcode/" + id + "?" + query, "GET")
        return json_deserialize(result)
    
    #**************************************************************************************//**
    # Inserts a new QR Code for tracking.
    #*****************************************************************************************/
    def insert_track_qrcode(self, model):
        d = { "auth" : self.auth }
        query = urllib.urlencode(d)
        data = json.dumps(model)
        result = self.request("/track/qrcode?" + query, "POST", data)
        return json_deserialize(result)
    
    #**************************************************************************************//**
    # Updates an existing QR Code for tracking.
    #*****************************************************************************************/
    def update_track_qrcode(self, id, model):
        d = { "auth" : self.auth }
        query = urllib.urlencode(d)
        data = json.dumps(model)
        result = self.request("/track/qrcode/" + id + "?" + query, "PUT", data)
    
    #**************************************************************************************//**
    # Deletes a QR Code for tracking.
    #*****************************************************************************************/
    def delete_track_qrcode(self, id):
        d = { "auth" : self.auth }
        query = urllib.urlencode(d)
        result = self.request("/track/qrcode/" + id + "?" + query, "DELETE")
        
    #**************************************************************************************//**
    # Exports statistics for a QR Code.
    #*****************************************************************************************/
    def get_statistics(self, id, format):
        d = { "auth" : self.auth }
        query = urllib.urlencode(d)
        result = self.request("/track/statistics/" + id + "." + format + "?" + query, "GET")
        return result
        
    #**************************************************************************************//**
    # Exports a QR Code for tracking.
    #*****************************************************************************************/
    def export_entries(self, format, image = "png"):
        d = { "auth" : self.auth }
        
        if format == "zip":
            d["format"] = image
        
        query = urllib.urlencode(d)
        result = self.request("/track/export" + "." + format + "?" + query, "GET")
        return result
        
    #**************************************************************************************//**
    # Imports a QR Code for tracking.
    #*****************************************************************************************/
    def import_entries(self, file, format = None):
        d = { "auth" : self.auth }
        
        if format != None:
            d["format"] = format
            
        query = urllib.urlencode(d)
        result = self.request("/track/import?" + query, "POST", file)
        return result

    