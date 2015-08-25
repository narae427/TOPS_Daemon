package tops.struct;

import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
//import biz.source_code.base64Coder.Base64Coder;
import java.security.PublicKey;
import java.util.logging.*;
import java.util.regex.*;

import tops.main.*;

public class Message {
	String message = "";
	Logger logger = Logger.getLogger(getClass().getName());
	
	Pattern commandPattern = Pattern.compile("'.*'");
	Pattern idPattern = Pattern.compile("!.*!");
	Pattern pvPattern = Pattern.compile("<pv>.*<pv>");
	Pattern ipPattern = Pattern.compile("#.*#");
	Pattern pnPattern = Pattern.compile("~.*~");
	Pattern pbPattern = Pattern.compile("<pb>.*<pb>");
	Pattern fidPattern = Pattern.compile("@.*@");
	Pattern fnamePattern = Pattern.compile(":.*:");
	Pattern modPattern = Pattern.compile("-.*-");
	Pattern expPattern = Pattern.compile("=.*=");
	Pattern freindPattern = Pattern.compile(";.*;");
	
	public String commandMessage;
	public String idMessage;
	public String pvMessage;
	public String pvipMessage;
	public String pvpnMessage;
	public String pbMessage;
	public String pbipMessage;
	public String pbpnMessage;
	public String fidMessage;
	public String fnameMessage;
	public String modMessage;
	public String expMessage;
	public String freindsInfoMessage;
	public String bfMessage;
	
	public String fi_idMessage;
	public String fi_pvMessage;
	public String fi_pvipMessage;
	public String fi_pvpnMessage;
	public String fi_pbMessage;
	public String fi_pbipMessage;
	public String fi_pbpnMessage;

	private String getPatternfromMSG(String message, Pattern p) {
		Pattern pattern = p;
		Matcher m = pattern.matcher(message);

		boolean find = false;

		String rstr = null;

		while (m.find()) {
			rstr = m.group();
			find = true;
		}

		if (find)
			rstr = rstr.substring(1, rstr.length() - 1);
		if (rstr == null)
			return "-1";
		else
			return rstr;
	}
	
	public void getPatternfromMSG(String message) {
		commandMessage = getPatternfromMSG(message, commandPattern);
		idMessage = getPatternfromMSG(message, idPattern);
		pvMessage = getPatternfromMSG(message, pvPattern);
		pvipMessage = getPatternfromMSG(pvMessage, ipPattern);
		pvpnMessage = getPatternfromMSG(pvMessage, pnPattern);
		pbMessage = getPatternfromMSG(message, pbPattern);
		pbipMessage = getPatternfromMSG(pbMessage, ipPattern);
		pbpnMessage = getPatternfromMSG(pbMessage, pnPattern);
		fidMessage = getPatternfromMSG(message, fidPattern);
		fnameMessage = getPatternfromMSG(message, fnamePattern);
		modMessage = getPatternfromMSG(message, modPattern);
		expMessage = getPatternfromMSG(message, expPattern);
		freindsInfoMessage = getPatternfromMSG(message, freindPattern);
	}
	
	public void getFreindPatternfromMSG(String message){
		fi_idMessage = getPatternfromMSG(message, fidPattern);
		fi_pvMessage = getPatternfromMSG(message, pvPattern);
		fi_pvipMessage = getPatternfromMSG(pvMessage, ipPattern);
		fi_pvpnMessage = getPatternfromMSG(pvMessage, pnPattern);
		fi_pbMessage = getPatternfromMSG(message, pbPattern);
		fi_pbipMessage = getPatternfromMSG(pbMessage, ipPattern);
		fi_pbpnMessage = getPatternfromMSG(pbMessage, pnPattern);
	}
	
	private void printMSG(String message, String freindId) {
		logger.log(Level.INFO,  "[SEND MESSAGE]	" + message + " to " + freindId);
	}
	
	
	public String ConnectionAlertMSGforMainServer(String freindID){
		printMSG("MS_Connection", "LoginServer");
		message = "'MS_Connection'" + "!" + TOPS_Daemon.myID + "!" + "#" + TOPS_Daemon.myPrivateIP+ "#" + "~" + String.valueOf(TOPS_Daemon.myPrivatePN) + "~"+ "@" + freindID + "@"; 
		System.out.println(message);
		return message;
	}
	
