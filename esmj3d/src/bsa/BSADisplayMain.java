package bsa;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import archive.Main;

public class BSADisplayMain extends Main
{
	public static void main(String args[])
	{
		try
		{
			fileSeparator = System.getProperty("file.separator");
			lineSeparator = System.getProperty("line.separator");
			tmpDir = System.getProperty("java.io.tmpdir");
			String option = System.getProperty("UseShellFolder");
			if (option != null && option.equals("0"))
				useShellFolder = false;
			String filePath = (new StringBuilder()).append(System.getProperty("user.home")).append(fileSeparator)
					.append("Application Data").append(fileSeparator).append("ScripterRon").toString();
			File dirFile = new File(filePath);
			if (!dirFile.exists())
				dirFile.mkdirs();
			filePath = (new StringBuilder()).append(filePath).append(fileSeparator).append("FO3Archive.properties").toString();
			propFile = new File(filePath);
			properties = new Properties();
			if (propFile.exists())
			{
				FileInputStream in = new FileInputStream(propFile);
				properties.load(in);
				in.close();
			}
			properties.setProperty("java.version", System.getProperty("java.version"));
			properties.setProperty("java.home", System.getProperty("java.home"));
			properties.setProperty("os.name", System.getProperty("os.name"));
			properties.setProperty("sun.os.patch.level", System.getProperty("sun.os.patch.level"));
			properties.setProperty("user.name", System.getProperty("user.name"));
			properties.setProperty("user.home", System.getProperty("user.home"));
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					createAndShowGUI();
				}
			});
		}
		catch (Throwable exc)
		{
			logException("Exception during program initialization", exc);
		}
	}

	public static void createAndShowGUI()
	{
		try
		{
			mainWindow = new BSAContentDisplayTest();
			mainWindow.pack();
			mainWindow.setVisible(true);
		}
		catch (Throwable exc)
		{
			logException("Exception while initializing application window", exc);
		}
	}
}
