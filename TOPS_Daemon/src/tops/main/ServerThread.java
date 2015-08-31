package tops.main;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.StringTokenizer;
import java.util.logging.*;

import tops.struct.*;

public class ServerThread implements Runnable {

	boolean open = false;

	UpdateFiles UF = null;
	DataOutputStream dos = null;
	DataInputStream dis = null;
	Message msg = null;
	Client_NodeThread MNT;
	Socket socket = null;
	static DatagramSocket ds = null;

	String message = "";
	String freindIds = null;

	Logger logger = Logger.getLogger(getClass().getName());

	public ServerThread(String message) throws IOException {
		this.msg = new Message();
		this.message = message;
	}

	private void printMSG(String message, String freindId) {
		logger.log(Level.INFO, "[RECEIVE MESSAGE]	" + message + " from "
				+ freindId);
	}

	private boolean checkUpdates(String fileName_s) {
		
		boolean recieved = false;
		
		StringTokenizer st = new StringTokenizer(fileName_s,";");
		String fileName = "";
		while (st.hasMoreTokens()) {
			fileName = st.nextToken();
			String[] fileNameTokens = fileName.split("_");
			final String id = fileNameTokens[0];

			File[] myUpdateFile = null;
			File myUpdateFilePath = new File(TOPS_Daemon.myFolderPath
					+ System.getProperty("file.separator") + "UpdateFile");
			myUpdateFile = myUpdateFilePath.listFiles(new FilenameFilter() { 

						@Override
						public boolean accept(File dir, String name) {
							// TODO Auto-generated method stub
							return name.startsWith(id + "_")
									&& name.contains("UpdateFile");
						}

					});

			if (myUpdateFile == null || myUpdateFile.length == 0) {
				myUpdateFile = new File[1];
				myUpdateFile[0] = new File(myUpdateFilePath
						+ System.getProperty("file.separator") + id + "_"
						+ "UpdateFile" + "_" + String.valueOf(0));
			}
			String temp = id + "_" + "UpdateFile" + "_";
			int oldVer = Integer.valueOf(myUpdateFile[0].getName().substring(
					temp.length()));
			TOPS_Daemon.freindVerHT.put(id, oldVer);

			int newVer = Integer.valueOf(fileName.substring(temp.length()));

			if (oldVer >= newVer) {
				recieved = true;
			} else {
				recieved = false;
				break;
			}
		}
		return recieved;
	}

	private boolean checkData(String fileName) {
		String[] fileNameTokens = fileName.split("_");
		final String id = fileNameTokens[0];

		File[] myUpdateFile = null;
		File myUpdateFilePath = new File(TOPS_Daemon.myFolderPath
				+ System.getProperty("file.separator") + "UpdateFile");
		
		myUpdateFile = myUpdateFilePath.listFiles(new FilenameFilter() { 

					@Override
					public boolean accept(File dir, String name) {
						// TODO Auto-generated method stub
						return name.startsWith(id + "_")
								&& name.contains("UpdateFile");
					}

				});

		if (myUpdateFile == null || myUpdateFile.length == 0) {
			myUpdateFile = new File[1];
			myUpdateFile[0] = new File(myUpdateFilePath
					+ System.getProperty("file.separator") + id + "_"
					+ "UpdateFile" + "_" + String.valueOf(0));
		}
		String temp = id + "_" + "UpdateFile" + "_";
		int oldVer = Integer.valueOf(myUpdateFile[0].getName().substring(
				temp.length()));
		TOPS_Daemon.freindVerHT.put(id, oldVer);

		int newVer = Integer.valueOf(fileName.substring(temp.length()));

		boolean recieved = false;

		if (oldVer >= newVer) {
			recieved = true;
		} else {
			recieved = false;
		}

		return recieved;
	}

