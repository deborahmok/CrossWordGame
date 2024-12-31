package CrossWordGame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Collections;
import java.util.Comparator;

public class Data {
	private String gameDataFile = "gamedata";
    public ArrayList<Data> datas = new ArrayList<>();
    public ArrayList<Data> acrossData = new ArrayList<>();
    public ArrayList<Data> downData = new ArrayList<>();
    public ArrayList<Data> copyacrossData = new ArrayList<>();
    public ArrayList<Data> copydownData = new ArrayList<>();
    public ArrayList<ArrayList<String>> gameBoard;
    private String number;
    private String answer;
    private String question;
    private String placement;
    private boolean included;
    private boolean placed;
    private int row;
    private int col;
    private boolean across = false, down = false;

    public Data() {
    	this.number = "";
        this.answer = "";
        this.question = "";
        this.placement = "";
        this.included = false; //to generate the gameboard
        this.row = -1;
        this.col = -1;
        this.placed = false; //on the blank gameboard
        this.copyacrossData = new ArrayList<>();
        this.copydownData = new ArrayList<>();
    }
    
    public Data(String number, String answer, String question, String placement, boolean included, int row, int col, boolean placed) {
        this.number = number;
        this.answer = answer;
        this.question = question;
        this.placement = placement;
        this.included = included;
        this.row = row;
        this.col = col;
        this.placed = placed;
        this.copyacrossData = new ArrayList<>();
        this.copydownData = new ArrayList<>();
    }
    
    public String getNumber() {
    	return number;
    }
    
    public ArrayList<Data> getDatas(){
    	return datas;
    }
    
    public void setPlaced(boolean ifPlaced) {
    	placed = ifPlaced;
    }
    
    public boolean getPlaced() {
    	return placed;
    }
    
    public String getAnswer() {
    	return answer;
    }
    
    public String getQuestion() {
    	return question;
    }
    
    public String getPlacement() {
    	return placement;
    }
    
    public boolean getIncluded() {
    	return included;
    }
    
    public int getRow() {
    	return row;
    }
    
    public int getCol() {
    	return col;
    }
    
    public void setPlacement(String placement) {
    	this.placement = placement;
    }
    
    public void setIncluded(boolean included) {
    	this.included = included;
    }
    
    public void setRow(int row) {
    	this.row = row;
    }
    
    public void setCol(int col) {
    	this.col = col;
    }

    public ArrayList<Data> getCopyAcrossData() {
        return copyacrossData;
    }

    public void setCopyAcrossData(ArrayList<Data> copyAcrossData) {
        this.copyacrossData = copyAcrossData;
    }

    public ArrayList<Data> getCopyDownData() {
        return copydownData;
    }

    public void setCopyDownData(ArrayList<Data> copyDownData) {
        this.copydownData = copyDownData;
    }

    
    public static void main(String[] args) {
    	Data data = new Data(); 
        List<File> csvFiles = data.scanCSVFiles();

        if (csvFiles.isEmpty()) {
            System.out.println("No CSV files found in the gamedata directory."); 
            return;
        } else {
            File randFile = data.randomlySelectCSVFile(csvFiles);
            System.out.println("Selected file: " + randFile.getName());
            data.readCSV(randFile);

            if (data.isValidCSVFile(randFile)) {
                System.out.println("The file is valid.");
            } else {
                System.out.println("The file is invalid.");
            }
        }

        System.out.println("");
        
        data.blankGameBoard();
        
        //first sort:
        data.sortingList();
    	
    	
    	//add words to gameBoard
        data.addWord();
    	
    	//print gameBoard
        data.printBoard();
    	
    	for(Data tempData: data.copyacrossData) {
    		System.out.println(tempData.getAnswer());
    	}
    }
    
