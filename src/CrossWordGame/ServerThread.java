package CrossWordGame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ServerThread extends Thread {

    private PrintWriter pw;
    private BufferedReader br;
    private ChatRoom cr;
    private Socket socket;
    private String acrossDownPrompt;
    private String numberPrompt;
    private int score;

    public ServerThread(Socket socket, ChatRoom cr) {
        try {
            this.cr = cr;
            this.socket = socket;
            this.score = 0;
            pw = new PrintWriter(socket.getOutputStream(), true); // Auto-flush enabled
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ioe) {
            System.out.println("ioe in ServerThread constructor: " + ioe.getMessage());
        }
    }
    
    // Synchronized method to increment the score
    public synchronized void incrementScore() {
        score++;
    }

    // Synchronized method to retrieve the current score
    public synchronized int getScore() {
        return score;
    }

    public Socket getSocket() {
        return socket;
    }

    public void sendMessage(String message) {
        pw.println(message);
        pw.flush();
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
    
    public void closeConnection() {
        try {
            if (pw != null) {
                pw.close();
            }
            if (br != null) {
                br.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            this.interrupt(); // Interrupt the thread if it's waiting/blocking
            System.out.println("Closed connection with client: " + socket.getInetAddress());
        } catch (IOException e) {
            System.out.println("Error closing client connection: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
    	try {
            // Continue listening for messages from the client
            while (true) {
                String line = br.readLine(); // Read messages from the client
                if (line == null) break; // Handle client disconnection
//                if (line == null) {
//                    System.out.println("Client disconnected: " + socket.getInetAddress());
//                    break;
//                }
//                System.out.println("received from the client: " + line);
                // Pass the message to ChatRoom for processing
//                System.out.println("server received: " + line);
                //do while loop ask again and get the answer back
                if (line.startsWith("Number of players: ")) {
                	try {
//                    	int totalPlayersWanted = extractNumberOfPlayers(message);
                    	int totalPlayersWanted = extractNumberOfPlayers(line);
                        if (totalPlayersWanted < 1 || totalPlayersWanted > 3) {
                            this.sendMessage("Please enter a valid number of players (1 to 3).");
                            cr.sendPrompt(this, "How many players will there be?");
                        }
                    } catch (NumberFormatException nfe) {
                        this.sendMessage("Please enter a valid number of players (1 to 3).");
                        cr.sendPrompt(this, "How many players will there be?");
                    }
//                	System.out.println("server received: " + line);
                	cr.processClientMessage(this, line);
                }
                else if (line.startsWith("a") || line.startsWith("d")) {
                	acrossDownPrompt = line;
//                	System.out.println("server received: " + line);
//                	cr.processClientMessage(this, line);
                	boolean hasRemaining = false;

                    if (acrossDownPrompt.equals("a")) {
                        // Check if there are any remaining across questions
                        for (Data data : cr.tempAcrossData) {
                            if (!data.getPlaced()) {
                                hasRemaining = true;
                                break;
                            }
                        }

                        if (hasRemaining) {
//                            System.out.println("Server received: " + line);
                            cr.processClientMessage(this, line);
                        } 
                        else {
                            // All across questions have been placed
//                            System.out.println("All across questions have been answered.");
                            this.sendMessage("Option 'a' is no longer available as all across questions have been answered.");
                            cr.sendPrompt(this, "Would you like to answer a question across (a) or down (d)?");
                        }
                    } 
                    else { // acrossDownPrompt.equals("d")
                        // Check if there are any remaining down questions
                        for (Data data : cr.tempDownData) {
                            if (!data.getPlaced()) {
                                hasRemaining = true;
                                break;
                            }
                        }

                        if (hasRemaining) {
//                            System.out.println("Server received: " + line);
                            cr.processClientMessage(this, line);
                        } 
                        else {
                            // All down questions have been placed
//                            System.out.println("All down questions have been answered.");
                            this.sendMessage("Option 'd' is no longer available as all down questions have been answered.");
                            cr.sendPrompt(this, "Would you like to answer a question across (a) or down (d)?");
                        }
                    }
                }
                else if (cr.isNumber(line)) {
//                	System.out.println("called in server");
                	boolean found = false;
//                	System.out.println("acrossDownPrompt is in handling: " + acrossDownPrompt);
                	if (acrossDownPrompt.equals("a")) {
//                		System.out.println("hello trying to process: " + line);
                		for (Data data: cr.tempAcrossData) {
//                			System.out.println("copyacrossdata numbers: " + data.getNumber());
                			if (line.equals(data.getNumber())) {
                				numberPrompt = line;
//                				System.out.println("server received: " + line);
                            	cr.processClientMessage(this, line);
            	                found = true;
                			}
                		}
                    }
                	else {
                		for (Data data: cr.tempDownData) {
                			if (line.equals(data.getNumber())) {
//                				System.out.println("server received: " + line);
                            	cr.processClientMessage(this, line);
            	                found = true;
                			}
                		}
                	}
                	if(!found) 
                	{
                		sendMessage("That is not a valid option.");
                	    cr.sendPrompt(this, "Which number?");
                	}
                    
                }
                else if (!cr.isNumber(line)) {
                	//maybe check if the answer is correct here
//                	System.out.println("server received: " + line);
                	cr.processClientMessage(this, line);
                }
            }
//            try {
//                br.close();
//                pw.close();
//                socket.close();
//            } catch (IOException e) {
//                System.out.println("Error closing resources: " + e.getMessage());
//            }
//            // Remove this thread from ChatRoom's serverThreads
//            cr.removeServerThread(this); // You'll need to implement this method
        } catch (IOException ioe) {
            System.out.println("ioe in ServerThread.run(): " + ioe.getMessage());
        }
    }
}