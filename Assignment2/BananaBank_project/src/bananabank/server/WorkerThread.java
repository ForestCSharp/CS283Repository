package bananabank.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class WorkerThread implements Runnable {
	
	protected Socket clientSocket = null;
	protected String inText = null;
	protected BananaBank Bank = null;
	
	public WorkerThread(Socket clientSocket, String inText, BananaBank bank) {
		this.clientSocket = clientSocket;
		this.inText = inText;
		this.Bank = bank;
	}
	
	public void run() {
		try {
			
			System.out.println("Worker Thread Entered");
		
				BufferedReader inFromClient = 
						new BufferedReader(
								new InputStreamReader(clientSocket.getInputStream()));
				
				PrintWriter outToClient = 
						new PrintWriter(clientSocket.getOutputStream());
				
			
			//Parse Input Stream (Fencepost)
			inText = null;
			
			//Take all of clients requests (inText not null?)
			while((inText = inFromClient.readLine())!= null) {
				
				
				//Space Delimiter
				String delims = "[ \n]+";
				String[] tokens = inText.split(delims);
				
				//Get IP Address	
				String ClientAddress = clientSocket.getRemoteSocketAddress().toString();
				String IPdelims = "[/:]+";
				String[] IPSplit = ClientAddress.split(IPdelims);
				
				//Check if IP Address is Local
				boolean IsLocal = false;
				if (IPSplit.length >1)
				{
					IsLocal = IPSplit[1].equals("127.0.0.1");
				}
							
				System.out.println(inText);
				
				if (tokens.length == 3) //Potential Account Transaction
				{
					
					//Get three data values
					int amount = Integer.parseInt(tokens[0]);
					int sourceAccountNum = Integer.parseInt(tokens[1]);
					int destAccountNum = Integer.parseInt(tokens[2]);
	
					
					//Check that both accounts exist (Find operations, no sync needed)
					Account SrcAccount = Bank.getAccount(sourceAccountNum);
					Account DstAccount = Bank.getAccount(destAccountNum);
					
					
					//Check that both accounts exist
					if (SrcAccount != null && DstAccount != null)
					{
						//Check that Source Account has enough funds (need to lock srcAccount starting at check amount and only release after transfer is complete)
						synchronized(SrcAccount)
						{
							boolean hasEnoughMoney = SrcAccount.getBalance() >= amount;
	
							//If enough funds: lock two accounts in question and make transfer
							if(hasEnoughMoney)
							{
									synchronized(DstAccount) //Lock Destination
									{
										SrcAccount.transferTo(amount, DstAccount);
										
										outToClient.println(Integer.toString(amount) 
												+ " transfered from " + Integer.toString(sourceAccountNum) 
												+ " to " + Integer.toString(destAccountNum));
										
										System.out.println(Integer.toString(amount) 
												+ " transfered from " + Integer.toString(sourceAccountNum) 
												+ " to " + Integer.toString(destAccountNum));
									}
							}
							else //Print error message about lack of funds
							{
								outToClient.println("Error: Not Enough Funds in Account " + tokens[1]);
								System.out.println("Error: Not Enough Funds in Account " + tokens[1]);
							}
						}
					
					}
					else //Print error message about account existence
					{
						if (SrcAccount == null)
						{
							outToClient.println("Invalid Source Account");
							System.out.println("Invalid Source Account");
						}
						if (DstAccount == null)
						{
							outToClient.println("Invalid Destination Account");
							System.out.println("Invalid Destination Account");
						}
						
					}
					
					
				}
				else if ( IsLocal && tokens.length == 1 && tokens[0].equals("SHUTDOWN") ) //Shutdown message
				{
					int totalBalance=0;
					
					//TODO: Print Output to client
					ArrayList<Account> Accounts = new ArrayList<Account>(Bank.getAllAccounts());
					for (int i = 0; i<Accounts.size(); ++i)
					{
						totalBalance += Accounts.get(i).getBalance();
					}
					
					outToClient.println(Integer.toString(totalBalance));
					System.out.println(Integer.toString(totalBalance));
					
					//Set stop signal to true
					ServerMain.stopSignal = true;
					break;
					
				}
				else //Incorrect Input
				{
					outToClient.println("Error: Invalid Input");
					System.out.println("Error: Invalid Input");
				}
			}
			
			inFromClient.close();
			outToClient.close();
			clientSocket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
