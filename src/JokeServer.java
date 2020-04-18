
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
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

enum ServerMode {
	joke("j"),
	proverb("p");
	
	private String base;
	
	ServerMode(String base){
		this.base = base;
	}
	
	@Override
	public String toString() {
		return base;
	}
}

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

class Account{
	List <Phrase> jokes;
	int jokeIndex = 0;
	List <Phrase> proverbs;
	int proverbIndex = 0;
	
	public Account(List<Phrase> jokes, List<Phrase> proverb) {
		super();
		this.jokes = jokes;
		this.proverbs = proverb;
		
		resetJokes();
		resetProverbs();
	}
	
	public void resetJokes() {
		Collections.shuffle(jokes);
		jokeIndex = 0;
	}
	
	public void resetProverbs() {
		Collections.shuffle(proverbs);
		proverbIndex = 0;
	}
	
	public String nextJoke(String user) {
		
		Phrase result = jokes.get(jokeIndex);
		jokeIndex++;
		
		if (jokeIndex >= jokes.size()) {
			resetJokes();
			return new StringBuilder(result.generate(user)).append("\nJOKE CYCLE COMPLETED").toString();
		}
		else {
			return new StringBuilder(result.generate(user)).append("\n").toString();
		}
	}

	public String nextProverb(String user) {
		
		Phrase result = proverbs.get(proverbIndex);
		proverbIndex++;
		
		if (proverbIndex >= proverbs.size()) {
			resetProverbs();
			return new StringBuilder(result.generate(user)).append("\nPROVERB CYCLE COMPLETED").toString();
		}
		else {
			return new StringBuilder(result.generate(user)).append("\n").toString();
		}
	}

}

public class JokeServer{

	static Dictionary<String, Account> accounts = new Hashtable<String, Account>();;
	
	public static ServerMode mode = ServerMode.joke;
	
	public static Phrase [] jokeList= {
			new Phrase("JA", "..."),
			new Phrase("JB", "..."),
			new Phrase("JC", "..."),
			new Phrase("JD", "...")
	};
	
	public static Phrase [] proverbList= {
			new Phrase("PA", "..."),
			new Phrase("PB", "..."),
			new Phrase("PC", "..."),
			new Phrase("PD", "...")
		};
	
	//Parameters
	private static final int queueLength = 6;
	
	
	//Execution
	public static void main (String [] args)  throws IOException{
		int clientPort;
		int adminPort;
		
		//Determine if which port to monitor depending on if this is the primary or secondary server.
		//jokeServer
		//jokeServer secondary
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
		@SuppressWarnings("resource")
		ServerSocket servsock = new ServerSocket (clientPort, queueLength);
		
		System.out.println("JokeServer now active.\nawaitning new requests...");
		
		//Busy waiting for a new connection request.
		while (true) {
			//Initialize a new connection
			sock = servsock.accept();
			//Run program with the connection.
			new Speaker(sock).start();
		}
	}
	
}

//----Joke/Proverb Handlers----

//Return jokes and proverbs to the client on command.
class Speaker extends Thread {

	Socket sock;
	
	int jokeIndex = 0;
	int proverbIndex = 0;
	String username;
	
	public Speaker(Socket sock) {
		super();
		this.sock = sock;
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
			//standardize user names for case.
			username = username.toUpperCase();
			
			//Look up user and add if not found
			Account account = JokeServer.accounts.get(username);
			if (account == null) {
				account = new Account(Arrays.asList(JokeServer.jokeList), Arrays.asList(JokeServer.proverbList));
				JokeServer.accounts.put(username, account);
			}
			
			//print a joke or proverb (Account takes care of it's own list cycle)
			String result;
			
			if (JokeServer.mode == ServerMode.joke)
				result = account.nextJoke(username);
			else 
				result = account.nextProverb(username);
			
			out.println(result);
			
		}
		catch(IOException ioe) {ioe.printStackTrace();} 
	}
	
}

//----Administrator connection handler----

//Handles requests to change the mode from the administrator port
class ModeChanger extends Thread {
	
	private int adminPort;
	private static int queueLength = 6;
	
	//Constructor(s)
	ModeChanger (int port){
		adminPort = port;
	}
	
	//Execution
	@Override
	public void run() {
		
		Socket sock;
		
		try {
			@SuppressWarnings("resource")
			ServerSocket servsock = new ServerSocket (adminPort, queueLength);
			
			//Busy waiting for a new connection request.
			while (true) {
				//Accept a new connection
				sock = servsock.accept();
				//Run program with the connection on a new thread.
				new adminHandler(sock).run();
			}
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
}

class adminHandler extends Thread{
	
	Socket sock;
	BufferedReader in = null;
	PrintStream out = null;
	
	public adminHandler(Socket sock){
		this.sock = sock;
	}
	
	@Override
	public void run() {
		
		try {
			
			//Acquire in and out streams
			in = new BufferedReader (new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());

			//Get instruction
			String command = in.readLine();
			
			switch (command) {
			case "t": //Toggle the server's mode btw joke and proverb
				if (JokeServer.mode == ServerMode.joke) {
					JokeServer.mode = ServerMode.proverb;
					out.println("Server changed to PROVERB mode...");
					System.out.println("Server changed to PROVERB mode...");
				}
				else {
					JokeServer.mode = ServerMode.joke;
					out.println("Server changed to JOKE mode...");
					System.out.println("Server changed to JOKE mode...");
				}
				break;
				
			default:
				out.println("(ERROR) Unrecognized command string");
				break;
			}
			
		}catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
	}
}















