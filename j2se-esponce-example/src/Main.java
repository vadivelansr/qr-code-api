import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.esponce.webservice.QRCodeClient;
import com.esponce.webservice.model.Campaign;
import com.esponce.webservice.model.ItemControl;
import com.esponce.webservice.model.QRCode;
import com.esponce.webservice.model.Record;
import com.esponce.webservice.model.TrackListResponse;

public class Main
{
	private static QRCodeClient client;
		
	private static final int MENU_API_KEY = -2;
	private static final int MENU_EXIT = -3;
	private static final int MENU_VERBOSE = -4;
	private static final int MENU_GENERATE = 1;
	private static final int MENU_DECODE = 2;
	private static final int MENU_TRACK_LIST = 3;
	private static final int MENU_CAMPAIGN_SELECT = 4;
	private static final int MENU_CAMPAIGN_INSERT = 5;
	private static final int MENU_CAMPAIGN_UPDATE = 6;
	private static final int MENU_CAMPAIGN_DELETE = 7;
	private static final int MENU_QRCODE_SELECT = 8;
	private static final int MENU_QRCODE_INSERT = 9;
	private static final int MENU_QRCODE_UPDATE = 10;
	private static final int MENU_QRCODE_DELETE = 11;
	private static final int MENU_STATISTICS_DISPLAY = 12;
	private static final int MENU_STATISTICS_DOWNLOAD = 13;
	private static final int MENU_EXPORT = 14;
	private static final int MENU_IMPORT = 15;
	
