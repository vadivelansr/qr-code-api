#!/usr/bin/python

# 2012 (c) Esponce
# Last mod: 2012-11-05
# Uses: Python 2.7.2
# Description: Test Esponce API 3.0

import sys

import helper
from helper import *

import qrcode
from qrcode import *

def main_menu():
    global api
    print "+------------------------------------------+"
    print "| Esponce QR Code API 3.0:                 |"
    print "| 1..generate                              |"
    print "| 2..decode                                |"
    if api.auth != "" and api.auth != None:
        print "| 3..get list of campaigns and QR Codes    |"
        print "| 4..select campaign                       |"
        print "| 5..insert campaign                       |"
        print "| 6..update campaign                       |"
        print "| 7..delete campaign                       |"
        print "| 8..select QR Code                        |"
        print "| 9..insert QR Code                        |"
        print "| 10..update QR Code                       |"
        print "| 11..delete QR Code                       |"
        print "| 12..export statistics                    |"
        print "| 13..export                               |"
        print "| 14..import                               |"
    print "| a..set API key                           |"
    print "| v..verbose on/off                        |"
    print "| x..exit                                  |"
    print "+------------------------------------------+"
    select = raw_input("Select: ")
    print ""
    return select


# Write test code here, comment or uncomment methods...
api = QRCodeAPI()
    