    //call this method only when you placed the word and have the words coordinates, so each time you placed a word, you'll check if they 
    //have another one if it's the same
    public void sameIndex(Data currData, int row, int col) {
    	String place = currData.getPlacement();
    	for (Data data : datas) {
    		if (currData.getNumber().equals(data.getNumber()) && data != currData) {
    			if (data.getPlacement() == "a") {
    				for (int i = 0; i < data.getAnswer().length(); i++) {
    	        		if (i==0) gameBoard.get(row).set(col + i, data.getNumber()); //putting idx question in the beginning
    	        		else gameBoard.get(row).set(col + i, Character.toString(data.getAnswer().charAt(i))); // Set each letter
    	        	}
    				data.setRow(row);
					data.setCol(col);
					data.setIncluded(true);
					int idx = acrossData.indexOf(data);
					acrossData.remove(idx);
					break;
    			}
    			else {
					for (int i = 0; i < data.getAnswer().length(); i++) {
    	        		if (i==0) gameBoard.get(row).set(col + i, data.getNumber());
    	        		else gameBoard.get(row + i).set(col, Character.toString(data.getAnswer().charAt(i)));
    	        	}
					data.setRow(row);
					data.setCol(col);
					data.setIncluded(true);
					int idx = downData.indexOf(data);
					downData.remove(idx); //find the idx of where data is located
					System.out.println(data.getAnswer());
					break;
    			}
    		}
    	}
    }
    
    public void clear() {
    	copyacrossData.clear();
        copydownData.clear();
        acrossData.clear();
        downData.clear();
        datas.clear();
        gameBoard.clear();
    }
    
