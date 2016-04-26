package main;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/**
 * 
 * @author McGee and Daniel
 * 
 *
 */
public class Window extends JFrame {

	private static JMenu mnAdd;
	private static JMenu mnDevice;
	private static JPanel contentPane;
	private static JPanel componentPanel;
	private static JPanel panel;
	private static JRadioButtonMenuItem showAllControllers;
	private static JRadioButtonMenuItem showComponentList;
	private static JMenuItem mntmMinimize;
	private static JMenuItem mntmMaximize;
	private static JMenuItem mntmExit;
	private static JMenuItem mntmOpen;
	private static JMenuItem mntmSave;
	private static JMenuItem mntmSaveAs;
	private static JMenuItem mntmStartServer;
	private static JMenuItem mntmStopServer;
	private static JMenu mnNetwork;
	private static JButton btnAddAsButton;
	private static JButton btnAddAsAxis;
	private static JButton cancelAddItem;
	private static TextArea componentList;
	private static JComboBox<String> comboBox;

	private static Window frame_minimized;
	private static Window frame_maximized;

	private static FileOutputStream fos;
	private static ObjectOutputStream oos;

	private static ObjectInputStream ois;
	private static FileInputStream fis;

	private static String deviceSelected;
	private static String saveName;
	public static int serverPort;
	private static boolean stopServer;
	private static int numOfComponents = 0;
	private static int deviceSelectedIndex;
	private static ArrayList<AddedComponent> addedComponents = new ArrayList<AddedComponent>();
	private static byte[] channels;

	private enum windowState {
		MINIMIZE, MAXIMIZE
	}

	private enum controllerType {
		LIMITED, ALL, NON_DISPLAY
	}

	private enum state {
		ENABLED, DISABLED
	}

	/**
	 * Launch the application.
	 * 
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame_minimized = new Window();
					frame_minimized.setVisible(true);
					mntmMaximize.setVisible(true);
					mntmMinimize.setVisible(false);

					beginListeners();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Thread.sleep(500);
		refreshDeviceList(controllerType.LIMITED);
		displayComponents();
		// (new Thread(new Window())).start();
		PrintStatements statements = new PrintStatements();
		statements.start();
	}

	/**
	 * Create the frame.
	 */
	public Window() {
		// ------------------------| Begin the Main Frame
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setBounds(100, 100, 450, 300);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// --------------------------------FILE TAB

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		// TODO create file that can be loaded with different channels and
		// devices

		mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);
		mntmSave.setEnabled(false);

		mntmSaveAs = new JMenuItem("Save As");
		mnFile.add(mntmSaveAs);

		mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);

		mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);

		// ------------------------ADD TAB

		mnAdd = new JMenu("Add");
		menuBar.add(mnAdd);

		mnDevice = new JMenu("Device");
		mnAdd.add(mnDevice);

		showAllControllers = new JRadioButtonMenuItem("Show All Controllers");

		mnAdd.add(showAllControllers);

		// --------------------------VIEW TAB
		JMenu mnView = new JMenu("View");
		menuBar.add(mnView);

		mntmMinimize = new JMenuItem("Minimize");
		mnView.add(mntmMinimize);

		mntmMaximize = new JMenuItem("Maximize");
		mnView.add(mntmMaximize);

		showComponentList = new JRadioButtonMenuItem("Show Component List");
		mnView.add(showComponentList);

		// ---------------------------SETUP TAB

		// TODO add preferences like customization(color and stuff) and wireless
		// settings
		// as well as adding channels that will be mapped to components added
		JMenu mnSetup = new JMenu("Setup");
		menuBar.add(mnSetup);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		mnNetwork = new JMenu("Network");
		mnSetup.add(mnNetwork);

		mntmStartServer = new JMenuItem("Start Server");
		mnNetwork.add(mntmStartServer);

		mntmStopServer = new JMenuItem("Stop Server");
		mnNetwork.add(mntmStopServer);
		mntmStopServer.setEnabled(false);

		// -----------------------Add Component PANEL
		comboBox = new JComboBox<String>();
		comboBox.setVisible(true);
		comboBox.setBounds(0, 0, 139, 22);
		panel = new JPanel();
		panel.setBackground(Color.GRAY);
		panel.setBounds(0, 0, 139, 118);
		contentPane.add(panel);
		panel.setLayout(null);
		panel.setVisible(false);
		panel.add(comboBox);

		btnAddAsButton = new JButton("Add As Button");
		btnAddAsButton.setBounds(0, 25, 127, 25);
		panel.add(btnAddAsButton);

		btnAddAsAxis = new JButton("Add As Axis");
		btnAddAsAxis.setBounds(0, 50, 127, 25);
		panel.add(btnAddAsAxis);

		cancelAddItem = new JButton("Cancel");
		cancelAddItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		cancelAddItem.setBounds(50, 80, 77, 25);
		panel.add(cancelAddItem);

		// ---------------------Component Listing PANEL

		componentPanel = new JPanel();
		componentPanel.setBackground(Color.LIGHT_GRAY);
		componentPanel.setBounds(173, 0, 259, 227);
		contentPane.add(componentPanel);
		componentPanel.setLayout(null);
		componentPanel.setVisible(false);

		componentList = new TextArea();
		componentList.setBounds(0, 0, 259, 227);
		componentPanel.add(componentList);
		componentList.setEditable(false);
		componentList.setFocusable(false);

	}

	/*
	 * Begins Action Listeners for the components of the frame.
	 */
	public static void beginListeners() {

		// ------------Add>Show All Controllers Radio Buttons

		showAllControllers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mnAdd.doClick();
				if (showAllControllers.isSelected()) {
					refreshDeviceList(controllerType.ALL);
				} else {
					refreshDeviceList(controllerType.LIMITED);
				}
			}

		});

		// -------------File Buttons

		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(1);

			}

		});

		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				beginOpening();

			}
		});

		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveComponents(saveName);
			}
		});

		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				beginSaving();

			}

		});

		// ------------------Add Item Panel
		btnAddAsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addedComponents.add(new AddedComponent());
				addedComponents.get(numOfComponents).setAsButton();
				addedComponents
						.get(numOfComponents).component = Devices.com[deviceSelectedIndex][comboBox.getSelectedIndex()]
								.getName();
				addedComponents.get(numOfComponents).controller = deviceSelected;
				numOfComponents++;
				addItemPanel(state.DISABLED);
				displayComponents();
			}

		});

		btnAddAsAxis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addedComponents.add(new AddedComponent());
				addedComponents.get(numOfComponents).setAsAxis();
				addedComponents
						.get(numOfComponents).component = Devices.com[deviceSelectedIndex][comboBox.getSelectedIndex()]
								.getName();
				addedComponents.get(numOfComponents).controller = deviceSelected;
				numOfComponents++;
				addItemPanel(state.DISABLED);
				displayComponents();
			}

		});

		cancelAddItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addItemPanel(state.DISABLED);
			}
		});
		// ------------------View TAB
		mntmMaximize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setWindow(windowState.MAXIMIZE);
				refreshDeviceList(controllerType.LIMITED);
			}
		});

		mntmMinimize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setWindow(windowState.MINIMIZE);
				refreshDeviceList(controllerType.LIMITED);
			}

		});

		showComponentList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (showComponentList.isSelected()) {
					componentPanel.setVisible(true);
				} else {
					componentPanel.setVisible(false);
				}
				displayComponents();
			}
		});

		// -------------------Setup Tab

		mntmStartServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmStartServer.setEnabled(false);
				mntmStopServer.setEnabled(true);
				try {
					beginServer(9090, state.ENABLED);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		mntmStopServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmStartServer.setEnabled(true);
				mntmStopServer.setEnabled(false);
				try {
					beginServer(0, state.DISABLED);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	/**
	 * sets the state of the window(minimized or maximized)
	 * 
	 * @param w
	 *            the window state that it will be changed to
	 */
	private static void setWindow(windowState w) {
		switch (w) {
		case MINIMIZE:
			frame_maximized.dispose();

			frame_minimized = new Window();
			frame_minimized.setTitle("Pi Driver Station");
			frame_minimized.setUndecorated(false);
			frame_minimized.setVisible(true);
			frame_minimized.setExtendedState(Frame.NORMAL);

			mntmMaximize.setVisible(true);
			mntmMinimize.setVisible(false);

			displayComponents();
			beginListeners();
			break;
		case MAXIMIZE:
			frame_minimized.dispose();

			frame_maximized = new Window();
			frame_maximized.setTitle("Pi Driver Station");
			frame_maximized.setUndecorated(true);
			frame_maximized.setVisible(true);
			frame_maximized.setExtendedState(Frame.MAXIMIZED_BOTH);

			mntmMaximize.setVisible(false);
			mntmMinimize.setVisible(true);

			displayComponents();
			beginListeners();
			break;
		}
	}

	/**
	 * Creates the panel that allows the user to add a button or axis
	 * 
	 * @param s
	 *            describes whether the frame is being enabled or disabled
	 */
	static void addItemPanel(state s) {
		refreshDeviceList(controllerType.NON_DISPLAY);

		switch (s) {
		case ENABLED:

			panel.setVisible(true);

			// get index of device selected
			for (int i = 0; i < Devices.con.length; i++) {
				if (Devices.con[i].getName() == deviceSelected) {
					deviceSelectedIndex = i;
					break;
				}
			}
			// clear comboBox if it has items
			if (comboBox.getItemCount() > 0) {
				comboBox.removeAllItems();
			}

			// populate ComboBox
			for (int i = 0; i < Devices.com[deviceSelectedIndex].length; i++) {
				comboBox.addItem(Devices.com[deviceSelectedIndex][i].getName());
			}
			break;

		case DISABLED:

			panel.setVisible(false);
			break;

		}
		if (showAllControllers.isSelected()) {
			refreshDeviceList(controllerType.ALL);
		} else {
			refreshDeviceList(controllerType.LIMITED);
		}

	}

	/*
	 * Refreshes the list of devices under Add > Device
	 */
	public static void refreshDeviceList(controllerType r) {

		Devices.con = ControllerEnvironment.getDefaultEnvironment().getControllers();
		Devices.com = new Component[Devices.con.length][];

		for (int i = 0; i < Devices.con.length; i++) {
			Devices.com[i] = Devices.con[i].getComponents();
		}
		if (mnDevice.getItemCount() > 0) {
			mnDevice.removeAll();
		}
		switch (r) {
		case LIMITED:
			for (int i = 0; i < Devices.con.length; i++) {
				if (Devices.con[i].getType() == Controller.Type.GAMEPAD
						|| Devices.con[i].getType() == Controller.Type.KEYBOARD) {
					final JMenuItem controller = new JMenuItem();
					controller.setText(Devices.con[i].getName());
					mnDevice.add(controller);
					controller.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							deviceSelected = controller.getText();
							addItemPanel(state.ENABLED);
						}
					});
				}

			}
			break;
		case ALL:
			for (int i = 0; i < Devices.con.length; i++) {
				final JMenuItem controller = new JMenuItem();
				controller.setText(Devices.con[i].getName());
				mnDevice.add(controller);
				controller.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						deviceSelected = controller.getText();
						addItemPanel(state.ENABLED);
					}
				});
			}
			break;

		case NON_DISPLAY:
			for (int i = 0; i < Devices.con.length; i++) {
				Devices.com[i] = Devices.con[i].getComponents();
			}
			break;
		}
	}

	/*
	 * Creates the TextArea that displays the axis and buttons added and sorts
	 * them at the same time.
	 */
	public static void displayComponents() {
		componentList.setText("");
		componentList.append("    Axis:\n");

		for (int i = 0; i < addedComponents.size(); i++) {
			if (addedComponents.get(i).isAxis) {
				componentList.append(addedComponents.get(i).controller + "\n");
				componentList.append("    -> " + addedComponents.get(i).component + "\n");
			}
		}
		componentList.append("\n \n");
		componentList.append("    Buttons:\n");

		for (int i = 0; i < addedComponents.size(); i++) {
			if (addedComponents.get(i).isButton) {
				componentList.append(addedComponents.get(i).controller + "\n");
				componentList.append("    -> " + addedComponents.get(i).component + "\n");
			}
		}

		componentList.append("\n \n     End");
	}

	/**
	 * Saves the Components to a file.
	 */
	protected static void saveComponents(String path) {
		try {
			fos = new FileOutputStream(path);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(addedComponents);
			fos.close();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mntmSave.setEnabled(true);
		System.out.println("Saved To File: " + saveName);
	}

	protected static void openComponents(String path) {
		try {
			fis = new FileInputStream(path);
			ois = new ObjectInputStream(fis);
			addedComponents = (ArrayList<AddedComponent>) ois.readObject();
			fis.close();
			ois.close();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Begins the process of saving the components to a file by opening the
	 * dialogue box.
	 */
	public static void beginSaving() {
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Configuration Files", "cfg");
		fileChooser.setFileFilter(filter);

		int rVal = fileChooser.showSaveDialog(fileChooser);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			saveName = fileChooser.getSelectedFile().getAbsolutePath();
			System.out.println(saveName);
			if (!saveName.toLowerCase().contains(".cfg"))
				saveName = saveName.concat(".cfg");

			File file = new File(saveName);
			if (!file.exists()) {
				saveComponents(saveName);
			} else {
				int result = JOptionPane.showConfirmDialog(fileChooser, "File Already Exists. Overwrite?");
				switch (result) {
				case JOptionPane.YES_OPTION:
					saveComponents(saveName);
					return;
				case JOptionPane.NO_OPTION:
					beginSaving();
					return;

				}
			}

		}
	}

	public static void beginOpening() {
		JFileChooser fileChooser = new JFileChooser();

		int rVal = fileChooser.showOpenDialog(fileChooser);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			saveName = fileChooser.getSelectedFile().getAbsolutePath();
			if (!saveName.toLowerCase().contains(".cfg"))
				saveName = saveName.concat(".cfg");
			File file = new File(saveName);
			if (!file.exists()) {
				JOptionPane.showMessageDialog(fileChooser, "The File Does Not Exist!");
				beginOpening();
			} else {
				openComponents(saveName);
			}
		}
	}

	public static void beginServer(int port, state s) throws IOException {
		
		switch (s) {
		case ENABLED:
			stopServer = false;
			serverPort = port;
			System.out.println("this is working");
			ServerThread server = new ServerThread();
			server.start();
			break;
		case DISABLED:
			stopServer = true;
			break;

		}

	}
	public static class PrintStatements extends Thread{
		public void run(){
			while(!stopServer){
//				System.out.println(serverIsRunning);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static class ServerThread extends Thread {
		
		public void sendJoystickVals(){
			
		}
		
		public boolean joystickSetup(){
			for(int i = 0; i < addedComponents.size(); i++){
				
				
				for(int b = 0; b < Devices.con.length; b++){
					if(addedComponents.get(i).controller == Devices.con[b].getName()){
						addedComponents.get(i).finalControllerNumber = b;
						break;
					}
				}
				
				for(int b = 0; b < Devices.con[addedComponents.get(i).finalControllerNumber].getComponents().length; b++){
					if(Devices.com[addedComponents.get(i).finalControllerNumber][b].getName() == addedComponents.get(i).component){
						addedComponents.get(i).finalComponentNumber = b;
						break;
					}
				}
				
			}
			
			return true;
		}
		
		public void run() {
			try {
				ServerSocket listener = new ServerSocket(serverPort);
				try {
					while (!stopServer) {
						Socket socket = listener.accept();
						try {
							PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
							out.println(new Date().toString());
						} finally {
							socket.close();
						}
					}
				} finally {
					listener.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			stopServer = false;
			System.out.println("Ended Thread");
		}

	}

	/**
	 * This is to help with listing and sending devices
	 * 
	 * @author McGee
	 *
	 */
	protected static class AddedComponent implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		boolean isButton;
		boolean isAxis;

		String component;
		String controller;
		
		int finalControllerNumber;
		int finalComponentNumber;

		public void setAsAxis() {
			isAxis = true;
			isButton = false;
		}

		public void setAsButton() {
			isButton = true;
			isAxis = false;
		}
	}
}
