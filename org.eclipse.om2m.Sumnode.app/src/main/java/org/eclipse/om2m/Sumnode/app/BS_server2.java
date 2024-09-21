package org.eclipse.om2m.Sumnode.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.lang.String;
import org.json.JSONArray;
import org.json.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class BS_server2 {

	public static int Sum = 0;
	public static int ID = 0;
	public static int Count = 1;
	private static String originator = "admin:admin";
	private static String cseProtocol = "http";
	private static String cseIp = "127.0.0.1";
	private static int csePort = 8080;
	private static String cseId = "in-cse";
	private static String cseName = "IN-CSE-BS_Server";
	private static String targetMN = "mn-cse/MN-CSE-Manager";
	private static String targetIN = "in-cse/IN-CSE-BS_Server";

	private static String aeNameBS = "IN-AE-BS_Server";
	private static String[] AENameNode = { "AE-nodeA", "AE-nodeB", "AE-nodeC" };

	private static String aeProtocol = "http";
	private static String aeIp = "127.0.0.1";
	private static int aePort = 1500;
	private static String subName = "BS_serverSub";

	private static String[] CntRequestData = { "Cnt_reqData_NodeA", "Cnt_reqData_NodeB", "Cnt_reqData_NodeC" };
	private static String[] CntName = { "Cnt_Data", "Cnt_SERVICE_req", "Cnt_SERVICE_resp" };
	private static String[] cntNameData = { "Cnt_Data_NodeA", "Cnt_Data_NodeB", "Cnt_Data_NodeC" };

	private static String targetNodeBContainer = "in-cse/IN-CSE-BS_Server/IN-AE-BS_Server/Cnt_reqData_NodeB";
	private static String targetNodeCContainer = "in-cse/IN-CSE-BS_Server/IN-AE-BS_Server/Cnt_reqData_NodeC";

	private static String csePoa = cseProtocol + "://" + cseIp + ":" + csePort;
	private static String appPoa = aeProtocol + "://" + aeIp + ":" + aePort;

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

		// initiate BS_server
		JSONArray array = new JSONArray();
		array.put(appPoa);
		JSONObject obj = new JSONObject();
		obj.put("rn", aeNameBS);
		obj.put("api", 12346);
		obj.put("rr", true);
		obj.put("poa", array);
		JSONObject resource = new JSONObject();
		resource.put("m2m:ae", obj);
		RestHttpClient.post(originator, csePoa + "/~/" + cseId + "/" + cseName, resource.toString(), 2);

		for (int j = 0; j < 3; j++) {

			// initiate Cnt_reqData_NodeA,B,C
			obj = new JSONObject();
			obj.put("rn", CntRequestData[j]);
			resource = new JSONObject();
			resource.put("m2m:cnt", obj);
			RestHttpClient.post(originator, csePoa + "/~/" + cseId + "/" + cseName + "/" + aeNameBS,
					resource.toString(), 3);

			// initiate Cnt_Data_NodeA,B,C
			obj = new JSONObject();
			obj.put("rn", cntNameData[j]);
			resource = new JSONObject();
			resource.put("m2m:cnt", obj);
			RestHttpClient.post(originator, csePoa + "/~/" + cseId + "/" + cseName + "/" + aeNameBS,
					resource.toString(), 3);
//		}
//
//		try {
//			Thread.sleep(4000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//
//		for (int j = 0; j < 3; j++) {
			// BS_Server Sub to Cnt-Service-req Node A,B,C
			array = new JSONArray();
			array.put("/" + cseId + "/" + cseName + "/" + aeNameBS);
			obj = new JSONObject();
			obj.put("nu", array);
			obj.put("rn", subName);
			obj.put("nct", 2);
			resource = new JSONObject();
			resource.put("m2m:sub", obj);
			while (RestHttpClient.post1(originator, csePoa + "/~/" + targetMN + "/" + AENameNode[j] + "/" + CntName[1],
					resource.toString(), 23).getStatusCode() != 201) {
				RestHttpClient.post(originator, csePoa + "/~/" + targetMN + "/" + AENameNode[j] + "/" + CntName[1],
						resource.toString(), 23);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// BS_Server SUB to Cnt_Data_NodeA,B,C
			while (RestHttpClient.post1(originator, csePoa + "/~/" + targetIN + "/" + aeNameBS + "/" + cntNameData[j],
					resource.toString(), 23).getStatusCode() != 201) {
				RestHttpClient.post(originator, csePoa + "/~/" + targetIN + "/" + aeNameBS + "/" + cntNameData[j],
						resource.toString(), 23);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

	}

	static class MyHandler implements HttpHandler {

		public void handle(HttpExchange httpExchange) {
			System.out.println("Event Recieved!");

			try {
				InputStream in = httpExchange.getRequestBody();

				String requestBody = "";
				int i;
				char c;
				while ((i = in.read()) != -1) {
					c = (char) i;
					requestBody = (String) (requestBody + c);
				}

				System.out.println(requestBody);

				JSONObject json = new JSONObject(requestBody);

				if (json.getJSONObject("m2m:sgn").has("m2m:vrq")) {
					System.out.println("Confirm subscription");
				} else {
					JSONObject rep = json.getJSONObject("m2m:sgn").getJSONObject("m2m:nev").getJSONObject("m2m:rep")
							.getJSONObject("m2m:cin");

					String con = rep.getString("con");
					JSONObject conJSONObject = new JSONObject(con);

					// check node A
					if (conJSONObject.getString("Service").equals("get sum")
							&& conJSONObject.getString("DataSource").equals("BC")) {
						JSONObject obj = new JSONObject();
						JSONObject content = new JSONObject();
						content.put("Service", "get");
						content.put("DataSource", "B");
						content.put("ID", conJSONObject.getInt("ID"));
						obj = new JSONObject();
						obj.put("cnf", "application/textNodeB");
						obj.put("con", content.toString());
						obj.put("rn", "cin-req-data_" + conJSONObject.getInt("ID"));
						JSONObject resource = new JSONObject();
						resource.put("m2m:cin", obj);
						RestHttpClient.post(originator, csePoa + "/~/" + targetNodeBContainer, resource.toString(), 4);
						
						content = new JSONObject();
						content.put("Service", "get");
						content.put("DataSource", "C");
						content.put("ID", conJSONObject.getInt("ID"));
						obj = new JSONObject();
						obj.put("cnf", "application/textNodeC");
						obj.put("con", content.toString());
						obj.put("rn", "cin-req-data_" + conJSONObject.getInt("ID"));
						resource = new JSONObject();
						resource.put("m2m:cin", obj);
						RestHttpClient.post(originator, csePoa + "/~/" + targetNodeCContainer, resource.toString(), 4);
					}

					// sum
					if (conJSONObject.getString("Service").equals("return result")) {
						if (conJSONObject.getInt("ID") == ID) {
							if ( Count <= 2) {
								Sum += conJSONObject.getInt("Value");
								Count++;
							} 
							if(Count > 2){
								JSONObject content = new JSONObject();
								content.put("Service", "return result");
								content.put("From", "BS_server");
								content.put("Value", Sum);
								content.put("ID", ID);
								JSONObject obj = new JSONObject();
								obj.put("cnf", "application/text");
								obj.put("con", content.toString());
								obj.put("rn", "cin-serv-resp_" + ID);
								JSONObject resource = new JSONObject();
								resource.put("m2m:cin", obj);
								RestHttpClient.post(originator,
										csePoa + "/~/" + targetMN + "/" + AENameNode[0] + "/" + CntName[2],
										resource.toString(), 4);
								Sum = 0;
								Count = 1;
								ID++;
							}
						} else {

							JSONObject content = new JSONObject();
							content.put("Service", "break");
							content.put("From", "BS_server");
							content.put("Value", 0);
							content.put("ID", 0);
							JSONObject obj = new JSONObject();
							obj.put("cnf", "application/text");
							obj.put("con", content.toString());
							obj.put("rn", "cin-serv-resp_" + conJSONObject.getInt("ID"));
							JSONObject resource = new JSONObject();
							resource.put("m2m:cin", obj);
							RestHttpClient.post(originator,
									csePoa + "/~/" + targetMN + "/" + AENameNode[0] + "/" + CntName[2],
									resource.toString(), 4);

						}
					}
				}
				String responseBudy = "";
				byte[] out = responseBudy.getBytes("UTF-8");
				httpExchange.sendResponseHeaders(200, out.length);
				OutputStream os = httpExchange.getResponseBody();
				os.write(out);
				os.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}