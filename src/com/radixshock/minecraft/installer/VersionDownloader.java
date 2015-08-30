package com.radixshock.minecraft.installer;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

public class VersionDownloader implements Runnable
{
	private String modName;
	private FrameMain frame;

	public VersionDownloader(String modName, FrameMain frame)
	{
		this.modName = modName;
		this.frame = frame;
	}

	@Override
	public void run() 
	{
		try
		{
			String trimmedModName = modName.replace(" ", "");
			frame.setProgress(0, "Getting available versions of " + modName + "...");
			frame.setProgressIndeterminate(true);
			
			//Download the MC versions.
			String url = "http://files.radix-shock.com/get-available-mc-versions.php?modName=%modName%".replace("%modName%", trimmedModName);
			String remoteMCVersions = NetIO.readStringFromURL(url);
			
			ArrayList<String> versions = new ArrayList<String>();

			for (String s : remoteMCVersions.split("\\|"))
			{
				if (!s.isEmpty())
				{
					versions.add(s);
				}
			}	
			
			//Get the latest mod version for all MC versions.
			HashMap<String, String> modVersions = new HashMap<String, String>();
			
			for (String version : versions)
			{
				url = "http://files.radix-shock.com/get-xml-property.php?modName=%modName%&mcVersion=%mcVersion%&xmlProperty=version".replace("%modName%", trimmedModName).replace("%mcVersion%", version);
				String latestVersion = NetIO.readStringFromURL(url);
				modVersions.put(version, latestVersion);
			}
			
			frame.addModVersions(modName, modVersions);
			frame.addMCVersions(modName, versions);
		}

		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "An unexpected error has occurred. Installation cannot continue. Error message: " + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
	}
}
