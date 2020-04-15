import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/*--------------------------------------------------------

1. Name / Date:

2. Java version used, if not the official version for the class:

e.g. build 1.5.0_06-b05

3. Precise command-line compilation examples / instructions:

e.g.:

> javac JokeServer.java

4. Precise examples / instructions to run this program:

e.g.:

In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For exmaple, if the server is running at
140.192.1.22 then you would type:

> java JokeClient 140.192.1.22
> java JokeClientAdmin 140.192.1.22

5. List of files needed for running the program.

e.g.:

 a. checklist.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

5. Notes:

e.g.:

I faked the random number generator. I have a bug that comes up once every
ten runs or so. If the server hangs, just kill it and restart it. You do not
have to restart the clients, they will find the server again when a request
is made.

----------------------------------------------------------*/

public class JokeClient {
	
	private static final int primaryPort= 4545;
	private static final int secondaryPort = 4546;
	
	public static void main (String args []) {
		
		Boolean primaryMode = true;
		
		String primaryServer;
		String secondServer;
		
		//Optionally acquire the host's & secondary hots's name if it was provided
		if (args.length < 1) {
			primaryServer = "localhost";
			secondServer = null;
		}
		else if (args.length < 2) {
			primaryServer = args[0];
			secondServer = null;
		}
		else {
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
			
			//Communicate with the console
			System.out.println("Commands:\np\t- get a new phrase\ns\t- Switch btw primary & secondary server\nquit\t- terminate connections\n");
			
			do {
			
			//Request new Input
			System.out.print("Command: ");
			command = in.readLine();
			System.out.println();
			
			switch(command) {
			
			case "p":
				requestPhrase(primaryServer, primaryPort, user);
				break;
				
			case "s":
				if (primaryMode) {
					primaryMode = false;
					System.out.println("Switched to Secondary Server");
				}
				else {
					primaryMode = true;
					System.out.println("Switched to Primary Server");
				}	
				break;
				
			default: 
				break;
				
			}
				
			} while (command.indexOf("quit") < 0);
			
			System.out.println("Local terminal stopped by user request.");
			
		}catch (IOException ioe) {
			//Print error code stack trace to the console.
			ioe.printStackTrace();
		}
		
	}
	
	static void requestPhrase(String Server, int port, String command) {
		
		Socket sock;
		BufferedReader fromStream;
		PrintStream toStream;
		
		try {
			
			//Acquire connection
			sock = new Socket(Server, port);
			fromStream = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toStream = new PrintStream(sock.getOutputStream());
			
			toStream.println(command);

			//Read line from the server
			for (int i = 0; i < 2; i++) {
				String result = fromStream.readLine();
				if (result != null) System.out.println(result);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
	}

}
