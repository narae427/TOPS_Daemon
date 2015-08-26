package tops.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

import com.google.common.base.*;
import com.google.common.hash.*;

import tops.struct.*;

public class UpdateFiles {
	Socket socket = null;
	static String myFolderPath = TOPS_Daemon.myFolderPath;
	Message msg = new Message();
	File myUpdateFilePath = new File(myFolderPath
			+ System.getProperty("file.separator") + "UpdateFile");

	public UpdateFiles(Socket socket) {
		this.socket = socket;
	}

	/*
	 * UpdateFile을 읽어와서 적용시킨다.
	 */
	public void DoUpdate(MessageType msgType, FreindNode fNode)
			throws Exception {
		System.out.println();
		System.out
				.println("===============================================================");

		File[] myUpdateFile = null;
		myUpdateFile = myUpdateFilePath.listFiles(new FilenameFilter() { // 내가
																			// 가지고
																			// 있는
																			// updateFiles

					@Override
					public boolean accept(File dir, String name) {
						// TODO Auto-generated method stub
						return !name.contains(String.valueOf(TOPS_Daemon.myID))
								&& name.contains("UpdateFile");
					}

				});

		for (File f : myUpdateFile) {
			int deleteCount = 0;
			int writeCount = 0;
			System.out.println();
			System.out.println(" ~ " + f.getName());
			File file = new File(f.getPath());
			LineNumberReader reader = new LineNumberReader(new FileReader(file));
			String[] fileNameTokens = f.getName().split("_");
			String id = fileNameTokens[0];

			int oldVersion = TOPS_Daemon.freindVerHT.get(id); // 내가 가지고 있는 친구의 최고 버전
			int newVersion = 0;
			System.out.println(" ~ " + f.getName() + "의 구버전 : " + oldVersion);
			while (true) {

				String str = reader.readLine();

				if (str == null)
					break;

				if (str.substring(0, 7).equals("VERSION")) {
					newVersion = Integer.valueOf(str.substring(8)); // 새로 들어온
																	// updateFile
																	// 에 있는 버전
					// if(newVersion <= oldVersion){
					// String trashStr = reader.readLine(); //write 이나 delete읽어서
					// 버림.
					// continue;
					// }
				}

				if (str.substring(0, 7).equals("[Write]")) {

					String[] fileNameTokens2 = str.split("]");
					fileNameTokens2 = fileNameTokens2[1].split("_");
					String id2 = fileNameTokens2[0];
					String fName = str.substring(7);
					File IDFolder = new File(myFolderPath
							+ System.getProperty("file.separator") + id2);
					if (!IDFolder.exists()) {
						IDFolder.mkdir();
					}
					File targetFile = new File(myFolderPath
							+ System.getProperty("file.separator") + id2
							+ System.getProperty("file.separator") + fName);
					if (!targetFile.exists()) {

						Client_NodeThread MNT = new Client_NodeThread(fNode);
						MNT.readyForRecieveFile(
								InetAddress.getByName(fNode.publicIP),
								fNode.publicPN, fName);
						writeCount++;
					}

				}

				if (str.substring(0, 8).equals("[Delete]")) {
					String[] fileNameTokens2 = str.split("]");
					fileNameTokens2 = fileNameTokens2[1].split("_");
					String id2 = fileNameTokens2[0];
					String fName = str.substring(8);
					File targetFile = new File(myFolderPath
							+ System.getProperty("file.separator") + id2
							+ System.getProperty("file.separator") + fName);
					if (targetFile.exists()) {
						targetFile.delete();
						deleteCount++;
					}
				}
			}

			TOPS_Daemon.freindVerHT.put(id, newVersion);
			System.out.println(" ~ " + id + "의 새버전 : " + newVersion);
			System.out.println(" ~ " + writeCount + "개 파일 생성, " + deleteCount
					+ "개 파일 삭제 완료");
			System.out.println();

			reader.close();

			if (writeCount > 0 || deleteCount > 0) {

				if (msgType == MessageType.Updates) {
					System.out.println("ADVERTISE UPDATES");
					Client.CallAdvertisement_UPDATE();

				} else if (msgType == MessageType.Data) {
					System.out.println("ADVERTISE DATA");
					Client.CallAdvertisement_DATA(id, f.getName());
				}
			}

		}

		TOPS_Server.sendMessage("'dm_ListUpdate'");
		
		System.out
				.println("===============================================================");
		System.out.println();

	}

