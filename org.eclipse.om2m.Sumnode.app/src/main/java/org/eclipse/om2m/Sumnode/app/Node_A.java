package org.eclipse.om2m.Sumnode.app;


import org.json.JSONObject;

public class Node_A {
 
	public static int ID = 0;
	private static String originator="admin:admin";
	private static String cseProtocol="http";
	private static String cseIp = "127.0.0.1";
	private static int csePort = 8282;
	private static String cseId = "mn-cse";
	private static String cseName = "MN-CSE-Manager";
 
	private static String aeName = "MN-AE-Manager";
	private static String cntNameNodeA = "AE-nodeA";
	private static String cntNameNodeB = "AE-nodeB";
	private static String cntNameNodeC = "AE-nodeC";
	private static String cntNameData = "Cnt_Data";
	private static String cntNameResp = "Cnt_SERVICE_resp";
	private static String cntNameCommand = "Cnt_SERVICE_req";
 
	private static String csePoa = cseProtocol+"://"+cseIp+":"+csePort;
 
	public static void main(String[] args) {
        
		JSONObject obj = new JSONObject();
        JSONObject resource = new JSONObject();
        obj.put("rn", cntNameResp);
        resource = new JSONObject();
        resource.put("m2m:cnt", obj);
        RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeA, resource.toString(), 3);
        
        obj = new JSONObject();
        obj.put("rn", cntNameCommand);
        resource = new JSONObject();
        resource.put("m2m:cnt", obj);
        RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeA, resource.toString(), 3);
        
        obj = new JSONObject();
        obj.put("rn", cntNameData);
        resource = new JSONObject();
        resource.put("m2m:cnt", obj);
        RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeA, resource.toString(), 3);
        
        try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
              
	while (true){
		int c=0;
		
		c=getAValue();
		obj = new JSONObject();
		obj.put("cnf", "application/textNodeA");
		obj.put("con", c);
		obj.put("lbl", "NodeA");
		obj.put("rn", "cin-data_"+c);
		resource = new JSONObject();
		resource.put("m2m:cin", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeA+"/"+cntNameData, resource.toString(), 4);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		c=getBValue();
		obj = new JSONObject();
		obj.put("cnf", "application/textNodeB");
		obj.put("con", c);
		obj.put("lbl", "NodeB");
		obj.put("rn", "cin-data_"+c);
		resource = new JSONObject();
		resource.put("m2m:cin", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeB+"/"+cntNameData, resource.toString(), 4);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		c=getCValue();
		obj = new JSONObject();
		obj.put("cnf", "application/textNodeC");
		obj.put("con", c);
		obj.put("lbl", "NodeC");
		obj.put("rn", "cin-data_"+c);
		resource = new JSONObject();
		resource.put("m2m:cin", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeC+"/"+cntNameData, resource.toString(), 4);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		c=getID();
		JSONObject content = new JSONObject();
		content.put("Service", "get sum");
		content.put("DataSource", "BC");
		content.put("ID", c );
		obj = new JSONObject();
		obj.put("cnf", "application/textNodeA");
		obj.put("con", content.toString());
		obj.put("lbl", "NodeA");
		obj.put("rn", "cin-serv-req_"+c);
		resource = new JSONObject();
		resource.put("m2m:cin", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+cntNameNodeA+"/"+cntNameCommand, resource.toString(), 4);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}
 
	public static int getID(){
		return ID++;
	}
	
	public static int getAValue(){
		int NodeA = (int)(Math.random()*50);
		System.out.println("value A = "+NodeA);
		return NodeA;
	}
	
	public static int getBValue(){
		int NodeB = (int)(Math.random()*50);
		System.out.println("value B = "+NodeB);
		return NodeB;
	}
	
	public static int getCValue(){
		int NodeC = (int)(Math.random()*50);
		System.out.println("value C = "+NodeC);
		return NodeC;
	}
}
