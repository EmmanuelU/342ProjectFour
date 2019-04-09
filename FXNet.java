package egs;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import egs.Game.GameCommands;

public class FXNet extends Application {

	private static boolean isServer;

	private NetworkConnection conn;
	private TextArea messages = new TextArea();
	private int port = 0;
	private String ip = "127.0.0.1"; /* Default IP */

	/* Server GUI */
	private Parent initServerUI(Stage primaryStage) {
		TextField textPort = new TextField("Enter Port ####");
		Button btnStart = new Button("Start Server");
		Button btnExit = new Button("Exit Game");

		HBox root = new HBox(20, textPort, btnStart, btnExit);
		root.setPrefSize(600, 600);

		btnStart.setOnAction(event -> {
			if (!Game.isInteger(textPort.getText()))
				textPort.setText("Integers Only eg: 5555");
			else
				port = Integer.parseInt(textPort.getText());

			conn = createServer();
			try {
				conn.startConn();

				primaryStage.setScene(new Scene(createGame(primaryStage)));
			} catch (Exception e) {
			}

		});

		btnExit.setOnAction(event -> {
			try {
				conn.closeConn();
			} catch (Exception e) {
			}

			System.exit(0);

		});

		return root;
	}

	/* Client GUI */
	private Parent initClientUI(Stage primaryStage) {
		TextField textIP = new TextField("127.0.0.1");
		TextField textPort = new TextField("Enter Port ####");
		Button btnStart = new Button("Connect to Server");
		Button btnExit = new Button("Exit Game");

		HBox root = new HBox(20, textIP, textPort, btnStart, btnExit);
		root.setPrefSize(650, 100);

		btnStart.setOnAction(event -> {
			if (!Game.isInteger(textPort.getText()))
				textPort.setText("Integers Only eg: 5555");
			else if (textIP.getText().equals(""))
				textIP.setText("127.0.0.1");
			else {
				ip = textIP.getText();
				port = Integer.parseInt(textPort.getText());

				conn = createClient();
				try {
					conn.startConn();
					primaryStage.setScene(new Scene(createGame(primaryStage)));
				} catch (Exception e) {
				}
			}
		});

		return root;
	}

	private Parent createGame(Stage primaryStage) {
		messages.setPrefHeight(550);

		primaryStage.setTitle(ip + " " + (isServer ? "Server GUI " : "Client GUI ") + port);

		if (isServer) {
			TextField textPortNum = new TextField();
			textPortNum.setText("5555");
			Button btnAnnounce = new Button("Announce Winner");
			Button btnExit = new Button("Exit Game");

			btnAnnounce.setOnAction(event -> {
				try {
					conn.send("");
				} catch (Exception e) {
				}
			});

			btnExit.setOnAction(event -> {
				try {
					conn.closeConn();
				} catch (Exception e) {
				}

				System.exit(0);

			});

			VBox root = new VBox(20, messages, btnAnnounce, btnExit);
			root.setPrefSize(600, 600);

			return root;
		} else {
			Button btnRock = new Button("Rock");
			Button btnPaper = new Button("Paper");
			Button btnScissors = new Button("Scissors");
			Button btnLizard = new Button("Lizard");
			Button btnSpock = new Button("Spock");
			Button btnExit = new Button("Exit Game");

			btnRock.setOnAction(event -> {
				messages.appendText(sendCommand(GameCommands.PLAY_ROCK) + "\n");
			});

			btnPaper.setOnAction(event -> {
				messages.appendText(sendCommand(GameCommands.PLAY_PAPER) + "\n");
			});

			btnScissors.setOnAction(event -> {
				messages.appendText(sendCommand(GameCommands.PLAY_SCISSORS) + "\n");
			});

			btnLizard.setOnAction(event -> {
				messages.appendText(sendCommand(GameCommands.PLAY_LIZARD) + "\n");
			});

			btnSpock.setOnAction(event -> {
				messages.appendText(sendCommand(GameCommands.PLAY_SPOCK) + "\n");
			});

			btnExit.setOnAction(event -> {
				try {
					conn.closeConn();
				} catch (Exception e) {
				}

				System.exit(0);

			});

			VBox root = new VBox(20, messages, btnRock, btnPaper, btnScissors, btnLizard, btnSpock, btnExit);
			root.setPrefSize(600, 600);

			if (conn.getNumClients() < 2)
				messages.appendText("Waiting for next player...");
			else if (conn.getNumClients() == 2)
				messages.appendText("Ready to Play");

			return root;
		}
	}

	String sendCommand(GameCommands command) {

		try {
			conn.send(command.toString());
		} catch (Exception e) {

		}

		return command.toString();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			if (args[0].equals("-s"))
				isServer = true;
			else if (args[0].equals("-c"))
				isServer = false;
			else {
				System.out.println("Usage: -s for Server, -c for Client");
				System.exit(-1);
			}
		} catch (Exception e) {
			System.out.println("Fatal Error... are you trying to launch without arguments?");
			System.exit(-1);
		}

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub

		primaryStage.setScene(isServer ? new Scene(initServerUI(primaryStage)) : new Scene(initClientUI(primaryStage)));
		primaryStage.show();

	}

	@Override
	public void init() throws Exception {

	}

	@Override
	public void stop() throws Exception {
		try {
			conn.closeConn();
		} catch (Exception e) {
		}
	}

	private Server createServer() {
		return new Server(port, data -> {
			Platform.runLater(() -> {
				messages.appendText(data.toString() + "\n");
			});
		});
	}

	private Client createClient() {
		return new Client(ip, port, data -> {
			Platform.runLater(() -> {
				messages.appendText(data.toString() + "\n");
			});
		});
	}

}
