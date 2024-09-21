package org.eclipse.om2m.Wateringsystem.app;

import org.json.JSONObject;

public class HumSensor {

		public static int HumsensorValue;
	 
		private static String originator="admin:admin";
		private static String cseProtocol="http";
		private static String cseIp = "127.0.0.1";
		private static int csePort = 8282;
		private static String cseId = "mn-cse";
		private static String cseName = "mn-name";
	 
		private static String aeName = "humsensor";
		private static String cntName = "data";
	 
		private static String csePoa = cseProtocol+"://"+cseIp+":"+csePort;
	 
		public static void main(String[] args) {
	 
			JSONObject obj = new JSONObject();
			obj.put("rn", aeName);
			obj.put("api", 12344);
			obj.put("rr", false);
			JSONObject resource = new JSONObject();
			resource.put("m2m:ae", obj);
			RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName, resource.toString(), 2);
	 
		        obj = new JSONObject();
		        obj.put("rn", cntName);
			resource = new JSONObject();
		        resource.put("m2m:cnt", obj);
		     
			RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+aeName, resource.toString(), 3);
			while (true){
	 
				obj = new JSONObject();
				obj.put("cnf", "application/textHum");
				obj.put("con", getSensorValue());
				obj.put("lbl", "Hum");
				resource = new JSONObject();
				resource.put("m2m:cin", obj);
				RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+aeName+"/"+cntName, resource.toString(), 4);
	 
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	 
		public static int getSensorValue(){
			HumsensorValue = 60+(int)(Math.random()*40);
			System.out.println("HumSensor value = "+HumsensorValue);
			return HumsensorValue;
		}

}
