package tops.main;

import java.io.*;

import tops.struct.*;
import net.rudp.*;

public class Client_LoginServer {
	int MS_SendPortNumber = 8089; //13339
	
	//int _MS_SendPortNumber = 8081;
	Message msg = new Message();
	

	public void sendMSGtoLoginServer(String message) {	
		
		ReliableSocketOutputStream outputStream = null;
		try {
			outputStream = (ReliableSocketOutputStream) Client.mainSocket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    PrintWriter outputBuffer = new PrintWriter(outputStream);
	 
	    outputBuffer.println(message);
	    outputBuffer.flush();
    }

	public void ConnectToMainServer() {
		if(Client.connectToMainServer == true){
			return;
		}
		Client.Imbusy = true;
		try {   		
			
			String freindIds = FreindList.getEntireFreindList();	
				sendMSGtoLoginServer(msg.ConnectionAlertMSGforMainServer(freindIds)); 	

				Server.confirm = false;
				
			Client.connectToMainServer = true;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Client.Imbusy = false;
	}
	
	public void UnconnectToMainServer() {
    	try {		
    		String freindIds = FreindList.getEntireFreindList();	
    		sendMSGtoLoginServer(msg.UnconnectionAlertMSGforMainServer(freindIds));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	
}