    public void addWord() {
    	boolean placed = false;
    	
    	if (downData.isEmpty() || acrossData.isEmpty()) {
            System.out.println("No words to add.");
            return;
        }

    	//placing the longest word in the middle
    	String longestWord = datas.get(0).getAnswer(); // Get the longest word
        int startRow = gameBoard.size() / 2; // Middle row
        int startCol = (gameBoard.get(0).size() - longestWord.length()) / 2; // Middle column, adjusted for word length
        datas.get(0).setRow(startRow);
        datas.get(0).setCol(startCol);
        datas.get(0).setIncluded(true);
      
        //finding if the longest word is across or down, and put it in the middle of gameboard
        if (datas.get(0).getPlacement().equals("a")) {
        	for (int i = 0; i < longestWord.length(); i++) {
        		if (i==0) gameBoard.get(startRow).set(startCol + i, datas.get(0).getNumber()); //putting idx question in the beginning
        		else gameBoard.get(startRow).set(startCol + i, Character.toString(longestWord.charAt(i))); // Set each letter
        		
        	}
        	acrossData.remove(0); 
    		across = true;
        }
        else {
        	for (int i = 0; i < longestWord.length(); i++) {
        		if (i==0) gameBoard.get(startRow).set(startCol + i, datas.get(0).getNumber());
        		else gameBoard.get(startRow + i).set(startCol, Character.toString(longestWord.charAt(i)));
        	}
        	downData.remove(0);
        	down = true;
        }
        
        sameIndex(datas.get(0), startRow, startCol);
      
        
        //start going down the list aside from the first word
        //access the downdata
        while(downData.size() > 0 && acrossData.size() > 0) {
    	
    	placed = false;
    	if(down) { //csci
    		Data currData = acrossData.get(0); //curr data
    		String currWord = currData.getAnswer(); //currword
    		for (int i = 0; i < longestWord.length(); i++) {
    			for (int j = 0; j <  currWord.length(); j++) {
    				if (currWord.charAt(j) == longestWord.charAt(i)) {
    					if(placeWord(currWord, i+startRow, startCol-j, "a", i+startRow, startCol)) { 
    						startRow = i+startRow;
    						startCol = startCol-j;
    						sameIndex(currData, startRow, startCol);
    						int idx = datas.indexOf(currData);
    						datas.get(idx).setRow(startRow);
    						datas.get(idx).setCol(startCol);
    						datas.get(idx).included = true;
    						longestWord = currWord;
    						acrossData.remove(0);
    						down = false;
    						across = true;
    						placed = true;
    						break;
    					}
    					else {
    						down = true;
    						across = false;
    						placed = false;
    					}
    				}
    			}
    			if (acrossData.isEmpty() || placed) {
    	            break; // Exit the outer loop if either list is empty
    	        }
    		}
    		if(!placed) {        			
    			for (Data data : datas) {
    				if(data.getIncluded() && data.getPlacement() == "d") {
    					String tempLong = data.getAnswer();
    					for (int i = 0; i < tempLong.length(); i++) {
    		    			for (int j = 0; j <  currWord.length(); j++) {
    		    				if (currWord.charAt(j) == tempLong.charAt(i)) {
    		    					if(placeWord(currWord, i+data.getRow(), data.getCol()-j, "a", i+data.getRow(), data.getCol())) { //HERE
    		    						startRow = i+data.getRow();
    		    						startCol = data.getCol()-j;
    		    						sameIndex(currData, startRow, startCol);
    		    						int idx = datas.indexOf(currData);
    		    						datas.get(idx).setRow(startRow);
    		    						datas.get(idx).setCol(startCol);
    		    						datas.get(idx).setIncluded(true);
    		    						longestWord = currWord;
    		    						acrossData.remove(0);
    		    						down = false;
    		    						across = true;
    		    						placed = true;
    		    						break;
    		    					}
    		    					else {
    		    						down = true;
    		    						across = false;
    		    						placed = false;
    		    					}
    		    				}
    		    			}
    		    			if (acrossData.isEmpty() || placed) {
    		                    break; // Exit the outer loop if either list is empty
    		                }
    		        	}
    					if (placed) break;
    				}
    			}
    			if(!placed) {
    	    		acrossData.add(acrossData.get(0));
    	    		acrossData.remove(0);
    	    	}
    		}
    	} 
    	
    	placed = false;
    	if(across) { 
	    	Data currData = downData.get(0); //curr data
	    	String currWord = currData.getAnswer(); //currword
	    	
	    	//checks if there is an intersection
			for (int i = 0; i < longestWord.length(); i++) {
				for (int j = 0; j <  currWord.length(); j++) {
					if (currWord.charAt(j) == longestWord.charAt(i)) {
						if(placeWord(currWord, startRow - j, startCol + i, "d", startRow, startCol + i)) {
							startRow = startRow - j;
							startCol = startCol + i;
							int idx = datas.indexOf(currData);
							datas.get(idx).setRow(startRow);
							datas.get(idx).setCol(startCol);
							datas.get(idx).included = true;
							longestWord = currWord;
							downData.remove(0);
							down = true;
							across = false;
							placed = true;
							break;
						}
						else {
	    					down = false;
							across = true;
							placed = false;
						}
					}
				}
				// Check the size of acrossData before accessing it
	            if (downData.isEmpty() || placed) {
	                break; // Exit the outer loop if either list is empty
	            }
			}
			if(!placed) {
				for (Data data : datas) {
	    			if(data.getIncluded() && data.getPlacement() == "a") {
	    				String tempLong = data.getAnswer();
	    				for (int i = 0; i < tempLong.length(); i++) {
	    	    			for (int j = 0; j <  currWord.length(); j++) {
	    	    				if (currWord.charAt(j) == tempLong.charAt(i)) {
	    	    					if(placeWord(currWord, data.getRow() - j, data.getCol() + i, "d", data.getRow()  , data.getCol() + i)) { //HERE
	    	    						startRow = data.getRow() - j;
	    	    						startCol = data.getCol() + i;
	    	    						int idx = datas.indexOf(currData);
	    	    						datas.get(idx).setRow(startRow);
	    	    						datas.get(idx).setCol(startCol);
	    	    						datas.get(idx).setIncluded(true);
	    	    						longestWord = currWord;
	    	    						downData.remove(0);
	    	    						down = true;
	    	    						across = false;
	    	    						placed = true;
	    	    						break;
	    	    					}
	    	    					else {
	    	    						down = false;
	    	    						across = true;
	    	    						placed = false;
	    	    					}
	    	    				}
	    	    			}
	    	    			if (downData.isEmpty() || placed) {
	    	                    break; // Exit the outer loop if either list is empty
	    	                }
	    	        	}
	    				if (placed) break;
	    			}
	    		}
	    		if(!placed) {
	    			downData.add(downData.get(0));
	    			downData.remove(0);
	        	}
	    	}
        }
        }
    }
    
