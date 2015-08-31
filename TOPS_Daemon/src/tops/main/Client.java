package tops.main;

import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.logging.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import tops.encryption.*;
import tops.struct.*;
import net.rudp.*;

public class Client extends Thread {

	static Socket socket = null;
	String myFolderPath = TOPS_Daemon.myFolderPath;
	DataOutputStream dos = null;
	DataInputStream dis = null;
	static Message msg = new Message();
	boolean askForConnection = false;
	static boolean connectToMainServer = false;
	static boolean Imbusy = false;

	static ReliableSocket mainSocket = null;

	public Client() throws  IOException {

	}

	public Client(FreindNode freindNode) throws IOException {
		askForConnection = true;
	}

	public boolean CheckIDM(String reciever, String ctor_s){
		StringTokenizer st = new StringTokenizer(ctor_s, ";");
		String ctor = null;
		while(st.hasMoreTokens()){
			ctor = st.nextToken();
			if(reciever == ctor) continue;
			if(TOPS_Daemon.IDM.get(reciever).get(ctor)==null) return true;
//			if(TOPS.IDM.get(TOPS.myID).get(ctor)==null) continue;
			if(TOPS_Daemon.IDM.get(reciever).get(ctor) < TOPS_Daemon.IDM.get(TOPS_Daemon.myID).get(ctor))////////////ERROR
				return true;
		}
		return false;
		
	}

