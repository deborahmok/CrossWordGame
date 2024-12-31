package CrossWordGame;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import CrossWordGame.Data;

public class ChatRoom {
    private Vector<ServerThread> serverThreads;
    private static int totalPlayersWanted = -1;//changed to static
    private static int currentPlayerCount = 0; //changed to static
    private ArrayList<ArrayList<String>> copiedBoard;
    private ServerSocket connectSocket;
    private static boolean allPresent = false;
    private final Object lock = new Object();
    private Data gameData;
    private ChatClient cc;
    private String acrossDownPromptChat = "";
    private String numberPromptChat = "";
    private String answerPromptChat = "";
    public ArrayList<Data> tempDatas = new ArrayList<>();
    public ArrayList<Data> tempAcrossData = new ArrayList<>();
    public ArrayList<Data> tempDownData = new ArrayList<>();

    public ChatRoom(int port) {
        try {
            connectSocket = new ServerSocket(port);

            serverThreads = new Vector<ServerThread>();
           
            gameData = new Data();
            
            //while shouldn't be in this constructor -> move this into a method -> call the method in main
            //because it would only run once in the beginning, and not run again at the second round of games
            
        } catch (IOException ioe) {
            System.out.println("ioe in ChatRoom constructor: " + ioe.getMessage());
        }
    }

    public int getThreadsSize() {
        return serverThreads.size();
    }

    public Vector<ServerThread> getServerThreads(){
    	return serverThreads;
    }
    
    public void setTotalPlayersWanted(int totalPlayersWanted) {
        synchronized (lock) {
            ChatRoom.totalPlayersWanted = totalPlayersWanted;
            lock.notifyAll(); // Notify waiting threads
        }
//        System.out.println("Numbers of players: " + ChatRoom.totalPlayersWanted);
    }
    
    public static void setAllPresent(boolean update) {
    	ChatRoom.allPresent = update;
    }
    
    public void setAcrossDownPrompt(String update) {
        synchronized (lock) {
            this.acrossDownPromptChat = update;
            lock.notify(); // Notify the waiting thread
        }
    }

    public void setNumberPrompt(String update) {
        synchronized (lock) {
            this.numberPromptChat = update;
            lock.notify(); // Notify the waiting thread
        }
    }
    
    public void setAnswerPrompt(String update) {
        synchronized (lock) {
            this.answerPromptChat = update;
            lock.notify(); // Notify the waiting thread
        }
    }
    
    // Method to update and broadcast which player we are waiting for
    public void announceWaitingfor() {
//    	System.out.println("totalPlayersWanted " + totalPlayersWanted);
//    	System.out.println("serverThreads.size() " + serverThreads.size());
        int totalPlayers = serverThreads.size(); // Current number of players connected
        int playersNeeded = totalPlayersWanted - totalPlayers; // How many more players are needed
        if (playersNeeded > 0) {
            // Still waiting for more players to join
        	if (totalPlayers == 2 && playersNeeded == 1) {
        		sendToClient("Player 2 has joined from " + serverThreads.get(1).getSocket().getInetAddress(), serverThreads.get(0));
        		sendToClient("There is a game waiting for you.\nPlayer 1 has already joined.", serverThreads.get(1));
        	}
        	System.out.println("Numbers of players: " + totalPlayersWanted);
            String waitingMessage = "Waiting for player " + (totalPlayers + 1) + ".";
            broadcast(waitingMessage, null);
            if (totalPlayers == 1) {
            	readFile();
            }
//            System.out.println("not in here");
        }
//        System.out.println("playersNeeded: " +playersNeeded);
        if (totalPlayersWanted != 0 && playersNeeded == 0){
            // If all required players have joined, start the game
        	if (totalPlayersWanted == 2) {
        		sendToClient("Player 2 has joined from " + serverThreads.get(1).getSocket().getInetAddress(), serverThreads.get(0));
        		sendToClient("There is a game waiting for you.\nPlayer 1 has already joined.", serverThreads.get(1));
        	}
        	else if (totalPlayersWanted == 3) {
        		sendToClient("Player 3 has joined from " + serverThreads.get(2).getSocket().getInetAddress(), serverThreads.get(0));
        		sendToClient("Player 3 has joined from " + serverThreads.get(2).getSocket().getInetAddress(), serverThreads.get(1));
        		sendToClient("There is a game waiting for you.\nPlayer 1 and Player 2 has already joined.", serverThreads.get(2));
        	}
        	if (totalPlayersWanted == 1) {
        		readFile();
//                System.out.println("should be in here ");
        	}
            System.out.println("Game can now begin.");
            initializeGame();
        }
    }

