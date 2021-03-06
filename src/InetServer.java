import java.io.*;
import java.net.*;

class Worker extends Thread{

	Socket sock;
	
	//Constructor
	public Worker (Socket s) {
		sock = s;
	}
	
	//Methods
	public void run() {
		PrintStream out = null;
		BufferedReader in = null;
		
		try {
			//Acquire in and out streams
			in = new BufferedReader (new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());
			
			try {
				//Read string sent by the client
				String name = in.readLine();
				
				//Print to the local terminal
				System.out.println("Looking up " + name);
				
				//Parse and return message
				printRemoteAddress(name, out);
			}
			catch (IOException ioe) {
				//Print to local terminal
				System.out.println("Server read error");
				ioe.printStackTrace();
			}
		}
		catch (IOException ioe) {
			//failure on acquiring and reading/writing from the stream.
			System.out.println(ioe);
		}
	}
	
	static void printRemoteAddress(String name, PrintStream out) {
		try {
			out.println("Looking up " + name + "...");
			
			//Acquire information on the sender
			InetAddress machine = InetAddress.getByName(name);
			
			//Write a message to the out stream
			out.println("Host name : " + machine.getHostName());
			out.println("Host IP : " + toText(machine.getAddress()));
		}
		catch (UnknownHostException ex) {
			System.out.println("Server read error");
			ex.printStackTrace();
		}
	}
	
	/*
	 * Convert a byte array to a string for the IP address
	 */
	static String toText (byte ip[]) {
		
		 StringBuffer result = new StringBuffer ();
		 
		 for (int i = 0; i < ip.length; ++ i) {
			 
			 if (i > 0) result.append (".");
			 
			 //Masking off the last 2 bytes
			 result.append (0xff & ip[i]);
			 
		 }
		 
		 return result.toString ();
	 }	
}


public class InetServer {

	public static void main (String args []) throws IOException {
		int q_len = 6;
		int port = 1570;
		Socket sock;
		
		//Create a socket that listens in on a specified port.
		ServerSocket servsock = new ServerSocket (port, q_len);
		
		System.out.println("ClarkElliot's Inet server 1.8 starting up, listeing at port " + port + "./n");
		
		//Busy waiting for a new connection request.
		while (true) {
			//Initialize a new connection
			sock = servsock.accept();
			//Run program with the connection.
			new Worker(sock).start();
		}
		
	}
	
}