	public void ReceiveFile(String fileName) throws IOException {
		DataInputStream dis = new DataInputStream(socket.getInputStream());

		/*
		 * if(fileName.equals("FINISH")){ //dos.writeUTF("FINISH"); return; }
		 */
		// dos.writeUTF(fileName); //어떤 파일을 받아야하는지
		String[] fileNameTokens = fileName.split("_");
		String id = fileNameTokens[0];

		File f = new File(myFolderPath + System.getProperty("file.separator")
				+ id + System.getProperty("file.separator") + fileName);
		FileOutputStream fos = new FileOutputStream(f);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		long fileLength = dis.readLong();
		long FileLengthFinal = fileLength;

		// long picFileLength = picFile.length();
		int fileTotalBuffer = 0;
		while (true) {
			if (fileTotalBuffer == FileLengthFinal) {
				break;
			}
			byte[] buffer = new byte[(int) fileLength];
			int data = 0;
			data = dis.read(buffer);
			bos.write(buffer, 0, data);
			bos.flush();

			fileTotalBuffer += data;
			fileLength -= data;

		}

		/*
		 * byte[] buffer = new byte[(int)fileSize]; int data = 0;
		 * 
		 * data = dis.read(buffer); bos.write(buffer, 0, data); bos.flush();
		 */
		String pictureName = dis.readUTF();
		if (pictureName.equals("NoPicture")) {

		} else {

			File picFile = new File(myFolderPath
					+ System.getProperty("file.separator") + id
					+ System.getProperty("file.separator") + "Pictures");
			if (!picFile.exists()) {
				picFile.mkdir();
			}
			FileOutputStream picFos = new FileOutputStream(picFile
					+ System.getProperty("file.separator") + pictureName);
			BufferedOutputStream picBos = new BufferedOutputStream(picFos);
			long picFileLength = dis.readLong();
			long picFileLengthFinal = picFileLength;

			// long picFileLength = picFile.length();
			int totalBuffer = 0;
			while (true) {
				if (totalBuffer == picFileLengthFinal) {
					break;
				}
				byte[] picBuffer = new byte[(int) picFileLength];
				int picData = 0;
				picData = dis.read(picBuffer);
				picBos.write(picBuffer, 0, picData);
				picBos.flush();

				totalBuffer += picData;
				picFileLength -= picData;
			}

			picBos.close();
			picFos.close();
		}
		bos.close();
		fos.close();
	}

	public int SendFile(String fileName) throws IOException {
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

		// String fileName = dis.readUTF(); //어떤 파일을 보내주어야하는지
		if (fileName.equals("FINISH")) {
			return -1;
		}

		String[] fileNameTokens = fileName.split("_");
		String id = fileNameTokens[0];

		File f = new File(myFolderPath + System.getProperty("file.separator")
				+ id + System.getProperty("file.separator") + fileName);
		if (!f.exists()) {
			FileWriter fw = new FileWriter(f);
			fw.write("Already Deleted ! ");
			fw.flush();
			f.setWritable(false);
			fw.close();
		}
		FileInputStream fin = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(fin);
		dos.writeLong(f.length());

		long FileLength = f.length();
		int fileTotalBuffer = 0;
		while (true) {
			if (fileTotalBuffer == f.length()) {
				break;
			}
			byte[] buffer = new byte[(int) FileLength];
			int data = 0;
			data = bis.read(buffer);
			dos.write(buffer, 0, data);
			dos.flush();
			fileTotalBuffer += data;
			FileLength -= data;
		}

		LineNumberReader reader = new LineNumberReader(new FileReader(f));
		String firstLine = reader.readLine(); // 그림파일 경로
		reader.close();

		if (firstLine.equals("Already Deleted ! ")
				|| firstLine.equals("[No Picture]")) {
			// 사진안보내야함.
			dos.writeUTF("NoPicture");
		} else {
			// 사진보내야함.
			StringTokenizer st = new StringTokenizer(firstLine);
			String pictureName = "";
			while (st.hasMoreTokens()) {
				pictureName = st
						.nextToken(System.getProperty("file.separator"));
			}

			dos.writeUTF(pictureName);

			File picFile = new File(firstLine);
			FileInputStream picFin = new FileInputStream(picFile);
			BufferedInputStream picBis = new BufferedInputStream(picFin);

			dos.writeLong(picFile.length());
			long picFileLength = picFile.length();
			int totalBuffer = 0;
			while (true) {
				if (totalBuffer == picFile.length()) {
					break;
				}
				byte[] picBuffer = new byte[(int) picFileLength];
				int picData = 0;
				picData = picBis.read(picBuffer);
				dos.write(picBuffer, 0, picData);
				dos.flush();
				totalBuffer += picData;
				picFileLength -= picData;
			}

			picBis.close();
			picFin.close();

		}

		fin.close();
		bis.close();

		if (!f.canWrite()) {
			f.delete();
		}

		return 1;

	}