def main_loop():
    global api
    api.auth = None
    running = True
    while running:
        select = main_menu()
    
        try:
            if select == "1":
                print "== Generate =="
                content = raw_input("Content [text|url|vcard]: ")
                format  = raw_input("Format [png|jpg|eps|svg]: ")
                file    = raw_input("Output File [qrcode." + format + "]: ")
                image = api.generate(content, format)
                f = open(file, "wb")
                f.write(image)
                f.close()
                
            elif select == "2":
                print "== Decode =="
                file    = raw_input("Input File [qrcode.png]: ")
                format  = raw_input("Format [png|jpg|eps|svg]: ")
                image = open(file, "rb")
                result = api.decode(image, format)
                image.close()
                
                print "Result:"
                print result
                if api.verbose:
                    print "--------------------------------------------------------------------------------"
                
            elif select == "3":
                print "== Get list of campaigns and QR Codes =="
                list = api.get_track_list()
                
                print "List of campaigns:"
                for campaign in list.campaigns:
                    print " * " + campaign.id + " = " + campaign.name
                
                print ""
                print "List of QR Codes:"
                for qrcode in list.qrcodes:
                    name = "???"
                    if hasattr(qrcode, 'name'):
                        name = qrcode.name
                    print " * " + qrcode.id + " = " + name
                    
            elif select == "4":
                print "== Get campaign data =="
                id = raw_input("Campaign id: ")
                result = api.get_track_campaign(id)
                print "id: " + result.id
                print "name: " + result.name
                if hasattr(result, "description"):
                    print "description: " + result.description
                if hasattr(result, "created"):
                    print "created: " + result.created
                    
            elif select == "5":
                print "== Create campaign =="
                name = raw_input("Campaign name: ")
                desc = raw_input("Description: ")
                result = api.insert_track_campaign({ "name": name, "description": desc })
                print "id: " + result.id
                print "created: " + result.created
            
            elif select == "6":
                print "== Update campaign =="
                id   = raw_input("Campaign id: ")
                name = raw_input("Campaign name: ")
                desc = raw_input("Description: ")
                model = { "name": name, "description": desc }
                api.update_track_campaign(id, model)
                
            elif select == "7":
                print "== Delete campaign =="
                id   = raw_input("Campaign id: ")
                keep = raw_input("Keep QR Codes? [y|n]: ")
                if keep == "y" or keep == "Y":
                    keep = True
                else:
                    keep = False
                api.delete_track_campaign(id, keep)
                
            elif select == "8":
                print "== Get QR Code data =="
                id = raw_input("QR Code id: ")
                result = api.get_track_qrcode(id)
                print "id: " + result.id
                if hasattr(result, "campaignId"):
                    print "campaignId: " + result.campaignId
                print "name: " + result.name
                if hasattr(result, "comment"):
                    print "comment: " + result.comment
                if hasattr(result, "urlinfo"):
                    print "urlinfo: " + result.urlinfo
                if hasattr(result, "shortUrl"):
                    print "shortUrl: " + result.shortUrl
                if hasattr(result, "imageUrl"):
                    print "imageUrl: " + result.imageUrl
                if hasattr(result, "content"):
                    print "content: " + result.content
                if hasattr(result, "scans"):
                    print "scans: " + str(result.scans)
                if hasattr(result, "created"):
                    print "created: " + result.created
                
                if hasattr(result, "properties"):
                    print "properties"
                    if hasattr(result.properties, "format"):
                        print "  format: " + result.properties.format
                    if hasattr(result.properties, "size"):
                        print "  size: " + str(result.properties.size)
                    if hasattr(result.properties, "version"):
                        print "  version: " + str(result.properties.version)
                    if hasattr(result.properties, "padding"):
                        print "  padding: " + str(result.properties.padding)
                    if hasattr(result.properties, "em"):
                        print "  em: " + result.properties.em
                    if hasattr(result.properties, "ec"):
                        print "  ec: " + result.properties.ec
                    if hasattr(result.properties, "foreground"):
                        print "  foreground: " + result.properties.foreground
                    if hasattr(result.properties, "background"):
                        print "  background: " + result.properties.background
                
                if hasattr(result, "location"):
                    print "location"
                    if hasattr(result.location, "address"):
                        print "  address: " + result.location.address
                    if hasattr(result.location, "latitude"):
                        print "  latitude: " + result.location.latitude
                    if hasattr(result.location, "longitude"):
                        print "  longitude: " + result.location.longitude
                        
            #if hasattr(result, "schedule"):
            #    print "schedule"
            #    if hasattr(result.schedule, "content"):
            #        print "  content: " + result.schedule.content
            #    if hasattr(result.schedule, "recurrence"):
            #        print "  recurrence: " + result.schedule.recurrence
            #    if hasattr(result.schedule, "date"):
            #        print "  date: " + result.schedule.date
                    
            elif select == "9":
                print "== Create QR Code =="
                content = raw_input("Content: ")
                name    = raw_input("Friendly name: ")
                comment = raw_input("Comment (optional): ")
                urlinfo = raw_input("URL info (optional): ")
            
                location = None
                choice = raw_input("Enter location? [y|n] ")
                if choice == "y" or choice == "Y":
                    address   = raw_input("Address: ")
                    latitude  = raw_input("Latitude: ")
                    longitude = raw_input("Longitude: ")
                    location = { "address": address, "latitude": latitude, "longitude": longitude }
                    
                properties = None
                choice = raw_input("Enter properties? [y|n] ")
                if choice == "y" or choice == "Y":
                    format  = raw_input("Format: ")
                    size    = raw_input("Size: ")
                    version = raw_input("Version: ")
                    padding = raw_input("Padding: ")
                    em      = raw_input("Encode mode: ")
                    ec      = raw_input("Error correction: ")
                    foreground = raw_input("Foreground: ")
                    background = raw_input("Background: ")
                    properties = {
                        "format": format,
                        "size": size,
                        "version": version,
                        "padding": padding,
                        "em": em,
                        "ec": ec,
                        "foreground": foreground,
                        "background": background
                    }
                    
                #schedule = None
                #choice = raw_input("Enter schedule? [y|n] ")
                #if choice == "y" or choice == "Y":
                #    content    = raw_input("Content: ")
                #    recurrence = raw_input("Recurrence: ")
                #    date       = raw_input("Date: ")
                #    schedule = { "content": content, "recurrence": recurrence, "date": date }
                    
                model = {
                    "name": name,
                    "content": content,
                    "comment": comment,
                    "urlinfo": urlinfo,
                    "location": location,
                    "properties": properties
                }
                
                result = api.insert_track_qrcode(model)
                print "id: " + result.id
                print "created: " + result.created
            
            elif select == "10":
                print "== Update QR Code =="
                id   = raw_input("QR Code id: ")
                name = raw_input("Friendly name: ")
                desc = raw_input("Description: ")
                model = { "name": name, "description": desc }
                api.update_track_qrcode(id, model)
                
            elif select == "11":
                print "== Delete QR Code =="
                id = raw_input("QR Code id: ")
                api.delete_track_qrcode(id)
                
            elif select == "12":
                print "== Export statistics =="
                id     = raw_input("QR Code id: ")
                file   = raw_input("Output File [output.csv]: ")
                format = raw_input("Format [csv|xls|xml]: ")
                result = api.get_statistics(id, format)
                f = open(file, "wb")
                f.write(result)
                f.close()
                
            elif select == "13":
                print "== Export campaigns and QR Codes =="
                file   = raw_input("Output File [output.csv]: ")
                format = raw_input("Format [csv|xls|xml|zip]: ")
                
                image = "png"
                if format == "zip":
                    image = raw_input("Images in ZIP [png|eps|svg]: ")
                    
                result = api.export_entries(format, image)
                f = open(file, "wb")
                f.write(result)
                f.close()
                
            elif select == "14":
                print "== Import campaigns and QR Codes =="
                file   = raw_input("Input File [import.csv]: ")
                format = raw_input("Format [csv|xls|xml|zip]: ")
                f = open(file, "rb")
                feedback = api.import_entries(f, format)
                f.close()
                
                f = open(file + ".json", "wb")
                f.write(feedback)
                f.close()
                
            elif select == "a":
                print "Current API key:", api.auth 
                api.auth = raw_input("Enter API key: ")
                
            elif select == "v":
                if api.verbose:
                    api.verbose = False
                else:
                    api.verbose = True
                
            elif select == "x":
                print "Completed!"
                print "--------------------------------------------------------------------------------"
                running = False
                
            else:
                print "Unknown option number, try again"
            
        except:
            print "Error: ", sys.exc_info()[1]
            print "--------------------------------------------------------------------------------"
            #print sys.exc_info()[1]


#******************************************************************************************
# Entry point
#******************************************************************************************
if __name__ == "__main__":
    main_loop()
        