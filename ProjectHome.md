## Introduction ##
Esponce provides REST web service to generate or decode QR Codes.

Registered users can manage trackable QR Codes and view scan statistics.

**Java and Python libraries** implement Esponce API 3.0 features.

## Esponce ##
  * [Esponce website](http://www.esponce.com/)
  * [API documentation](http://www.esponce.com/help), REST web service
  * [QR Code Plugins](http://code.google.com/p/qr-code-plugins/), related project
  * [The easiest way to generate a QR Code for developers](http://tech.avivo.si/2012/01/the-easiest-way-to-generate-a-qr-code-for-programmers/), blog post with copy-paste samples

## Usage in Java ##
```
import com.esponce.webservice.QRCodeClient;

//Define content
String content = "Hello World!";
String path = "qrcode.png";

//Call web service to generate QR code image
QRCodeClient client = new QRCodeClient();
BufferedInputStream ins = client.generate(content);

//Save image to file
FileOutputStream fos = new FileOutputStream(path);
BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);

int length = 0;
byte[] data = new byte[1024];
while ((length = ins.read(data, 0, 1024)) > 0)
{
  bos.write(data, 0, length);
}

bos.close();
fos.close();
ins.close();
```

See [example source code](http://code.google.com/p/qr-code-api/source/browse/trunk/j2se-esponce-example/src/Main.java) for more info.

## Usage in Python ##
```
import qrcode
from qrcode import *

content = raw_input("Content [text, url, etc.]: ")
format = raw_input("Format [png|jpg|eps|svg]: ")
file = raw_input("Output file [qrcode." + format + "]: ")
image = api.generate(content, format)
f = open(file, "wb")
f.write(image)
f.close()
```

## Other languages ##
[CodePlex](http://qrcodeapi.codeplex.com/) hosts .NET 3.5, Silverlight 4, Windows Phone 7.1 and Windows RT (Windows 8) library