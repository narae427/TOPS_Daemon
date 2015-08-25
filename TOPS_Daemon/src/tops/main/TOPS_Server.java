package tops.main;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import java.util.regex.*;

import tops.struct.*;


public class TOPS_Server implements Runnable{
	Socket sock = null;
	String line = null;
	OutputStream out = null;
	InputStream in = null;
    BufferedReader br = null;
	public static PrintWriter pw = null;
	
	public static int ServerPN = 0;
	public TOPS_Server(){
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		ServerSocket server;
		try {
			ServerPN = (int) (Math.random()*10010+10000);
			server = new ServerSocket(ServerPN);
			sock = server.accept();
			out = sock.getOutputStream();
			in = sock.getInputStream();
			
			pw = new PrintWriter(new OutputStreamWriter(out));
			br = new BufferedReader(new InputStreamReader(in));
			
			line = null;
			while(true){
				line = br.readLine();
				if(line == null) continue;
				Message msg = new Message();
				msg.getPatternfromMSG(line);
				
				if(msg.commandMessage.equals("dm_Login")){
					TOPS_Daemon dm = new TOPS_Daemon();
					dm.initialize(msg.idMessage);
					Client_LoginServer MMS = new Client_LoginServer();
					try {
						MMS.ConnectToMainServer();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else if(msg.commandMessage.equals("dm_Logout")){
					try {
						Client_LoginServer MMS = new Client_LoginServer();
						MMS.UnconnectToMainServer();
						Thread.sleep(5000);
						System.exit(0);
					} catch (Exception ee) {
						System.exit(1);
					}
				}else if(msg.commandMessage.equals("dm_AddFriend")){
						try {
							Client_LoginServer MMS = new Client_LoginServer();
							MMS.sendMSGtoLoginServer(msg.RequestAddFreindMSG(TOPS_Daemon.myID,	msg.fidMessage));

						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}else if(msg.commandMessage.equals("dm_AllowFriend")){
						Client_LoginServer MMS = new Client_LoginServer();
						MMS.sendMSGtoLoginServer(msg.AllowAddFreindMSG(TOPS_Daemon.myID,	msg.fidMessage));
					}else if(msg.commandMessage.equals("dm_PushData")){
						
						Hashtable<String, Integer> tempIDM = new Hashtable<String, Integer>();
						if(TOPS_Daemon.IDM.get(TOPS_Daemon.myID) == null || TOPS_Daemon.IDM.get(TOPS_Daemon.myID).get(TOPS_Daemon.myID) == null) tempIDM.put(TOPS_Daemon.myID, 1);
						else	tempIDM.put(TOPS_Daemon.myID, TOPS_Daemon.IDM.get(TOPS_Daemon.myID).get(TOPS_Daemon.myID)+1);
						TOPS_Daemon.IDM.put(TOPS_Daemon.myID, tempIDM);
						
						Client.CallPushData(msg.fnameMessage);
					}else if(msg.commandMessage.equals("dm_AdvUpdate")){
						Client.CallAdvertisement_UPDATE();
					}else if(msg.commandMessage.equals("dm_CmnFriend")){
						BFilter.calcCommonFriend(msg.fidMessage);
					}
				}
//			pw.close();
//			br.close();
//			sock.close();
//			System.out.println("¼ÒÄÏ´ÝÈû");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidKeySpecException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
	}
	public static void sendMessage(String line) {
		pw.println(line);
		pw.flush();

	}
}
