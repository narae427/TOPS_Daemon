package tops.main;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import tops.struct.*;

public class TOPS_Sync {
	Socket socket = TOPS_Server.sock;
	static String myFolderPath = TOPS_Daemon.myFolderPath;
	Message msg = new Message();
	File myUpdateFilePath = new File(myFolderPath
			+ System.getProperty("file.separator") + "UpdateFile");

	DataOutputStream dos = null;
	DataInputStream dis = null;

	FileInputStream fin = null;
	BufferedInputStream bis = null;

	public TOPS_Sync() {
	}

	/*
	 * UpdateFile을 읽어와서 적용시킨다.
	 */
	public File[] getFiles(File dir) {
		File[] files = null;
		File dirFile = new File(dir.getPath());
		files = dirFile.listFiles();

		return files;
	}

	public void sendFiles(File[] files) throws IOException {

		System.out.println("sendFIles");
		for (File f : files) {
			if (f.isDirectory()) {
				File[] dFiles = getFiles(f);
				sendFiles(dFiles);
			} else {

				String filePath = f.getPath();
				String fileName = f.getName();
				
				System.out.println("Send File : " + filePath + " * " + fileName);

				dos.writeUTF(filePath);
				dos.writeUTF(fileName);

				fin = new FileInputStream(f);
				bis = new BufferedInputStream(fin);

				dos.writeLong(f.length());

				byte[] buffer = new byte[(int) f.length()];
				int data = 0;
				data = bis.read(buffer);
				dos.write(buffer, 0, data);
				dos.flush();

				try {
					bis.close();
					fin.close();
				} catch (Exception e) {

				}

			}
		}

	}

	public void DoSynchronize() throws IOException {
		File dmPath = new File(TOPS_Daemon.myHomePath
				+ System.getProperty("file.separator") + "TOPS_Daemon");
		File[] dmFile = null;
		dmFile = dmPath.listFiles();

		System.out.println("dmPath : " + dmPath);

		try {
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		sendFiles(dmFile);
		

		try {
			dos.writeUTF("FINISH");
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
