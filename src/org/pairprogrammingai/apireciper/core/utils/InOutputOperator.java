package org.pairprogrammingai.apireciper.core.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

public class InOutputOperator {
	
	public static void writeln(String filepath, String str){
		try{
			File outFile = new File(filepath);
			FileWriter out = new FileWriter(outFile);
			out.write(str + "\n");
			out.close();
		} catch(IOException e) {
        }
	}
	
	public static void write(String filepath, String str){
		try{
			File outFile = new File(filepath);
			FileWriter out = new FileWriter(outFile);
			out.write(str);
			out.close();
		} catch(IOException e) {
        }
	}
	
	public static String read(String filepath){
		String ret = "";
		try{
			File file = new File(filepath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String str = br.readLine();
			while(str != null){
				ret = ret + str + "\n";
			    str = br.readLine();
			}
			br.close();
		} catch(IOException e) {
        }
		return ret;
	}
	
	public static List<String> readLists(String filepath){
		List<String> ret = new ArrayList<String>();
		try{
			File file = new File(filepath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String str = br.readLine();
			while(str != null){
				ret.add(str);
			    str = br.readLine();
			}
			br.close();
		} catch(IOException e) {
        }
		return ret;
	}
	
	public static void concatFiles(String[] inputFilePaths, String outputFilePath) throws IOException{
        FileList fl = new FileList(inputFilePaths);
        SequenceInputStream exSequence = new SequenceInputStream(fl);
        
        File outFile = new File(outputFilePath);
		FileWriter out = new FileWriter(outFile);

        int contents;
        while((contents = exSequence.read()) != -1) {
        	out.write(contents);
        }
        exSequence.close();
		out.close();
	}
	
	public static class FileList implements Enumeration {
	    private String[] FileList;
	    private int count = 0;

	    public FileList(String[] FileList) {
	        this.FileList = FileList;
	    }

	    public boolean hasMoreElements() {
	        if (count < FileList.length) {
	            return true;
	        } else {
	            return false;
	        }
	    }

	    public InputStream nextElement() {
	        InputStream in = null;
	        if (!hasMoreElements()) {
	            throw new NoSuchElementException("No File");
	        } else {
	            String nextFile = FileList[count];
	            count++;
	            try {
	                in = new FileInputStream(nextFile);
	            } catch (FileNotFoundException e) {
	            }
	        }
	        return in;
	    }
	}
}
