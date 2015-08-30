package com.radixshock.minecraft.installer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.json.JSONObject;

public class Installer implements Runnable
{
	private String modName;
	private String mcVersion;
	private String modVersion;
	private FrameMain frame;

	public Installer(FrameMain frame, String modName, String modVersion, String mcVersion)
	{
		this.frame = frame;
		this.modName = modName;
		this.modVersion = modVersion;
		this.mcVersion = mcVersion;
	}

	@Override
	public void run() 
	{
		try
		{
			modName = modName.replace(" ", "");
			Path tempDir = Files.createTempDirectory("RadixShockModInstaller");

			frame.setProgress(10, "Getting ready to install...");
			String radixCoreVersion = NetIO.readStringFromURL("http://files.radix-shock.com/get-xml-property.php?modName=RadixCore&mcVersion=%mcVersion%&xmlProperty=version".replace("%mcVersion%", mcVersion));
			String forgeVersion = NetIO.readStringFromURL("http://files.radix-shock.com/get-xml-property.php?modName=%modName%&mcVersion=%mcVersion%&xmlProperty=forge".replace("%modName%", modName).replace("%mcVersion%", mcVersion));

			String modFileName = modName + "-" + mcVersion + "-" + modVersion + "-universal.jar";
			String rcFileName = "RadixCore-" + mcVersion + "-" + radixCoreVersion + "-universal.jar";
			String fgFileName = "forge-%mcVersion%-%forgeVersion%-installer.jar".replace("%mcVersion%", mcVersion).replace("%forgeVersion%", forgeVersion);

			//Download mod
			frame.setProgress(20, "Downloading " + modName + "...");
			frame.setProgressIndeterminate(true);

			String url = ("http://files.radix-shock.com/serve.php?path=%modName%/%mcVersion%/%modFileName%".replace("%modName%", modName).replace("%mcVersion%", mcVersion).replace("%modFileName%", modFileName));
			NetIO.downloadFile(url, tempDir.toString() + "/" + modFileName);

			//Download RadixCore
			frame.setProgress(30, "Downloading RadixCore...");

			url = "http://files.radix-shock.com/serve.php?path=RadixCore/%mcVersion%/%rcFileName%".replace("%mcVersion%", mcVersion).replace("%rcFileName%", rcFileName);
			NetIO.downloadFile(url, tempDir.toString() + "/" + rcFileName);

			//Download Forge
			frame.setProgressIndeterminate(true);
			frame.setProgress(50, "Downloading Forge...");

			url = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/%mcVersion%-%forgeVersion%/forge-%mcVersion%-%forgeVersion%-installer.jar".replace("%mcVersion%", mcVersion).replace("%forgeVersion%", forgeVersion);
			NetIO.downloadFile(url, tempDir.toString() + "/" + fgFileName);

			//Allow Forge to install.
			frame.setProgress(60, "Waiting for Forge installer to complete...");

			JOptionPane.showMessageDialog(null, "Now opening the Forge installer. When it appears, choose 'Install Client' and click Ok.", "Installing Forge", JOptionPane.INFORMATION_MESSAGE);
			Process proc = Runtime.getRuntime().exec("java -jar " + tempDir.toString() + "/" + fgFileName);
			proc.waitFor();

			//Modify launcher profiles.
			frame.setProgressIndeterminate(false);
			frame.setProgress(70, "Installing mod profile...");

			File launcherProfiles = new File(Installer.getMinecraftDirectory() + "/launcher_profiles.json");
			Scanner scanner = new Scanner(launcherProfiles);
			JSONObject rawJSON = new JSONObject(scanner.useDelimiter("\\Z").next());
			scanner.close();

			JSONObject profilesObj = rawJSON.getJSONObject("profiles");
			String lastVersionId = mcVersion + "-Forge" + forgeVersion;

			JSONObject profileData = new JSONObject();
			String profileName = (modName + "-" + mcVersion).trim();
			
			profileData.put("name", profileName);
			profileData.put("lastVersionId", lastVersionId);

			profilesObj.put(profileName, profileData);
			rawJSON.put("selectedProfile", profileName);

			Files.delete(launcherProfiles.toPath());
			Files.write(launcherProfiles.toPath(), rawJSON.toString().getBytes(), StandardOpenOption.CREATE_NEW);

			//Move mods to mods folder
			frame.setProgress(80, "Installing mod...");

			File modsFolder = new File(Installer.getMinecraftDirectory() + "/mods/");

			if (!modsFolder.exists())
			{
				modsFolder.mkdirs();
			}

			//Remove old mods.
			for (File file : modsFolder.listFiles())
			{
				if (file.getName().contains(modName) || file.getName().contains("RadixCore"))
				{
					Files.delete(file.toPath());
				}
			}

			//Move the mod file and RadixCore to mods folder.
			Files.move(Paths.get(tempDir.toString() + "/" + modFileName), Paths.get(modsFolder.getPath() + "/" + modFileName), StandardCopyOption.REPLACE_EXISTING);
			Files.move(Paths.get(tempDir.toString() + "/" + rcFileName), Paths.get(modsFolder.getPath() + "/" + rcFileName), StandardCopyOption.REPLACE_EXISTING);

			frame.setProgress(90, "Cleaning up...");

			//Cleanup.
			Files.delete(Paths.get(tempDir.toString() + "/" + fgFileName));
			Files.delete(tempDir);

			frame.setProgress(100, "Complete!");
			JOptionPane.showMessageDialog(null, "Installation has completed successfully! Start your Minecraft launcher and click Play.", "Install Complete", JOptionPane.INFORMATION_MESSAGE);
		}

		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "An unexpected error has occurred. Installation cannot continue. Error message: " + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}

		finally
		{
			System.exit(0);
		}
	}

	public static String getMinecraftDirectory()
	{
		String OS = System.getProperty("os.name").toUpperCase();

		if (OS.contains("WIN"))
		{
			return System.getenv("APPDATA") + "/.minecraft";
		}

		else if (OS.contains("MAC"))
		{
			return System.getProperty("user.home") + "/Library/Application Support/minecraft";
		}

		else if (OS.contains("NUX"))
		{
			return System.getProperty("user.home") + "/minecraft";
		}

		return System.getProperty("user.dir") + "/minecraft";
	}
}