    public boolean placeWord(String word, int i, int j, String place, int wordRow, int wordCol) {
        // Check boundaries before starting to place the word
    	String currWordNum = "";
    	for(Data data: datas) {
    		if (data.getAnswer().equals(word)) {
    			currWordNum = data.getNumber();
    		}
    	}
        if (place.equals("a")) { // Across
            // Check if the word can fit horizontally
            if (j + word.length() > gameBoard.get(i).size()) {
                return false; // Not enough space to place the word
            }
            
         // Check left and right adjacent positions
            if ((j > 0 && !gameBoard.get(i).get(j - 1).equals(" ")) || 
                (j + word.length() < gameBoard.get(i).size() && !gameBoard.get(i).get(j + word.length()).equals(" "))) {
//                System.out.println("adjacent position is occupied horizontally " + word);
                return false;
            }
            
            //check up and down, except the word at intersection
            for (int k = 0; k < word.length(); k++) {
        	    // Check if there's a character above or below at any position except for wordCol
        	    if (k != wordCol - j) { // Exclude the column at wordCol
        	        if (!gameBoard.get(i - 1).get(j + k).equals(" ") || !gameBoard.get(i + 1).get(j + k).equals(" ")) {
//        	            System.out.println("Position at row " + i + ", column " + (j + k) + " has a character above or below.");
        	            return false;
        	        }
        	    }
            }
          
            for (int k = 0; k < word.length(); k++) {
            	// Only the exact character at the position can remain, all others must be blank
                if (!gameBoard.get(i).get(j + k).equals(" ") && !gameBoard.get(i).get(j+k).equals(Character.toString(word.charAt(k))) || !isNotNumber(gameBoard.get(i).get(j+k))) {
                	return false; 
                }
            }
            
            // Place the word if all checks passed
            for (int k = 0; k < word.length(); k++) {
            	if (k==0) gameBoard.get(i).set(j + k, currWordNum);
            	else gameBoard.get(i).set(j + k, Character.toString(word.charAt(k))); //original
            }
        } 
        else { // Down
            // Check if the word can fit vertically
            if (i + word.length() > gameBoard.size()) {
                return false; // Not enough space to place the word
            }
            
            // Check up and down adjacent positions
            if ((i > 0 && !gameBoard.get(i - 1).get(j).equals(" ")) || 
                (i + word.length() < gameBoard.size() && !gameBoard.get(i + word.length()).get(j).equals(" "))) {
                return false;
            }
            
          //check left and right, except the word at intersection
            for (int k = 0; k < word.length(); k++) {
            	// Check if there's a character to the left or right at any position except for wordRow
                if (k != wordRow - i) { // Exclude the row at wordRow
                    if (!gameBoard.get(i + k).get(j - 1).equals(" ") || !gameBoard.get(i + k).get(j + 1).equals(" ")) {
                        return false;
                    }
                }
            }
            
            for (int k = 0; k < word.length(); k++) {
            	// Only the exact character at the position can remain, all others must be blank
                if (!gameBoard.get(i + k).get(j).equals(" ") && !gameBoard.get(i+k).get(j).equals(Character.toString(word.charAt(k))) || !isNotNumber(gameBoard.get(i+k).get(j))) {
                    return false; 
                }
            }
            // Place the word if all checks passed
            for (int k = 0; k < word.length(); k++) {
            	if (k==0) gameBoard.get(i + k).set(j, currWordNum);
            	else gameBoard.get(i + k).set(j, Character.toString(word.charAt(k))); //original
            }
        }
        return true; // Word successfully placed
    }
    
    public boolean isNotNumber(String value) {
        try {
            Double.parseDouble(value); // Or use Integer.parseInt for integers
            return false; // It is a number
        } catch (NumberFormatException e) {
            return true; // It's not a number
        }
    }
    
    public void printBoard() {
        for (ArrayList<String> row : gameBoard) {
            for (String col : row) {
                if (col.equals(" ")) {
                    System.out.print("  "); // Replace space with ". "
                } else if (col.matches("\\d+")) { // Check if it's a number (one or more digits)
                    System.out.print(col + " "); // Keep the number as is
                } else {
                    System.out.print("_ "); // Replace characters with "_ "
                }
            }
            System.out.println(); // Move to the next line after each row
        }
    }
    
    public ArrayList<ArrayList<String>> getGameBoard() {
        return gameBoard;
    }
    