	public static void main(String[] args) throws Exception
	{
		client = new QRCodeClient();
		
		int i = 0;
		while (true)
		{
			//Display menu options
			System.out.println("-------------------------------------------------------------------------------");
			System.out.println("Esponce API demo");
			System.out.println();
			System.out.println("1. Generate a QR code");
			System.out.println("2. Decode a QR code");
			System.out.println();
			System.out.println("A. Enter API key");
			System.out.println("3. Get list of campaigns and QR codes");
			System.out.println("4. Select campaign");
			System.out.println("5. Insert campaign");
			System.out.println("6. Update campaign");
			System.out.println("7. Delete campaign");
			System.out.println("8. Select QR code");
			System.out.println("9. Insert QR code");
			System.out.println("10. Update QR code");
			System.out.println("11. Delete QR code");
			System.out.println("12. Display statistics");
			System.out.println("13. Download statistics");
			System.out.println("14. Export data");
			System.out.println("15. Import data");
			System.out.println();
			System.out.println("V. Toggle verbose mode");
			System.out.println("X. Exit");
			System.out.println();
			System.out.println("Your choice: ");
			
			int choice = -1;
			
			//Enter option number
			try
			{
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String line = br.readLine();
				
				if (line.equals("a") || line.equals("A"))
				{
					choice = MENU_API_KEY;
				}
				else if (line.equals("x") || line.equals("X"))
				{
					choice = MENU_EXIT;
				}
				else if (line.equals("v") || line.equals("V"))
				{
					choice = MENU_VERBOSE;
				}
				else
				{
					choice = Integer.parseInt(line);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			//Separator
			System.out.println("-------------------------------------------------------------------------------");
			
			try
			{
				switch (choice)
				{
					case MENU_API_KEY:
						String auth = client.getApiKey();
						System.out.println("Current API key: " + auth);
						auth = readLine("Enter new API key: ", auth);
						client.setApiKey(auth);
						break;
					
					case MENU_GENERATE:
						{
							//Collect parameters
							String content = readLine("Content: ");
							String format = readLine("Format [png]: ", "png");
							String size = readLine("Module size [8]: ", "8");
							String foreground = readLine("Foreground color [black]: ");
							String background = readLine("Background color [white]: ");
							String path = readLine("Save path [qrcode." + format + "]: ", "qrcode." + format);
							
							//Call web service to generate QR code image
							BufferedInputStream ins = client.generate(content, format, null, Integer.parseInt(size), null, null, null, foreground, background, null);
							
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
							
							//Print a message to console
							System.out.println();
							System.out.println("QR code saved to " + path);
						}
						break;
					
					case MENU_DECODE:
						{
							//Get image path
							String path = readLine("Path to QR code image [qrcode.png]: ", "qrcode.png");
							
							//Open the file for reading
							FileInputStream fis = new FileInputStream(path);
							BufferedInputStream bis = new BufferedInputStream(fis);
							
							//Call web service to decode the image
							String content = client.decode(bis);
							
							//Print result to console
							System.out.println();
							System.out.print("Content from QR code: " + content);
						}
						break;
					
					case MENU_TRACK_LIST:
						TrackListResponse response = client.getList();
						
						System.out.println();
						System.out.println("-- Campaigns: ");
						for (i = 0; i < response.campaigns.size(); i++)
						{
							Campaign campaign = response.campaigns.get(i);
							System.out.println("[" + campaign.id + "] " + campaign.name);
						}
						
						System.out.println();
						System.out.println("-- QR codes: ");
						for (i = 0; i < response.qrcodes.size(); i++)
						{
							QRCode qrcode = response.qrcodes.get(i);
							System.out.println("[" + qrcode.id + "] " + qrcode.name);
						}
						break;
					
					case MENU_CAMPAIGN_SELECT:
						{
							String id = readLine("Campaign id: ");
							Campaign campaign = client.getCampaign(id);
							
							System.out.println();
							System.out.println("Campaign id: " + campaign.id);
							System.out.println("Campaign name: " + campaign.name);
							System.out.println("Campaign description: " + campaign.description);
							System.out.println("Campaign created (UTC): " + campaign.created);
							System.out.println();
						}
						break;
					
					case MENU_CAMPAIGN_INSERT:
						{
							String name = readLine("Campaign name [My First Campaign]: ", "My First Campaign");
							String description = readLine("Campaign description [null]: ", null);
							
							Campaign campaign = client.insertCampaign(name, description);
							
							System.out.println();
							System.out.println("Campaign id: " + campaign.id);
							System.out.println("Created (UTC): " + campaign.created);
							System.out.println();
						}
						break;
					
					case MENU_CAMPAIGN_UPDATE:
						{
							Campaign campaign = new Campaign();
							campaign.id = readLine("Campaign id");
							campaign.name = readLine("Campaign name");
							campaign.description = readLine("Campaign description");
							
							client.updateCampaign(campaign);
							System.out.println();
							System.out.println("Campaign updated!");
						}
						break;
					
					case MENU_CAMPAIGN_DELETE:
						{
							String id = readLine("Campaign id: ");
							String delete = readLine("Delete associated QR codes [y]: ", "y");
							Boolean keep = !(delete == "y" || delete == "Y");
							
							client.deleteCampaign(id, keep);
							
							System.out.println();
							System.out.println("Campaign deleted!");
						}
						break;
					
					case MENU_QRCODE_SELECT:
						{
							String id = readLine("QR code id: ");
							QRCode qrcode = client.getQRCode(id);
							
							System.out.println();
							System.out.println("QR code name: " + qrcode.name);
							System.out.println("QR code created (UTC): " + qrcode.created);
							if (qrcode.isAvailable)
							{
								System.out.println("QR code content: " + qrcode.content);
								System.out.println("QR code image URL: " + qrcode.imageUrl);
								System.out.println("Trackable short URL: " + qrcode.shortUrl);
								System.out.println("Campaign id: " + qrcode.campaignId);
								System.out.println("Comment: " + qrcode.comment);
								System.out.println("Scans: " + qrcode.scans);
							}
							else
							{
								System.out.println("Please upgrade your subscription plan to view other properties.");
							}
							System.out.println();
						}
						break;
					
					case MENU_QRCODE_INSERT:
						{
							QRCode qrcode = new QRCode();
							qrcode.name = readLine("QR code name: ");
							qrcode.content = readLine("QR code content: ");
							
							QRCode result = client.insertQRCode(qrcode);
							
							System.out.println();
							System.out.println("QR code id: " + result.id);
							System.out.println("QR code created (UTC): " + result.created);
							System.out.println();
						}
						break;
					
					case MENU_QRCODE_UPDATE:
						{
							QRCode qrcode = new QRCode();
							qrcode.id = readLine("QR code id: ");
							qrcode.name = readLine("QR code name: ");
							qrcode.content = readLine("QR code content: ");
							
							client.updateQRCode(qrcode);
							
							System.out.println();
							System.out.println("QR code updated!");
							System.out.println();
						}
						break;
					
					case MENU_QRCODE_DELETE:
						{
							String id = readLine("QR code id: ");
							client.deleteQRCode(id);
							
							System.out.println();
							System.out.println("QR code deleted!");
							System.out.println();
						}
						break;
					
					case MENU_STATISTICS_DISPLAY:
						{
							String id = readLine("QR code id: ");
							ArrayList<Record> records = client.getStatistics(id);
							
							for (i = 0; i < records.size(); i++)
							{
								Record record = records.get(i);
								String line = record.created + " by " + record.manufacturer + " " + record.deviceModel + " from " + record.city + ", " + record.countryCode;
								System.out.println(line);
							}
							
							System.out.println();
						}
						break;
							
					case MENU_STATISTICS_DOWNLOAD:
						{
							String id = readLine("QR code id: ");
							String format = readLine("Format (csv, xls, xml, or json) [csv]:", "csv");
							String path = readLine("Save path [statistics." + format + "]: ", "statistics." + format);
							
							BufferedInputStream ins = client.getStatistics(id, format);
							FileOutputStream fos = new FileOutputStream(path);
							BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
							
							int size = 0;
							byte[] data = new byte[1024];
							while ((size = ins.read(data, 0, 1024)) > 0)
							{
								bos.write(data, 0, size);
							}
							
							bos.close();
							fos.close();
							ins.close();
							
							System.out.println();
							System.out.println("Statistics saved to " + path);
						}
						break;
					
					case MENU_EXPORT:
						{
							String format = readLine("Format (csv, xls, xml, json, or zip) [csv]:", "csv");
							
							String imageFormat = null;
							if (format.equals("zip"))
							{
								imageFormat = readLine("Image format (png, eps or svg) [png]: ", "png");
							}
							
							String path = readLine("Path to export file [export.csv]: ", "export.csv");
							
							BufferedInputStream ins = client.exportData(format, imageFormat);
							FileOutputStream fos = new FileOutputStream(path);
							BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
							
							int size = 0;
							byte[] data = new byte[1024];
							while ((size = ins.read(data, 0, 1024)) > 0)
							{
								bos.write(data, 0, size);
							}
							
							bos.close();
							fos.close();
							ins.close();
							
							System.out.println();
							System.out.println("Export completed!");
						}
						break;
					
					case MENU_IMPORT:
						{
							//Get import file path
							String path = readLine("Path to import file [import.csv]: ", "import.csv");
							
							String format = readLine("Format (csv, xls, xml, json, or zip) [csv]:", "csv");
							//String format = path.substring(path.length() - 3, path.length());
							
							File file = new File(path);
							FileInputStream fis = new FileInputStream(file);
							BufferedInputStream ins = new BufferedInputStream(fis);
							
							ArrayList<ItemControl> results = new ArrayList<ItemControl>();
							results = client.importData(ins, format);
							
							System.out.println();
							if (!results.isEmpty())
							{
								System.out.println("except:");
								for (i = 0; i < results.size(); i++)
								{
									System.out.println(results.get(i).name);
								}
							}
							
							System.out.println("Import completed!");
						}
						break;
						
					case MENU_VERBOSE:
						client.verbose ^= true;
						System.out.println("Verbose mode: " + (client.verbose ? "on" : "off"));
						break;
					
					case MENU_EXIT:
						System.exit(0);
						break;
						
					default:
						System.out.println("Unknown option number: " + choice);
						break;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Thread.sleep(100);
			}

			//Wait for a key to continue
			System.out.println();
			System.out.println("Press ENTER to continue...");
			new BufferedReader(new InputStreamReader(System.in)).read();
		}
	}

	private static String readLine(String prompt)
	{
		return readLine(prompt, null);
	}
	
	private static String readLine(String prompt, String defaultValue)
	{
		String content = null;
		
		//Show prompt message
		System.out.println();
		System.out.println(prompt);
		
		//Wait until enter is pressed
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try
		{
			content = br.readLine();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		//Set the default value in case user pressed enter key without entering the content
		if (content != null && content.length() == 0)
		{
			content = defaultValue;
		}
		
		return content;
	}
}
