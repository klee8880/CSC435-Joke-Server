import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/*--------------------------------------------------------

1. Name / Date: Kevin Lee 4/19/2020

2. Java version used, if not the official version for the class:

e.g. build 1.5.0_06-b05

3. Precise command-line compilation examples / instructions:

> javac JokeServer.java

4. Precise examples / instructions to run this program:

In separate shell windows or computers:

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

----------------------------------------------------------*/

import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

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
		return header + " <" + username + "> : \"" + phrase+ "\"";
	}
}

class Account{
	List <Phrase> jokes;
	int jokeIndex = 0;
	List <Phrase> proverbs;
	int proverbIndex = 0;
	
	public Account(Phrase[] jokeList, Phrase[] proverbList) {
		super();
		this.jokes = Arrays.asList(jokeList);
		this.proverbs = Arrays.asList(proverbList);
		
		Collections.shuffle(this.jokes);
		Collections.shuffle(this.proverbs);
	}
	
	public void resetJokes() {
		Collections.shuffle(jokes);
	}
	
	public void resetProverbs() {
		Collections.shuffle(proverbs);
	}
	
	public String nextJoke(String user) {
		
		Phrase result = jokes.get(jokeIndex);
		jokeIndex++;
		
		if (jokeIndex >= jokes.size()) {
			resetJokes();
			jokeIndex = 0;
			return result.generate(user) + "\nJOKE CYCLE COMPLETED";
		}
		else
			return result.generate(user) + "\n";
	}

	public String nextProverb(String user) {
		
		Phrase result = proverbs.get(proverbIndex);
		proverbIndex++;
		
		if (proverbIndex >= proverbs.size()) {
			resetProverbs();
			proverbIndex = 0;
			return result.generate(user) + "\nPROVERB CYCLE COMPLETED";
		}
		else
			return result.generate(user) + "\n";
	}

}

class AccountList{
	
	private Map<String, Account> accounts = new Hashtable<String, Account>();
	
	//Add an account to the map. Returns the account itself
	public synchronized Account addAccount(String user, Phrase [] jokes, Phrase [] proverbs){
		Account newAccount = new Account(jokes, proverbs);
		accounts.put(user, newAccount);
		return newAccount;
	}
	
	//Look up account for user.
	public synchronized Account findAccount(String user) {
		Account result = accounts.get(user);
		return result;
	}
}

public class JokeServer{
	
	//Parameters
	private static final int queueLength = 6;
	private static boolean mode = true;
	public static AccountList accounts= new AccountList();

	public static void main (String [] args)  throws IOException{
		int clientPort;
		int adminPort;
		boolean secondary = false;
		
		//Determine if which port to monitor depending on if this is the primary or secondary server.
		if (args.length > 0 && args[0].equals("secondary")) {
			clientPort = 4546;
			adminPort = 5051;
			secondary = true;
		}
		else {
			clientPort = 4545;
			adminPort = 5050;
		}
		
		//Tell the user about the ports and servers in use.
		System.out.println("Listening for client connections on port: " + clientPort);
			
		//spawn mode server thread.
		System.out.println("Listening for admin connections on port: " + adminPort);
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
			
			System.out.println("Connection Acquired");
			//Run program with the connection.
			new Speaker(sock, secondary).start();
		}
	}
	
	//get the current mode of the server
	//Semaphore locked to avoid thread conflicts
	public static synchronized boolean getMode() {
	
		boolean result = mode;
		return result;
	}
	
	//toggle the mode of the server
	//Semaphore locked to avoid thread conflicts
	public static synchronized void changeMode() {
		if (mode) mode = false;
		else mode = true;
	}
	
}


//----Joke/Proverb Handlers----

//Return jokes and proverbs to the client on command.
class Speaker extends Thread {

	Socket sock;
	
	int jokeIndex = 0;
	int proverbIndex = 0;
	String username;
	boolean secondary;
	
	//Static arrays
	public static Phrase [] jokeList= {
			new Phrase("JA", "There was a kidnapping earlier, but I woke him up."),
			new Phrase("JB", "What did the sushi say to the bee? Wasabi."),
			new Phrase("JC", "Hear about the guy who got his left side removed? He's alright now."),
			new Phrase("JD", "My friends bakery burned down, now his business is toast.")
	};
	
	public static Phrase [] proverbList= {
			new Phrase("PA", "A bird in the hand, is worth 2 in the bush."),
			new Phrase("PB", "Look strong when you are weak, and weak when you are strong."),
			new Phrase("PC", "If you know your enemy, and know yourself, you need not fear the result of a hundred battles"),
			new Phrase("PD", "I know not which ways the wind will blow. Only how to set my sails.")
	};
	
	public Speaker(Socket socketPassed, boolean secondary) {
		super();
		this.sock = socketPassed;
		this.secondary = secondary;
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
			Account account = JokeServer.accounts.findAccount(username);
			if (account == null) account = JokeServer.accounts.addAccount(username, jokeList, proverbList);
			
			//print a joke or proverb (Account takes care of it's own list cycle)
			String result;
			
			if (JokeServer.getMode())
				result = account.nextJoke(username);
			else 
				result = account.nextProverb(username);
			
			if (secondary)
				out.println("<S2> " + result);
			else
				out.println(result);
			
		}
		catch(Exception ex) {ex.printStackTrace();} 
	}
	
}

//----Administrator connection handler----

//Handles requests to change the mode from the administrator port
class ModeChanger extends Thread {
	
	private int adminPort;
	private static final int queueLength = 6;
	
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
				System.out.println("Admin connection accepted");
				//Run program with the connection on a new thread.
				new AdminHandler(sock).run();
			}
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
}

class AdminHandler extends Thread{
	
	Socket sock;
	BufferedReader in = null;
	PrintStream out = null;
	
	public AdminHandler(Socket sock){
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
			
			System.out.println(command);
			
			switch (command) {
			case "t": //Toggle the server's mode btw joke and proverb
				JokeServer.changeMode();
				out.println("Mode changed");
			default:
				out.println("(ERROR) Unrecognized command string");
				break;
			}
			
		}catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
	}
}
