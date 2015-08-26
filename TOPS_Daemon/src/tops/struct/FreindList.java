package tops.struct;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.StringTokenizer;

import tops.main.*;



public class FreindList {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static HashMap<String, FreindNode> freindList = new HashMap();
	
	static public FreindNode  getFriendNode(String id){
		return freindList.get(id);
	}
	static public void putOnlineFreind(String id, FreindNode fNode) throws NumberFormatException, IOException{
		freindList.put(id, fNode);
		printFreindList();
	}
	
	
	static public void removeOfflineFreind(String id){
		freindList.remove(id);
		System.out.println("REMOVE " + id);
		printFreindList();
	}
	
	static public String getFreindId(InetAddress ia, int pn){
		java.util.Iterator<String> it = freindList.keySet().iterator();
		StringTokenizer st = new StringTokenizer(String.valueOf(ia));
		String iaStr = st.nextToken("/");
		while(it.hasNext()){
			String key = (String)it.next();
			if(freindList.get(key).publicIP.equals(iaStr) && freindList.get(key).publicPN == pn){
				return freindList.get(key).freindID;
			}
		}
		
		return null;
		
	}
	
	@SuppressWarnings("resource")
	static public String getEntireFreindList() throws IOException{
		File freindListFile = new File(TOPS_Daemon.myFolderPath + System.getProperty("file.separator") + TOPS_Daemon.myID + "_FreindList" );
		if(!freindListFile.exists())
			return null;
		String entireFreindList = "";
		LineNumberReader reader = new LineNumberReader(new FileReader(freindListFile));
		while(true){
			String freindID = reader.readLine();
			if(freindID == null)
				break;
			entireFreindList += (freindID + ";");
		}
		
		return entireFreindList;
	}
	
	@SuppressWarnings("resource")
	static public int getEntireFreindSize() throws IOException{
		int numFriends = 0;
		
		File freindListFile = new File(TOPS_Daemon.myFolderPath + System.getProperty("file.separator") + TOPS_Daemon.myID + "_FreindList" );
		if(!freindListFile.exists())
			return -1;
		LineNumberReader reader = new LineNumberReader(new FileReader(freindListFile));
		while(true){
			String freindID = reader.readLine();
			if(freindID == null)
				break;
			numFriends++;
		}
		
		return numFriends;
	}
	
	
	static public void printFreindList(){
		System.out.println(" *** ���� Online�� ģ��");
		for(FreindNode fn : freindList.values()){
			System.out.println(fn.freindID);
		}
		System.out.println(" **********************");
	}

}
