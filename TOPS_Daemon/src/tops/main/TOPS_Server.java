package tops.main;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;

import tops.struct.*;

public class TOPS_Server implements Runnable {
	public static Socket sock = null;
	String line = null;
	OutputStream out = null;
	InputStream in = null;
	BufferedReader br = null;
	public static PrintWriter pw = null;
	ServerSocket server;
	public static int ServerPN = 0; //GUI
	public static int FilePN = 0;//GUI
	public static ServerSocket dmServerSocket = null;
	public static ServerSocket dmFileSocket = null;
	TOPS_Server tops_server = null;

	public TOPS_Server(TOPS_Server tops_server) throws IOException {
		this.tops_server = tops_server;
//		ServerPN = 9626;
//		FilePN = 9262;
//		
//		System.out.println("Daemon_ServerPN " + ServerPN);
//		server = new ServerSocket(ServerPN);
	}

	@Override
	public void run() {
	//	while (true) {
			// TODO Auto-generated method stub

			try {
				 System.out.println("--Daemon_ServerPN " + ServerPN);
				 System.out.println("--Daemon_FilePN " + FilePN);
				System.out.println("Daemon RESTART");
				
				sock = server.accept();
				out = sock.getOutputStream();
				in = sock.getInputStream();

				pw = new PrintWriter(new OutputStreamWriter(out));
				br = new BufferedReader(new InputStreamReader(in));

				line = null;
				while (true) {
					line = br.readLine();
					if (line == null)
						continue;
					System.out.println("LINE : " + line);
					Message msg = new Message();
					PM pm = new PM();
					msg.getPatternfromMSG(line, pm);

					if (pm.commandMessage.equals("dm_Login")) {
						TOPS_Daemon dm = new TOPS_Daemon();
						dm.initialize(pm.idMessage);
						Client_LoginServer MMS = new Client_LoginServer();
						try {
							MMS.ConnectToMainServer();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} else if (pm.commandMessage.equals("dm_Logout")) {
						Client_LoginServer MMS = new Client_LoginServer();
						MMS.UnconnectToMainServer();
						break;
					} else if (pm.commandMessage.equals("dm_AddFriend")) {
						try {
							Client_LoginServer MMS = new Client_LoginServer();
							MMS.sendMSGtoLoginServer(msg.RequestAddFreindMSG(
									TOPS_Daemon.myID, pm.fidMessage));

						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} else if (pm.commandMessage.equals("dm_AllowFriend")) {
						Client_LoginServer MMS = new Client_LoginServer();
						MMS.sendMSGtoLoginServer(msg.AllowAddFreindMSG(
								TOPS_Daemon.myID, pm.fidMessage));
					} else if (pm.commandMessage.equals("dm_PushData")) {

						sendMessage("'dm_Sync'");

						TOPS_Sync sync = new TOPS_Sync();
						sync.DoSynchronize_Recieve();

						Hashtable<String, Integer> tempIDM = new Hashtable<String, Integer>();
						if (TOPS_Daemon.IDM.get(TOPS_Daemon.myID) == null
								|| TOPS_Daemon.IDM.get(TOPS_Daemon.myID).get(
										TOPS_Daemon.myID) == null)
							tempIDM.put(TOPS_Daemon.myID, 1);
						else
							tempIDM.put(
									TOPS_Daemon.myID,
									TOPS_Daemon.IDM.get(TOPS_Daemon.myID).get(
											TOPS_Daemon.myID) + 1);
						TOPS_Daemon.IDM.put(TOPS_Daemon.myID, tempIDM);

						Client.CallPushData(pm.fnameMessage);
					} else if (pm.commandMessage.equals("dm_AdvUpdate")) {
						Client.CallAdvertisement_UPDATE();
					} else if (pm.commandMessage.equals("dm_CmnFriend")) {
						BFilter.calcCommonFriend(pm.fidMessage);
					} else if (pm.commandMessage.equals("dm_Sync")) {

						TOPS_Sync sync = new TOPS_Sync();
						try {
							sync.DoSynchronize_Send();
						} catch (IOException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
						sendMessage("'dm_Sync'");

						sync = new TOPS_Sync();
						sync.DoSynchronize_Recieve();

					}
				}

				Thread.sleep(10000);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvalidKeySpecException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	//}

	public static void sendMessage(String line) {
		pw.println(line);
		pw.flush();

	}
}
