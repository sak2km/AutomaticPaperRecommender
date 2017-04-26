package mains;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.json.JSONException;

import wrappers.*;

public class CrawlerInputReader {
	
    
	public CrawlerInputReader() throws IOException, InterruptedException, JSONException
	{
		System.out.println("in crawler");
		File file = new File("./data/WebOfScience/input.txt");
		
	    BufferedReader br = null;
	    String homeUrl = null;
	    String outputFile= null;
	    int pageLimit = 1;
	    BufferedWriter writer = null;
	    
	
        java.io.FileReader fr = new java.io.FileReader(file);
        br = new BufferedReader(fr);

        String line;
        int i=0;
        String overall = "1.0";
        while( (line = br.readLine()) != null ) 
        {
        	System.out.println(line);
        	if(i%3==0){
        		homeUrl = line;
        	}
        	if(i%3==1){
        		outputFile = "./data/NewData/"+line;
//        		outputFile = "./data/WebOfScience/"+line;
//        		
        	}
        	if(i%3==2){
        		pageLimit = Integer.parseInt(line);
        		
        	}
        	
        	i++;
        	if(i==3){
        		Wrapper4WebMD wrapper = new Wrapper4WebMD(homeUrl, outputFile, pageLimit, overall);
            	wrapper.processCrwling();
        		overall = "2.0";
        		i = 0;
        	}
        }

    
        	
        	
        	
        
    }

}