	static public ReliableSocket getSocket(FreindNode node) {
		ReliableSocket rs;
		try {
			rs = new ReliableSocket();

			if (!rs.isConnected()) {
				rs.connect(
						new InetSocketAddress(node.privateIP, node.privatePN),
						10000);
				System.out.println(node.freindID + "에게 PRIVATE으로 Connect");
				return rs;
			}
			if (!rs.isConnected()) {
				rs.connect(new InetSocketAddress(node.publicIP, node.publicPN),
						10000);
				System.out.println(node.freindID + "에게 PUBLIC으로 Connect");
				return rs;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println("Connection FAIL");
		}

		return null;
	}

	static public void sendMSG(FreindNode node, String message, boolean encrypt)
			throws IOException {
		
		if (node.socket == null || node.socket.isClosed()) {
			node.socket = getSocket(node);
		}
		try {
			if (encrypt)
				message = RSAcrypto.encrypt(node.freindID, message); ////////////ERROR
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | InvalidKeySpecException
				| NoSuchProviderException | IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			ReliableSocketOutputStream outputStream = (ReliableSocketOutputStream) node.socket
					.getOutputStream();
			PrintWriter outputBuffer = new PrintWriter(outputStream);

			outputBuffer.println(message);
			outputBuffer.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	static public void CallExchangePublicKey() throws UnknownHostException,
			IOException {
		ExchangePublicKey EP = new ExchangePublicKey();
		EP.start();
	}

	static public void CallExchangePublicKey(FreindNode fNode)
			throws UnknownHostException, IOException {
		ExchangePublicKey EP = new ExchangePublicKey(fNode);
		EP.start();
	}

	static public void CallPushData(String fileName)
			throws UnknownHostException, IOException {

		Advertisement AD = new Advertisement(MessageType.Push, fileName);
		AD.start();
	}

	static public void CallAdvertisement_UPDATE()  {// 접속중인 친구들한테 나한테 접속하라고
							// 메세지보내는 용도
		Advertisement AD;
		
		try {
			AD = new Advertisement(MessageType.Advertisement_Update);
			AD.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static public void CallAdvertisement_DATA(String ctorID, String fileName){// 접속중인 친구들한테 나한테 접속하라고
														// 메세지보내는 용도
		Advertisement AD;
		try {
			AD = new Advertisement(MessageType.Advertisement_Data, ctorID, fileName);
			AD.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	static public void CallAdvertisement_BloomFilter() {
		Advertisement AD;
		try {
			AD = new Advertisement(MessageType.BloomFilter);
			AD.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	

	static public void CallConnectToFreindNode(FreindNode fNode)
			throws UnknownHostException, IOException {
		ConnectFreindNode CFN = new ConnectFreindNode(fNode);
		CFN.start();
	}

	public void run() {
		try {
			mainSocket = new ReliableSocket();
			mainSocket.connect(new InetSocketAddress("127.0.0.1", 8089), 10000);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

class Advertisement extends Client implements Runnable {
	String updateFileName = null;
	MessageType msgType = null;
	String ctorID = null;

	public Advertisement(MessageType msgType) throws UnknownHostException,
			IOException {
		this.msgType = msgType;
	}

	public Advertisement(MessageType msgType, String fileName)
			throws IOException {
		this.updateFileName = fileName;
		this.msgType = msgType;
	}

	public Advertisement(MessageType msgType, String ctorID, String fileName)
			throws IOException {
		this.updateFileName = fileName;
		this.msgType = msgType;
		this.ctorID = ctorID;
	}

	public void run() {
		Logger logger = Logger.getLogger(getClass().getName());
		logger.setLevel(Level.INFO);

		Random rand = new Random();
		rand.setSeed(System.currentTimeMillis());
		for (FreindNode fNode : FreindList.freindList.values()) {
			if (rand.nextDouble() < 0)
				continue;

			logger.log(
					Level.INFO,
					"---------------------------------------------------------------------------------------------------------------Advertisement"
							+ " : " + fNode.freindID + " | " + fNode.publicPN);

			String message = null;
			if (msgType == MessageType.Push) {
				if(!CheckIDM(fNode.freindID, TOPS_Daemon.myID)){
					System.out.println("======================================Push IDM CONTINUE");
					continue;
				}
				message = msg.Push(updateFileName);

			} else if (msgType == MessageType.Advertisement_Data) {
				if(!CheckIDM(fNode.freindID, ctorID)){
					System.out.println("======================================Advertisement_Data IDM CONTINUE");
					continue;
				}
				
				message = msg.Advertisement_Data(ctorID, updateFileName);

			} else if (msgType == MessageType.Advertisement_Update) {
				if(!CheckIDM(fNode.freindID,BFilter.getCommonFriendList(fNode.bloomFilter))){
					System.out.println("======================================Advertisement_Update IDM CONTINUE");
					continue;
				}
//					message = msg.Advertisement_Update(Writing.getEntireFileList());
				message = msg.Advertisement_Update(BFilter.getCommonFileList(fNode.bloomFilter));
			}else if(msgType == MessageType.BloomFilter){
				message = msg.BloomFilter();
			}

			try {
				sendMSG(fNode, message, true);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

class ExchangePublicKey extends Client implements Runnable {
	FreindNode fNode = null;

	public ExchangePublicKey() throws UnknownHostException, IOException {
		super();

		// TODO Auto-generated constructor stub
	}

	public ExchangePublicKey(FreindNode fNode) throws UnknownHostException,
			IOException {
		super();
		this.fNode = fNode;
		// TODO Auto-generated constructor stub
	}

	public void run() {
		synchronized (this) {
			if (fNode != null) {
				Message msg = new Message();
				String keyMSG = msg.SendPublicKey(TOPS_Daemon.pubMod, TOPS_Daemon.pubExp);

				fNode.socket = getSocket(fNode);

				try {
					sendMSG(fNode, keyMSG, false);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fNode.keySendChk = true;
			} else {
				Message msg = new Message();
				String keyMSG = msg.SendPublicKey(TOPS_Daemon.pubMod, TOPS_Daemon.pubExp);
//				ArrayList<Client_NodeThread> MNTList = new ArrayList<Client_NodeThread>();

				for (FreindNode fNode : FreindList.freindList.values()) {
					try {
						sendMSG(fNode, keyMSG, false);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					fNode.keySendChk = true;
				}

			}
		}
	}
}

class ConnectFreindNode extends Client implements Runnable {
	FreindNode fNode;

	public ConnectFreindNode(FreindNode fNode) throws UnknownHostException,
			IOException {
		super();
		this.fNode = fNode;
		// TODO Auto-generated constructor stub
	}

	public void run() {

		Client_NodeThread MNT = null;
		try {
			MNT = new Client_NodeThread(fNode);
			MNT.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

class ConnectFreindNodes extends Client implements Runnable {

	public ConnectFreindNodes() throws UnknownHostException, IOException {
		super();
		// TODO Auto-generated constructor stub
	}

	public void run() {

		ArrayList<Client_NodeThread> MNTList = new ArrayList<Client_NodeThread>();

		for (FreindNode fNode : FreindList.freindList.values()) {

			Client_NodeThread mnt = null;
			try {
				mnt = new Client_NodeThread(fNode);
				MNTList.add(mnt);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Client_NodeThread mnt : MNTList) {
			mnt.start();
		}
	}

}