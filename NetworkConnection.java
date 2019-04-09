package egs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class NetworkConnection {
	
	private ConnThread connthread;
	private Consumer<Serializable> callback;
	
	ArrayList<ClientThread> threads;
	String clientOneResponse = null;
	String clientTwoResponse = null;
	int clientOnePoints = 0;
	int clientTwoPoints = 0;
	
	public NetworkConnection(Consumer<Serializable> callback) {
		this.callback = callback;
		connthread = new ConnThread();
		connthread.setDaemon(true);
		
		threads = new ArrayList<ClientThread>();
	}
	
	public int getNumClients()
	{
		return threads.size();
	}
	
	public void startConn() throws Exception{

		connthread.start();
	}
	
	public void send(Serializable data) throws Exception{
		if(isServer())
		{
			if(clientOneResponse != null && clientTwoResponse != null)
			{

				//check responses
				int winnerID = Game.scoreHand(clientOneResponse, clientTwoResponse);
				if(winnerID == 1)
					clientOnePoints++;
				else if(winnerID == 2)
					clientTwoPoints++;
				
				final String dataString = "\nPlayer One (" + clientOnePoints + " points) played " + clientOneResponse + "\n" + 
						"Player Two (" + clientTwoPoints + " points) played " + clientTwoResponse + "\n"
							+ (winnerID > 0 ? 
								("Player " + winnerID + " has won the round.") : 
								("This round is a tie.")) + "\n";

				callback.accept(dataString);
				
				Game.print(clientOneResponse + " " + clientTwoResponse);
				
				threads.forEach((client) -> {
					try {
						client.out.writeObject(dataString);
					} catch (IOException e) {}
				});
				
				
				//reset responses
				clientOneResponse = null;
				clientTwoResponse = null;
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
		
		
		public void run() {
			try{
				this.out = new ObjectOutputStream(socket.getOutputStream());
				this.in = new ObjectInputStream(socket.getInputStream());
			
				socket.setTcpNoDelay(true);
				
				while(true) {
					Serializable data = (Serializable) in.readObject();
					
					if(id == 1) //for Game Commands
					{
						clientOneResponse = data.toString();
					}
					else if (id == 2)
					{
						clientTwoResponse = data.toString();
					}
					
					callback.accept("Player " + id + ": " + data);
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
					while(threads.size() < 2)
					{
						ClientThread client = new ClientThread(server.accept(), threads.size() + 1);
						threads.add(client);
						client.start();	
							
						callback.accept("New Client Connection: Player " + threads.size());
					}
					callback.accept("Maximum players.");
				}
				catch(Exception e)
				{
					
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
					callback.accept("connection Closed");
				}
			}
		}
	}
	
}	

