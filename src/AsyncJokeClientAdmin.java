/*--------------------------------------------------------

1. Name / Date: Kevin Lee 4/19/2020

2. Java version used, if not the official version for the class:

build 1.8.0_161

3. Precise command-line compilation examples / instructions:

> javac JokeClientAdmin.java

4. Precise examples / instructions to run this program:

In separate shell windows:

> java JokeServer (2)
> java JokeClient
> java JokeClientAdmin

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For exmaple, if the server is running at
140.192.1.22 then you would type:

> java JokeClient 140.192.1.22
> java JokeClientAdmin 140.192.1.22

5. List of files needed for running the program.

 a. JokeServer.java
 b. JokeClient.java
 c. JokeClientAdmin.java

5. Notes:
If no secondary port is specified in the main arguments then it is assumed no secondary is needed. 
----------------------------------------------------------*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;


public class AsyncJokeClientAdmin {
	
	private static final int primaryPort= 5050;
	private static final int secondaryPort = 5051;
	
	public static void main (String args []) {
		String primaryServer;
		String secondServer;
		boolean primaryMode = true;
				
		//Process the args array or use local host as default
		if (args.length < 1) { // Default to using 1 connection local host
			primaryServer = "localhost";
			secondServer = null;
		}
		
		else if (args.length < 2) { // Use provided argument for connection
			primaryServer = args[0];
			secondServer = null;
		}
		else { // User provided 2 arguments for connections
			primaryServer = args[0];
			secondServer = args[1];
		}
		
		//Prompt the user with command options
		System.out.println("Kevin Lee's Joke Client, v.03");
		System.out.println("S - switch btw primary and secondary servers");
		System.out.println("T - toggle the current server's mode");
		System.out.println("Quit - end program");
		
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		
		//Process user input(s)
		try {
			
			String command;
			
			do {			
				//Get their command
				System.out.print("Command: ");
				command = console.readLine();
				
				switch (command.toUpperCase()) {
				
				case "S": //Switch btw the primary and secondary server
					if (secondServer == null) {
						System.out.println("No secondary server was provided on startup");
					}
					else if (primaryMode) {
						primaryMode = false;
						System.out.println("Changed to secondary mode");
					}
					else {
						primaryMode = true;
						System.out.println("Changed to primary mode");
					}
					break;
				
				case "T": //Toggle current server btw Joke and Proverb mode.
					if (primaryMode)
						connect(primaryPort, primaryServer, "t");
					else 
						connect(secondaryPort, secondServer, "t");
					break;
				
				case "Quit":
					break;
					
				default:
					System.out.println("(ERROR) Unrecognized input");
					break;
				}
				
			} while (!command.equals("Quit"));
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
