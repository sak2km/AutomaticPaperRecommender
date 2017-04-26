package wrappers;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Wrapper4WebScience {
	public Wrapper4WebScience()
	{
		
		/*File input = new File("./data/html/WebOfScience/web.html");
		try {
			Document doc = Jsoup.parse(input, "UTF-8");
			System.out.println(doc.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//Document doc = null;
		
		Document doc = null;
		try {
			doc = Jsoup.connect("https://apps.webofknowledge.com/Search.do?product=WOS&SID=1FGHNybHaHetXQutfL7&search_mode=GeneralSearch&prID=2ce8d453-4fb4-4f0e-b65a-53ab2f30d59f").get();
			System.out.println("In Class");
			//Elements elms = doc.getElementsByClass("small);
			System.out.println(doc.toString());
			//System.out.println("In Class");
		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//System.out.println(doc.getElementById("tmp_chunk_data_1").toString());
        Elements links = doc.select("a[href]");
        
		
		Element e= doc.getElementById("mw-content-text");
		//System.out.println(e.attr("href").toString());

	}
	public static void main(String[] srgs)
	{
		Wrapper4WebScience wrapper = new Wrapper4WebScience();
		//wrapper.
		
	}
}