    public int extractNumberOfPlayers(String input) {
        String prefix = "Number of players:";
        if (input.startsWith(prefix)) {
            String numberStr = input.substring(prefix.length()).trim();
            return Integer.parseInt(numberStr);
        } else {
            // Handle the case where the string doesn't start with the expected prefix
            throw new IllegalArgumentException("Input string does not start with 'Number of players:'");
        }
    }
    
    public boolean isNumber(String line) {
        try {
            Integer.parseInt(line); // You can use Long.parseLong or Double.parseDouble if needed
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public void broadcast(String message, ServerThread excludeSender) {
        if (message != null) {
            System.out.println(message);
            for (ServerThread threads : serverThreads) {
                if (excludeSender != threads) {
                    threads.sendMessage(message);
                }
            }
        }
    }

    public synchronized void readFile() {
    	gameData.datas.clear();
    	gameData.acrossData.clear();
    	gameData.downData.clear();
    	gameData.copyacrossData.clear();
    	gameData.copydownData.clear();
    	// Load and set up the game
    	List<File> csvFiles = gameData.scanCSVFiles();

        if (csvFiles.isEmpty()) {
            System.out.println("No CSV files found in the gamedata directory.");
            broadcastMessage("No game data available. Please try again later.");
            return;
        } else {
            System.out.println("Reading random game file.");
            File randFile = gameData.randomlySelectCSVFile(csvFiles);
            gameData.readCSV(randFile);

            if (gameData.isValidCSVFile(randFile)) {
                System.out.println("File read successfully.");
                gameData.blankGameBoard();
                gameData.sortingList();
                gameData.addWord();
            } else {
                System.out.println("The file is invalid.");
                broadcastMessage("Game data is invalid. Please try again later.");
                return;
            }
        }
//        System.out.println("End readFile");

    }
    
    public synchronized void initializeGame() {
        // Load and set up the game
//        readFile();

        // Copy the game board (if needed)
        copyingBoard(gameData.getGameBoard());

        // Notify the server
        System.out.println("Sending game board.");

        // Notify all clients that the game is starting
        broadcastMessage("The game is beginning.");

        // Send the game board to all clients
        for (ServerThread client : serverThreads) {
            sendGameBoardToClient(client);
        }

        // Send the questions to all clients
        sendQuestionsToClients();

        setAllPresent(true);
//        System.out.println("All players are present.");
    }

    public void sendGameBoardToClient(ServerThread client) {
        if (client != null && copiedBoard != null) {
            for (ArrayList<String> row : copiedBoard) {
                StringBuilder rowBuilder = new StringBuilder();
                for (String col : row) {
                    rowBuilder.append(col);
                }
                client.sendMessage(rowBuilder.toString());
            }
        }
    }
    
    public void sendQuestionsToClients() {
//    	System.out.println("Sending questions to clients.");
        StringBuilder message = new StringBuilder();

        // Clear previous data to prevent duplication
        tempDatas.clear();
        tempAcrossData.clear();
        tempDownData.clear();
        
        // Display the "ACROSS" data
        if (!gameData.getCopyAcrossData().isEmpty()) {
            message.append("Across:\n");
            for (Data clue : gameData.getCopyAcrossData()) {
            	tempDatas.add(clue);
            	tempAcrossData.add(clue);
                message.append(clue.getNumber()).append(" "); //when we're printing the numbers here, Its working
                message.append(clue.getQuestion()).append("\n");
            }
        } else {
            System.out.println("Across data is empty.");
        }

        // Display the "DOWN" data
        if (!gameData.getCopyDownData().isEmpty()) {
            message.append("Down:\n");
            for (Data clue : gameData.getCopyDownData()) {
            	tempDatas.add(clue);
            	tempDownData.add(clue);
                message.append(clue.getNumber()).append(" ");
                message.append(clue.getQuestion()).append("\n");
            }
        } else {
            System.out.println("Down data is empty.");
        }

        // Now send the constructed message to all clients
//        System.out.println("Constructed message:\n" + message.toString());
        broadcastMessage(message.toString());
    }
    
    public synchronized void broadcastMessage(String message) {
        for (ServerThread client : serverThreads) {
            client.sendMessage(message);
        }
    }

    public void sendToClient(String message, ServerThread client) {
        if (client != null) {
            client.sendMessage(message);
        }
    }

    public void firstClientProcessor() {
//    	System.out.println("firstbeginning"); // IP address
    	Socket socket;
		try {
			socket = connectSocket.accept();
			System.out.println("Connection from: " + socket.getInetAddress()); // IP address
			
	        // Create and start a new ServerThread for the connected client
			ServerThread serverThread = new ServerThread(socket, this);
	        serverThreads.add(serverThread);
	        serverThread.start(); 
	    	if (serverThreads.size() == 1) {
	            // Ask only the first client for the total number of players
	            serverThread.sendMessage("How many players will there be?");
	        }
	    	
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void makingThreads() {
    	//-1 is when first client hasn't intialized totalPlayersWanted yet
    	synchronized (lock) {
            while (totalPlayersWanted == -1) {
                try {
                    lock.wait(); // Wait until totalPlayersWanted is updated
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
//    	if (totalPlayersWanted == 1) return;
//    	System.out.println("after nasty while loop");
    	while (totalPlayersWanted > serverThreads.size()) {
//    		System.out.println("calls it here 1");
			announceWaitingfor();
//			System.out.println("in here");
			try {
				Socket socket = connectSocket.accept();
				System.out.println("Connection from: " + socket.getInetAddress()); // IP address
	            // Create and start a new ServerThread for the connected client
				ServerThread serverThread = new ServerThread(socket, this);
	            serverThreads.add(serverThread);
//	            System.out.println("updated size of server: " + serverThreads.size());
	            serverThread.start(); // Start the thread to handle this client
	            } 
			catch (Exception e) {
	            	e.printStackTrace();
			} // blocking
	 
        }
		if(totalPlayersWanted == serverThreads.size()) {
//			System.out.println("calls it here 3");
			announceWaitingfor();
			
			try {
	            connectSocket.close();
	            setAllPresent(true);
//	            System.out.println("Server socket closed after all players have joined.");
	        } catch (IOException e) {
	            System.out.println("Error closing server socket: " + e.getMessage());
	        }
		}
    }
    
    // Method to process messages from clients
    public synchronized void processClientMessage(ServerThread st, String message) {
        // Check if the message contains the total number of players
        if (message.matches("Number of players:\\s*\\d+")) { //PARSE
        	setTotalPlayersWanted(extractNumberOfPlayers(message));
        } 
        else if (message.matches("a") || message.matches("d")) { //PARSE
        	setAcrossDownPrompt(message);
//        	System.out.println("across of down: "+ message);
        } 
        else if	(isNumber(message)) {
        	setNumberPrompt(message);
//        	System.out.println("number: "+ message);
        }
        else {
        	setAnswerPrompt(message);
//        	System.out.println("answer: " + message);
        }
    }
    
    public void sendPrompt(ServerThread targetClient, String prompt) {
        targetClient.sendMessage(prompt);
//        System.out.println("Prompt sent to Player " + (serverThreads.indexOf(targetClient) + 1));
        // Do not wait; handle the response in processClientMessage when it arrives
    }
    
    public String sendPromptAndWait(int playerIndex, String prompt) {
        // Send the prompt to the specified client
        ServerThread targetClient = serverThreads.get(playerIndex);
        targetClient.sendMessage(prompt);
//        System.out.println("Prompt sent to Player " + (playerIndex + 1));

        String response = "";
        synchronized (lock) {
            if (prompt.startsWith("Would you like")) {
                while (acrossDownPromptChat.isEmpty()) { // Wait for 'a' or 'd'
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
//                        System.out.println("Main thread interrupted while waiting for client response.");
                    }
                }
                response = acrossDownPromptChat;
                acrossDownPromptChat = ""; // Reset for next prompt
            } 
            else if (prompt.startsWith("Which number?")) {
                while (numberPromptChat.isEmpty()) { // Wait for number
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
//                        System.out.println("Main thread interrupted while waiting for client response.");
                    }
                }
                response = numberPromptChat;
                numberPromptChat = ""; // Reset for next prompt
            }
            else if (prompt.startsWith("What is your guess")) {
                while (answerPromptChat.isEmpty()) { // Wait for number
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
//                        System.out.println("Main thread interrupted while waiting for client response.");
                    }
                }
                response = answerPromptChat;
                answerPromptChat = ""; // Reset for next prompt
            }
        }
        return response;
    }
    
    public void sendAgainPromptAndWait(ServerThread targetClient, String prompt) {
        // Send the prompt to the specified client
        int targetClientIdx = serverThreads.indexOf(targetClient);
        targetClient.sendMessage(prompt);
        System.out.println("Prompt sent to Player " + (targetClientIdx + 1));

        synchronized (lock) { //when do you potentially think this lock is supposed to release?
            if (prompt.startsWith("Would you like")) {
                while (acrossDownPromptChat.isEmpty()) { // Wait for 'a' or 'd'
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
//                        System.out.println("Main thread interrupted while waiting for client response.");
                    }
                }
                acrossDownPromptChat = ""; // Reset for next prompt
            } 
            else if (prompt.startsWith("Which number")) {
                while (numberPromptChat.isEmpty()) { // Wait for number
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
//                        System.out.println("Main thread interrupted while waiting for client response.");
                    }
                }
                numberPromptChat = ""; // Reset for next prompt
            }
            else if (prompt.startsWith("What is your guess")) {
                while (answerPromptChat.isEmpty()) { // Wait for number
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
//                        System.out.println("Main thread interrupted while waiting for client response.");
                    }
                }
                answerPromptChat = ""; // Reset for next prompt
            }
        }
    }
    
    public synchronized void copyingBoard(ArrayList<ArrayList<String>> gameBoard) {
        copiedBoard = new ArrayList<>();
        for (ArrayList<String> row : gameBoard) {
            ArrayList<String> copiedRow = new ArrayList<>();
            for (String col : row) {
                if (col.equals(" ")) {
                    copiedRow.add("   "); // Space representation
                } else if (col.matches("\\d+")) {
                    copiedRow.add(col + "_ "); // Keep the number
                } else {
                    copiedRow.add(" _ "); // Replace characters
                }
            }
            copiedBoard.add(copiedRow);
        }
    }

    public void changeCopyBoard(Data data) {
    	int row = data.getRow();
    	int col = data.getCol();
    	String placement = data.getPlacement();
    	String answer = data.getAnswer();
    	
    	for (int i = 0; i < answer.length(); i++) {
            String charToInsert = String.valueOf(answer.charAt(i));

            if (placement.equals("a")) { // Across (Horizontal)
                int currentRow = row;
                int currentCol = col + i;

                // Get the current cell value
                String currentCell = copiedBoard.get(currentRow).get(currentCol);

                // If the cell is empty or matches the character to insert, proceed
                if(currentCell.startsWith(data.getNumber() + "_")) {
                	copiedBoard.get(currentRow).set(currentCol, (data.getNumber() + charToInsert + " "));
                }
                else if (currentCell.equals("   ") || currentCell.equals(" _ ") || currentCell.equals(" " + charToInsert + " ") || currentCell.equals(data.getNumber() + charToInsert + " ")) {
                	if(currentCell.equals(data.getNumber() + charToInsert + " ")) copiedBoard.get(currentRow).set(currentCol, data.getNumber() + charToInsert + " ");
                    else copiedBoard.get(currentRow).set(currentCol, " " + charToInsert + " ");
                } 
                else {
                    System.out.println("Conflict at (" + currentRow + ", " + currentCol + "): " + currentCell + "vs " + charToInsert);
                    return;
                }

            } 
            else { // Down (Vertical)
                int currentRow = row + i;
                int currentCol = col;

                // Get the current cell value
                String currentCell = copiedBoard.get(currentRow).get(currentCol);

                // If the cell is empty or matches the character to insert, proceed
                if(currentCell.startsWith(data.getNumber() + "_")) {
                	copiedBoard.get(currentRow).set(currentCol, data.getNumber() + charToInsert + " ");
                }
                else if (currentCell.equals("   ") || currentCell.equals(" _ ") || currentCell.equals(" " + charToInsert + " ") || currentCell.equals(data.getNumber() + charToInsert + " ")) {
                	if(currentCell.equals(data.getNumber() + charToInsert + " ")) copiedBoard.get(currentRow).set(currentCol, data.getNumber() + charToInsert + " ");
                	else copiedBoard.get(currentRow).set(currentCol, " " + charToInsert + " ");
                } 
                else {
                    System.out.println("Conflict at (" + currentRow + ", " + currentCol + "): " + currentCell + " vs " + charToInsert);
                    // Handle conflict as above
                    return;
                }
            }
        }
    }
    
    public boolean finishGame() {
    	boolean allPlaced = true;
    	for(Data data: tempDatas) { 
			if (!data.getPlaced()) {
				allPlaced = false;
				break;
			}
		}
    	return allPlaced;
    }
    
    public void winnerCalc() {
    	boolean tie = false;
    	ServerThread maxThread = serverThreads.get(0);
    	for (ServerThread client : serverThreads) {
            if (client.getScore() == maxThread.getScore()) {
            	maxThread = client;
            	tie = true;
            }
            else if (client.getScore() > maxThread.getScore()) {
            	maxThread = client;
            	tie = false;
            }
            else {
            	tie = false;
            }
        }
    	if (totalPlayersWanted == 1) broadcastMessage("\nPlayer " + (serverThreads.indexOf(maxThread)+1) + " is the winner.");
    	else if (tie) broadcastMessage("\nA tie.");
    	else broadcastMessage("\nPlayer " + (serverThreads.indexOf(maxThread)+1) + " is the winner.");
    }
    
    public void disconnectAllClients() {
//        System.out.println("Disconnecting all clients...");
        for (ServerThread client : serverThreads) {
            if (client != null) {
                try {
                    client.sendMessage("<Client terminates>");
                    ////after this prints out, it prints null, do you know where it prints that?
                    client.closeConnection(); // Implement this method in ServerThread
                } catch (Exception e) {
                    System.out.println("Error disconnecting client: " + e.getMessage());
                }
            }
        }
        serverThreads.clear(); // Remove all clients from the list
    }
    
    public void resetGameData() {
    	totalPlayersWanted = -1;
        gameData.clear(); // Implement a method in Data class to clear data
        copyingBoard(gameData.getGameBoard()); // Re-initialize the game board
        copiedBoard.clear(); // Clear the copied board
//        System.out.println("Game data has been reset.");
    }
    
    public static void main(String[] args) {
    	ChatRoom cr = new ChatRoom(3456);
    	while(true) {
	        System.out.println("Listening on port 3456.");
	        System.out.println("Waiting for players...");
	        
	        cr.firstClientProcessor(); //console error refers to here
	                
	        cr.makingThreads();
	        
//	        System.out.println("outside."); //doesn't reach here
//	        System.out.println("allpresent after intializating: " + allPresent);
	        int currPlayer = 1;
	        
	        while(!cr.finishGame()) {
	        	boolean correct = false;
	        	ServerThread playerThread = cr.serverThreads.get(currPlayer - 1);
	        	
	        	cr.broadcast("Player " + currPlayer + "'s turn.", playerThread);
	        	
				String acrossdownResponse = cr.sendPromptAndWait(currPlayer-1, "Would you like to answer a question across (a) or down (d)?");
//				System.out.println("getting across: " + acrossdownResponse);
				
//				for(Data data: cr.tempDatas) {
//					System.out.println("acrossData placed or not: " + data.getPlaced());
//				}
				
				String numberResponse = cr.sendPromptAndWait(currPlayer -1, "Which number?");
//				System.out.println("getting number: " + numberResponse); // Now calling the client side
				
//				String answerResponse = cr.sendPromptAndWait(currPlayer -1, "What is your guess for " + numberResponse + " across?"); //POSSIBLE ISSUE? before here
				String answerResponse = "";
				if (acrossdownResponse.equals("a")) {
					answerResponse = cr.sendPromptAndWait(currPlayer -1, "What is your guess for " + numberResponse + " across?"); //POSSIBLE ISSUE?
					cr.broadcast("Player " + currPlayer + " guessed “" + answerResponse + "” for " + numberResponse + " across.", playerThread); 
				}
				else {
					answerResponse = cr.sendPromptAndWait(currPlayer -1, "What is your guess for " + numberResponse + " down?"); //POSSIBLE ISSUE?
					cr.broadcast("Player " + currPlayer + " guessed “" + answerResponse + "” for " + numberResponse + " down.", playerThread);  //POSSIBLE ISSUE
				}
//				System.out.println("getting answer: " + answerResponse); // Now calling the client side
				
//				cr.broadcast("Player " + currPlayer + " guessed “" + answerResponse + "” for " + numberResponse + " " + answerResponse, null);  //POSSIBLE ISSUE? before here
				
				if (acrossdownResponse.equals("a")) {
					for(Data data: cr.tempAcrossData) { //where the exception is referring to
						if (data.getNumber().equals(numberResponse)) {
//							System.out.println("here buddy");
//							System.out.println("data.getAnswer(): " + data.getAnswer());
							if (data.getAnswer().equalsIgnoreCase(answerResponse)) {
								correct = true;
//								System.out.println("correct is " + correct);
								cr.changeCopyBoard(data);
								int idx = cr.tempDatas.indexOf(data);
								cr.tempDatas.get(idx).setPlaced(true);
								int deleteidx = cr.gameData.getCopyAcrossData().indexOf(data);
								cr.gameData.getCopyAcrossData().remove(deleteidx);
								//making placed = true for the specific data, for tempdatas and for tempacross?
								
							}
						}
					}
				}
				else {
					for(Data data: cr.tempDownData) {
						if (data.getNumber().equals(numberResponse)) {
//							System.out.println("here buddy");
//							System.out.println("data.getAnswer(): " + data.getAnswer());
							if (data.getAnswer().equalsIgnoreCase(answerResponse)) {
								correct = true;
//								System.out.println("correct is " + correct);
								cr.changeCopyBoard(data);
								//making placed = true for the specific data	
								int idx = cr.tempDatas.indexOf(data);
								cr.tempDatas.get(idx).setPlaced(true);
								int deleteidx = cr.gameData.getCopyDownData().indexOf(data);
								cr.gameData.getCopyDownData().remove(deleteidx);
							}
						}
					}
				}
//				System.out.println("out of if correct is " + correct);
				if (correct) {
					cr.broadcast("That is correct.", null);
					System.out.println("Sending game board.");
					for (ServerThread client : cr.serverThreads) {
			            cr.sendGameBoardToClient(client);
			        }
		            playerThread.incrementScore();
					cr.sendQuestionsToClients();
				}
				else {
					cr.broadcast("That is incorrect.", null);
					System.out.println("Sending game board.");
					for (ServerThread client : cr.serverThreads) {
			            cr.sendGameBoardToClient(client);
			        }
					cr.sendQuestionsToClients();
					if (currPlayer == totalPlayersWanted) {
						currPlayer = 1;
					}
					else {
						currPlayer++;
					}
				}
	        }
	        System.out.println("The game has concluded.\nSending scores.");
	        
	        cr.broadcastMessage("Final Score");
	        for (ServerThread client : cr.serverThreads) {
	        	cr.broadcastMessage("Player " + (cr.getServerThreads().indexOf(client)+1) + " - " + client.getScore() + " correct answers.");  
	        }
	        cr.winnerCalc();
	        
	        //cleaning for next players
	        cr.disconnectAllClients();
	        cr.resetGameData();
	        
	        // Reinitialize the server socket for the next game
            try {
                cr.connectSocket = new ServerSocket(3456);
                allPresent = false;
            } 
            catch (IOException e) {
                System.out.println("Error reopening server socket: " + e.getMessage());
                break; // Exit the loop if unable to reopen the server socket
            }
    	}
    }
}