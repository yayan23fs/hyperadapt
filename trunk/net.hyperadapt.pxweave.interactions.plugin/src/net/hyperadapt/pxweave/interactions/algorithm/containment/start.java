package net.hyperadapt.pxweave.interactions.algorithm.containment;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class start {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		URL url;
		try {
			url = new URL("file:///Users/danielkadner/Documents/Diplom/eclipse_amacont_ws/Amaconda/./src/main/resources/COB-INF/soundnexus/index.ama/../adaptationaspects.xml");
		
		
		 URLConnection connection = url.openConnection();
		 
		 // XML Daten einlesen
		 ByteArrayOutputStream result = new ByteArrayOutputStream();
		 InputStream input = connection.getInputStream();
		 byte[] buffer = new byte[1000];
		 int amount = 0;    
		 
		 // Inhalt lesen
		 while(amount != -1){
		 
		   result.write(buffer, 0, amount);
		   amount = input.read(buffer);
		 
		 }
		 
		 System.out.println(result.toString());

		 
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		File f = new File("file:///Users/danielkadner/Documents/Diplom/eclipse_amacont_ws/Amaconda/src/main/resources/COB-INF/soundnexus/adaptationaspects.xml");
		System.out.println(f.exists());
		
	}

}
