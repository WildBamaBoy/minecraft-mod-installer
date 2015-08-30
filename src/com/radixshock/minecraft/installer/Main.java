package com.radixshock.minecraft.installer;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;

import javax.swing.UIManager;

public class Main 
{
	public static void main(String[] args)
	{
		if (GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length == 0)
		{
			System.err.println("This installer cannot be run on headless systems.");
			System.exit(-1);
		}
		
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run() 
			{
				String os = System.getProperty("os.name").toUpperCase();
				
				if (os.contains("WIN"))
				{
					try
					{
						UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
					}
					
					catch (Exception e)
					{
						//Ignore.
					}
				}
				
				FrameMain mainFrame = new FrameMain();
				mainFrame.setVisible(true);
			}
		});
	}
}
