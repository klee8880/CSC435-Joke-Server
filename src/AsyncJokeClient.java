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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class AsyncJokeClient {
	
	public static final String PROMPT= "Enter A, B, C, etc to get a joke or proverb, or numbers for sum: ";
	public static final String LOCATION = "localhost";
	public static final String PORTTAG = "[PortReqeust: ";
	public static final String PHRASETAG = "[Phrase: ";
	public static final Semaphore consoleLock = new Semaphore(1);
	
	public static void main (String args []) {
		//Variable
		Map<String, Integer> servers = new Hashtable<String, Integer>();
		
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
		StringBuilder output = new StringBuilder("Kevin Lee's Joke Client, v1.2 \n");
		
		//Perform operations needed for all known processes
		for (String c: servers.keySet()) {
			//Add to the user prompt
			output.append("Server ").append(c).append( " at ").append(servers.get(c)).append('\n');
		}
		
		System.out.println(output.toString());
		
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
			
			//TODO: Acquire all response ports
			for (String c: servers.keySet()) {
				//Get the response port from server
				int port = servers.get(c);
				port = getResponsePort(LOCATION, port, user);
				
				//Start a response thread.
				System.out.println("New response thread on listener port " + port);
				new ResponseListener(consoleLock, port, writer).start();
			}
			
			System.out.print(PROMPT);
			
			do {
				//Semaphore lock console so 
				consoleLock.acquire();
				
				//Request new Input
				command = in.readLine().trim();//Read & sanitize inputs
				System.out.println();
				
				//Parse commands
				Integer targetPort = servers.get(command);
				if (targetPort != null) {
					requestPhrase(LOCATION, targetPort, user);
					System.out.print(PROMPT);
				}
				else if (!arithmetic(command)) { //Arithmetic request.
					System.out.println("Unrecognized Command");
					System.out.print(PROMPT);
				}
				else System.out.print(PROMPT);
				
				//Let any other requesting process proceed.
				consoleLock.release();
				
				TimeUnit.SECONDS.sleep(5);
				
			} while (command.indexOf("QUIT") < 0);
			
			writer.close();
			System.out.println("Local terminal stopped by user request.");
			
		}catch (IOException | InterruptedException ioe) {ioe.printStackTrace();}
		
	}
	
	/**Send message to the Joke Server requesting the response port
	 * @param location
	 * @param port
	 * @param username
	 * @return response port #
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	static int getResponsePort(String location, int port, String username) throws UnknownHostException, IOException {

		//Initialize variables
		Socket sock = new Socket(location, port);
		PrintStream toStream = new PrintStream(sock.getOutputStream());
		BufferedReader fromStream = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		
		//Send message to the server
		toStream.println(PORTTAG + username + ']');
		
		//Receive message.
		String response;
		
		//Listen for a response up to 3 times
		response = fromStream.readLine();
		sock.close();//Close the socket.
		return Integer.parseInt(response);//Parse the integer
		
	}
	
	/**Take the string and subdivide it into numbers. Returns the sum of each element potentially with comments
	 * @param command
	 * @return
	 */
	static boolean arithmetic(String command) {
		//Variable declaration
		LinkedList<Integer> numbers = new LinkedList<Integer>();
		
		String[] elements = command.split(" ");//Split command string by spaces
		try {
		
		//Parse all the subdivided strings
		for (int i = 0; i < elements.length; i++) {
			numbers.add(Integer.parseInt(elements[i]));
		}
				
		}catch(NumberFormatException nfe) {return false;}//If one of the arguments isn't a number return false. 

		//Sum all the parsed numbers in the list
		int sum = 0;
		for (Integer nextNum: numbers) {
			sum += nextNum;
		}
		
		//Prompt the user
		System.out.print("Your sum is: " + sum);
		//Joke(s)
		if (sum == 69) System.out.println("... Nice.");
		else if (sum == 420) System.out.println(" Blaze it.");
		else System.out.println();
		System.out.flush();
		
		return true;
	}
	
	static void requestPhrase(String Server, int port, String username) {
		
		Socket sock;
		PrintStream toStream;
		
		try {
			
			//Acquire connection
			sock = new Socket(Server, port);
			toStream = new PrintStream(sock.getOutputStream());
			
			//Send request to the server with the user name.
			toStream.println(PHRASETAG + username + ']');
			
			//Close the connection
			sock.close();

		} catch (IOException ioe) {ioe.printStackTrace();}
		
	}
	
}

class ResponseListener extends Thread{
	Semaphore consoleGate;
	int port;
	BufferedWriter writer;
	private static final int queueLength = 6;
	
	public ResponseListener(Semaphore consoleGate, int port, BufferedWriter writer) {
		super();
		this.consoleGate = consoleGate;
		this.port = port;
		this.writer = writer;
	}

	@Override
	public void run(){
		Socket sock;
		
		try {
			//Open sockets
			@SuppressWarnings("resource")
			ServerSocket servsock = new ServerSocket (port, queueLength);
			
			//Listen in a loop
			while (true) {
				//Accept a new connection
				sock = servsock.accept();
				//Run program with the connection on a new thread.
				new Printer(sock, consoleGate, writer).start();
			}
		} catch (IOException e) {e.printStackTrace();}
		
	}
}

class Printer extends Thread{
	Socket sock;
	Semaphore consoleGate;
	BufferedWriter writer;
	
	//Constructor
	public Printer(Socket sock, Semaphore consoleGate, BufferedWriter writer) {
		super();
		this.sock = sock;
		this.consoleGate = consoleGate;
		this.writer = writer;
	}

	@Override
	public void run() {
		try {
			
			BufferedReader fromStream = new BufferedReader (new InputStreamReader(sock.getInputStream()));
			
			//Read 2 line from the server
			for (int i = 0; i < 2; i++) {
				String result = fromStream.readLine();
				if (result != null) {
					System.out.println("\nNow waiting for semaphore release.");
					consoleGate.acquire();
					System.out.println('\n' + result);
					System.out.print(AsyncJokeClient.PROMPT);
					writeFile(writer, result);
					consoleGate.release();
				}
			}
			
		} catch (IOException | InterruptedException e) {e.printStackTrace();}
	}
	
	//Write server result to a file for audit
	static synchronized void writeFile(BufferedWriter writer, String line) {
		
		try {
			writer.write(line + '\n');
		} catch (IOException e) {e.printStackTrace();}
	}
}
