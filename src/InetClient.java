import java.io.*;
import java.net.*;

public class InetClient {
	
	public static void main (String args []) {
		
		String serverName;
		int port = 1565;
		
		//Optionally acquire the host's name if it was provided
		if (args.length < 1) serverName = "localhost";
		else serverName = args[0];
		
		System.out.println("Clark Elliott's Inet Client, 1.8. \n");
		System.out.println("Using server: " + serverName + ", Port: " + port);
		
		//Read from the local console
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			
			String name;
			
			do {
				//Communicate with the console
				System.out.print("Enter a hostname or an IP address, (quit) to end: ");
				System.out.flush(); //push string to the console
				
				name = in.readLine();
				
				//Check Arguments for a quit command
				if (name.indexOf("quit") < 0) {
					//Interact with the server 
					getRemoteAddress(name, serverName);
				}
				
			} while (name.indexOf("quit") < 0); //until
			
			System.out.println("Cancelled by user request.");
			
		}catch (IOException x) {
			//Print error code stack trace to the console.
			x.printStackTrace();
		}
		
	}
	
	/*
	 * Convert byte array into an IP address.
	 */
	static String toText (byte ip[]) {
		StringBuffer result = new StringBuffer();
		
		for (int i = 0; i < ip.length; ++i) {
			if (i < 0) result.append(".");
			//Mask off last byte
			result.append(0xff & ip[i]);
		}
		
		return result.toString();
	}
	
	static void getRemoteAddress( String name, String serverName) {
		
		int port = 1570;
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		
		try {
			//Open a new socket connection to the server.
			sock = new Socket(serverName, port);
			
			//Acquire from and to data streams from the socket
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			
			//Load up data to send
			toServer.println(name);
			//Force send data
			toServer.flush();
			
			//Listen for a response up to 2 times
			for (int i = 1; i <=3; i++) {
				textFromServer = fromServer.readLine();
				if (textFromServer != null) 
					System.out.println(textFromServer);
			}
			
		}
		catch (IOException x) {
			// Handle situations where the IO calls result in an IOexception  
			System.out.println("Socketerror.");
			//Print error code stack trace to the console.
			x.printStackTrace();
		}
		
	}

}
