package jautomata.generator;

import java.io.*;


/**
 * @author Bastian Polewka
*/
public class CSVFilter {
	
	public CSVFilter() {
		
	}
		
	public void removePreviousLines(File inputCsv, String property) {
	
	    try {
	        BufferedReader reader = new BufferedReader(new FileReader(inputCsv));
	        String line;
	
	        String headerLine = reader.readLine();
	        headerLine = reader.readLine();
	        if (headerLine == null) {
	            reader.close();
	            throw new RuntimeException("The file is empty.");
	        }
	
	        String[] headers = headerLine.split(",");
             
	        int targetColumnIndex = -1;
	        for (int i = 0; i < headers.length; i++) {
	            if (headers[i].trim().equalsIgnoreCase("Property")) {
	                targetColumnIndex = i;
	                break;
	            }
	        }
	
	        if (targetColumnIndex == -1) {
	        	reader.close();
	            throw new RuntimeException("Column 'Property' not found.");
	        }
	
	        BufferedWriter writer = new BufferedWriter(new FileWriter(inputCsv));
	
	        writer.write(headerLine);
	        writer.newLine();
	
	        while ((line = reader.readLine()) != null) {
	            String[] values = line.split(",");
	
	            // Check if the value in the target column matches the one to delete
	            if (!values[targetColumnIndex].trim().equalsIgnoreCase(property)) {
	                writer.write(line);
	                writer.newLine();
	            }
	        }
	
	        // Close the resources
	        reader.close();
	        writer.close();
	
	        System.out.println("Filtered CSV written to " + property);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
