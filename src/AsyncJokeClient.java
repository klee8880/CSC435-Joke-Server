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
import java.net.Socket;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;


public class AsyncJokeClient {
	
	private static final String PROMPT= "Enter A or B to get a joke or proverb, or numbers for sum: ";
	private static final String LOCATION = "localhost";
	
	public static void main (String args []) {
		
		//Variable
		Map<Character, Integer> servers = new Hashtable<Character, Integer>();
		
		//TODO: Change so that each argument presented is a port to listen on.
		//Default single port
		if (args.length < 1) {servers.put('A', 4545);}
		//Additional ports
		else {
			//Assign a new ASCII character to each provided port #
			int ascii = 65;
			for (int i = 0; i < args.length; i++) {
				try {
					int port = Integer.parseInt(args[i]);
					servers.put((char) ascii, port);					
					ascii++;//increment ASCII
				} catch (Exception e) {}
			}
		}
		StringBuilder output = new StringBuilder("Kevin Lee's Joke Client, v.03 \n");
		
		//Show user all parsed connections
		for (Character c: servers.keySet()) {
			output.append("Server ").append(c).append( " at ").append(servers.get(c)).append('\n');
		}
		
		System.out.println(output.toString());
		
		//TODO: Spawn listeners threads at all listed ports.
		
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
			System.out.println(PROMPT);
			
			do {
			
			//Request new Input
			System.out.print("Command: ");
			command = in.readLine().trim();//Read & sanitize inputs
			System.out.println();
			
			//TODO: Parse commands
			switch(command.toUpperCase()) {
			case "quit":
				break;
			default:
				//Check for an arithmetic command
				if (!arithmetic(command))
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
	
	static boolean arithmetic(String command) {
		//Variable declaration
		LinkedList<Integer> numbers = new LinkedList<Integer>();
		
		//TODO: Check for Arithmetic request.
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
		if (sum == 69) System.out.println("...Nice.");
		else if (sum == 420) System.out.println(" Blaze zit.");
		else System.out.println();
		System.out.flush();
		
		return true;
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
			writer.write(line + '\n');
		} catch (IOException e) {e.printStackTrace();}
	}
	
}
