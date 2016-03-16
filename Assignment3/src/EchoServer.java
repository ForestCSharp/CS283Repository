import java.io.*; 
import java.net.*;

public class EchoServer {

	public static void main(String[] args) throws Exception {
		
		System.out.println("EchoServer");
		
		DatagramSocket serverSocket = new DatagramSocket(2000);
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		while(true)
		{
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			String receivedString = new String(receivePacket.getData());
			
			//Print received data
			System.out.println("RECEIVED: " + receivedString);
			
			InetAddress receivedIP = receivePacket.getAddress();
			int port = receivePacket.getPort();
			
			//Print out sender info
			System.out.println("IPAddress: " + receivedIP);
			System.out.println("Port: " + port);
			
			//  Echo Functionality Below  //
			sendData = receivedString.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivedIP, port);
			serverSocket.send(sendPacket);
		}	
	}

}
