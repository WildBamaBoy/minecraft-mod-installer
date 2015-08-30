package com.radixshock.minecraft.installer;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class NetIO 
{
	public static String readStringFromURL(String urlString) throws IOException
	{
		URL url = new URL(urlString);
		URLConnection connection = url.openConnection();
		connection.connect();

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String output = in.readLine();
		in.close();
		
		return output;
	}
	
	public static void downloadFile(String urlString, String outputPath) throws IOException
	{		
		URL url = new URL(urlString);
		
		ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		FileOutputStream fos = new FileOutputStream(outputPath);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		
		fos.close();
	}
	
	private NetIO(){}
}
