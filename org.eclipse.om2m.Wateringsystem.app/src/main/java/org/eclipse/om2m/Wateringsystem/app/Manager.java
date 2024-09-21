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
 
public class Manager {
 
	private static String originator="admin:admin";
	private static String cseProtocol="http";
	private static String cseIp = "127.0.0.1";
	private static int csePort = 8080;
	private static String cseId = "in-cse";
	private static String cseName = "in-name";
 
	private static String aeName = "manager";
	private static String aeProtocol="http";
	private static String aeIp = "127.0.0.1";
	private static int aePort = 1500;	
	private static String subName="managersub";
	private static String targetSensorContainerT="mn-cse/mn-name/tempsensor/data";
	private static String targetSensorContainerH="mn-cse/mn-name/humsensor/data";
	private static String targetActuatorContainer="mn-cse/mn-name/Pumactuator/data";
 
	private static String csePoa = cseProtocol+"://"+cseIp+":"+csePort;
	private static String appPoa = aeProtocol+"://"+aeIp+":"+aePort;

	public static int TempsensorValue;
	public static int HumsensorValue;
	public static int flag = 1; 
	
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
 
		// tao Manager trong in
		JSONArray array = new JSONArray();
		array.put(appPoa);
		JSONObject obj = new JSONObject();
		obj.put("rn", aeName);
		obj.put("api", 12346);
		obj.put("rr", true);
		obj.put("poa",array);
		JSONObject resource = new JSONObject();
		resource.put("m2m:ae", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName, resource.toString(), 2);
 
		// sub vao Temp va Hum
		array = new JSONArray();
		array.put("/"+cseId+"/"+cseName+"/"+aeName);
		obj = new JSONObject();
		obj.put("nu", array);
		obj.put("rn", subName);
		obj.put("nct", 2);
		resource = new JSONObject();		
		resource.put("m2m:sub", obj);
		
		//if(RestHttpClient.post1(originator, csePoa+"/~/"+targetSensorContainerT, resource.toString(), 23).getStatusCode()!=201)
		while(RestHttpClient.post1(originator, csePoa+"/~/"+targetSensorContainerT, resource.toString(), 23).getStatusCode()!=201){
			RestHttpClient.post(originator, csePoa+"/~/"+targetSensorContainerT, resource.toString(), 23);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//if(RestHttpClient.post1(originator, csePoa+"/~/"+targetSensorContainerH, resource.toString(), 23).getStatusCode()!=201)
		while(RestHttpClient.post1(originator, csePoa+"/~/"+targetSensorContainerH, resource.toString(), 23).getStatusCode()!=201){
			RestHttpClient.post(originator, csePoa+"/~/"+targetSensorContainerH, resource.toString(), 23);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	
	}
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
				
				if (json.getJSONObject("m2m:sgn").has("m2m:vrq")) {
					System.out.println("Confirm subscription");
				} else {
					JSONObject rep = json.getJSONObject("m2m:sgn").getJSONObject("m2m:nev")
							.getJSONObject("m2m:rep").getJSONObject("m2m:cin");
					
					String lbl= (String) rep.getJSONArray("lbl").get(0);
					System.out.println("lbl =====>"+lbl);
					int ty = rep.getInt("ty");
					System.out.println("Resource type: "+ty);
					
					
					if (lbl.equals("Temp")) {
						String con = rep.getString("con");
						TempsensorValue = Integer.parseInt(con);
					}
					if (lbl.equals("Hum")) {
						String con = rep.getString("con");
						HumsensorValue = Integer.parseInt(con);		
					}
					
						boolean actuatorState;
						if(TempsensorValue>=22&&HumsensorValue<70){
							System.out.print(" OBSERVATION: TempSensor Value "+TempsensorValue+"\n");
							System.out.println(" OBSERVATION: HumSensor Value "+HumsensorValue);
							System.out.println(" -> HIGH");
							actuatorState=true;
							//flag = 1;
						}else{
							System.out.print(" OBSERVATION: TempSensor Value "+TempsensorValue+"\n");
							System.out.println(" OBSERVATION: HumSensor Value "+HumsensorValue);
							System.out.println(" -> LOW");
							actuatorState=false;
							//flag = 0;
						}
						System.out.println("ACTION: switch actuator state to "+actuatorState+"\n");
 
						// gui noi dung cho pumactuator
						if(flag==1 && actuatorState==true || flag==0 && actuatorState==false ) {
						JSONObject obj = new JSONObject();
						obj = new JSONObject();
						obj.put("cnf", "application/textPum");
						obj.put("con", actuatorState);
						JSONObject resource = new JSONObject();
						resource.put("m2m:cin", obj);
						RestHttpClient.post(originator, csePoa+"/~/"+targetActuatorContainer, resource.toString(), 4);
						if(actuatorState==true)
						flag = 0;
						else flag = 1;
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
}