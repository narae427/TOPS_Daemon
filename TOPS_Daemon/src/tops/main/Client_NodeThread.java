package tops.main;
import java.io.*;
import java.net.*;
import tops.struct.*;

public class Client_NodeThread extends Thread {
	protected FreindNode fNode = null;
	protected Message msg = new Message();
	protected UpdateFiles UF = null;
	Socket socket = null;

	public Client_NodeThread(FreindNode fNode) throws UnknownHostException,
			IOException {
		this.fNode = fNode;
	}
	
	public void readyForReceive_BloomFilter(String friendId) throws Exception {
		synchronized(this){
		socket = new Socket(InetAddress.getByName(fNode.privateIP),fNode.privatePN);
		try {
			Client.sendMSG(fNode, msg.Request_BloomFilter(), true);
		} catch (IOException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}
		
		UF = new UpdateFiles(socket);
		UF.ReceiveBloomFilter(MessageType.Data, fNode);
		
		socket.close();
		}
	}

	public void readyForReceiveUpdateFile_UPDATE() throws Exception {
		synchronized(this){
		socket = new Socket(InetAddress.getByName(fNode.privateIP),fNode.privatePN);
		
		try {
			Client.sendMSG(fNode, msg.Request_Updates(), true);
		
		} catch (IOException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}
		
		UF = new UpdateFiles(socket);
		UF.ReceiveUpdateFiles(MessageType.Updates, fNode);
		
		socket.close();
		}
	}

	public void readyForReceiveUpdateFile_DATA(String friendId) throws Exception {
		synchronized(this){
		socket = new Socket(InetAddress.getByName(fNode.privateIP),fNode.privatePN);
		try {
			Client.sendMSG(fNode, msg.Request_Data(friendId), true);
		
		} catch (IOException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}
		
		UF = new UpdateFiles(socket);
		UF.ReceiveUpdateFiles(MessageType.Data, fNode);
		
		socket.close();
		}
	}

	
	public void readyForRecieveFile(InetAddress ia, int portNumber,String fileName) throws Exception {
		synchronized(this){

		socket = new Socket(InetAddress.getByName(fNode.privateIP),fNode.privatePN);
		
		try {
			Client.sendMSG(fNode, msg.Request_File( fileName), true);
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		UF = new UpdateFiles(socket);
		UF.ReceiveFile(fileName); 
		socket.close();
		}
	}

	public void run() {
			try {

				System.out.println("Advertisement_Login1  to " + fNode.freindID);
				
				Client.sendMSG(fNode, msg.Advertisement_Login1(), true);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println();
				System.out.println("*** CLIENT "
						+ "**************************************"
						+ fNode.publicPN + "���� ����");
				System.out.println();
				e.printStackTrace();
			}
	}
		

}
