import java.io.*;
import java.net.*;

public class EchoClient {

	public static void main(String[] args) throws Exception {

		System.out.println("EchoClient");
		
		DatagramSocket clientSocket = new DatagramSocket();
		
		//TODO: Change LocalHost IP to desired IP
		//InetAddress IPAddress = InetAddress.getByName("localhost");
		InetAddress HostAddressA = InetAddress.getByName("52.91.104.136");

		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		
		String payloadString = "This is EchoClient";
		sendData = payloadString.getBytes();
		
		//Communicate with server
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, HostAddressA, 2000);
		clientSocket.send(sendPacket);
		
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		
		String receivedSentence = new String(receivePacket.getData());
		System.out.println("Received IP: " + receivePacket.getAddress());
		System.out.println("Received Port: " + receivePacket.getPort());
	}

}
