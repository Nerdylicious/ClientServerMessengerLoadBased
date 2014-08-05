/**
 * redirectHost.java
 *
 * PURPOSE:			The redirect host. It will return the IP address of one of two servers with
 *					the lightest load (with the fewest number of users logged into the machine). 
 *					If both servers have the same load, then the default server's IP address will
 *					be returned if it is up (falcon.cs.umanitoba.ca). If one server is down then
 *					the IP address of the active server will be returned. If both servers are down
 *					then the IP address 0.0.0.0 will be returned to the client.
 *
 * PLATFORM:		Linux
 *
 */

import java.net.*;
import java.io.*;

public class redirectHost {

    public static void main(String[] args) {

		final int NUM_SERVERS = 2; 
		String out = "";
		
		ServerSocket sock = null;    // server's master socket
		Socket cliSock = null;       // socket to the client
		Socket servSock = null;		 // socket to the servers

		String server1 = "falcon.cs.umanitoba.ca";
		String server2 = "osprey.cs.umanitoba.ca";
		
		String command1 = "ping -c 1 falcon";
		String command2 = "ping -c 1 osprey";

		InetAddress addr1 = null;
		InetAddress addr2 = null;

		try{
			addr1 = InetAddress.getByName(server1); //ip addresses of servers
			addr2 = InetAddress.getByName(server2);
		}
		catch(Exception e){			
			System.out.println("\nError in InetAddress\n");
			System.exit(1);
		}
		
		String command[] = {command1, command2};
		String ipAddress[] = {addr1.getHostAddress(), addr2.getHostAddress()};
		InetAddress inetAddr[] = {addr1, addr2};

		System.out.println("\nRedirect Host Starting\nViewing Ping Results:\n");
		
		try {
			InetAddress addr = InetAddress.getLocalHost();
			sock = new ServerSocket(3111, 3, addr); // create server socket:
			                                      // on port 3111, with
		  	                                          // backlog of 3, on
			                                      // on this machine
		} catch (Exception e) {
			System.out.println("Creation of ServerSocket failed.");
			System.exit(1);
		}
		
		while(true){
		
			boolean[] serverStatus = {false, false};
			int[] serverLoad = {Integer.MAX_VALUE, Integer.MAX_VALUE};

			try {
				cliSock = sock.accept(); //continuously accept connections from clients
			} catch (Exception e) {
				System.out.println("Accept failed.");
				System.exit(1);
			}

			for(int pos = 0; pos < NUM_SERVERS; pos ++){

				try{
					Runtime runtime = Runtime.getRuntime();
					Process process = runtime.exec(command[pos]);	//do the ping call to the server

					BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

					while((out = br.readLine()) != null){
			
						System.out.println(out);
						if(out.indexOf("1 received") != -1){
							serverStatus[pos] = true;
						}
					}
					
					br.close();
					
					//make a connection to the server so the server can do an uptime call
					if(serverStatus[pos] == true){
					
						servSock = new Socket(inetAddr[pos], 3111);
						br = new BufferedReader(new InputStreamReader(servSock.getInputStream()));
					
						String load = br.readLine();
						if(load != null){
							serverLoad[pos] = Integer.parseInt(load);
						}
					
						br.close();
						servSock.close();					
					}
				}
				catch(Exception e){
					System.out.println("\nError doing server command(s)\n");
					System.exit(1);
				}
			}

			try{
			
				PrintWriter writer = new PrintWriter(cliSock.getOutputStream(), true);
				int temp = serverLoad[0];
			
				//server 1 is lightest load
				if(temp <= serverLoad[1] && serverStatus[0] == true){
				 				
					writer.println(ipAddress[0]);
				}
				//server 2 is lightest load
				else if(serverStatus[1] == true){

					writer.println(ipAddress[1]);
				}
				//both servers are down
				else{

					writer.println("0.0.0.0");
				}				
				writer.close();
				
			}catch(Exception e){			
				System.out.println("\nError in print writer\n");
				System.exit(1);
			}

			try{				
				cliSock.close();
				
			}catch(Exception e){
				System.out.println("\nError in closing socket\n");
				System.exit(1);
			}
		}
    }
}
