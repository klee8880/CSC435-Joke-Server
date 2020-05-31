/*--------------------------------------------------------

1. Name / Date: Kevin Lee 4/19/2020

2. Java version used, if not the official version for the class:

build 1.8.0_161

3. Precise command-line compilation examples / instructions:

> javac JokeServer.java

4. Precise examples / instructions to run this program:

In separate shell windows or computers:

> java AsyncJokeServer (2)
> java AsyncJokeClient
> java AsyncJokeClientAdmin

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For example, if the server is running at
140.192.1.22 then you would type:

> java AsyncJokeClient 140.192.1.22
> java AsyncJokeClientAdmin 140.192.1.22

5. List of files needed for running the program.

 a. AsyncJokeServer.java
 b. AsyncJokeClient.java
 c. AsyncJokeClientAdmin.java

5. Notes:

----------------------------------------------------------*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
	static int portIndex = 6000;
	
	List <Phrase> jokes;
	int jokeIndex = 0;
	List <Phrase> proverbs;
	int proverbIndex = 0;
	int port; //Port # to listen on for this account. Would be better implemented as a connection pool.
	
	public Account(Phrase[] jokeList, Phrase[] proverbList) {
		super();
		this.jokes = Arrays.asList(jokeList);
		this.proverbs = Arrays.asList(proverbList);
		Collections.shuffle(this.jokes);
		Collections.shuffle(this.proverbs);
		
		//Assign this account a port
		port = portIndex;
		portIndex++;
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

public class AsyncJokeServer extends Thread{
	//Constants
	public static final String PORTTAG = "[PortReqeust: ";
	public static final String PHRASETAG = "[Phrase: ";
	private static final int queueLength = 6;
	
	//Parameters
	private Semaphore modeLock = new Semaphore(1);
	private boolean mode = true;
	public static AccountList accounts= new AccountList();
	private int clientPort;
	private int adminPort;
	
	//Constructor
	public AsyncJokeServer(int clientPort) {
		super();
		this.clientPort = clientPort;
		this.adminPort = clientPort + 10;
	}

	//Getters and Setters
	public boolean getMode() throws InterruptedException {
		modeLock.acquire();
		boolean result = mode;
		modeLock.release();
		return result;
	}
	
	public void changeMode() throws InterruptedException {
		modeLock.acquire();
		if (mode) mode = false;
		else mode = true;
		modeLock.release();
	}
	
	@Override
	public void run() {
		//Tell the user about the ports and servers in use.
		System.out.println("Listening for client connections on port: " + clientPort);
			
		//spawn mode server thread.
		System.out.println("Listening for admin connections on port: " + adminPort);
		new ModeChanger(adminPort, this).start();
			
		try {
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
				new Speaker(sock, this).start();
			}
		} catch (IOException e) {e.printStackTrace();}
	}
	
//-----MAIN CLASS FOR FILE-----
	public static void main (String [] args) throws Exception{
		int port;
		
		//Check arguments
		if (args.length < 1) {port = 4545;} //Defaults to 4545 if no arguments passed
		else {port = Integer.parseInt(args[0]);} //Parse the argument into a integer.
		//Spawn the server
		new AsyncJokeServer(port).start();
	}
}


//----Joke/Proverb Handlers----

//Return jokes and proverbs to the client on command.
class Speaker extends Thread {
	AsyncJokeServer parent;
	Socket sock;
	int jokeIndex = 0;
	int proverbIndex = 0;
	String command;
	
	//Static arrays
	public static Phrase [] jokeList= {
			new Phrase("JA", "There was a kidnapping earlier, but I woke him up."),
			new Phrase("JB", "What did the sushi say to the bee? Wasabi."),
			new Phrase("JC", "Hear about the guy who got his left side removed? He's alright now."),
			new Phrase("JD", "My friends bakery burned down, now his business is toast.")
	};
	
	public static Phrase [] proverbList= {
			new Phrase("PA", "A bird in the hand, is worth 2 in the bush."),
			new Phrase("PB", "Look strong where you are weak, and weak where you are strong."),
			new Phrase("PC", "True humility is the only antidote to shame."),
			new Phrase("PD", "I know not which ways the wind will blow. Only how to set my sails.")
	};
	
	public Speaker(Socket socketPassed, AsyncJokeServer parent) {
		super();
		this.sock = socketPassed;
		this.parent = parent;
	}

	public void run() {
		
		BufferedReader in = null;
		PrintStream out = null;
		
		try {

			//Acquire in and out streams
			in = new BufferedReader (new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());
			
			//Prompt for a User Name
			command = in.readLine().toUpperCase();
			
			//Close the socket
			sock.close();
			
			//Detect type of command
			if (command.indexOf(AsyncJokeServer.PORTTAG) > -1) {
				out.println(phraseRequest(command));
			}
			else if (command.indexOf(AsyncJokeServer.PHRASETAG) > -1) {
				out.println(portRequest(command));
			}
			
		}
		catch(Exception ex) {ex.printStackTrace();} 
	}
	
	/**Retrieve the next joke or proverb assigned to this account.
	 * @param command
	 * @return string
	 */
	private String phraseRequest(String command) {
		
		//Parse the user name
		String username = parseUserName (command, AsyncJokeServer.PHRASETAG);
		
		//Look up user and add if not found
		Account account = AsyncJokeServer.accounts.findAccount(username);
		if (account == null) account = AsyncJokeServer.accounts.addAccount(username, jokeList, proverbList);
		
		//print a joke or proverb (Account takes care of it's own list cycle)
		//TODO: add phrase functionality
		return account.nextJoke(username);
	}
	
	/**Retreive the port number attached to this account
	 * @param command
	 * @return int with a port number
	 */
	private int portRequest(String command) {
		
		//Parse the user name
		String username = parseUserName (command, AsyncJokeServer.PORTTAG);
		//Look up user and add if not found
		Account account = AsyncJokeServer.accounts.findAccount(username);
		if (account == null) account = AsyncJokeServer.accounts.addAccount(username, jokeList, proverbList);
				
		//Return the port number of this account.
		return account.port;
	}
	
	//Parse the user name out of a given request string and provided tag
	private String parseUserName(String input, String tag) {
		return input.substring(input.indexOf(tag), input.indexOf("]", input.indexOf(tag)));
	}
	
}

//----Administrator connection handler----

//Handles requests to change the mode from the administrator port
class ModeChanger extends Thread {
	AsyncJokeServer parent;
	private int adminPort;
	private static final int queueLength = 6;
	
	//Constructor(s)	
	ModeChanger (int port, AsyncJokeServer parent){
		adminPort = port;
		this.parent = parent;
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
				new AdminHandler(sock, parent).run();
			}
			
		} catch (IOException ioe) {ioe.printStackTrace();}
	}
	
}

class AdminHandler extends Thread{
	AsyncJokeServer parent;
	Socket sock;
	BufferedReader in = null;
	PrintStream out = null;
	
	public AdminHandler(Socket sock, AsyncJokeServer parent){
		this.sock = sock;
		this.parent = parent;
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
				parent.changeMode();
				out.println("Mode changed");
			default:
				out.println("(ERROR) Unrecognized command string");
				break;
			}
			
		}catch (IOException | InterruptedException ex) {ex.printStackTrace();}
		
	}
}
