#!/usr/bin/python

# 2012 (c) Esponce
# Last mod: 2011-09-23
# Uses: Python 2.7.2
# Description: Frequently used functions

import json
import pprint
import hashlib;

#******************************************************************************************
# Converts dictionary to object
# http://stackoverflow.com/questions/1305532/convert-python-dict-to-object
#******************************************************************************************
def dict2obj(d):
    if isinstance(d, list):
        d = [dict2obj(x) for x in d]
    if not isinstance(d, dict):
        return d
    class C(object):
        pass
    o = C()
    for k in d:
        o.__dict__[k] = dict2obj(d[k])
    return o

#******************************************************************************************
# Converts dictionary to object
# Keywords: python json deserialize ascii site:stackoverflow.com
# Reference: http://stackoverflow.com/questions/956867/how-to-get-string-objects-instead-unicode-ones-from-json-in-python
#******************************************************************************************
def _decode_list(lst):
    newlist = []
    for i in lst:
        if isinstance(i, unicode):
            i = i.encode("utf-8")
        elif isinstance(i, list):
            i = _decode_list(i)
        newlist.append(i)
    return newlist

def _decode_dict(dct):
    newdict = {}
    for k, v in dct.iteritems():
        if isinstance(k, unicode):
            k = k.encode("utf-8")
        if isinstance(v, unicode):
             v = v.encode("utf-8")
        elif isinstance(v, list):
            v = _decode_list(v)
        newdict[k] = v
    return newdict

#******************************************************************************************
# Creates object from JSON string
#******************************************************************************************
def json_deserialize(raw):
    d = json.loads(raw, object_hook=_decode_dict)
    return dict2obj(d)

#******************************************************************************************
# Nicely formats JSON string and prints it to the screen
#******************************************************************************************
def json_dump(raw):
    pp = pprint.PrettyPrinter(indent=2)
    d = json.loads(raw, object_hook=_decode_dict)
    pp.pprint(d)

#******************************************************************************************
# Generates SHA256 hash from content
#******************************************************************************************
def sha256(content):
    sha = hashlib.sha256()
    sha.update(content)
    #return sha.digest()
    return sha.hexdigest()

#******************************************************************************************
# Generates SHA512 hash from content
# Reference: http://ubuntuforums.org/showthread.php?t=293567
#******************************************************************************************
def sha512(content):
    m = hashlib.sha512()
    m.update(content)
    return m.hexdigest()