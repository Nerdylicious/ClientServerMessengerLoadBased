/**
 * msgClient.java
 *
 * PURPOSE:			A client that sends text messages to a server.
 *					The client can disconnect to the server by typing "logout" 
 *					(without quotations) in the command line.
 *					The server should be running in owl.cs.umanitoba.ca
 *					The server is not mulithreaded.
 *
 * PLATFORM:		Linux
 *
 */

import java.net.*;
import java.io.*;

public class msgClient {

    public static void main(String[] args) {

		BufferedReader br = null;
		InetAddress addr = null;
		Socket sock = null;              // client's socket
		String redirectHostname = "owl.cs.umanitoba.ca";
		String ipCollectionServer = null;

		//create a socket for redirect host
		try {
			addr = InetAddress.getByName(redirectHostname);			
			sock = new Socket(addr, 3111); 
		} catch (Exception e) {
			System.out.println("\nCreation of client's Socket failed.\n");
			System.exit(1);
		}
		
		try{
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		}
		catch(Exception e){
			System.out.println("\nCreation of buffered reader failed\n");
			System.exit(1);
		}

		//get ip address of a server from the redirect host
		try{
		
			if((ipCollectionServer = br.readLine()) != null){
				//System.out.println("Received the ip address: " + ipCollectionServer);
			}
			else{
				System.out.println("\nFailed to receive an IP address");
			}
			br.close();
		}
		catch(Exception e){
			System.out.println("\nReading from socket failed\n");
			System.exit(1);
		}
		
		try{
			sock.close();
		}
		catch(Exception e){
			System.out.println("\nSocket close failed\n");
			System.exit(1);
		}
		
		BufferedReader stdin = null;     // buffered version of instrm
		InputStreamReader instrm = null; // terminal input stream
		PrintWriter writer = null;	   // used to write to socket
		
		if(ipCollectionServer.equals("0.0.0.0") == false){

			//create a socket for one of the actual message collection servers
			try {
				addr = InetAddress.getByName(ipCollectionServer);
				sock = new Socket(addr, 3111); 
			} catch (Exception e) {
				System.out.println("\nCreation of client's Socket failed.\n");
				System.exit(1);
			}
		
			// set up terminal input and socket output streams
			try {
				instrm = new InputStreamReader(System.in);
				stdin = new BufferedReader(instrm);
				writer = new PrintWriter(sock.getOutputStream(), true);
			} catch (Exception e) {
				System.out.println("\nSocket output stream failed.\n");
				System.exit(1);
			}
		
			System.out.println("\nMessages will be sent to " + addr.getHostName() + " (" + ipCollectionServer + ")\n");

			String msg;
			boolean isDone = false;
			try{
				while((msg = stdin.readLine()) != null && (msg.equals("logout") == false)){
					if(msg.length() <= 128){
						writer.println(msg);	//write to the socket
					}
					else{
						System.out.println("<Message must be 128 characters or less. Message was not sent.>");
					}
				}
				if(msg != null && msg.equals("logout")){
					writer.println(msg);
				}
				isDone = true;
			}
			catch(IOException e){
				System.out.println("\nError in readLine()\n");
				System.exit(1);
			}
		
			// close the streams and socket
			if(isDone){
				try {
					writer.close();
					stdin.close();
					instrm.close();
					sock.close();
				} catch (Exception e) {
					System.out.println("Client couldn't close socket.");
					System.exit(1);
				}

				System.out.println("\nClient finished.\n");
			}
		}
		else{
			System.out.println("\nError: Service not available\n");
			System.exit(1);
		}
    }
}
