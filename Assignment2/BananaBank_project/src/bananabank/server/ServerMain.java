package bananabank.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerMain {
	
	public static final int PORT = 2000;
	static boolean stopSignal = false;

	public static void main(String[] args) throws Exception {
		
		
		BananaBank Bank = new BananaBank("accounts.txt");
		
		ArrayList<Thread> Threads = new ArrayList<Thread>();
		ArrayList<Socket> Sockets = new ArrayList<Socket>();
		
		ServerSocket welcomeSocket = new ServerSocket(PORT);
		welcomeSocket.setSoTimeout(50);
		
		while(!stopSignal)
		{
			
			System.out.println("Tick");
			
			Socket connectionSocket = null;
			try {
				connectionSocket = welcomeSocket.accept();
				Sockets.add(connectionSocket);
			}
			catch (SocketTimeoutException e) {
				if (stopSignal)
				{
					System.out.println("Server Shutdown");
					break;
				}
				else
				{
					continue;
				}
			}
			catch (IOException e) {
				
				//Server stop signal when thread throws IOException after setting stopSignal
				if(stopSignal)
				{
					System.out.println("Server Shutdown");
					break;
				}
				throw new RuntimeException("Error accepting Client connection", e);
			}
			
			//Start thread for open connection
			Thread MyThread = new Thread(new WorkerThread(connectionSocket, "Multithreaded Server", Bank));
			
			Threads.add(MyThread);
			MyThread.start();
			
		}
		
		//stopSignal is now true. Need to wait for worker threads to finish
		for (int i = 0; i < Threads.size(); ++i)
		{
			Threads.get(i).join();		
		}
		
		Bank.save("accounts.txt");
		
		//Close the server socket
		welcomeSocket.close();
		

	}

}