    public ArrayList<ArrayList<String>> blankGameBoard () {
    	//placing the longest word on the middle column
    	gameBoard = new ArrayList<ArrayList<String>>(50);    	
    	
    	//fill in with blank spots for now
    	for (int i = 0; i < 50; ++i) {
    		ArrayList<String> row = new ArrayList<>();
    		for (int j = 0; j < 50; ++j) {
    			row.add(" ");
    		}
    		gameBoard.add(row);
    	}
    	return gameBoard;
    }
    
    public void sortingList() {
        // Sort `acrossData` first by answer length in descending order
        Collections.sort(acrossData, new Comparator<Data>() {
            @Override
            public int compare(Data d1, Data d2) {
                return Integer.compare(d2.getAnswer().length(), d1.getAnswer().length()); // Descending order
            }
        });
        
        Collections.sort(copyacrossData, new Comparator<Data>() {
            @Override
            public int compare(Data d1, Data d2) {
                return Integer.compare(d2.getAnswer().length(), d1.getAnswer().length()); // Descending order
            }
        });

        // Sort `downData` by answer length in descending order
        Collections.sort(downData, new Comparator<Data>() {
            @Override
            public int compare(Data d1, Data d2) {
                return Integer.compare(d2.getAnswer().length(), d1.getAnswer().length()); // Descending order
            }
        });

        Collections.sort(copydownData, new Comparator<Data>() {
            @Override
            public int compare(Data d1, Data d2) {
                return Integer.compare(d2.getAnswer().length(), d1.getAnswer().length()); // Descending order
            }
        });
        
        // Now, clear and repopulate `datas` with sorted `acrossData` and `downData`
        datas.clear();
        datas.addAll(acrossData); // First add sorted `acrossData`
        datas.addAll(downData);   // Then add sorted `downData`

        // Sort the combined `datas` again to ensure it's in descending order
        Collections.sort(datas, new Comparator<Data>() {
            @Override
            public int compare(Data d1, Data d2) {
                return Integer.compare(d2.getAnswer().length(), d1.getAnswer().length()); // Descending order
            }
        });
    }

