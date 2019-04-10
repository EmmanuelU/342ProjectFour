package egs;

import java.io.IOException;

import egs.NetworkConnection.ClientThread;

public class ClientInfo {
	private int id;
	private int points = 0;
	private int opponentID = 0;
	private ClientThread thread;
	private String response = null;
	
	ClientInfo(ClientThread thread)
	{
		this.thread = thread;
		id = this.thread.getID();
	}
	
	public void startThread()
	{
		thread.start();
	}
	
	public boolean isBusy()
	{
		return opponentID > 0;
	}
	
	public void startRound(int opponentID)
	{
		this.opponentID = opponentID;
	}
	
	public void resetRound()
	{
		this.opponentID = 0;
	}
	
	public void addPoint()
	{
		points++;
	}
	
	public int getID()
	{
		return id;
	}
	
	public int getPoints()
	{
		return points;
	}
	
	public boolean hasResponded()
	{
		return response != null;
	}
	
	public String getResponse()
	{
		return response;
	}
	
	public void clearResponse()
	{
		response = null;
	}
	
	public void setResponse(String response)
	{
		this.response = response;
	}
	
	public void sendData(Object data) throws IOException
	{
		thread.out.writeObject(data);
	}
}