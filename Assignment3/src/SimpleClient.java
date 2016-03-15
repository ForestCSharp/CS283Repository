import java.io.*;
import java.net.*;

public class SimpleClient {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		System.out.println("SimpleClient");
		
		DatagramSocket clientSocket = new DatagramSocket();
		
		//TODO: Change LocalHost IP to desired IP
		InetAddress IPAddress = InetAddress.getByName("localhost");
		
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		
		String payloadString = "This is SimpleClient";
		sendData = payloadString.getBytes();
		
		//Communicate with server
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 2000);
		clientSocket.send(sendPacket);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		
		String receivedSentence = new String(receivePacket.getData());
		System.out.println("From Server: " + receivedSentence);
	}

}
