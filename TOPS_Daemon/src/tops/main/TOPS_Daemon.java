package tops.main;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

import com.google.common.hash.*;

import tops.struct.*;
import net.rudp.*;

//TOPS : Triangle oriented P2P SNS
public class TOPS_Daemon {

	public static String myID = "";
	public static int myPublicPN = 0;
	public static int myPrivatePN = 0;
	@SuppressWarnings("rawtypes")
	public static BloomFilter bloomFilter;

	// static String myInternalIpAddress = "";
	static String myPublicIP = "";
	public static String myPrivateIP = "";

	// static ServerSocket ss = null;
	static Hashtable<String, Integer> freindVerHT = new Hashtable<String, Integer>();
	static Hashtable<String, Hashtable<String, Integer>> IDM = new Hashtable<String, Hashtable<String, Integer>>();

	Server server = null;
	Client client = null;
	String topDirPath = null;
	public static String myHomePath = null;
	public static String myFolderPath = null;
	public static String myOS = null;
	Message msg = new Message();

	static BigInteger pubMod = null;
	static BigInteger pubExp = null;
	public static BigInteger privMod = null;
	public static BigInteger privExp = null;

	static int DHP = 7001;
	static int DHG = 7;

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
//		myPrivateIP = InetAddress.getLocalHost().getHostAddress();
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					myPrivateIP = InetAddress.getLocalHost().getHostAddress();
//					myOS = System.getProperty("os.name");
//					TOPS tops = new TOPS();
//
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//		TOPS tops = new TOPS();
		
	}

	public TOPS_Daemon() {
		
//		TOPS_Server server = new TOPS_Server();
//		new Thread(server).start();
	}
	
	public void executeServer(){
		TOPS_Server server = new TOPS_Server();
		new Thread(server).start();
	}
	
	@SuppressWarnings("static-access")
	public  void initialize(String myID) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException{
		this.myID = myID;
		myPrivateIP = InetAddress.getLocalHost().getHostAddress();
		String HomeDir = System.getProperty("user.home");
		myHomePath = HomeDir;
		System.out.println("Daemon Home Directory :" + HomeDir);
		File MyDir = new File(HomeDir
				+ System.getProperty("file.separator") + "TOPS"
				+ System.getProperty("file.separator") + myID);
		if (!MyDir.exists()) {
			MyDir.mkdirs();
		}

		myFolderPath = MyDir.getPath();
		System.out.println("Daemon My Folder Path : " + myFolderPath);
		System.out.println();

			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			KeyPair kp = kpg.genKeyPair();
//			Key publicKey = kp.getPublic();
//			Key privateKey = kp.getPrivate();

			KeyFactory fact = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(),
					RSAPublicKeySpec.class);
			RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(),
					RSAPrivateKeySpec.class);

			// RSAcrypto.saveToFile(TOPS.myID + "public.key",
			// pub.getModulus(), pub.getPublicExponent());
			pubMod = pub.getModulus();
			pubExp = pub.getPublicExponent();
			// RSAcrypto.saveToFile(TOPS.myID + "private.key",
			// priv.getModulus(), priv.getPrivateExponent());
			privMod = priv.getModulus();
			privExp = priv.getPrivateExponent();

			BFilter.makeBloomFilter();

			server = new Server();
			server.start();

			client = new Client();
			client.start();

	}
	/*
	 * write 하거나 delete 할떄 UpdateFile에 기록해줌.
	 */

	public void writeUpdateFile(String msg) {
		File updateFilePath = new File(myFolderPath
				+ System.getProperty("file.separator") + "UpdateFile");

		File[] myUpdateFile = null;
		myUpdateFile = updateFilePath.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return name.contains(myID) && name.contains("UpdateFile");
			}

		});

		if (myUpdateFile == null || myUpdateFile.length == 0) {
			if (!updateFilePath.exists()) {
				updateFilePath.mkdir();
			}
			myUpdateFile = new File[1];
			myUpdateFile[0] = new File(updateFilePath
					+ System.getProperty("file.separator") + myID + "_"
					+ "UpdateFile" + "_" + String.valueOf(0));

		}
		String temp = myID + "_" + "UpdateFile" + "_";
		int ver = Integer.valueOf(myUpdateFile[0].getName().substring(
				temp.length())); // //////////////////////////////////////////////////////////////////////
		ver++;

		File oldUpdateFile = new File(myUpdateFile[0].getPath());
		File newUpdateFile = new File(updateFilePath
				+ System.getProperty("file.separator")
				+ myUpdateFile[0].getName().substring(0, temp.length())
				+ String.valueOf(ver));

		oldUpdateFile.renameTo(newUpdateFile);
		if (oldUpdateFile.exists()) {
			System.out.println("DELETE : " + oldUpdateFile.delete());
		}

		try {
			FileWriter fw = new FileWriter(newUpdateFile, true);
			fw.write("VERSION " + ver + "\n");
			fw.write(msg);
			fw.flush();
			fw.close();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}


	public String getTime() {
		long time = System.currentTimeMillis();
		SimpleDateFormat dayTime = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String strTime = dayTime.format(new Date(time));
		return strTime;
	}

	static public int getAvailablePortNumber() {
		int availablePortNumber = 0;
		for (int pn = 1024; pn < 65535; pn++) {
			try {
				ReliableServerSocket socket = new ReliableServerSocket(pn);
				socket.setReuseAddress(true);
				availablePortNumber = pn;
				socket.close();
				return availablePortNumber;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				continue;
			}

		}

		return -1;
	}

}
