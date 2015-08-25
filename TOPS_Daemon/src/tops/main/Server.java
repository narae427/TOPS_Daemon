package tops.main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

import tops.encryption.*;
import tops.struct.*;
import net.rudp.*;

public class Server implements Runnable {

//	final static ExecutorService pool = Executors.newFixedThreadPool(100);
	final static ExecutorService pool = Executors.newCachedThreadPool();

	static ReliableServerSocket serverSocket = null;

	static ReliableSocket socket = null;
	static ServerSocket fileSocket = null;
	final Thread serverThread;

	volatile boolean started = false;
	volatile boolean stopped = false;

	boolean open = false;

	UpdateFiles UF = null;
	DataOutputStream dos = null;
	DataInputStream dis = null;
	Message msg = new Message();
	// static DatagramSocket ds = null;
	String printMsg = "";
	String printPT = "";
	boolean chk = false; // MS_ResultOfCheckOnlineFreind -> chk = true,
							// MS_ResultOfCheckOnlineFreinds -> chk = false

	int hnr = -1;

	static boolean confirm = false;

	public Server() throws IOException {
		serverSocket = new ReliableServerSocket();
		TOPS_Daemon.myPrivatePN = serverSocket.getLocalPort();
		fileSocket = new ServerSocket(TOPS_Daemon.myPrivatePN);
		serverThread = new Thread(this);
	}

	public void start() {
		serverThread.start();
	}

	public void stop() {
		stopped = true;
	}

	public void run() {
		try {
			started = true;
			while (!stopped) {
				socket = (ReliableSocket) serverSocket.accept();
				// pool.execute(new Request(socket));
				Request req = new Request(socket);
				new Thread(req).start();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static class Request implements Runnable {

		final Socket socket;

		public Request(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				InputStreamReader inputStream = new InputStreamReader(
						socket.getInputStream());
				BufferedReader buffReader = new BufferedReader(inputStream);
				while (true) {
					String line = buffReader.readLine();

					// socket.close();
					if (line != null) {

						String message = new String(line).trim();
						if (message.equals(""))
							return;

						if (!message.contains("MS_")
								&& !message.contains("PublicKey")
								&& !message.contains("OPEN")) {
							String decryptionMsg = RSAcrypto.decrypt(message);
							message = decryptionMsg;
						}

						pool.execute(new ServerThread(message));
						
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
