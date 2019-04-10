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
	int playerOne = 1;
	int playerTwo = 2;
	
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
	
	public boolean isClientID(int id)
	{
		return getClientByID(id).getID() == id; //connected
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
			
			if(clientOne.hasResponded() && clientTwo.hasResponded())
			{

				//check responses
				int winnerID = Game.scoreHand(clientOne.getResponse(), clientTwo.getResponse());
				if(winnerID == 1)
					clientOne.addPoint();
				else if(winnerID == 2)
					clientTwo.addPoint();
				
				final String dataString = "\nPlayer One (" + clientOne.getPoints() + " points) played " + clientOne.getResponse() + "\n" + 
						"Player Two (" + clientTwo.getPoints() + " points) played " + clientTwo.getResponse() + "\n"
							+ (winnerID > 0 ? 
								("Player " + winnerID + " has won the round.") : 
								("This round is a tie.")) + "\n";

				callback.accept(dataString);
				
				Game.print(clientOne.getResponse() + " " + clientTwo.getResponse());
				
				clients.forEach((client) -> {
					try {
						client.sendData(dataString);
						client.clearResponse();
					} catch (IOException e) {}
				});
			
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
					else //no commands detected
					{
						getClientByID(id).setResponse(data.toString());
						callback.accept("Player " + id + ": " + data);
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
							
						callback.accept("New Client Connection: Player " + getNumClients());
						
						//client.sendData("You are Player " + getNumClients());
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

