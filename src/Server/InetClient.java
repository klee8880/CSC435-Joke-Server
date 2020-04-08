package Server;

import java.io.*;
import java.net.*;

public class InetClient {
	
	public static void main (String args []) {
		
		String serverName;
		
		//Optionally acquire the host's name 
		if (args.length < 1) serverName = "localhost";
		else serverName = args[0];
		
		System.out.println("Clark Elliott's Inet Client, 1.8. \n");
		System.out.println("Using server: " + serverName + ", Port: 1565");
		
		//Read from the local console
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			
			String name;
			
			
			do {
				System.out.print("Enter a hostname or an IP address, (quit to end: ");
				System.out.flush();
				
				name = in.readLine();
				
				//Check Arguments for a quit command
				if (name.indexOf("quit") < 0) {
					getRemoteAddress(name, serverName);
				}
			} while (name.indexOf("quit") < 0);
			
			System.out.println("Cancelled by user request.");
			
		}catch (IOException x) {
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
		
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		
		try {
			//Open a new socket connection to the server.
			sock = new Socket(serverName, 1565);
			
			//Acquire to from and to data streams
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			
			//Load up data to send
			toServer.println(name);
			//Force send data
			toServer.flush();
			
			//Listen for a response up to 3 times
			for (int i = 1; i <=3; i++) {
				textFromServer = fromServer.readLine();
				if (textFromServer != null) 
					System.out.println(textFromServer);
			}
			
		}
		catch (IOException x) {
			System.out.println("Socketerror.");
			x.printStackTrace();
		}
		
	}

}
