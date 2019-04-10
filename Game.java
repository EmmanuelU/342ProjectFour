package egs;

import java.io.*;

public class Game {
	
	/*resource: https://www.baeldung.com/java-check-string-number*/
	public static boolean isInteger(String str) {
	    try {
	        int i = Integer.parseInt(str);
	    } catch (Exception e) {
	        return false;
	    }
	    
	    return true;
	}
	
	public enum GameCommands
	{
		CLIENT_NOTIFY,
		CLIENT_CHALLENGE,
		
		PLAY_ROCK,
		PLAY_PAPER,
		PLAY_SCISSORS,
		PLAY_LIZARD,
		PLAY_SPOCK,
		
		MISC_MESSAGE,
	};
	
	public static GameCommands stringToCommand(String command) {

	    for (GameCommands c : GameCommands.values()) {
	        if (c.name().equals(command)) {
	            return c;
	        }
	    }

	    return GameCommands.MISC_MESSAGE;
	}
	
	public static boolean matchCommand(String input, GameCommands command)
	{
		return input.startsWith(command.toString());
	}
	
	public static int scoreHand(String inputOne, String inputTwo) //return id of winner
	{
		GameCommands playerOne = stringToCommand(inputOne);
		GameCommands playerTwo = stringToCommand(inputTwo);
		
		if(playerOne.toString().equals(GameCommands.PLAY_PAPER.toString()) && 
			playerTwo.toString().equals(GameCommands.PLAY_ROCK.toString()))
			return 1;
		else if(playerTwo.toString().equals(GameCommands.PLAY_PAPER.toString()) && 
				playerOne.toString().equals(GameCommands.PLAY_ROCK.toString()))
			return 2;
		
		else if(playerOne.toString().equals(GameCommands.PLAY_SCISSORS.toString()) && 
				playerTwo.toString().equals(GameCommands.PLAY_PAPER.toString()))
			return 1;
		else if(playerTwo.toString().equals(GameCommands.PLAY_SCISSORS.toString()) && 
				playerOne.toString().equals(GameCommands.PLAY_PAPER.toString()))
			return 2;
		
		else if(playerOne.toString().equals(GameCommands.PLAY_ROCK.toString()) && 
				playerTwo.toString().equals(GameCommands.PLAY_SCISSORS.toString()))
			return 1;
		else if(playerTwo.toString().equals(GameCommands.PLAY_ROCK.toString()) && 
				playerOne.toString().equals(GameCommands.PLAY_SCISSORS.toString()))
			return 2;
		
		else if(playerOne.toString().equals(GameCommands.PLAY_ROCK.toString()) && 
				playerTwo.toString().equals(GameCommands.PLAY_LIZARD.toString()))
			return 1;
		else if(playerTwo.toString().equals(GameCommands.PLAY_ROCK.toString()) && 
				playerOne.toString().equals(GameCommands.PLAY_LIZARD.toString()))
			return 2;
		
		else if(playerOne.toString().equals(GameCommands.PLAY_LIZARD.toString()) && 
				playerTwo.toString().equals(GameCommands.PLAY_SPOCK.toString()))
			return 1;
		else if(playerTwo.toString().equals(GameCommands.PLAY_LIZARD.toString()) && 
				playerOne.toString().equals(GameCommands.PLAY_SPOCK.toString()))
			return 2;
		
		else if(playerOne.toString().equals(GameCommands.PLAY_SPOCK.toString()) && 
				playerTwo.toString().equals(GameCommands.PLAY_SCISSORS.toString()))
			return 1;
		else if(playerTwo.toString().equals(GameCommands.PLAY_SPOCK.toString()) && 
				playerOne.toString().equals(GameCommands.PLAY_SCISSORS.toString()))
			return 2;
		
		else if(playerOne.toString().equals(GameCommands.PLAY_SCISSORS.toString()) && 
				playerTwo.toString().equals(GameCommands.PLAY_LIZARD.toString()))
			return 1;
		else if(playerTwo.toString().equals(GameCommands.PLAY_SCISSORS.toString()) && 
				playerOne.toString().equals(GameCommands.PLAY_LIZARD.toString()))
			return 2;
		
		else if(playerOne.toString().equals(GameCommands.PLAY_LIZARD.toString()) && 
				playerTwo.toString().equals(GameCommands.PLAY_PAPER.toString()))
			return 1;
		else if(playerTwo.toString().equals(GameCommands.PLAY_LIZARD.toString()) && 
				playerOne.toString().equals(GameCommands.PLAY_PAPER.toString()))
			return 2;
		
		else if(playerOne.toString().equals(GameCommands.PLAY_PAPER.toString()) && 
				playerTwo.toString().equals(GameCommands.PLAY_SPOCK.toString()))
			return 1;
		else if(playerTwo.toString().equals(GameCommands.PLAY_PAPER.toString()) && 
				playerOne.toString().equals(GameCommands.PLAY_SPOCK.toString()))
			return 2;
		
		else if(playerOne.toString().equals(GameCommands.PLAY_SPOCK.toString()) && 
				playerTwo.toString().equals(GameCommands.PLAY_ROCK.toString()))
			return 1;
		else if(playerTwo.toString().equals(GameCommands.PLAY_SPOCK.toString()) && 
				playerOne.toString().equals(GameCommands.PLAY_ROCK.toString()))
			return 2;
		
		return 0;
	}
	
	static void print(Object obj)
	{
		System.out.println(obj);
	}

}
