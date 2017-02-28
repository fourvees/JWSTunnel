# JWSTunnel
Java WebSocket Tunnel

Java WebSocket based tunneling program with enhanced performance with NIO2. This project is made up of 2 components.

A server and a client component.

The server component supports multiple websocket clients forwarding to multiple backend servers.

The client component supports connecting to the websocket server component over corporate proxy.

To build this project 

	git clone https://github.com/fourvees/JWSTunnel.git 
	mvn package

To start the websocket server  

	java -jar JWSServer.jar

To configure the websocket client

	java -jar JWSClient.jar config

To run the websocket client

	java -jar JWSClient.jar <LocalPort> <BackEndHost> <BackEndPort>

For Eg. to tunnel SSH

	java -jar JWSClient.jar 3383 127.0.0.1 22

Here 3383 is the local port on which you can connect your SSH client(Putty) behind your proxy.


	
