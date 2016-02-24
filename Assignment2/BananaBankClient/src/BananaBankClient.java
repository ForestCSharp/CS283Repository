import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;

public class BananaBankClient {

	
	static final String SERVER_ADDRESS = "localhost";
	public static final int PORT = 2000;
	
	
	public static void main(String[] args) {
		
		System.out.println("BananaBank Test Client");
		try {
			Socket socket = new Socket(BananaBankClient.SERVER_ADDRESS, BananaBankClient.PORT);

			PrintWriter outToClient = new PrintWriter(socket.getOutputStream());

			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			outToClient.println("50 33333 44444");
			outToClient.println("40 43434 43434");
			outToClient.println("60 33333 44444");
			outToClient.println("SHUTDOWN");
			outToClient.flush();
			
			
			String ResponseString = inFromClient.readLine();
			System.out.println("Client: " +ResponseString);
			
		}
		catch (IOException e) {
			
		}
	}

}
