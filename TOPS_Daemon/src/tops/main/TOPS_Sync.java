package tops.main;

import java.io.*;
import java.net.*;
import java.util.*;

import tops.struct.*;

public class TOPS_Sync {
	Socket socket = null;
	static String myFolderPath = TOPS_Daemon.myFolderPath;
	Message msg = new Message();
	File myUpdateFilePath = new File(myFolderPath
			+ System.getProperty("file.separator") + "UpdateFile");

	DataOutputStream dos = null;
	DataInputStream dis = null;

	FileInputStream fin = null;
	BufferedInputStream bis = null;

	public TOPS_Sync() throws IOException {
		this.socket = Server.dm_fileSocket.accept();

		try {
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public File[] getFiles(File dir) {
		File[] files = null;
		File dirFile = new File(dir.getPath());
		files = dirFile.listFiles();

		return files;
	}

	public void sendFiles(File[] files) throws IOException {
		for (File f : files) {
			if (f.isDirectory()) {
				File[] dFiles = getFiles(f);
				sendFiles(dFiles);
			} else {

				String filePath = f.getPath();
				String fileName = f.getName();

				dos.writeUTF(filePath);
				dos.writeUTF(fileName);

				String yn = dis.readUTF(); // 중복파일인지 아닌지
				if (yn.equals("NO")) {
					continue;
				}

				fin = new FileInputStream(f);
				bis = new BufferedInputStream(fin);

				dos.writeLong(f.length());

				byte[] buffer = new byte[(int) f.length()];
				int data = 0;
				data = bis.read(buffer);
				dos.write(buffer, 0, data);
				dos.flush();
				System.out.print("GUI에게 파일보냄 : "+ fileName + " 내용 : " );
				System.out.write(buffer);
				System.out.println();
				try {
					bis.close();
					fin.close();
				} catch (Exception e) {

				}

			}
		}

	}

	public void DoSynchronize_Send() throws IOException {
		File dmPath = new File(TOPS_Daemon.myFolderPath);
		File[] dmFile = null;
		dmFile = dmPath.listFiles();

		sendFiles(dmFile);

		try {
			dos.writeUTF("FINISH");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void DoSynchronize_Recieve() throws IOException {
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;

		while (true) {
			boolean recieve = true;
			String filePath = dis.readUTF();

			if (filePath.equals("FINISH")) {
				break;
			}

			String fileName = dis.readUTF();

			String[] filePathTokens = filePath.split("TOPS");
			String newFile = filePathTokens[1];

			File newFilePath = new File(TOPS_Daemon.myHomePath
					+ System.getProperty("file.separator") + "TOPS_Daemon"
					+ System.getProperty("file.separator") + newFile); // updatefile
			System.out.println("뭥미 : " + newFilePath);

			String[] fileNameTokens = fileName.split("_UpdateFile_");
			final String newFileName = fileNameTokens[0];

			if (newFilePath.toString().contains("_UpdateFile_")) {
				int newVer = Integer.valueOf(fileNameTokens[1]);

				int oldVer = -1;

				File[] udFile = null;
				udFile = newFilePath.getParentFile().listFiles(
						new FilenameFilter() {

							@Override
							public boolean accept(File dir, String name) {
								// TODO Auto-generated method stub
								return name.startsWith(newFileName);
							}

						});

				if (udFile != null) {
					for (File f : udFile) {
						oldVer = Integer.valueOf(fileNameTokens[1]);
						if (oldVer > newVer)
							recieve = false;
						else
							f.delete();
					}
				}

			}
			if (newFilePath.exists())
				recieve = false;

			if (!recieve) {
				dos.writeUTF("NO");
				continue;
			} else {
				dos.writeUTF("YES");
			}

			if (!newFilePath.exists()) {
				newFilePath.getParentFile().mkdirs();
			}
			fos = new FileOutputStream(newFilePath);
			bos = new BufferedOutputStream(fos);
			long fileSize = dis.readLong();
			byte[] buffer = new byte[(int) fileSize];
			int data = 0;

			data = dis.read(buffer);
			bos.write(buffer, 0, data);
			bos.flush();

			try {
				bos.close();
				fos.close();
			} catch (Exception e) {

			}

		}
	}

}
