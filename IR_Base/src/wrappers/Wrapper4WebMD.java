/**
 * 
 */
package wrappers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;





/**
 * @author hongning
 * @version 0.1
 * @category Wrapper
 * sample code for parsing html files from WebMD forum and extract threaded discussions to json format 
 */



/**
 * Modified by Rizwan for crawling Web of Sciene
 */




public class Wrapper4WebMD extends WrapperBase {
	//TODO: You need to extend this wrapper to deal with threaded discussion across multiple pages
	// 1. get the right "next page"
	// 2. avoid any duplication
	// 3. extract the right reply-to relation when across pages
	// 4. clean up the local structures for processing different threaded discussions 
	File file = new File("./data/WebOfScience/input.txt");

    BufferedReader br = null;
    String homeUrl = null;
    String outputFile= null;
    int pageLimit = 1;
    BufferedWriter writer = null;
    String overall = null;
    
	
	public Wrapper4WebMD(String home, String outFile, int max_page, String rating) {
		super();
		homeUrl = home;
		outputFile = outFile;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		overall = rating;
		pageLimit =max_page;
		
		
		m_dateParser = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (z)");//Date format in this forum:  Mon Sep 17 2012 13:47:16 GMT-0400 (EDT)
	}
		
	protected void  get_abstract(ArrayList<URL> urls) throws IOException{
		//BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./data/json/WebOfScience/parsed3.txt"), "UTF-8"));
		
		
		
		for(URL url:urls)
		{
			//System.out.println(url);
			Document doc = Jsoup.parse(url, 200000);
			//Document doc = Jsoup.parse(new File("./data/HTML/webOfScience/abstract.html"), "UTF-8");
			Elements elms = doc.getElementsByClass("title");
			String title = elms.first().getElementsByTag("value").first().text();
			Elements tmpElms = doc.getElementsByClass("block-record-info");
			//Element e = tmpElms.re
			elms = tmpElms.tagName("p");
			String str = elms.get(2).text().replaceFirst("Abstract ", "");
				
			writer.write("Title: \n");
			writer.write(title+"\n"+"Abstract:\n"+str+"\n\n");
				
			
		}
		System.out.println("Done");
		
		
	}
	
	@Override
	protected String extractReplyToID(String text) {
		return text.replaceAll("\\?pg=\\d+#", "/");//normlize the replyToID to the corresponding postID
	}

	@Override
	protected boolean parseHTML(Document doc) {
		ArrayList<URL> paperUrls = new ArrayList<URL>();
		int temp = Integer.parseInt(doc.getElementById("pageCount.top").text().replaceAll(",", ""));
		if(pageLimit>temp)pageLimit = temp;
		for(Element node:doc.getElementsByClass("smallV110")) {
			try {
				URL url = new URL("http://apps.webofknowledge.com/" + node.attr("href").toString());
				//System.out.println(url);
				paperUrls.add(url);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		try {
			get_abstract(paperUrls);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		Elements postElms = doc.getElementsByClass("search-results-content");
//		
//		Element tmpElmA, tmpElmB;
//		System.out.println(postElms.toString());
//		//System.out.println("g");
		
		homeUrl = doc.getElementsByClass("paginationNext").first().attr("href").toString();
		return true;
	}	
	
//	public void writeJson(JsonArray array)
//	{
//		
//	}
	
	public void processCrwling() throws IOException, InterruptedException, JSONException
	{

//		Wrapper4WebMD wrapper = new Wrapper4WebMD();
//		
//		try {
//	        java.io.FileReader fr = new java.io.FileReader(wrapper.file);
//	        wrapper.br = new BufferedReader(fr);
//
//	        String line;
//	        int i=0;
//	        while( (line = wrapper.br.readLine()) != null ) {
//	        	if(i==0){
//	        		wrapper.homeUrl = line;
//	        	}
//	        	if(i==1){
//	        		//wrapper.outputFile = new File("./data/HTML/WebOfScience/"+line);
//	        		wrapper.outputFile = "./data/WebOfScience/"+line;
////	        		wrapper.outputFile = line;
//	        		wrapper.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(wrapper.outputFile), "UTF-8"));
//	        		
//	        	}
//	        	if(i==2){
//	        		wrapper.pageLimit = Integer.parseInt(line);
//	        		
//	        	}
//	        	
//	        	i++;
//	        }
//
//	    } catch (FileNotFoundException e) {
//	        System.out.println("File not found: " + wrapper.file.toString());
//	    } catch (IOException e) {
//	        System.out.println("Unable to read file: " + wrapper.file.toString());
//	    }
//	    finally {
//	        try {
//	        	wrapper.br.close();
//	        } catch (IOException e) {
//	            System.out.println("Unable to close file: " + wrapper.file.toString());
//	        }
//	        catch(NullPointerException ex) {
//	        }
//	    }
		
		for(int i = 1;i<=pageLimit;i++)
			{
				System.out.println("now printing page: "+ i + " and pagelimit is: " + pageLimit);
//				if(i%200 == 0)
//				{
//					System.out.println("########## Sleeping for 10 minutes. ##############");
//					Thread.sleep(10*1000*60);
//					System.out.println("############### Wake up and is working ############");
//				}
					
					
				parseHTML(homeUrl);
			}
		writer.close();
		System.out.println("generating json file");
		JsonWorker jsonGenerator = new JsonWorker ();
//		String overall = "1.0"; // 1.0 for relevant, 2.0 for irrelevant 
		jsonGenerator.generate(outputFile, outputFile+".json",overall );
		System.out.println("generating completed");
		
	}
}