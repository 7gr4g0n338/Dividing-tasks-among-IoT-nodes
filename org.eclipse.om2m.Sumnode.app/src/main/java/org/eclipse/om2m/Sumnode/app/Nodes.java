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

public class Nodes {

	private static String originator = "admin:admin";
	private static String cseProtocol = "http";
	private static String cseIp = "127.0.0.1";
	private static int csePort = 8282;
	private static String cseId = "mn-cse";
	private static String cseName = "MN-CSE-Manager";
	private static String targetIN = "in-cse/IN-CSE-BS_Server";

	private static String aeNameBS = "IN-AE-BS_Server";
	private static String aeName = "MN-AE-Manager";

	private static String[] AENameNode = { "AE-nodeA", "AE-nodeB", "AE-nodeC" };
	private static String[] CntNameData = { "Cnt_Data_NodeA", "Cnt_Data_NodeB", "Cnt_Data_NodeC" };
	private static String[] CntName = { "Cnt_Data", "Cnt_SERVICE_req", "Cnt_SERVICE_resp" };
	private static String[] CntRequestData = { "Cnt_reqData_NodeA", "Cnt_reqData_NodeB", "Cnt_reqData_NodeC" };

	private static String aeProtocol = "http";
	private static String aeIp = "127.0.0.1";
	private static int aePort = 1400;
	private static String subName = "Manager_Sub";

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

		// tao MN-AE
		JSONArray array = new JSONArray();
		array.put(appPoa);
		JSONObject obj = new JSONObject();
		obj.put("rn", aeName);
		obj.put("api", 12345);// appicaiton ID
		obj.put("rr", true);// Request Reachability
		obj.put("poa", array);// Point of Access
		JSONObject resource = new JSONObject();
		resource.put("m2m:ae", obj);
		RestHttpClient.post(originator, csePoa + "/~/" + cseId + "/" + cseName, resource.toString(), 2);

		for (int i = 0; i < 3; i++) {
			// initiate Node A,B,C
			obj = new JSONObject();
			obj.put("rn", AENameNode[i]);
			obj.put("api", 12345);// appicaiton ID
			obj.put("rr", true);// Request Reachability
			obj.put("poa", array);// Point of Access
			resource = new JSONObject();
			resource.put("m2m:ae", obj);
			RestHttpClient.post(originator, csePoa + "/~/" + cseId + "/" + cseName, resource.toString(), 2);

			// initiate CNT
			for (int j = 0; j < 3; j++) {
				obj = new JSONObject();
				obj.put("rn", CntName[j]);
				resource = new JSONObject();
				resource.put("m2m:cnt", obj);
				RestHttpClient.post(originator, csePoa + "/~/" + cseId + "/" + cseName + "/" + AENameNode[i],
						resource.toString(), 3);
			}

			// Manager Sub to Cnt-reqData Node A,B,C
			array = new JSONArray();
			array.put("/" + cseId + "/" + cseName + "/" + aeName);
			obj = new JSONObject();
			obj.put("nu", array);
			obj.put("rn", subName);
			obj.put("nct", 2);
			resource = new JSONObject();
			resource.put("m2m:sub", obj);
			while (RestHttpClient.post1(originator,
					csePoa + "/~/" + targetIN + "/" + aeNameBS + "/" + CntRequestData[i], resource.toString(), 23)
					.getStatusCode() != 201) {
				RestHttpClient.post(originator, csePoa + "/~/" + targetIN + "/" + aeNameBS + "/" + CntRequestData[i],
						resource.toString(), 23);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// bo phan xu li thong tin nhan duoc
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
				if (json.getJSONObject("m2m:sgn").has("m2m:vrq")) {// vrq verificationRequest
					System.out.println("Confirm subscription");

				} else {
					JSONObject rep = json.getJSONObject("m2m:sgn").getJSONObject("m2m:nev").getJSONObject("m2m:rep")
							.getJSONObject("m2m:cin");
					String con = rep.getString("con");
					JSONObject conJSONObject = new JSONObject(con);

					// xem co phai command yeu cau gui du lieu node B cho BS khong
					if (conJSONObject.getString("Service").equals("get")
							&& conJSONObject.getString("DataSource").equals("B")) {
						JSONObject getBody = new JSONObject(RestHttpClient.get(originator,
								csePoa + "/~/" + cseId + "/" + cseName + "/" + AENameNode[1] + "/" + CntName[0] + "/la")
								.getBody());
						System.out.println("=================>" + getBody.getJSONObject("m2m:cin").getInt("con"));
						JSONObject content = new JSONObject();
						content.put("Service", "return result");
						content.put("From", "B");
						content.put("Value", getBody.getJSONObject("m2m:cin").getInt("con"));
						content.put("ID", conJSONObject.getInt("ID"));
						JSONObject obj = new JSONObject();
						obj.put("cnf", "application/textNodeB");
						obj.put("con", content.toString());
						obj.put("rn", "cin-data_" + getBody.getJSONObject("m2m:cin").getInt("con"));
						JSONObject resource = new JSONObject();
						resource.put("m2m:cin", obj);
						RestHttpClient.post(originator,
								csePoa + "/~/" + targetIN + "/" + aeNameBS + "/" + CntNameData[1], resource.toString(),
								4);
					}

					// xem co phai command yeu cau gui du lieu node C cho BS khong
					if (conJSONObject.getString("Service").equals("get")
							&& conJSONObject.getString("DataSource").equals("C")) {
						JSONObject getBody = new JSONObject(RestHttpClient.get(originator,
								csePoa + "/~/" + cseId + "/" + cseName + "/" + AENameNode[2] + "/" + CntName[0] + "/la")
								.getBody());
						System.out.println("=================>" + getBody.getJSONObject("m2m:cin").getInt("con"));
						JSONObject content = new JSONObject();
						content.put("Service", "return result");
						content.put("From", "C");
						content.put("Value", getBody.getJSONObject("m2m:cin").getInt("con"));
						content.put("ID", conJSONObject.getInt("ID"));
						JSONObject obj = new JSONObject();
						obj.put("cnf", "application/textNodeC");
						obj.put("con", content.toString());
						obj.put("rn", "cin-data_" + getBody.getJSONObject("m2m:cin").getInt("con"));
						JSONObject resource = new JSONObject();
						resource.put("m2m:cin", obj);
						RestHttpClient.post(originator,
								csePoa + "/~/" + targetIN + "/" + aeNameBS + "/" + CntNameData[2], resource.toString(),
								4);
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
