
 LONG POLLING – IMPLEMENTATION – WEBSOCKET4J [SERVER]

   Long polling is a variation of the traditional polling technique and allows emulation of an information push from a server to a client. With long polling, the client requests information from the server in a similar way to a normal poll .

  About the Implementation

     Its a simple client and server implementation to implement long polling [comet] . This project is as Proof Of Concept [POC] to demonstrate long polling using websocket in Java ,also since these is a test assignment with time constraint . A production quality implementation is out of scope of this implementation.Mars.2 Release (4.5.2)

  Technology stack used

1. OS : Windows 10 64 bit
2. JavaSE – 1.8.0_74
3. IDE : Eclipse Mars.2 Release (4.5.2)
4. To run the client : Windows command prompt  
  
  API used for Implementation : 
  
    websocket4j – Its an open sourced GNU licensed API [ https://launchpad.net/websocket4j ]
    
  Packages

1. comet_server_master.zip

                  This contains the code necessary to implement a websocket4j server . Its an eclipse project .
  
 Main class : WebSocketMessageServer.java

    This is the polling server . It waits for accept client requests at the port 9000. My implementation is limited to 6 clients , which are known to the server (simulation of login and authentication) . Users are user1,user2,user3,user4,user5 and a guest user. Users other than this are not accepted and this particular users connection will be closed. (No of users can be scaled , but limited to 6 users for simplicity since its a POC)

  For the accepted connections , the server will send notification messages randomly , ie not in fixed intervals. Each user will receive messages send to it . 

Steps to run server.

       –      Extract the project in your local eclispe workspace.    
       –      Import the project in eclipse.
       
Build the project
Run WebSocketMessageServer.java. This will start the server and wait for client requests in port 9000

Now build and run the client from the git project [websocket_client_master.zip] and follow the instructions in the readme file to run the application
