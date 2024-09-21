package org.eclipse.om2m.Sumnode.app;


import org.json.JSONObject;

public class Command {
 
	public static int ID = 0;
	private static String originator="admin:admin";
	private static String cseProtocol="http";
	private static String cseIp = "127.0.0.1";
	private static int csePort = 8282;
	private static String cseId = "mn-cse";
	private static String cseName = "MN-CSE-Manager";
 
	private static String []AENameNode = {"AE-nodeA","AE-nodeB","AE-nodeC"};
	private static String cntNameNodeA = "AE-nodeA";
	
	private static String cntNameData = "Cnt_Data";
	private static String cntNameCommand = "Cnt_SERVICE_req";
 
	private static String csePoa = cseProtocol+"://"+cseIp+":"+csePort;
 
	public static void main(String[] args) {
              
	while (true){
		int c=0;
		for(int i=0; i<3; i++) {
			// send data to node
			c=(int)(Math.random()*50);
			JSONObject obj = new JSONObject();
			obj.put("cnf", "application/textNode");
			obj.put("con", c);
			obj.put("lbl", AENameNode[i]);
			obj.put("rn", "cin-data_"+c);
			JSONObject resource = new JSONObject();
			resource.put("m2m:cin", obj);
			RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+AENameNode[i]+"/"+cntNameData, resource.toString(), 4);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}		
		
		// Node A request
		c=getID();
		JSONObject content = new JSONObject();
		content.put("Service", "get sum");
		content.put("DataSource", "BC");
		content.put("ID", c );
		JSONObject obj = new JSONObject();
		obj.put("cnf", "application/textNodeA");
		obj.put("con", content.toString());
		obj.put("lbl", "NodeA");
		obj.put("rn", "cin-serv-req_"+c);
		JSONObject resource = new JSONObject();
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
}