	public void ReceiveUpdateFiles(MessageType msgType, FreindNode fnode)
			throws Exception {
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		int count = 0;

		if (!myUpdateFilePath.exists()) {
			myUpdateFilePath.mkdir();
		}

		while (true) {

			String fileName = dis.readUTF();
			String[] fileNameTokens = fileName.split("_");
			final String id = fileNameTokens[0];

			if (fileName.equals("FINISH")) {
				break;
			}

			File updateFilePath = new File(myUpdateFilePath
					+ System.getProperty("file.separator") + fileName); // updatefile
																		// 경로
			// File myUpdateFilePath = new File(myFolderPath);

			File[] myUpdateFile = null;
			myUpdateFile = myUpdateFilePath.listFiles(new FilenameFilter() { // 내가
																				// 가지고
																				// 있는
																				// updateFiles

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

			System.out.println(" ~ " + "친구 리스트 추가 : " + id + " 	" + oldVer);
			int newVer = Integer.valueOf(fileName.substring(temp.length()));

			Hashtable<String, Integer> tempIDM = new Hashtable<String, Integer>();
			tempIDM.put(id, newVer);
			TOPS_Daemon.IDM.put(fnode.freindID, tempIDM);
			TOPS_Daemon.IDM.put(TOPS_Daemon.myID, tempIDM);

			if (updateFilePath.exists() || oldVer >= newVer) {
				dos.writeUTF("NO");
				continue;
			} else {
				dos.writeUTF("YES");
			}

			File oldUpdateFile = myUpdateFile[0];
			File newUpdateFile = updateFilePath;
			oldUpdateFile.renameTo(newUpdateFile);
			if (oldUpdateFile.exists()) {
				System.out.println("DELETE : " + oldUpdateFile.delete());
			}

			fos = new FileOutputStream(updateFilePath);
			bos = new BufferedOutputStream(fos);
			long fileSize = dis.readLong();
			byte[] buffer = new byte[(int) fileSize];
			int data = 0;

			data = dis.read(buffer);
			bos.write(buffer, 0, data);
			bos.flush();

			count++;

		}
		try {
			bos.close();
			fos.close();
		} catch (Exception e) {

		}

		System.out.println(" ~ " + count + "개 파일 UpdateFile Receive 업데이트 완료");

		if (count > 0)
			DoUpdate(msgType, fnode);

	}

	@SuppressWarnings("unchecked")
	public void SendUpdateFiles(MessageType msgType, FreindNode fnode,
			final String friendId) throws IOException {
		System.out.println("SEND UPDATEFILES");
		File[] tempFiles = null;
		ArrayList<File> fileArr = new ArrayList<File>();

		if (msgType == MessageType.Request_Updates) {
			tempFiles = myUpdateFilePath.listFiles();
		} else if (msgType == MessageType.Request_Data) {
			tempFiles = myUpdateFilePath.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File f, String fName) {
					// TODO Auto-generated method stub
					return fName.startsWith(friendId);
				}

			});
		}