	public String UnconnectionAlertMSGforMainServer(String freindID){
		printMSG("MS_Unconnection", "LoginServer");
		message = "'MS_Unconnection'" + "!" + TOPS_Daemon.myID + "!" + "@" + freindID + "@";
		return message;
	}
	
	public String CheckOnlineFreindMSG(String freindID){
		printMSG("MS_CheckOnlineFreind", "LoginServer");
		message = "'MS_CheckOnlineFreind'" + "!" + TOPS_Daemon.myID + "!" + "@" + freindID + "@";
		return message;
	}
	
	public String CheckOnlineFreindsMSG(String freindID){
		printMSG("MS_CheckOnlineFreinds", "LoginServer");
		message = "'MS_CheckOnlineFreinds'" + "!" + TOPS_Daemon.myID + "!" + "@" + freindID + "@";
		return message;
	}
	
	public String RequestAddFreindMSG(String myID, String freindID){
		printMSG("MS_RequestAddFreind", "LoginServer");
		message = "'MS_RequestAddFreind'" + "!" + myID + "!" + "@" + freindID + "@";
		return message;
	}
	
	public String AllowAddFreindMSG(String myID, String freindID){
		printMSG("MS_AllowAddFreind", "LoginServer");
		message = "'MS_AllowAddFreind'" + "!" + myID + "!" + "@" + freindID + "@";
		return message;
	}
	
	public String SendPublicKey(BigInteger mod, BigInteger exp){
		printMSG("PublicKey", "Freind");
		message = "'PublicKey'" + "!" + TOPS_Daemon.myID + "!" + "-" + mod + "-" + "=" + exp + "=";
		return message;
	}
	
	public String Advertisement_Login1(){
		printMSG("Advertisement_Login1", "Freind");
		message = "'Advertisement_Login1'"+ "!" + TOPS_Daemon.myID + "!";
		return message;
	}
	public String Advertisement_Login2(){
		printMSG("Advertisement_Login2", "Freind");
		message = "'Advertisement_Login2'"+ "!" + TOPS_Daemon.myID + "!";
		return message;
	}
	public String Request_Updates(){
		printMSG("Request_Updates", "Freind");
		message = "'Request_Updates'" + "!" + TOPS_Daemon.myID + "!";
		return message;
	}
	public String Updates(){
		printMSG("Updates", "Freind");
		message = "'Updates'" + "!" + TOPS_Daemon.myID + "!";
		return message;
	}
	public String Advertisement_Update(String fileName_s){
		printMSG("Advertisement_Update", "Freind");
		message = "'Advertisement_Update'" + "!" + TOPS_Daemon.myID + "!" + ":" + fileName_s + ":";
		return message;
	}
	public String Push(String fileName){
		printMSG("Push", "Freind");
		message = "'Push'" + "!" + TOPS_Daemon.myID + "!" +":" + fileName + ":";
		return message;
	}
	public String Advertisement_Data(String ctorID, String fileName){
		printMSG("Advertisement_Data", "Freind");
		message = "'Advertisement_Data'" + "!" + TOPS_Daemon.myID + "!"  + "@" + ctorID + "@"+ ":" + fileName + ":";
		return message;
	}
	public String Request_Data(String freindID){
		printMSG("Request_Data", "Freind");
		message = "'Request_Data'" + "!" + TOPS_Daemon.myID + "!"  + "@" + freindID + "@";
		return message;
	}
	public String Request_File( String fileName){
		printMSG("Request_File", "Freind");
		message = "'Request_File'" + "!" + TOPS_Daemon.myID + "!"  +  ":" + fileName + ":";
		return message;
	}
	public String Data(String ctorID, String fileName){
		printMSG("Data", "Freind");
		message = "'Data'" + "!" + TOPS_Daemon.myID + "!" + "@" + ctorID + "@"+ ":" + fileName + ":";
		return message;
	}
	public String BloomFilter(){
		printMSG("BloomFilter", "Freind");
		message = "'BloomFilter'" + "!" + TOPS_Daemon.myID + "!" ;
		return message;
	}
	
	public String Request_BloomFilter(){
		printMSG("Request_BloomFilter", "Freind");
		message = "'Request_BloomFilter'" + "!" + TOPS_Daemon.myID + "!" ;
		return message;
	}
	
}
