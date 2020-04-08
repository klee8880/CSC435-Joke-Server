package Server;

import java.io.*;
import java.net.*;

public class InetServer {

	public static void main (String args []) throws IOException {
		int q_len = 6;
		int port = 1565;
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
