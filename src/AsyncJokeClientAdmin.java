/*--------------------------------------------------------

1. Name / Date: Kevin Lee 4/19/2020

2. Java version used, if not the official version for the class:

build 1.8.0_161

3. Precise command-line compilation examples / instructions:
> javac AsyncJokeServer.java
> javac AsyncJokeClient.java
> javac AsyncJokeClientAdmin.java

4. Precise examples / instructions to run this program:

In separate shell windows:

> java AsyncJokeServer (2)
> java AsyncJokeClient (n)
> java AsyncJokeClientAdmin

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For exmaple, if the server is running at
140.192.1.22 then you would type:

> java JokeClient 140.192.1.22
> java JokeClientAdmin 140.192.1.22

5. List of files needed for running the program.

 a. AsyncJokeServer.java
 b. AsyncJokeClient.java
 c. AsyncJokeClientAdmin.java

5. Notes:
If no secondary port is specified in the main arguments then it is assumed no secondary is needed. 
----------------------------------------------------------*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;


public class AsyncJokeClientAdmin {
	
	public static void main (String args []) {
		String primaryServer;
		String secondServer;
		boolean primaryMode = true;
		Map<String, Integer> servers = new Hashtable<String, Integer>();
		
		//TODO: Redo argument list.
		//Default single port
		if (args.length < 1) {servers.put("A", 4545);}
		//Additional ports
		else {
			//Assign a new ASCII character to each provided port #
			int ascii = 65;
			for (int i = 0; i < args.length; i++) {
				try {
					int port = Integer.parseInt(args[i]);
					servers.put("" + (char) ascii, port);					
					ascii++;//increment ASCII
				} catch (Exception e) {}
			}
		}
		
		//Prompt the user with command options
		System.out.println("Kevin Lee's Joke Client, v.03");
		System.out.println("Enter A, B, C, etc to toggle the server.");
		System.out.println("Quit - end program");
		
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		
		//Process user input(s)
		try {
			
			String command;
			
			while (true) {			
				//Get their command
				System.out.print("Command: ");
				command = console.readLine().trim().toUpperCase();
				//Break the loop on QUIT
				if (command.equals("QUIT")) break;
				
				Integer port = servers.get(command);
				
				if (port == null) {
					System.out.println("Unsupported command.");
				}
				else {
					connect(port, "localhost", "t");
				}
			}
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	private static void connect (int portNum, String server, String command) {
		
		Socket sock;
		BufferedReader fromStream;
		PrintStream toStream;
		
		try {
			
			//Acquire connection
			sock = new Socket(server, portNum);
			fromStream = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toStream = new PrintStream(sock.getOutputStream());
			
			//Send request to the server
			toStream.println(command); toStream.flush();
			
			//Read back response and print to user.
			String result = fromStream.readLine();
			if (result != null) System.out.println(result);
			
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
	}
	
}
