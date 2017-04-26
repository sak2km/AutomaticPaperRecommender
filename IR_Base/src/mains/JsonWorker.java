package mains;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


     

public class JsonWorker {
	
	public static void main(String[] args) throws JSONException, IOException {
		
		
		
		BufferedReader br = null;
		JSONArray Reviews = new JSONArray();
//		JSONArray info = new JSONArray();

		String sCurrentLine, temp, content, title;

		br = new BufferedReader(new FileReader("./mat.txt"));
		
		int i = 0;
		while ((sCurrentLine = br.readLine()) != null) 
		{
//			System.out.println(sCurrentLine);
			if(sCurrentLine.equals("Title: "))
			{
//				System.out.println(" Cfbcfg ");
				JSONObject obj = new JSONObject();
				title = br.readLine();
				obj.put("Author", "");
				obj.put("ReviewID", String.valueOf(i++));
				obj.put("Overall", "1.0");
				content = null;
				temp = br.readLine();
				if(temp.equals("Abstract:"))
				{
					while((temp = br.readLine()) != null)
					{
						if(temp.equals(""))break;
						else{
							content += temp;
						}
					}
				}
				obj.put("Content", content);
				obj.put("Title", title);
				obj.put("Date", "June 17, 2005");
				Reviews.put(obj);
			}
//			if(i>=0)break;
			
			
			
		}
		
		JSONObject ProductInfo = new JSONObject();
		ProductInfo.put("Price", "");
		ProductInfo.put("ProductID", "B00007FHEN");
		ProductInfo.put("Features", "");
		ProductInfo.put("ImgURL", "");
		ProductInfo.put("Name", "");
		System.out.println(Reviews);
		
		JSONObject info = new JSONObject();
		info.put("Reviews", Reviews);
		info.put("ProductInfo",ProductInfo);
//		System.out.println(info);

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./mat.json"), "UTF-8"));
		writer.write(info.toString());
		writer.close();
		
		
//		int pageLimit = 5;
//		
//		for(int n = 0; n < pageLimit; n++)
//		{
//		    
//		    // do some stuff....
//		    JSONObject obj = new JSONObject();
//			obj.put("name", "mkyong.com");
//			obj.put("age", new Integer(100));
//			
//			obj.put("name", "mk.com");
//			obj.put("age", new Integer(100));
//			array.put(obj);
//		}
		
//		for(int n = 0; n < array.length(); n++)
//		{
//		    JSONObject obj = array.getJSONObject(n);
//		    System.out.println(obj.toString());
//		    // do some stuff....
//		}



	     }

}
