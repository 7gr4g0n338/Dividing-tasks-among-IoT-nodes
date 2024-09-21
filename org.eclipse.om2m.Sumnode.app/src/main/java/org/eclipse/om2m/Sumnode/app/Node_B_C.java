package org.eclipse.om2m.Sumnode.app; 

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

public class Node_B_C {
	
	private static String originator="admin:admin";
	private static String cseProtocol="http";
	private static String cseIp = "127.0.0.1";
	private static int csePort = 8282;
	private static String cseId = "mn-cse";
	private static String cseName = "MN-CSE-Manager";
	private static String targetIN="in-cse/IN-CSE-BS_Server";
 
	private static String aeNameBS = "IN-AE-BS_Server";
	private static String aeName = "MN-AE-Manager";
	private static String cntNameNodeA = "AE-nodeA";
	private static String cntNameDataB = "Cnt_Data_NodeB";
	private static String cntNameDataC = "Cnt_Data_NodeC";
	private static String cntNameNodeB = "AE-nodeB";
	private static String cntNameNodeC = "AE-nodeC";
	private static String cntNameData = "Cnt_Data";
	private static String cntNameCommand = "Cnt_SERVICE_req";
	private static String cntNameResp = "Cnt_SERVICE_resp";
	
	private static String aeProtocol="http";
	private static String aeIp = "127.0.0.1";
	private static int aePort = 1400;	
	private static String subName="Manager_Sub";
 
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
		
		// tao Node trong mn
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
 
		// tao node A trong MN

		obj = new JSONObject();
        obj.put("rn", cntNameNodeA);
        obj.put("api", 12345);// appicaiton ID
		obj.put("rr", true);// Request Reachability
        obj.put("poa",array);// Point of Access
        resource = new JSONObject();
        resource.put("m2m:ae", obj);
        RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName, resource.toString(), 2);
		
		// Tao node B
		obj = new JSONObject();
        obj.put("rn", cntNameNodeB);
        obj.put("api", 12345);// appicaiton ID
		obj.put("rr", true);// Request Reachability
        obj.put("poa",array);// Point of Access
        resource = new JSONObject();
        resource.put("m2m:ae", obj);
        RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName, resource.toString(), 2);
        
		obj = new JSONObject();
        obj.put("rn", cntNameData);
        resource = new JSONObject();
        resource.put("m2m:cnt", obj);
        RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeB, resource.toString(), 3);
        
        obj = new JSONObject();
        obj.put("rn", cntNameCommand);
        resource = new JSONObject();
        resource.put("m2m:cnt", obj);
        RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeB, resource.toString(), 3);
        
        obj = new JSONObject();
        obj.put("rn", cntNameResp);
        resource = new JSONObject();
        resource.put("m2m:cnt", obj);
        RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeB, resource.toString(), 3);
        
        
        // tao node C
        obj = new JSONObject();
        obj.put("rn", cntNameNodeC);
        obj.put("api", 12345);// appicaiton ID
		obj.put("rr", true);// Request Reachability
        obj.put("poa",array);// Point of Access
        resource = new JSONObject();
        resource.put("m2m:ae", obj);
        RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName, resource.toString(), 2);
        
		
		obj = new JSONObject();
        obj.put("rn", cntNameData);
        resource = new JSONObject();
        resource.put("m2m:cnt", obj);
        RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeC, resource.toString(), 3);
        
        obj = new JSONObject();
        obj.put("rn", cntNameCommand);
        resource = new JSONObject();
        resource.put("m2m:cnt", obj);
        RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeC, resource.toString(), 3);
		
        obj = new JSONObject();
        obj.put("rn", cntNameResp);
        resource = new JSONObject();
        resource.put("m2m:cnt", obj);
        RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeC, resource.toString(), 3);
        
		//sub vao CntNameCommand node B
		array = new JSONArray();
		array.put("/"+cseId+"/"+cseName+"/"+aeName);
		obj = new JSONObject();
		obj.put("nu", array);
		obj.put("rn", subName);
		obj.put("nct", 2);
		resource = new JSONObject();		
		resource.put("m2m:sub", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeB+"/"+cntNameCommand, resource.toString(), 23);
		
		//sub vao CntNameCommand node C
		array = new JSONArray();
		array.put("/"+cseId+"/"+cseName+"/"+aeName);
		obj = new JSONObject();
		obj.put("nu", array);
		obj.put("rn", subName);
		obj.put("nct", 2);
		resource = new JSONObject();		
		resource.put("m2m:sub", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeC+"/"+cntNameCommand, resource.toString(), 23);			
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
					JSONObject rep = json.getJSONObject("m2m:sgn").getJSONObject("m2m:nev")
							.getJSONObject("m2m:rep").getJSONObject("m2m:cin");
					String con= rep.getString("con");
					JSONObject conJSONObject = new JSONObject (con);
					
					//xem co phai command yeu cau gui du lieu node B cho BS khong
					if(conJSONObject.getString("Service").equals("get") && conJSONObject.getString("DataSource").equals("B")) {
					JSONObject getBody = new JSONObject(RestHttpClient.get(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeB+"/"+cntNameData+"/la").getBody());
					System.out.println("=================>"+getBody.getJSONObject("m2m:cin").getInt("con"));
					JSONObject content = new JSONObject();
					content.put("Service", "return result");
					content.put("From", "B");
					content.put("Value", getBody.getJSONObject("m2m:cin").getInt("con"));
					content.put("ID", conJSONObject.getInt("ID"));
					JSONObject obj = new JSONObject();
					obj.put("cnf", "application/textNodeB");
					obj.put("con", content.toString());
					obj.put("rn", "cin-data_"+getBody.getJSONObject("m2m:cin").getInt("con"));
					JSONObject resource = new JSONObject();
					resource.put("m2m:cin", obj);
					RestHttpClient.post(originator, csePoa+"/~/"+targetIN+"/"+aeNameBS+"/"+cntNameDataB, resource.toString(), 4);
					}
					
					//xem co phai command yeu cau gui du lieu node C cho BS khong
					if(conJSONObject.getString("Service").equals("get") && conJSONObject.getString("DataSource").equals("C")) {
						JSONObject getBody = new JSONObject(RestHttpClient.get(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeC+"/"+cntNameData+"/la").getBody());
						System.out.println("=================>"+getBody.getJSONObject("m2m:cin").getInt("con"));
						JSONObject content = new JSONObject();
						content.put("Service", "return result");
						content.put("From", "C");
						content.put("Value", getBody.getJSONObject("m2m:cin").getInt("con"));
						content.put("ID", conJSONObject.getInt("ID"));
						JSONObject obj = new JSONObject();
						obj.put("cnf", "application/textNodeC");
						obj.put("con", content.toString());
						obj.put("rn", "cin-data_"+getBody.getJSONObject("m2m:cin").getInt("con"));
						JSONObject resource = new JSONObject();
						resource.put("m2m:cin", obj);
						RestHttpClient.post(originator, csePoa+"/~/"+targetIN+"/"+aeNameBS+"/"+cntNameDataC, resource.toString(), 4);
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
