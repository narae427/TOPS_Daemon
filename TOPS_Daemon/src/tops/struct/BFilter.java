package tops.struct;

import java.io.*;

import tops.main.*;

import com.google.common.base.*;
import com.google.common.hash.*;

public class BFilter {

	public BFilter() {
	}

	@SuppressWarnings("unchecked")
	static public void makeBloomFilter() throws IOException{
		double probability=0.0;
		
		int h = 3; //parameters.optimal_parameters.number_of_hashes;
		int x = FreindList.getEntireFreindSize();
		if(x < 20) probability = 0.01;
		else if(x > 160) probability = 0.1;
		else probability = ((x-20)/(160-20)) * (0.1-0.01) + 0.01;
		
		double a = 1;
		double b = 1-(double)Math.pow(1-Math.pow(probability,(double)1/h), (double)1/(Math.abs(x)*h));
		double tblsize = (double)a/b;
		tblsize = (int)(tblsize / 8 + 1) * 8;
		
		TOPS_Daemon.bloomFilter = BloomFilter.create(
				Funnels.stringFunnel(Charsets.UTF_8), (int)tblsize);
		File freindListFile = new File(TOPS_Daemon.myFolderPath
				+ System.getProperty("file.separator") + TOPS_Daemon.myID
				+ "_FreindList");
		
		if (!freindListFile.exists())
			return;
		LineNumberReader reader;
		try {
			reader = new LineNumberReader(new FileReader(freindListFile));
			while (true) {
				String freindID;
				freindID = reader.readLine();
				if (freindID == null)
					break;
				TOPS_Daemon.bloomFilter.put(freindID);

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
 @SuppressWarnings("unchecked")
public static void calcCommonFriend(String fid) {
	FreindNode fn = FreindList.getFriendNode(fid);
	@SuppressWarnings("rawtypes")
	BloomFilter f_BF = fn.bloomFilter;
	String vList = "";
		File freindListFile = new File(TOPS_Daemon.myFolderPath
				+ System.getProperty("file.separator") + TOPS_Daemon.myID
				+ "_FreindList");
		if (!freindListFile.exists())
			return;
		LineNumberReader reader;
		try {
			reader = new LineNumberReader(new FileReader(freindListFile));
			while (true) {
				String friendID;
				friendID = reader.readLine();
				if (friendID == null)
					break;
//				System.out.println("BF : " + friendID);

				if (f_BF.mightContain(friendID)) {
//					System.out.println("Common Friend : " + friendID);

					vList += (friendID+";");

				}
			}
			TOPS_Server.sendMessage("'dm_ShowCmnFriend'"+"@"+vList + "@");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "resource", "unchecked" })
	static public String getCommonFriendList( @SuppressWarnings("rawtypes") BloomFilter f_BF) {
		String entireFreindList = "";
		File freindListFile = new File(TOPS_Daemon.myFolderPath
				+ System.getProperty("file.separator") + TOPS_Daemon.myID
				+ "_FreindList");
		if (!freindListFile.exists())
			return null;
		LineNumberReader reader;
		try {
			reader = new LineNumberReader(new FileReader(freindListFile));
			while (true) {
				String friendID;
				friendID = reader.readLine();
				if (friendID == null)
					break;

				if (f_BF.mightContain(friendID)) {
					entireFreindList += (friendID + ";");
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return entireFreindList;
	}

	@SuppressWarnings("unchecked")
	static public String getCommonFileList(@SuppressWarnings("rawtypes") BloomFilter f_BF) {
		File[] myUpdateFile = null;

		File myUpdateFilePath = new File(TOPS_Daemon.myFolderPath
				+ System.getProperty("file.separator") + "UpdateFile");

		myUpdateFile = myUpdateFilePath.listFiles(new FilenameFilter() { // �����������ִ�updateFiles

					@Override
					public boolean accept(File dir, String name) {
						// TODO Auto-generated method stub
						return name.contains("UpdateFile");
					}

				});

		String entireFileList = "";
		System.out
				.println("Writing getEntireFileList = " + myUpdateFile.length);
		for (int i = 0; i < myUpdateFile.length; i++) {
			String fileName = myUpdateFile[i].getName();
			String friendID = fileName.substring(0,
					fileName.indexOf("_UpdateFile"));
			if (f_BF.mightContain(friendID))
				entireFileList += (fileName + ";");
		}

		System.out.println("ectire File LIST : " + entireFileList);
		return entireFileList;
	}
}
