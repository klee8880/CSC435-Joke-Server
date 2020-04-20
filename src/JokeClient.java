import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/*--------------------------------------------------------

1. Name / Date: Kevin Lee 4/19/2020

2. Java version used, if not the official version for the class:

e.g. build 1.5.0_06-b05

3. Precise command-line compilation examples / instructions:

> javac JokeClient.java

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

public class JokeClient {
	
	private static final int primaryPort= 4545;
	private static final int secondaryPort = 4546;
	
	public static void main (String args []) {
		
		//Variable
		boolean primaryMode = true;
		String primaryServer;
		String secondServer;
		
		//Optionally acquire the host's & secondary hots's name if it was provided
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
		
		StringBuilder output = new StringBuilder("Kevin Lee's Joke Client, v.03 \nServer One: " );
		
		output.append(primaryServer).append(", port ").append(primaryPort).append('\n');
		
		if (secondServer != null) {output.append("Server Two: ").append(secondServer).append(", port ").append(secondaryPort);}
		
		
		System.out.println(output.toString());
		System.out.flush();
		
		//Read from the local console
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			String user;
			String command;
			
			//Get User Name
			System.out.print("Please enter a username: ");
			user = in.readLine();
			
			//Open a file for the user
			BufferedWriter writer = new BufferedWriter(new FileWriter(user + ".txt"));
			
			//Communicate with the console
			System.out.println("Commands:\np\t- get a new phrase\ns\t- Switch btw primary & secondary server\nquit\t- terminate connections\n");
			
			do {
			
			//Request new Input
			System.out.print("Command: ");
			command = in.readLine();
			System.out.println();
			
			switch(command) {
			//Make communication with the target server
			case "p":
				requestPhrase(primaryServer, primaryPort, user, writer);
				break;
			//Switch modes between the primary and secondary server
			case "s":
				if (primaryMode && secondServer != null) {
					primaryMode = false;
					System.out.println("Switched to Secondary Server");
				}
				else {
					primaryMode = true;
					System.out.println("Switched to Primary Server");
				}
				break;
			case "quit":
				break;
			default:
				System.out.println("Unrecognized Command");
				break;
			}
				
			} while (command.indexOf("quit") < 0);
			
			writer.close();
			System.out.println("Local terminal stopped by user request.");
			
		}catch (IOException ioe) {
			//Print error code stack trace to the console.
			ioe.printStackTrace();
		}
		
	}
	
	static void requestPhrase(String Server, int port, String username, BufferedWriter writer) {
		
		Socket sock;
		BufferedReader fromStream;
		PrintStream toStream;
		
		try {
			
			//Acquire connection
			sock = new Socket(Server, port);
			fromStream = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toStream = new PrintStream(sock.getOutputStream());
			
			//Send request to the server with the user name.
			toStream.println(username);

			//Read 2 line from the server
			for (int i = 0; i < 2; i++) {
				String result = fromStream.readLine();
				if (result != null) {
					System.out.println(result);
					writeFile(writer, result);
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
	}

	//Write server result to a file for audit
	static synchronized void writeFile(BufferedWriter writer, String line) {
		
		try {
			writer.write(line);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