    public void readCSV(File csvFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            
            boolean readingAcross = false;
            boolean readingDown = false;

            // Flags to ensure ACROSS and DOWN appear only once
            boolean acrossFound = false;
            boolean downFound = false;
            
            // To keep track of line numbers for better error messages
            int lineNumber = 0;
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim(); // Remove leading and trailing whitespaces

                if (line.isEmpty()) {
                    continue;
                }
                
                // Check for ACROSS and DOWN sections
                if (line.startsWith("ACROSS")) {
                    if (acrossFound) {
                        throw new IllegalArgumentException("ACROSS section appears more than once. Error at line " + lineNumber);
                    }
                    acrossFound = true;
                    readingAcross = true;
                    readingDown = false;
                    // Optionally, validate that the line is exactly "ACROSS,,"
                    String[] headerData = line.split(",", -1); // -1 to include trailing empty strings
                    if (headerData.length != 3 || 
                        !headerData[1].trim().isEmpty() || 
                        !headerData[2].trim().isEmpty()) {
                        throw new IllegalArgumentException("ACROSS header must be in the format 'ACROSS,,'. Error at line " + lineNumber);
                    }
                    continue; // Move to the next line (skip the header)
                } 
                else if (line.startsWith("DOWN")) {
                    if (downFound) {
                        throw new IllegalArgumentException("DOWN section appears more than once. Error at line " + lineNumber);
                    }
                    downFound = true;
                    readingAcross = false;
                    readingDown = true;
                    // Optionally, validate that the line is exactly "DOWN,,"
                    String[] headerData = line.split(",", -1);
                    if (headerData.length != 3 || 
                        !headerData[1].trim().isEmpty() || 
                        !headerData[2].trim().isEmpty()) {
                        throw new IllegalArgumentException("DOWN header must be in the format 'DOWN,,'. Error at line " + lineNumber);
                    }
                    continue; // Move to the next line (skip the header)
                }

                // Ensure data lines are within ACROSS or DOWN sections
                if (!(readingAcross || readingDown)) {
                    throw new IllegalArgumentException("Data line found outside of ACROSS or DOWN sections. Error at line " + lineNumber);
                }
                
                // Split the line into parts, considering empty fields
                String[] data = line.split(",", -1); // -1 to include trailing empty strings

                // Validate that there are exactly three parameters
                if (data.length != 3) {
                    throw new IllegalArgumentException("Invalid number of fields at line " + lineNumber + ". Expected 3, found " + data.length);
                }

                String numberStr = data[0].trim();
                String answer = data[1].trim();
                String question = data[2].trim();

                // Validate that the answer does not contain any whitespace
                if (answer.contains(" ")) {
                    throw new IllegalArgumentException("Answer contains whitespace at line " + lineNumber + ": '" + answer + "'");
                }

                // Validate that the question contains at least one non-whitespace character
                if (question.isEmpty()) {
                    throw new IllegalArgumentException("Question cannot be empty at line " + lineNumber + ".");
                }
                
                // Validate that the first parameter is a valid integer
                int number;
                try {
                    number = Integer.parseInt(numberStr);
                    if (number <= 0) {
                        throw new IllegalArgumentException("Number must be a positive integer at line " + lineNumber + ".");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number format at line " + lineNumber + ": '" + numberStr + "'");
                }

                // Create a new Data instance
                String placement = readingAcross ? "a" : "d";
                Data newData = new Data(numberStr, answer, question, placement, false, -1, -1, false);

                // Add to appropriate lists
                if (readingAcross) {
                    acrossData.add(newData);
                    copyacrossData.add(newData);
                    datas.add(newData); 
                } else if (readingDown) {
                    downData.add(newData);
                    copydownData.add(newData);
                    datas.add(newData);
                }
            }

            // After reading all lines, validate that both sections were found
            if (!acrossFound) {
                throw new IllegalArgumentException("ACROSS section is missing in the CSV file.");
            }
            if (!downFound) {
                throw new IllegalArgumentException("DOWN section is missing in the CSV file.");
            }

            // Additional post-processing if needed, such as matching numbers between sections

//            System.out.println("CSV file read and validated successfully.");

        } 
        catch (IOException e) {
            System.err.println("Error reading the CSV file: " + e.getMessage());
        }
        catch (IllegalArgumentException e) {
            System.err.println("CSV validation error: " + e.getMessage());
        }
    }
    
    public void printOutQuestions() {
    	// Display the "ACROSS" data
      if (!acrossData.isEmpty()) {
          System.out.println("For ACROSS:");
          for (int i = 0; i < acrossData.size(); ++i) {
              System.out.println("Number: " + acrossData.get(i).getNumber());
              System.out.println("Answer: " + acrossData.get(i).getAnswer());
              System.out.println("Question: " + acrossData.get(i).getQuestion());
              System.out.println("Placement: " + acrossData.get(i).getPlacement());
          }
      }

      // Display the "DOWN" data
      if (!downData.isEmpty()) {
          System.out.println("For ACROSS:");
          for (int i = 0; i < acrossData.size(); ++i) {
              System.out.println("Number: " + downData.get(i).getNumber());
              System.out.println("Answer: " + downData.get(i).getAnswer());
              System.out.println("Question: " + downData.get(i).getQuestion());
              System.out.println("Placement: " + downData.get(i).getPlacement());
          }
      }
    }
    
    // Scan for all .csv files in the "gamedata" directory
    public List<File> scanCSVFiles() {
        List<File> csvFiles = new ArrayList<>();
        try {
            // Using Files.walk() to get a Stream<Path> and iterate with a traditional for loop
            List<Path> paths = Files.walk(Paths.get(gameDataFile)).toList(); // Collect the stream into a list
            for (Path path : paths) {
                if (Files.isRegularFile(path)) {
                    File file = path.toFile();
                    // Check if the file ends with .csv
                    if (file.getName().toLowerCase().endsWith(".csv")) {
                        csvFiles.add(file);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error scanning for CSV files: " + e.getMessage());
        }
        return csvFiles;
    }

    // Randomly select one of the CSV files
    public File randomlySelectCSVFile(List<File> csvFiles) {
        Random random = new Random();
        return csvFiles.get(random.nextInt(csvFiles.size()));
    }

    // Check if the CSV file is valid (simple check: file is non-empty and has at least one line)
    public boolean isValidCSVFile(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            return br.readLine() != null; // Check if the file has at least one line
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            return false;
        }
    }
}
