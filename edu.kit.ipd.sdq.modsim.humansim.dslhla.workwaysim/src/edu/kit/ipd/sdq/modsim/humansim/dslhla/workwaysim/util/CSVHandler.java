package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CSVHandler {
	
	public static final String CSV_DELIMITER= ";";
	public static final String NEWLINE = "\n";
	
	public static void writeCSVFile(String filename, String csvString){
		FileWriter fileWriter = null;
		//Path workingDirectory=Paths.get(".").toAbsolutePath();
		
		String workingDirectory = "HumanSimData";
		
//		System.out.println(workingDirectory);
   		try {
       		fileWriter = new FileWriter(workingDirectory.toString() + "\\" + filename + ".csv" + "\\");
       		fileWriter.append(csvString);
       	} catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {            
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {

                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
	}
	
	public static String readCSV(String fileName){
		
		String csvString = "";
		String line = "";
		BufferedReader fileReader = null;
		String workingDirectory = "HumanSimData";

		try{
			fileReader = new BufferedReader(new FileReader(workingDirectory.toString() + "\\" + fileName + ".csv"));
			
			
			
			while((line = fileReader.readLine()) != null){
				csvString += line;
			}
			
		} catch (Exception e){
			System.out.println("Error in CSVFileReader");
		} finally {
			try{
				fileReader.close();
			} catch (IOException e){
				System.out.println("Error while closing fileReader !!!");
			}
			
			
		} 	
		
		
		return csvString;
	}
	
	public static void readCSVAndAppend(String fileName, String appendString){
		String s = "";
		//Path workingDirectory=Paths.get(".").toAbsolutePath();
		String workingDirectory = "HumanSimData";
		System.out.println(workingDirectory);
		File f = new File(workingDirectory + "\\" + fileName + ".csv");
		if(f.exists() && !f.isDirectory()){
		s = readCSV(fileName);
		}

		s += appendString + NEWLINE;
	
		writeCSVFile(fileName, s);
		
	}
	
}
