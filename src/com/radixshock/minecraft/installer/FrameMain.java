package com.radixshock.minecraft.installer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class FrameMain extends JFrame implements ActionListener
{
	private static int WIDTH = 750;
	private static int HEIGHT = 435;
	private static Map<String, List<String>> mcVersionMap = new HashMap<String, List<String>>();
	private static Map<String, Map<String, String>> modVersionMap = new HashMap<String, Map<String, String>>();
	private static Image backgroundImage;
	private static Font minecraftFont;
	private static Font minecraftFontSmall;
	
	private JComboBox<String> boxMcVersionSelect;
	private JComboBox<String> boxModSelect;
	private JLabel labelReady;
	private JLabel labelProgress;
	private JProgressBar barProgress;
	private JButton buttonInstall;
	private static final String readyString = "Ready to install %mod% version %version% for Minecraft %mcVersion%";

	public FrameMain()
	{
		initializeComponents();
	}

	private void initializeComponents()
	{
		this.setTitle("Official RadixShock Minecraft Mod Installer");
		this.setSize(WIDTH, HEIGHT);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false);

		try
		{
			GraphicsEnvironment gfx = GraphicsEnvironment.getLocalGraphicsEnvironment();
			backgroundImage = ImageIO.read(getClass().getResourceAsStream("/resources/background.png"));
			minecraftFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/resources/Minecraftia.ttf"));

			gfx.registerFont(minecraftFont);

			minecraftFont = new Font(minecraftFont.getName(), Font.PLAIN, 18);
			minecraftFontSmall = new Font(minecraftFont.getName(), Font.PLAIN, 14);
		}

		catch (Exception e)
		{
			e.printStackTrace();	
		}

		JLabel panel = new JLabel(new ImageIcon(backgroundImage));
		this.setLayout(new BorderLayout());
		this.add(panel);
		panel.setLayout(new FlowLayout());

		JLabel labelIntro = new JLabel("Welcome to the RadixShock Minecraft mod installer!", SwingConstants.CENTER);
		labelIntro.setPreferredSize(new Dimension(WIDTH, 40));
		labelIntro.setFont(minecraftFont);
		labelIntro.setForeground(Color.WHITE);
		
		JLabel labelDescription = new JLabel("<html><center>This will install the most up-to-date version of your selected mod to the <br> default Minecraft launcher. If it's already installed, this installer will update it for you.</center></html>");
		labelDescription.setPreferredSize(new Dimension(WIDTH, 100));
		labelDescription.setFont(minecraftFontSmall);
		labelDescription.setForeground(Color.WHITE);
		
		JLabel labelModSelect = new JLabel("Choose the mod you'd like to install: ");
		labelModSelect.setFont(minecraftFontSmall);
		labelModSelect.setForeground(Color.WHITE);
		
		boxModSelect = new JComboBox<String>();
		boxModSelect.addItem("MCA");
		boxModSelect.addItem("Spider Queen");
		boxModSelect.setSelectedIndex(-1);
		boxModSelect.setPreferredSize(new Dimension(140, 20));
		boxModSelect.addActionListener(this);

		JLabel labelMcVersionSelect = new JLabel("Choose your preferred Minecraft version: ");
		labelMcVersionSelect.setFont(minecraftFontSmall);
		labelMcVersionSelect.setForeground(Color.WHITE);
		
		boxMcVersionSelect = new JComboBox<String>();
		boxMcVersionSelect.setPreferredSize(new Dimension(140, 20));
		boxMcVersionSelect.addActionListener(this);

		JLabel labelSpacer = new JLabel("");
		labelSpacer.setPreferredSize(new Dimension(WIDTH, 50));
		
		labelReady = new JLabel(readyString, SwingConstants.CENTER);
		labelReady.setFont(minecraftFontSmall);
		labelReady.setForeground(Color.GREEN);
		
		labelReady.setPreferredSize(new Dimension(WIDTH, 40));
		labelReady.setVisible(false);

		buttonInstall = new JButton("Install");
		buttonInstall.setFont(minecraftFontSmall);
		buttonInstall.setPreferredSize(new Dimension(180, 35));
		buttonInstall.addActionListener(this);
		buttonInstall.setEnabled(false);

		labelProgress = new JLabel("Progress", SwingConstants.LEFT);
		labelProgress.setFont(minecraftFontSmall);
		labelProgress.setForeground(Color.WHITE);
		labelProgress.setPreferredSize(new Dimension(WIDTH - 25, 20));
		labelProgress.setVisible(false);

		barProgress = new JProgressBar();
		barProgress.setPreferredSize(new Dimension(WIDTH - 25, 25));
		barProgress.setVisible(false);

		panel.add(labelIntro);
		panel.add(labelDescription);
		panel.add(labelModSelect);
		panel.add(boxModSelect);
		panel.add(labelMcVersionSelect);
		panel.add(boxMcVersionSelect);
		panel.add(labelSpacer);
		panel.add(labelReady);
		panel.add(buttonInstall);
		panel.add(labelProgress);
		panel.add(barProgress);
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == boxMcVersionSelect)
		{
			if (boxMcVersionSelect.getSelectedIndex() != -1)
			{
				String modName = boxModSelect.getSelectedItem().toString();
				String mcVersion = boxMcVersionSelect.getSelectedItem().toString();
				String modVersion = modVersionMap.get(modName).get(mcVersion);
				labelReady.setText(readyString.replace("%mod%", modName).replace("%version%", modVersion).replace("%mcVersion%", mcVersion));
			}

			labelReady.setVisible(true);
			buttonInstall.setEnabled(true);
		}

		else if (e.getSource() == boxModSelect)
		{
			String modName = boxModSelect.getSelectedItem().toString();
			List<String> mcVersions = mcVersionMap.get(modName);

			if (mcVersions != null)
			{
				populateMcVersionSelectBox(mcVersions);
			}

			else
			{
				new Thread(new VersionDownloader(modName, this)).start();
			}
		}

		else if (e.getSource() == buttonInstall)
		{
			String modName = boxModSelect.getSelectedItem().toString();
			String mcVersion = boxMcVersionSelect.getSelectedItem().toString();
			String modVersion = modVersionMap.get(modName).get(mcVersion);

			File mcVersionFile = new File(Installer.getMinecraftDirectory() + "/versions/" + mcVersion);

			if (!mcVersionFile.exists())
			{
				JOptionPane.showMessageDialog(null, "You must run the " + mcVersion + " version of Minecraft at least once before the installation can continue.");
			}

			else
			{
				new Thread(new Installer(this, modName, modVersion, mcVersion)).start();
				buttonInstall.setEnabled(false);
			}
		}
	}

	public void addMCVersions(String modName, List<String> versions)
	{
		mcVersionMap.put(modName, versions);
		populateMcVersionSelectBox(versions);
	}

	public void addModVersions(String modName, Map<String, String> modVersions)
	{
		modVersionMap.put(modName, modVersions);
	}

	public void setProgress(int progressValue, String message)
	{
		labelProgress.setVisible(true);
		barProgress.setVisible(true);

		labelProgress.setText(message);
		barProgress.setValue(progressValue);
	}

	public void setProgressIndeterminate(boolean newValue) 
	{
		barProgress.setIndeterminate(newValue);
	}

	private void populateMcVersionSelectBox(List<String> versions)
	{
		boxMcVersionSelect.removeAllItems();

		Collections.sort(versions, versionComparator);
		Collections.reverse(versions);

		for (String version : versions)
		{
			boxMcVersionSelect.addItem(version);
		}

		labelProgress.setVisible(false);
		barProgress.setIndeterminate(false);
		barProgress.setVisible(false);
	}

	private static int compareVersions(String v1, String v2)
	{
		String[] version1 = v1.split("\\.");
		String[] version2 = v2.split("\\.");
		int i = 0;

		while (i < version1.length && i < version2.length && version1[i].equals(version2[i])) 
		{
			i++;
		}

		if (i < version1.length && i < version2.length) 
		{
			int delta = Integer.valueOf(version1[i]).compareTo(Integer.valueOf(version2[i]));
			return Integer.signum(delta);
		}

		else
		{
			return Integer.signum(version1.length - version2.length);
		}
	}

	private static final Comparator<String> versionComparator = new Comparator<String>() 
	{
		public int compare(String arg0, String arg1) 
		{
			return compareVersions(arg0, arg1);
		}
	};
}
