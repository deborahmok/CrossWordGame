package CrossWordGame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import CrossWordGame.Data;

public class ChatClient extends Thread {
	private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;
    private String hostnameInput;
    private int portInput;
    private int totalPlayersWanted;
    private Scanner scanner;
    private String acrossDownPrompt;
    private String numberPrompt;
    private Data gameData;
    
    // Constructor for ChatClient
    public ChatClient() {
    	gameData = new Data();
    	String line = null;
    	scanner = new Scanner(System.in);
    	System.out.print("Welcome to 201 Crossword!\n");
		 
		// Prompt for hostname and port
		System.out.print("Enter the server hostname: ");
		String hostNameInput = scanner.nextLine(); // Get hostname input
		
		System.out.print("Enter the server port: ");
		int portInput = Integer.parseInt(scanner.nextLine()); // Get port input

		while(true) {
			try {
				Socket s = new Socket(hostNameInput, portInput);
				socket = s;
				br = new BufferedReader(new InputStreamReader(s.getInputStream()));
				pw = new PrintWriter(s.getOutputStream());  
				this.start(); // Start the thread for receiving messages
//				System.out.println("starting a new thread");
				break;
	
			} catch (IOException ioe) {
			    System.out.println("ioe in ChatClient constructor: " + ioe.getMessage());
			}
			System.out.print("Enter the server hostname: ");
			hostNameInput = scanner.nextLine(); // Get hostname input
			
			System.out.print("Enter the server port: ");
			portInput = Integer.parseInt(scanner.nextLine()); 
		}
    }

    public void run() {
        try {
            while (true) {
                String line = br.readLine(); // Read input from the server
                if (line == null) {
//                    System.out.println(line); // Display the message to the user
                    break;
                } //line where console runs into issues
                
                System.out.println(line); // Display the message to the user
                
                if (line.startsWith("How many players will there be?")) { //issue referring at
                	line = scanner.nextLine();
//        			System.out.println("checking Number of players: " + line);
                	pw.println("Number of players: " + line);
        			pw.flush();
                }
                else if (line.startsWith("Would you like to answer a question across (a) or down (d)?")) {
                	line = scanner.nextLine();
                	handleAcrossDownPrompt(line);
                }
                else if (line.startsWith("Which number?")) {
//                	System.out.println("caught it again");
                	line = scanner.nextLine();
//                	System.out.println("sending " + line + "to server");
//                	handleNumberPrompt(line);
                	pw.println(line);
                    pw.flush();
                }
                else if (line.startsWith("What is your guess for")) {
                	line = scanner.nextLine();
                	pw.println(line);
                    pw.flush();
                }
            }
        } 
        catch (IOException ioe) {
            System.out.println("ioe in ChatClient.run(): " + ioe.getMessage());
        }
        finally {
        	cleanup();
        }
    }
    
    private void cleanup() {
        try {
            if (br != null) br.close();
            if (pw != null) pw.close();
            if (socket != null && !socket.isClosed()) socket.close();
            if (scanner != null) scanner.close();
//            System.out.println("Client resources have been closed.");
        } 
        catch (IOException e) {
            System.out.println("Error closing client resources: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Create a new ChatClient instance with user input
        ChatClient cc = new ChatClient();
    }

    private void handleAcrossDownPrompt(String line) {
        while (true) {
            if (line.equals("a") || line.equals("d")) {
            	acrossDownPrompt = line;
                pw.println(line);
                pw.flush();
                break;
            } else {
//                System.out.println("Invalid input. Please enter 'a' or 'd'.");
            	System.out.println("That is not a valid option.");
                line = scanner.nextLine();
            }
        }
    }
    
//    private void handleNumberPrompt(String line) {
//    	boolean found = false;
////    	System.out.println("acrossDownPrompt is in handling: " + acrossDownPrompt);
//        while (true) {
//        	if (acrossDownPrompt.equals("a")) {
////        		System.out.println("hello trying to ");
//        		if(gameData.getCopyAcrossData().isEmpty()) {
////        			System.out.println("dude is empty"); //from here data isn't being read
//        		}
//        		for (Data data: gameData.getCopyAcrossData()) {
////        			System.out.println("copyacrossdata numbers: " + data.getNumber());
//        			if (line.equals(data.getNumber())) {
//        				numberPrompt = line;
//        				pw.println(line);
//    	                pw.flush();
////    	                System.out.println("found it");
//    	                found = true;
//    	                break;
//        			}
//        		}
//            }
//        	else {
//        		for (Data data: gameData.getCopyDownData()) {
//        			if (line.equals(data.getNumber())) {
//        				pw.println(line);
//    	                pw.flush();
//    	                found = true;
//    	                break;
//        			}
//        		}
//        	}
//        	if(found) break;
//        	else {
//		    	System.out.println("That is not a valid option.");
//		        line = scanner.nextLine();
//        	}
//        }
//    }
    
    public String getNumberPrompt() {
    	return numberPrompt;
    }
    
	public String getHostnameInput() {
		return hostnameInput;
	}

	public void setHostnameInput(String hostnameInput) {
		this.hostnameInput = hostnameInput;
	}

	public int getPortInput() {
		return portInput;
	}

	public void setPortInput(int portInput) {
		this.portInput = portInput;
	}
}