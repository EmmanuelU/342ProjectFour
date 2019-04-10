package egs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

import egs.Game;
import egs.Game.GameCommands;

public abstract class NetworkConnection {
	
	private ConnThread connthread;
	private Consumer<Serializable> callback;

	ArrayList<ClientInfo> clients;
	int playerOne = 0;
	int playerTwo = 0;
	
	private final int MAX_PLAYERS = 8;
	
	public NetworkConnection(Consumer<Serializable> callback) {
		this.callback = callback;
		connthread = new ConnThread();
		connthread.setDaemon(true);
		
		clients = new ArrayList<ClientInfo>();
	}
	
	public int getNumClients()
	{
		return clients.size();
	}
	
	public int getClientID()
	{
		
		return 0; //server
	}
	
	public ClientInfo getClientByID(int id)
	{
		for(ClientInfo client : clients)
		{
			if(client.getID() == id)
				return client;
		}
		
		return null;
	}
	
	public void startConn() throws Exception{

		connthread.start();
	}
	
	public void send(Serializable data) throws Exception{
		if(isServer())
		{
			ClientInfo clientOne = getClientByID(playerOne);
			ClientInfo clientTwo = getClientByID(playerTwo);
			
			if(playerOne > 0 && playerTwo > 0 && clientOne.hasResponded() && clientTwo.hasResponded())
			{

				//check responses
				int winnerID = Game.scoreHand(clientOne.getResponse(), clientTwo.getResponse());
				if(winnerID == 1)
				{
					clientOne.addPoint();
					winnerID = clientOne.getID();
				}
				else if(winnerID == 2)
				{
					clientTwo.addPoint();
					winnerID = clientTwo.getID();
				}
				
				final String dataString = "\nPlayer " + clientOne.getID() + " (" + clientOne.getPoints() + " points) played " + clientOne.getResponse() + "\n" + 
						"Player " + clientTwo.getID() + " (" + clientTwo.getPoints() + " points) played " + clientTwo.getResponse() + "\n"
							+ (winnerID > 0 ? 
								("Player " + winnerID + " has won the round.") : 
								("This round is a tie.")) + "\n";

				callback.accept(dataString);
				
				clients.forEach((client) -> {
					try {
						client.sendData(dataString);
						client.resetRound(); //clear opponents
						client.clearResponse();
					} catch (IOException e) {}
				});
				
				playerOne = 0;
				playerTwo = 0;
			
			}
			else
				callback.accept("Player(s) still need to select hand");
		}
		else
			connthread.out.writeObject(data);
	}
	
	public void closeConn() throws Exception{
		if(connthread.socket != null)
			connthread.socket.close();
	}
	
	abstract protected boolean isServer();
	abstract protected String getIP();
	abstract protected int getPort();
	
	class ClientThread extends Thread{

		private Socket socket;
		private int id;
		ObjectOutputStream out;
		ObjectInputStream in;
		
		ClientThread(Socket socket, int id)
		{
			this.socket = socket;
			this.id = id;
		}
		
		public int getID()
		{
			return id;
		}
		
		public void run() {
			try{
				this.out = new ObjectOutputStream(socket.getOutputStream());
				this.in = new ObjectInputStream(socket.getInputStream());
			
				socket.setTcpNoDelay(true);
				
				while(true) { //Incoming data from client
					Serializable data = (Serializable) in.readObject();
					
					String dataString = data.toString().trim();
					
					if(Game.matchCommand(dataString, GameCommands.CLIENT_WHOAMI))
					{
						getClientByID(id).sendData("You are Player " + id);
					}
					else if(Game.matchCommand(dataString, GameCommands.CLIENT_LOBBY))
					{
						String lobby = "Current Lobby: \n";
						for(ClientInfo client : clients)
						{
							int c = client.getID();
							
							if(id != c)
								lobby += "Player " + c + "\n";
						}
						getClientByID(id).sendData(lobby);
					}
					else if(Game.matchCommand(dataString, GameCommands.CLIENT_CHALLENGE))
					{
						int clientID;
						//messages.appendText("Player not found" + NEWLINE);
						String clientStringID = dataString.replace(GameCommands.CLIENT_CHALLENGE.toString(), "");
						if(!Game.isInteger(clientStringID))
						{
							getClientByID(id).sendData("Invalid Challenge");
						}
						else
						{
							clientID = Integer.parseInt(clientStringID);
							boolean foundClient = false;
							if(clientID == id)
								getClientByID(id).sendData("You cannot play yourself");
							else
							{
								for(ClientInfo client : clients)
								{
									if(clientID == client.getID())
									{
										foundClient = true;
										if(getClientByID(clientID).isBusy())
										{
											getClientByID(clientID).sendData("Player " + id + " wanted to challenge you. You are already in a match.");
											getClientByID(id).sendData("Player " + clientID + " is already in a match.");
										}
										else
										{
											getClientByID(id).startRound(clientID);
											getClientByID(clientID).startRound(id);
											
											playerOne = clientID;
											playerTwo = id;
											
											getClientByID(id).sendData("Challenged Player " + clientID + ", press any action to play.");
											getClientByID(clientID).sendData("Player " + id + " has challenged you, press any action to play.");
										}
									}
								}
								
								if(!foundClient)
									getClientByID(id).sendData("Player not found ID " + clientStringID);
							}
						}
						
					}
					else //no commands detected
					{
						if(getClientByID(id).isBusy())
						{
							getClientByID(id).setResponse(data.toString());
							callback.accept("Player " + id + ": " + data);
						}
						else
						{
							getClientByID(id).sendData("You must challenge a player first.");
						}
					}
				}
				
			}
			catch(Exception e) {
				callback.accept("connection Closed");
			}
		}
	}
	
	class ConnThread extends Thread{
		
		Socket socket;
		private ObjectOutputStream out;
		
		public void run() {
			
			if(isServer())
			{
				try(ServerSocket server = new ServerSocket(getPort())) {
					while(getNumClients() < MAX_PLAYERS)
					{
						ClientInfo client = new ClientInfo(new ClientThread(server.accept(), getNumClients() + 1));
						clients.add(client);
						
						client.startThread();	
							
						callback.accept("Player " + getNumClients() + " has connected.");
					}
					callback.accept("Maximum players.");
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				try(Socket socket = new Socket(getIP(), getPort());
						ObjectOutputStream out = new ObjectOutputStream( socket.getOutputStream());
						ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){
					
					this.socket = socket;
					this.out = out;
					socket.setTcpNoDelay(true);
					
					while(true) {
						Serializable data = (Serializable) in.readObject();
						callback.accept(data);
					}
					
				}
				catch(Exception e) {
					e.printStackTrace();
					callback.accept("connection Closed");
				}
			}
		}
	}
	
}	

