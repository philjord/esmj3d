package bsa;

import gui.ArchiveNode;
import gui.FileNode;
import gui.FolderNode;
import gui.StatusDialog;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import archive.ArchiveEntry;
import archive.ArchiveFileFilter;
import archive.Main;

// Referenced classes of package FO3Archive:
//	            ArchiveNode, ArchiveFileFilter, StatusDialog, CreateTask, 
//	            ArchiveFile, LoadTask, FolderNode, FileNode, 
//	            ExtractTask, Main

public class BSAContentDisplayTest extends JFrame implements ActionListener
{
	public static boolean LOAD_ALL = true;

	private boolean windowMinimized;

	private JTree tree;

	private DefaultTreeModel treeModel;

	private BSAFileSet bsaFileSet;

	private JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("Load all BSA Archives");

	private JCheckBoxMenuItem sopErrMenuItem = new JCheckBoxMenuItem("SOP errors only");

	public BSAContentDisplayTest()
	{
		super("BSA test display");
		windowMinimized = false;
		setDefaultCloseOperation(2);
		String propValue = Main.properties.getProperty("window.main.position");
		if (propValue != null)
		{
			int sep = propValue.indexOf(',');
			int frameX = Integer.parseInt(propValue.substring(0, sep));
			int frameY = Integer.parseInt(propValue.substring(sep + 1));
			setLocation(frameX, frameY);
		}
		int frameWidth = 800;
		int frameHeight = 640;
		propValue = Main.properties.getProperty("window.main.size");
		if (propValue != null)
		{
			int sep = propValue.indexOf(',');
			frameWidth = Integer.parseInt(propValue.substring(0, sep));
			frameHeight = Integer.parseInt(propValue.substring(sep + 1));
		}
		setPreferredSize(new Dimension(frameWidth, frameHeight));
		JMenuBar menuBar = new JMenuBar();
		menuBar.setOpaque(true);
		JMenu menu = new JMenu("File");
		menu.setMnemonic(70);
		boolean loadAll = Boolean.parseBoolean(Main.properties.getProperty("load.all"));
		cbMenuItem.setSelected(loadAll);
		menu.add(cbMenuItem);
		menu.add(sopErrMenuItem);
		JMenuItem menuItem = new JMenuItem("Open Archive");
		menuItem.setActionCommand("open");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuItem = new JMenuItem("Close Archive");
		menuItem.setActionCommand("close");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuItem = new JMenuItem("Exit Program");
		menuItem.setActionCommand("exit");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuBar.add(menu);
		menu = new JMenu("Action");
		menu.setMnemonic(65);
		menuItem = new JMenuItem("Display Selected Files");
		menuItem.setActionCommand("display selected");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuItem = new JMenuItem("Display All Files");
		menuItem.setActionCommand("display all");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuItem = new JMenuItem("Verify Selected Files");
		menuItem.setActionCommand("verify selected");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuItem = new JMenuItem("Verify All Files");
		menuItem.setActionCommand("verify all");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuBar.add(menu);
		menu = new JMenu("Help");
		menu.setMnemonic(72);
		menuItem = new JMenuItem("About");
		menuItem.setActionCommand("about");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuBar.add(menu);
		setJMenuBar(menuBar);
		treeModel = new DefaultTreeModel(new ArchiveNode());
		tree = new JTree(treeModel);
		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setPreferredSize(new Dimension(700, 540));
		JPanel contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		contentPane.add(scrollPane);
		setContentPane(contentPane);
		addWindowListener(new ApplicationWindowListener());

		tree.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					try
					{
						displayFiles(false, false);
					}
					catch (Throwable exc)
					{
						Main.logException("Exception while processing action event", exc);
					}
				}
			}
		});
	}

	public void actionPerformed(ActionEvent ae)
	{
		try
		{
			String action = ae.getActionCommand();
			if (action.equals("open"))
				openFile();
			else if (action.equals("close"))
				closeFile();
			else if (action.equals("exit"))
				exitProgram();
			else if (action.equals("about"))
				aboutProgram();
			else if (action.equals("display selected"))
				displayFiles(false, false);
			else if (action.equals("display all"))
				displayFiles(true, false);
			else if (action.equals("verify selected"))
				displayFiles(false, true);
			else if (action.equals("verify all"))
				displayFiles(true, true);
		}
		catch (Throwable exc)
		{
			Main.logException("Exception while processing action event", exc);
		}
	}

	private void openFile() throws IOException
	{
		closeFile();
		String currentDirectory = Main.properties.getProperty("current.directory");
		JFileChooser chooser;
		if (currentDirectory != null)
		{
			File dirFile = new File(currentDirectory);
			if (dirFile.exists() && dirFile.isDirectory())
				chooser = new JFileChooser(dirFile);
			else
				chooser = new JFileChooser();
		}
		else
		{
			chooser = new JFileChooser();
		}
		chooser.putClientProperty("FileChooser.useShellFolder", Boolean.valueOf(Main.useShellFolder));
		chooser.setDialogTitle("Select Archive File");
		chooser.setFileFilter(new ArchiveFileFilter());
		if (chooser.showOpenDialog(this) == 0)
		{
			File file = chooser.getSelectedFile();
			Main.properties.setProperty("current.directory", file.getParent());

			Main.properties.setProperty("load.all", Boolean.toString(cbMenuItem.isSelected()));

			if (cbMenuItem.isSelected())
			{
				bsaFileSet = new BSAFileSet(file.getParent(), true, true);
			}
			else
			{
				bsaFileSet = new BSAFileSet(file.getAbsolutePath(), false, true);
			}

			DefaultMutableTreeNode root = new DefaultMutableTreeNode();
			for (MutableTreeNode node : bsaFileSet.nodes)
			{
				root.add(node);
			}

			treeModel = new DefaultTreeModel(root);
			tree.setModel(treeModel);

		}
	}

	private void closeFile() throws IOException
	{
		if (bsaFileSet != null)
		{
			bsaFileSet.close();
			bsaFileSet = null;
		}
		treeModel = new DefaultTreeModel(new ArchiveNode());
		tree.setModel(treeModel);
	}

	private void displayFiles(boolean displayAllFiles, boolean verifyOnly) throws InterruptedException
	{
		if (bsaFileSet == null)
		{
			JOptionPane.showMessageDialog(this, "You must open an archive file", "No archive file", 0);
			return;
		}
		StatusDialog statusDialog = new StatusDialog(this, "Displaying files from " + bsaFileSet.getName());

		List<ArchiveEntry> entries = null;
		if (displayAllFiles)
		{
			entries = bsaFileSet.getEntries(statusDialog);
		}
		else
		{
			TreePath treePaths[] = tree.getSelectionPaths();
			if (treePaths == null)
			{
				JOptionPane.showMessageDialog(this, "You must select one or more files to extract", "No files selected", 0);
				return;
			}
			entries = new ArrayList<ArchiveEntry>(100);
			for (int i = 0; i < treePaths.length; i++)
			{
				TreePath treePath = treePaths[i];
				Object obj = treePath.getLastPathComponent();
				if (obj instanceof FolderNode)
				{
					addFolderChildren((FolderNode) obj, entries);
					continue;
				}
				if (!(obj instanceof FileNode))
					continue;
				ArchiveEntry entry = ((FileNode) obj).getEntry();
				if (!entries.contains(entry))
					entries.add(entry);
			}

		}

		DisplayTask displayTask = new DisplayTask(bsaFileSet, entries, statusDialog, verifyOnly, sopErrMenuItem.isSelected());
		displayTask.start();
		statusDialog.showDialog();
		displayTask.join();
	}

	private void addFolderChildren(FolderNode folderNode, List<ArchiveEntry> entries)
	{
		int count = folderNode.getChildCount();
		for (int i = 0; i < count; i++)
		{
			TreeNode node = folderNode.getChildAt(i);
			if (node instanceof FolderNode)
			{
				addFolderChildren((FolderNode) node, entries);
				continue;
			}
			if (!(node instanceof FileNode))
				continue;
			ArchiveEntry entry = ((FileNode) node).getEntry();
			if (!entries.contains(entry))
				entries.add(entry);
		}

	}

	private void exitProgram()
	{
		if (!windowMinimized)
		{
			Point p = Main.mainWindow.getLocation();
			Dimension d = Main.mainWindow.getSize();
			Main.properties.setProperty("window.main.position", "" + p.x + "," + p.y);
			Main.properties.setProperty("window.main.size", "" + d.width + "," + d.height);
		}
		Main.saveProperties();
		System.exit(0);
	}

	private void aboutProgram()
	{
		String info = "<html>Phil's reworking of the fallout BSA file manager<br>";
		info += "<br>User name: ";
		info += System.getProperty("user.name");
		info += "<br>Home directory: ";
		info += System.getProperty("user.home");
		info += "<br><br>OS: ";
		info += System.getProperty("os.name");
		info += "<br>OS version: ";
		info += System.getProperty("os.version");
		info += "<br>OS patch level: ";
		info += System.getProperty("sun.os.patch.level");
		info += "<br><br>Java vendor: ";
		info += System.getProperty("java.vendor");
		info += "<br>Java version: ";
		info += System.getProperty("java.version");
		info += "<br>Java home directory: ";
		info += System.getProperty("java.home");
		info += "<br>Java class path: ";
		info += System.getProperty("java.class.path");
		info += "</html>";
		JOptionPane.showMessageDialog(this, info.toString(), "About This Utility", 1);
	}

	private class ApplicationWindowListener extends WindowAdapter
	{

		public ApplicationWindowListener()
		{

		}

		public void windowIconified(WindowEvent we)
		{
			windowMinimized = true;
		}

		public void windowDeiconified(WindowEvent we)
		{
			windowMinimized = false;
		}

		public void windowClosing(WindowEvent we)
		{
			try
			{
				exitProgram();
			}
			catch (Exception exc)
			{
				Main.logException("Exception while closing application window", exc);
			}
		}

	}

}
