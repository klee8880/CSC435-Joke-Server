
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

/*
 * Administered for the serving jokes/proverbs to clients, Also handles when an administrator logs in on a separate channel.
 * Spawns threads to handle all the clients
 */

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class Phrase{
	public String header;
	public String phrase;	
	//Constructor
	public Phrase(String header, String phrase) {
		super();
		this.header = header;
		this.phrase = phrase;
	}
	public String generate (String username) {
		return new StringBuilder(header).append(" <").append(username).append("> : \"").append(phrase).append("\"").toString();
	}
}

enum ServerMode {
	joke,
	proverb;
}

public class JokeServer{
	
	static ServerMode mode = ServerMode.joke;
	
	private static Phrase [] jokes= {
			new Phrase("JA", "..."),
			new Phrase("JB", "..."),
			new Phrase("JC", "..."),
			new Phrase("JD", "...")
	};
	
	private static Phrase [] proverb= {
			new Phrase("PA", "..."),
			new Phrase("PB", "..."),
			new Phrase("PC", "..."),
			new Phrase("PD", "...")
		};
	
	//Parameters
	private static int queueLength = 6;
	
	
	//Execution
	public static void main (String [] args)  throws IOException{
		int clientPort;
		int adminPort;
		
		//Determine if which port to monitor depending on if this is the primary or secondary server.
		if (args.length > 0 && args[0].equals("secondary")) {
			clientPort = 4546;
			adminPort = 5051;
		}
		else {
			clientPort = 4545;
			adminPort = 5050;
		}
			
		//spawn mode server thread.
		System.out.println("Spawning admin mode change thread...");
		new ModeChanger(adminPort).start();
			
		//Wait for client connections and spawn threads.
		Socket sock;
		ServerSocket servsock = new ServerSocket (clientPort, queueLength);
		
		System.out.println("JokeServer now active.\nawaitning new requests...");
		
		//Busy waiting for a new connection request.
		while (true) {
			//Initialize a new connection
			sock = servsock.accept();
			//Run program with the connection.
			System.out.println("New connection Acquired...");
			new Orator(sock, jokes, proverb).start();
		}
	}
	
}

//Return jokes and proverbs to the client on command.
class Orator extends Thread {

	Socket sock;
	List<Phrase> jokes;
	List<Phrase> proverbs;
	int jokeIndex = 0;
	int proverbIndex = 0;
	String username;
	
	//Constructor
	public Orator(Socket sock, Phrase[] jokes, Phrase[] proverbs) {
		this.sock = sock;
		this.jokes = Arrays.asList(jokes);
		this.proverbs = Arrays.asList(proverbs);
		
		//Randomize the list of phrases to be presented
		Collections.shuffle(this.jokes);
		Collections.shuffle(this.proverbs);
	}
	
	public void run() {
		
		BufferedReader in = null;
		PrintStream out = null;
		
		try {
			//Acquire in and out streams
			in = new BufferedReader (new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());
			
			//Prompt for a User Name
			username = in.readLine();
			//TODO: Add ability to log on and return to previous position.
			
			
			//Loop Jokes until the user requests disconnection.
			String input = in.readLine();
			
			while (input.indexOf("quit") < 0) {
				
				//Joke Function
				if (JokeServer.mode == ServerMode.joke) {
					if (input.equals("p")) {
						out.println(jokes.get(jokeIndex).generate(username));
						jokeIndex++;
					}
					
					if (jokeIndex >= jokes.size()) {
						jokeIndex = 0;
						out.println("JOKE CYCLE COMPLETE");
					}
					else out.println();
				}
				
				//Proverb Function
				else {
					if (input.equals("p")) {
						out.println(proverbs.get(proverbIndex).generate(username));
						proverbIndex++;
					}
					
					if (proverbIndex >= jokes.size()) {
						proverbIndex = 0;
						out.println("JOKE CYCLE COMPLETE");
					}
					else out.println();
				}
				
				input = in.readLine();
			}
			
			System.out.println("Connection Terminated...");
			
		}
		catch(IOException ioe) {ioe.printStackTrace();} 
	}
	
}


//Handles requests to change the mode from the administrator port
class ModeChanger extends Thread {
	
	private int adminPort;
	private static int queueLength = 6;
	
	//Constructor(s)
	ModeChanger (int port){
		adminPort = port;
	}
	
	//Execution
	public void run() {
		
		Socket sock;
		
		try {
			ServerSocket servsock = new ServerSocket (adminPort, queueLength);
			
			//Busy waiting for a new connection request.
			while (true) {
				//Initialize a new connection
				sock = servsock.accept();
				//Run program with the connection.
			}
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
}