	// SendFiles -> RecieveFiles
	@SuppressWarnings("unchecked")
	public void run() {

		if (message.equals("OPEN")) {
			open = true;
		}
		PM pm = new PM();
		msg.getPatternfromMSG(message, pm);

		Client_LoginServer MMS = new Client_LoginServer();

		FreindNode fnode = FreindList.freindList.get(pm.idMessage);
		String MS_DoneMSG = "";

		MessageType type = MessageType.valueOf(pm.commandMessage);
		int messageType = type.ordinal();
		switch (messageType) {
		case 0: // "PublicKey"

			fnode = FreindList.freindList.get(pm.idMessage);

			printMSG(pm.commandMessage, fnode.freindID);

//			if (fnode.pubMod != null && fnode.pubExp != null)
//				break;
			fnode.pubMod = new BigInteger(pm.modMessage);
			fnode.pubExp = new BigInteger(pm.expMessage);

			fnode.keyRecieveChk = true;
			
			Client.CallAdvertisement_BloomFilter();
			if (!fnode.keyRecieveChk || !fnode.keySendChk) {
				try {
					Client.CallExchangePublicKey(fnode);
				} catch (IOException e1) {
					e1.printStackTrace();
				} 
			} 

			break;
		case 1: // "Advertisement_Login1"
			fnode = FreindList.freindList.get(pm.idMessage);

			printMSG(pm.commandMessage, fnode.freindID);

			try {
				MNT = new Client_NodeThread(fnode);
				MNT.readyForReceiveUpdateFile_UPDATE();
				System.out.println("Ready For Recieve Update File");
			} catch (Exception e3) {
				e3.printStackTrace();
			}

			try {
				Client.sendMSG(fnode, msg.Advertisement_Login2(), true);
			} catch (IOException e6) {
				e6.printStackTrace();
			}

//			Client.CallAdvertisement_BloomFilter();
			break;

		case 2: // "Advertisement_Login2"
			fnode = FreindList.freindList.get(pm.idMessage);

			printMSG(pm.commandMessage, fnode.freindID);

			try {
				MNT = new Client_NodeThread(fnode);
				MNT.readyForReceiveUpdateFile_UPDATE();
			} catch (Exception e) {
				e.printStackTrace();
			}
//			Client.CallAdvertisement_BloomFilter();
			break;

		case 3: // "Request_Updates"
			fnode = FreindList.freindList.get(pm.idMessage);

			printMSG(pm.commandMessage, fnode.freindID);

			Socket socket = null;
			try {
				socket = Server.fileSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
			UF = new UpdateFiles(socket);
			try {
				UF.SendUpdateFiles(MessageType.Request_Updates, fnode, null);
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			break;

		case 4: // "Request_Data"

			fnode = FreindList.freindList.get(pm.idMessage);

			printMSG(pm.commandMessage, fnode.freindID);

			try {
				socket = Server.fileSocket.accept();

				UF = new UpdateFiles(socket);
				UF.SendUpdateFiles(MessageType.Request_Data, fnode,
						pm.fidMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}

			break;

		case 5: // "Updates"

			fnode = FreindList.freindList.get(pm.idMessage);

			Client.CallAdvertisement_UPDATE();

			break;

		case 6: // "Data"

			fnode = FreindList.freindList.get(pm.idMessage);

			printMSG(pm.commandMessage, fnode.freindID);
			System.out.println("Data MSG :  " + message);

			Client.CallAdvertisement_DATA(pm.fidMessage, pm.fnameMessage);

			break;
		case 7: // "Advertisement_Update"
			if (checkUpdates(pm.fnameMessage)){
				System.out.println(" BREAK !!!");
				break;
			}
				
			printMSG(pm.commandMessage, fnode.freindID);

			try {
				MNT = new Client_NodeThread(fnode);
				MNT.readyForReceiveUpdateFile_UPDATE();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		case 8: // "Advertisement_Data":
			printMSG(pm.commandMessage, fnode.freindID);
			fnode = FreindList.freindList.get(pm.idMessage);

			if (checkData(pm.fnameMessage)) {
				System.out.println(" BREAK !!!");
				break;
			}

			try {
				MNT = new Client_NodeThread(fnode);
				MNT.readyForReceiveUpdateFile_DATA(pm.fidMessage);
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;
		case 9: // "Push":
			printMSG(pm.commandMessage, fnode.freindID);
			fnode = FreindList.freindList.get(pm.idMessage);

			try {
				MNT = new Client_NodeThread(fnode);
				MNT.readyForReceiveUpdateFile_DATA(fnode.freindID);
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		case 10: // "Request_File":
			fnode = FreindList.freindList.get(pm.idMessage);

			printMSG(pm.commandMessage, fnode.freindID);

			try {
				socket = Server.fileSocket.accept();
				UF = new UpdateFiles(socket);
				UF.SendFile(pm.fnameMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;

		case 11: // "MS_ConfirmConnection":

			TOPS_Daemon.myPublicIP = pm.pbipMessage;
			TOPS_Daemon.myPublicPN = Integer.valueOf(pm.pbpnMessage);

			printMSG(pm.commandMessage, "LoginServer");

			try {
				freindIds = FreindList.getEntireFreindList();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (freindIds != null)
				try {
					MMS.sendMSGtoLoginServer(msg
							.CheckOnlineFreindsMSG(freindIds));
				} catch (Exception e) {
					e.printStackTrace();
				}

			break;

		case 12: // "Ask_Connection":
			fnode = FreindList.freindList.get(pm.idMessage);

			printMSG(pm.commandMessage, fnode.freindID);

			if (fnode != null) { 
				try {
					Client.CallConnectToFreindNode(fnode);
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

			break;

		case 13: // "MS_NoticeOnlineFreind":
			printMSG(pm.commandMessage, "LoginServer");

			System.out.println("NOTICE ONLINE " + message + " "
					+ pm.fidMessage);
			try {
				fnode = new FreindNode(pm.fidMessage, pm.pvipMessage,
						Integer.valueOf(pm.pvpnMessage), pm.pbipMessage,
						Integer.valueOf(pm.pbpnMessage), null, null, null);
				FreindList.putOnlineFreind(pm.fidMessage, fnode);
			} catch (Exception e) {
			}

			break;

		case 14: // "MS_NoticeOfflineFreind":
			printMSG(pm.commandMessage, "LoginServer");
			try {
				FreindList.removeOfflineFreind(pm.fidMessage);
			} catch (Exception e) {
			}
			break;

		case 15: // "MS_ResultOfCheckOnlineFreind":
			printMSG(pm.commandMessage, "LoginServer");

			if (message.contains("OFFLINE")) {
				break;
			}

			if (!message.contains("OFFLINE")) {

				FreindNode newfnode = new FreindNode(pm.fidMessage,
						pm.pvipMessage, Integer.valueOf(pm.pvpnMessage),
						pm.pbipMessage, Integer.valueOf(pm.pbpnMessage),
						null, null, null);

				try {
					FreindList.putOnlineFreind(pm.fidMessage, newfnode);
				} catch (NumberFormatException | IOException e) {
					e.printStackTrace();
				} 

			}

			fnode = FreindList.freindList.get(pm.fidMessage);
			if (fnode != null) { 
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					Client.CallExchangePublicKey(fnode);
//					Client.CallAdvertisement_BloomFilter();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 

			}

			try {
				freindIds = FreindList.getEntireFreindList();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MS_DoneMSG = "'MS_Done'" + "!" + TOPS_Daemon.myID + "!" + "@" + freindIds
					+ "@";
			try {
				MMS.sendMSGtoLoginServer(MS_DoneMSG);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;

		case 16: // "MS_ResultOfCheckOnlineFreinds":
			printMSG(pm.commandMessage, "LoginServer");

			StringTokenizer st = new StringTokenizer(pm.freindsInfoMessage,
					";");
			String freindInfo = "";
			while (st.hasMoreTokens()) {
				freindInfo = st.nextToken();
				System.out.println("Info : " + freindInfo);
				if (!freindInfo.contains("OFFLINE")) {
					msg.getPatternfromMSG(freindInfo, pm);
					System.out.println("msg.fi_pvpnMessage : "
							+ pm.fidMessage + " " + pm.pvipMessage + " "
							+ pm.pvpnMessage + " " + pm.pbipMessage
							+ " " + pm.pbpnMessage);
					FreindNode newfnode = new FreindNode(pm.fidMessage,
							pm.pvipMessage,
							Integer.valueOf(pm.pvpnMessage),
							pm.pbipMessage,
							Integer.valueOf(pm.pbpnMessage), null, null,
							null);
					try {
						FreindList.putOnlineFreind(pm.fidMessage, newfnode);
					} catch (NumberFormatException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 

				}
			}

			if (FreindList.freindList.size() > 0) {
				try {
					Client.CallExchangePublicKey();
//					Client.CallAdvertisement_BloomFilter();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}

			try {
				freindIds = FreindList.getEntireFreindList();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MS_DoneMSG = "'MS_Done'" + "!" + TOPS_Daemon.myID + "!" + "@" + freindIds
					+ "@";
			try {
				MMS.sendMSGtoLoginServer(MS_DoneMSG);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;

		case 17: // "MS_RequestAddFreind":

			TOPS_Server.sendMessage("'dm_RequestAddFriend'"+"!" + pm.idMessage + "!");
			break;

		case 18: // "MS_AllowAddFreind":
			printMSG(pm.commandMessage, "LoginServer");
			String freindId = pm.idMessage;

			boolean exist = false;
			File freindListFile = new File(TOPS_Daemon.myFolderPath
					+ System.getProperty("file.separator") + TOPS_Daemon.myID
					+ "_FreindList");
			LineNumberReader reader;
			if (freindListFile.exists()) {
				try {
					reader = new LineNumberReader(
							new FileReader(freindListFile));
					while (true) {
						String str = reader.readLine();
						if (str == null) {
							break;
						}
						if (str.equals(freindId)) {
							exist = true;
							System.out.println(" str equals " + freindId);
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 

			}
			if (!exist) {
				FileWriter fw;
				try {
					fw = new FileWriter(freindListFile, true); 	// AllowAddFreind�� ���ʿ���.
					fw.write(freindId + "\n");
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			TOPS_Server.sendMessage("'dm_FriendUpdate'");

			TOPS_Daemon.bloomFilter.put(freindId);
			try {
				MMS.sendMSGtoLoginServer(msg.CheckOnlineFreindMSG(freindId));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		case 19: //BloomFilter
			printMSG(pm.commandMessage, pm.idMessage);
			try {
				MNT = new Client_NodeThread(fnode);
				MNT.readyForReceive_BloomFilter(pm.fidMessage);
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;
		case 20: //Request_BloomFilter
			printMSG(pm.commandMessage, pm.idMessage);

			fnode = FreindList.freindList.get(pm.idMessage);

			Socket bf_socket = null;
			try {
				bf_socket = Server.fileSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
			UF = new UpdateFiles(bf_socket);
			try {
				UF.SendBloomFilter(MessageType.Request_BloomFilter, fnode);
				
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			break;
		}
		Thread.currentThread().interrupt();
	}
	
}