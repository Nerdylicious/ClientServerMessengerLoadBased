/**
 * msgServer.java
 *
 * PURPOSE:			A server that receives text messages from clients.
 *					This server code should be running in owl.cs.umanitoba.ca
 *					This server will run continuously until terminated by the user (Ctrl-C)
 *					If Messages.txt is an already existing file, then messages will just
 *					be appended to this file.
 *					The client will send the message "logout" when it is done sending messages.
 *					This server is not mulithreaded.
 *
 * PLATFORM:		Linux
 *
 */

import java.net.*;
import java.io.*;

public class msgServer {

    public static void main(String[] args) {

		ServerSocket sock = null;    // server's master socket
		InetAddress addr = null;     // address of server
		Socket cliSock = null;       // socket to the client
		InetSocketAddress clientSockAddr = null;	// address of client
		InetAddress clientAddr = null;
		String clientIP = null;	// IP address of client
		BufferedReader br = null;
		PrintWriter writer = null;	   // used to write to socket
		String redirectHostAddress = "owl.cs.umanitoba.ca";

		System.out.println("\nServer starting.");

		// Create Socket
		try {
			addr = InetAddress.getLocalHost();
			sock = new ServerSocket(3111, 3, addr); // create server socket:
			                                      // on port 3111, with
		  	                                          // backlog of 3, on
			                                      // on this machine
		} catch (Exception e) {
			System.out.println("Creation of ServerSocket failed.");
			System.exit(1);
		}

		// Accept a connection
		while(true){
			try {
				cliSock = sock.accept(); //repeatedly accept connections from clients
				clientSockAddr = (InetSocketAddress)cliSock.getRemoteSocketAddress();
				clientAddr = clientSockAddr.getAddress();
				clientIP = clientAddr.getHostAddress();

			} catch (Exception e) {
				System.out.println("Accept failed.");
				System.exit(1);
			}
			
			//server must make it's own call to uptime
			if(clientAddr.getHostName().equals(redirectHostAddress)){
				
				System.out.println("\n<Client (" + clientIP + ") retrieving server load>");
				try{
					Runtime runtime = Runtime.getRuntime();
					Process process = runtime.exec("uptime");
					
					br = new BufferedReader(new InputStreamReader(process.getInputStream()));
					writer = new PrintWriter(cliSock.getOutputStream(), true);
					
					String out = br.readLine();
					int load = Integer.MAX_VALUE;
					
					if(out != null){
						System.out.println("uptime results: " + out);
						String[] values = out.split(",");
						load = Integer.parseInt(values[2].replaceAll("\\s", "").substring(0,1));
						System.out.println("load: " + load + " users");
					}
					writer.println(load);
					
					writer.close();
					br.close();
					cliSock.close();
				}
				catch(Exception e){
					System.out.println("uptime command failed.");
					System.exit(1);
				}
			}
			else{
			
				System.out.println("\n<Client (" + clientIP + ") connected>");
				try {
					br = new BufferedReader(new InputStreamReader(cliSock.getInputStream()));
				} catch (Exception e) {
					System.out.println("Couldn't create socket input stream.");
					System.exit(1);
				}

				String msg;
				try{
		
					try{
						BufferedWriter bw = new BufferedWriter(new FileWriter("Messages.txt", true));
	
						while((msg = br.readLine()) != null && (msg.equals("logout") == false)){
							if(msg.length() <= 128){
								System.out.println("<" + clientIP + ">: " + msg);
								bw.write("<" + clientIP + ">: " + msg);	//write the message to Messages.txt
								bw.newLine();
							}
							else{
								System.out.println("<Message received is too long, it will not be written to the file.>");
							}
						}
						if(msg == null){
							System.out.println("<Client (" + clientIP + ") logged out>");
						}
						if(msg != null && msg.equals("logout")){
							System.out.println("<Client (" + clientIP + ") logged out>");
						}
						bw.close();
						br.close();
						cliSock.close(); //close the connection to the client
					}
					catch(Exception e){
						System.out.println("Creation of FileWriter failed.");
						System.exit(1);
					}

				}
				catch(Exception e){
					System.out.println("Error in readLine()");
					System.exit(1);
				}
			}
		}
    }
}
