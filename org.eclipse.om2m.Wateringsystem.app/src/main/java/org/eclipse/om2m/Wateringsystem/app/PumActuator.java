package org.eclipse.om2m.Wateringsystem.app; 

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class PumActuator {
		private static String originator="admin:admin";
		private static String cseProtocol="http";
		private static String cseIp = "127.0.0.1";
		private static int csePort = 8282;
		private static String cseId = "mn-cse";
		private static String cseName = "mn-name";
	 
		private static String aeName = "Pumactuator";
		private static String cntName = "data";
	 
		private static String aeProtocol="http";
		private static String aeIp = "127.0.0.1";
		private static int aePort = 1400;	
		private static String subName="Pumactuatorsub";
	 
		private static String csePoa = cseProtocol+"://"+cseIp+":"+csePort;
		private static String appPoa = aeProtocol+"://"+aeIp+":"+aePort;
	 
		public static void main(String[] args) {
	 
			// bat server
			HttpServer server = null;
			try {
				server = HttpServer.create(new InetSocketAddress(aePort), 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
			server.createContext("/", new MyHandler());
			server.setExecutor(Executors.newCachedThreadPool());
			server.start();
	 
			// tao Pumactuator trong mn
			JSONArray array = new JSONArray();
			array.put(appPoa);
			JSONObject obj = new JSONObject();
			obj.put("rn", aeName);
			obj.put("api", 12345);// appicaiton ID
			obj.put("rr", true);// Request Reachability
			obj.put("poa",array);// Point of Access
			JSONObject resource = new JSONObject();
			resource.put("m2m:ae", obj);
			RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName, resource.toString(), 2);
	 
			// tao vung chua
	        obj = new JSONObject();
	        obj.put("rn", cntName);
	        resource = new JSONObject();
	        resource.put("m2m:cnt", obj);
			RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+aeName, resource.toString(), 3);
	 
			//sub vao chinh no
			array = new JSONArray();
			array.put("/"+cseId+"/"+cseName+"/"+aeName);
			obj = new JSONObject();
			obj.put("nu", array);
			obj.put("rn", subName);
			obj.put("nct", 2);
			resource = new JSONObject();		
			resource.put("m2m:sub", obj);
			RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+aeName+"/"+cntName, resource.toString(), 23);
			
		}
	 
		// bo phan xu li thong tin nhan duoc
		static class MyHandler implements HttpHandler {
	 
			public void handle(HttpExchange httpExchange)  {
				System.out.println("Event Recieved!");
	 
				try{
					InputStream in = httpExchange.getRequestBody();
	 
					String requestBody = "";
					int i;char c;
					while ((i = in.read()) != -1) {
						c = (char) i;
						requestBody = (String) (requestBody+c);
					}
	 
					System.out.println(requestBody);
	 
					JSONObject json = new JSONObject(requestBody);
					if (json.getJSONObject("m2m:sgn").has("m2m:vrq")) {//vrq verificationRequest
						System.out.println("Confirm subscription");
						
					} else {
						// bat tat may bom
						JSONObject rep = json.getJSONObject("m2m:sgn").getJSONObject("m2m:nev") // nev notificationEvent
								.getJSONObject("m2m:rep").getJSONObject("m2m:cin");
						int ty = rep.getInt("ty");
						System.out.println("Resource type: "+ty);
	 
						if (ty == 4) {
							String con = rep.getString("con");
							System.out.println("Actuator state = "+con);
							setActuatorValue(Boolean.parseBoolean(con));
						}
					}	
	 
					String responseBudy ="";
					byte[] out = responseBudy.getBytes("UTF-8");
					httpExchange.sendResponseHeaders(200, out.length);
					OutputStream os = httpExchange.getResponseBody();
					os.write(out);
					os.close();
	 
				} catch(Exception e){
					e.printStackTrace();
				}		
			}
		}
	 
		public static void setActuatorValue(boolean actuatorValue) {
		}

}