		if (tempFiles == null) {
			System.out.println("UpdateFile 없음"); // //여기가 문제네
			return;
		}

		for (File f : tempFiles) {
			String id = f.getName().substring(0,
					f.getName().indexOf("_UpdateFile"));
			if (fnode.bloomFilter.mightContain(id))
				fileArr.add(f);
		}

		if (fileArr.size() == 0) {
			System.out.println("updateFile 없음");
			return;
		}
		DataOutputStream dos = null;
		DataInputStream dis = null;
		try {
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		FileInputStream fin = null;
		BufferedInputStream bis = null;
		for (int i = 0; i < fileArr.size() + 1; i++) {
			if (i == fileArr.size()) {
				try {
					dos.writeUTF("FINISH");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}

			String fileName = String.valueOf(fileArr.get(i).getName());
			String friendID = fileName.substring(0, fileArr.get(i).getName()
					.indexOf("_UpdateFile"));
			String trashStr = fileName.substring(fileName.length() - 1,
					fileName.length());

			if (trashStr.equals("~")) {
				continue;
			}

			String temp = fileName.substring(0, fileName.lastIndexOf("_") + 1);

			int newVer = Integer.valueOf(fileName.substring(temp.length()));

			Hashtable<String, Integer> tempIDM = new Hashtable<String, Integer>();
			tempIDM.put(friendID, newVer);
			TOPS_Daemon.IDM.put(fnode.freindID, tempIDM);
			TOPS_Daemon.IDM.put(TOPS_Daemon.myID, tempIDM);

			dos.writeUTF(fileName);
			String yn = dis.readUTF(); // 중복파일인지 아닌지
			if (yn.equals("NO")) {
				continue;
			}

			File f = new File(fileArr.get(i).getPath());
			fin = new FileInputStream(f);
			bis = new BufferedInputStream(fin);

			dos.writeLong(f.length());

			byte[] buffer = new byte[(int) f.length()];
			int data = 0;
			data = bis.read(buffer);
			dos.write(buffer, 0, data);
			dos.flush();

		}

		try {
			bis.close();
			fin.close();
		} catch (Exception e) {

		}

		// dis.close();
		// dos.close();
	}

	public static File[] OrganizeFiles(String path) { // 디렉토리안에 파일들 뽑아내주기

		File f = new File(path);
		File[] directories = f.listFiles(new FileFilter() {
			@Override
			public boolean accept(File d) {
				// TODO Auto-generated method stub
				return d.isDirectory();
			}

		});
		int numberOfFiles = 0;
		File[] files = null;
		if (directories.length > 0) {
			for (File d : directories) {
				String dName = d.getName();
				File fDir = new File(path
						+ System.getProperty("file.separator") + dName);
				File[] tempFiles = fDir.listFiles();
				numberOfFiles += tempFiles.length;
			}

			files = new File[numberOfFiles];
			numberOfFiles = 0;
			for (File d : directories) {
				String dName = d.getName();
				File fDir = new File(path
						+ System.getProperty("file.separator") + dName);

				File[] tempFiles = fDir.listFiles();
				System.arraycopy(tempFiles, 0, files, numberOfFiles,
						tempFiles.length);
				numberOfFiles += tempFiles.length;
			}

		} else if (directories.length == 0) {
			files = f.listFiles(new FileFilter() {

				@Override
				public boolean accept(File file) {
					// TODO Auto-generated method stub
					return file.isFile();
				}

			});
		}
		return files;
	}

	public void ReceiveBloomFilter(MessageType msgType, FreindNode fnode)
			throws Exception {
		DataInputStream dis = new DataInputStream(socket.getInputStream());

		fnode.bloomFilter = BloomFilter.readFrom(dis,
				Funnels.stringFunnel(Charsets.UTF_8));

		Client.CallConnectToFreindNode(fnode);
	}

	public void SendBloomFilter(MessageType msgType, FreindNode fnode)
			throws IOException {
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		TOPS_Daemon.bloomFilter.writeTo(dos);
	}

}
