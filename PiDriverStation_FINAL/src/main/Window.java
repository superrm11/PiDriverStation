package main;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSpinner;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import javax.swing.JLabel;

/**
 * 
 * @author McGee and Daniel
 * 
 *
 */
public class Window extends JFrame /* implements Runnable */ {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static JMenu mnAdd;
	private static JMenu mnDevice;
	private static JMenu mnSetup;
	private static JPanel contentPane;
	private static JPanel componentPanel;
	private static JPanel panel;
	private static JRadioButtonMenuItem showAllControllers;
	private static JRadioButtonMenuItem showComponentList;
	private static JMenuItem mntmMinimize;
	private static JMenuItem mntmMaximize;
	private static JMenuItem mntmExit;
	private static JMenuItem mntmUpdate;
	private static JMenuItem mntmOpen;
	private static JMenuItem mntmSave;
	private static JMenuItem mntmSaveAs;
	private static JMenuItem mntmStartServer;
	private static JMenuItem mntmStopServer;
	private static JMenu mnNetwork;
	private static JButton btnAddAsButton;
	private static JButton btnAddAsAxis;
	private static JButton btnCancelAddItem;
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
	private static int previousComponent;
	public static boolean stopSendServer;
	private static boolean stopRecieveServer;
	private static int numOfComponents = 0;
	private static int deviceSelectedIndex;
	private static ArrayList<AddedComponent> addedComponents = new ArrayList<AddedComponent>();

	public static boolean stopClientSend = false;
	public static boolean stopClientRecieve = false;

	public static Controller[] con;
	public static Component[][] com;
	private static JMenuItem mntmDeadZone;
	private static JPanel deadzonePanel;
	private static JComboBox<String> deadzoneComSel;
	private static JComboBox<String> deadzoneConSel;
	private static JButton btnCancelAddDeadzone;
	private static JSpinner deadzonePercent;
	private static JButton btnAddDeadzone;

	private enum windowState {
		MINIMIZE, MAXIMIZE
	}

	private enum Refresh {
		LIMITED, ALL, NON_DISPLAY
	}

	private enum state {
		ENABLED, DISABLED, FIRST_START
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
		Thread.sleep(1000);
		refreshDeviceList(Refresh.LIMITED);
		displayComponents();
		// (new Thread(new Window())).start();
		PrintStatements statements = new PrintStatements();
		statements.start();
		populateDeadzoneMaterials(state.FIRST_START);
		Background b = new Background();
		b.start();

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

		mntmUpdate = new JMenuItem("Update Device List");
		mnFile.add(mntmUpdate);

		mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);

		// ------------------------ADD TAB

		mnAdd = new JMenu("Add");
		menuBar.add(mnAdd);

		mnDevice = new JMenu("Device");
		mnAdd.add(mnDevice);

		mntmDeadZone = new JMenuItem("Dead Zone");
		mnAdd.add(mntmDeadZone);

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
		mnSetup = new JMenu("Setup");
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

		btnCancelAddItem = new JButton("Cancel");
		btnCancelAddItem.setBounds(30, 88, 97, 25);
		panel.add(btnCancelAddItem);

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

		// -------------------Add Dead Zone Panel

		deadzonePanel = new JPanel();
		deadzonePanel.setBackground(Color.GRAY);
		deadzonePanel.setBounds(0, 0, 139, 98);
		contentPane.add(deadzonePanel);
		deadzonePanel.setLayout(null);

		deadzoneConSel = new JComboBox<String>();
		deadzoneConSel.setBounds(0, 0, 139, 22);
		deadzonePanel.add(deadzoneConSel);

		deadzoneComSel = new JComboBox<String>();
		deadzoneComSel.setBounds(0, 23, 139, 22);
		deadzonePanel.add(deadzoneComSel);

		deadzonePercent = new JSpinner();
		deadzonePercent.setBounds(89, 74, 50, 22);
		deadzonePanel.add(deadzonePercent);

		JLabel lblPercentage = new JLabel("%");
		lblPercentage.setForeground(Color.WHITE);
		lblPercentage.setBounds(120, 58, 19, 16);
		deadzonePanel.add(lblPercentage);

		btnAddDeadzone = new JButton("Finish");
		btnAddDeadzone.setBounds(0, 73, 81, 25);
		deadzonePanel.add(btnAddDeadzone);

		btnCancelAddDeadzone = new JButton("Cancel");
		btnCancelAddDeadzone.setBounds(0, 46, 81, 25);
		deadzonePanel.add(btnCancelAddDeadzone);
		deadzonePanel.setVisible(false);

	}

	/**
	 * Begins Action Listeners for the components of the frame.
	 */
	private static void beginListeners() {

		// ------------Add>Show All Controllers Radio Buttons

		showAllControllers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mnAdd.doClick();
				if (showAllControllers.isSelected()) {
					refreshDeviceList(Refresh.ALL);
				} else {
					refreshDeviceList(Refresh.LIMITED);
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
				if (SendServerThread.joystickSetup() == true) {
					System.out.println("All Controllers and Devices Loaded Successfully.");
				} else {
					System.out.println("Please make sure all controllers are plugged in.");
				}
				displayComponents();

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

		mntmUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshDeviceList(Refresh.ALL);
			}
		});
		// ------------------Add Item Panel
		btnAddAsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addedComponents.add(new AddedComponent());
				addedComponents.get(numOfComponents).setAsButton();
				addedComponents.get(numOfComponents).component = com[deviceSelectedIndex][comboBox.getSelectedIndex()]
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
				addedComponents.get(numOfComponents).component = com[deviceSelectedIndex][comboBox.getSelectedIndex()]
						.getName();
				addedComponents.get(numOfComponents).controller = deviceSelected;
				numOfComponents++;
				addItemPanel(state.DISABLED);
				displayComponents();
			}

		});

		// ------------------View TAB
		mntmMaximize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setWindow(windowState.MAXIMIZE);
				refreshDeviceList(Refresh.LIMITED);
			}
		});

		mntmMinimize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setWindow(windowState.MINIMIZE);
				refreshDeviceList(Refresh.LIMITED);
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

		btnCancelAddItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addItemPanel(state.DISABLED);
			}

		});
		// -------------------Setup Tab

		mntmStartServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmStartServer.setEnabled(false);
				mntmStopServer.setEnabled(true);
				try {
					startAllServers();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});

		mntmStopServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmStartServer.setEnabled(true);
				mntmStopServer.setEnabled(false);
				try {
					stopAllServers();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});

		// -------------------Add Dead Zone Panel

		mntmDeadZone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deadzonePanel.setVisible(true);
			}
		});

		btnCancelAddDeadzone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deadzonePanel.setVisible(false);
			}
		});

		btnAddDeadzone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deadzonePanel.setVisible(false);
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
	private static void addItemPanel(state s) {
		refreshDeviceList(Refresh.NON_DISPLAY);

		switch (s) {
		case ENABLED:

			panel.setVisible(true);

			// get index of device selected
			for (int i = 0; i < con.length; i++) {
				if (con[i].getName() == deviceSelected) {
					deviceSelectedIndex = i;
					break;
				}
			}
			// clear comboBox if it has items
			if (comboBox.getItemCount() > 0) {
				comboBox.removeAllItems();
			}

			// populate ComboBox
			for (int i = 0; i < com[deviceSelectedIndex].length; i++) {
				comboBox.addItem(com[deviceSelectedIndex][i].getName());
			}
			break;

		case DISABLED:

			panel.setVisible(false);
			break;
		default:
			break;

		}
		if (showAllControllers.isSelected()) {
			refreshDeviceList(Refresh.ALL);
		} else {
			refreshDeviceList(Refresh.LIMITED);
		}

	}

	/*
	 * Refreshes the list of devices under Add > Device
	 */
	private static void refreshDeviceList(Refresh r) {

		con = ControllerEnvironment.getDefaultEnvironment().getControllers();
		com = new Component[con.length][];

		for (int i = 0; i < con.length; i++) {
			com[i] = con[i].getComponents();
		}
		switch (r) {
		case LIMITED:

			if (mnDevice.getItemCount() > 0) {
				mnDevice.removeAll();
			}

			for (int i = 0; i < con.length; i++) {
				if (con[i].getType() == Controller.Type.GAMEPAD || con[i].getType() == Controller.Type.KEYBOARD) {
					final JMenuItem controller = new JMenuItem();
					controller.setText(con[i].getName());
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

			if (mnDevice.getItemCount() > 0) {
				mnDevice.removeAll();
			}

			for (int i = 0; i < con.length; i++) {
				final JMenuItem controller = new JMenuItem();
				controller.setText(con[i].getName());
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
			for (int i = 0; i < con.length; i++) {
				com[i] = con[i].getComponents();
			}
			break;
		}
	}

	/*
	 * Creates the TextArea that displays the axis and buttons added and sorts
	 * them at the same time.
	 */
	private static void displayComponents() {
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
	private static void saveComponents(String path) {
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

	/**
	 * Saves a component to the path specified
	 * 
	 * @param path
	 */
	@SuppressWarnings({ "unchecked" })
	private static void openComponents(String path) {
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
	private static void beginSaving() {
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

	/**
	 * Begins the process of opening a file, calls the openComponents method
	 */
	private static void beginOpening() {
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

	/**
	 * Sets the deadzone of a component based on the Frame's JComboBox
	 */
	private static void setDeadzone() {
		for (int i = 0; i < addedComponents.size(); i++) {
			if (addedComponents.get(i).finalControllerNumber == deadzoneConSel.getSelectedIndex()
					&& addedComponents.get(i).deadzoneIndex == deadzoneComSel.getSelectedIndex()) {
				addedComponents.get(i).deadzonePercentage = (int) deadzonePercent.getValue();
				System.out.println(addedComponents.get(i).deadzonePercentage);
			}
		}

	}

	/**
	 * Populates the controllers and components for the deadzone comboboxes.
	 * 
	 * @param s
	 */
	private static void populateDeadzoneMaterials(state s) {
		refreshDeviceList(Refresh.NON_DISPLAY);
		switch (s) {
		case FIRST_START:
			deadzoneConSel.removeAllItems();
			for (int i = 0; i < con.length; i++) {
				deadzoneConSel.addItem(con[i].getName());

			}
		case ENABLED:
			deadzoneComSel.removeAllItems();
			SendServerThread.joystickSetup();
			int numOfDeadzones = 0;
			for (int i = 0; i < addedComponents.size(); i++) {
				if (addedComponents.get(i).isAxis
						&& addedComponents.get(i).finalControllerNumber == deadzoneConSel.getSelectedIndex()) {
					deadzoneComSel.addItem(addedComponents.get(i).component);
					addedComponents.get(i).deadzoneIndex = numOfDeadzones;
					numOfDeadzones++;
				}
			}
		default:
			break;
		}

	}

	private static void stopAllServers() throws InterruptedException {
		stopClientSend = true;
		stopClientRecieve = true;
		Thread.sleep(50);
		stopSendServer = true;
		stopRecieveServer = true;
	}

	private static void startAllServers() throws InterruptedException {
		SendServerThread server1 = new SendServerThread(9090);
		server1.start();
		RecieveServerThread server2 = new RecieveServerThread(9091);
		server2.start();
		Thread.sleep(50);
		SendClientThread client1 = new SendClientThread(9091);
		client1.start();
		RecieveClientThread client2 = new RecieveClientThread(9090);
		client2.start();
	}

	/**
	 * Continuously sends print statements for testing.
	 * 
	 * @author superrm11
	 *
	 */
	public static class PrintStatements extends Thread {
		public void run() {
			while (!stopSendServer) {
				// System.out.println(serverIsRunning);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Contains a thread that will run code outside the main thread or listeners
	 * 
	 * @author superrm11
	 *
	 */
	public static class Background extends Thread {
		public void run() {
			while (true) {
				if (deadzonePanel.isVisible() && previousComponent != deadzoneConSel.getSelectedIndex()) {
					previousComponent = deadzoneConSel.getSelectedIndex();
					populateDeadzoneMaterials(state.ENABLED);
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Contains a thread that runs the server for sending joystick values
	 * 
	 * @author superrm11
	 *
	 */
	public static class SendServerThread extends Thread implements Serializable {

		private static final long serialVersionUID = 1L;
		private static int sendPort;
		private ServerSocket sendListener = null;
		private static byte[] sendChannels;
		private static byte[] temp = { 1, 2, 3, 4 };
		private Socket sendSocket = null;

		SendServerThread(int sendPort) {
			SendServerThread.sendPort = sendPort;
		}

		public byte[] sendJoystickVals() {
			for (int i = 0; i < con.length; i++) {
				con[i].poll();
			}

			for (int i = 0; i < addedComponents.size(); i++) {
				sendChannels[i] = (byte) (Math.round(
						com[addedComponents.get(i).finalControllerNumber][addedComponents.get(i).finalComponentNumber]
								.getPollData() * 127));
			}
			// System.out.println(sendChannels[0]);
			return sendChannels;
		}

		public static boolean joystickSetup() {
			for (int i = 0; i < addedComponents.size(); i++) {
				addedComponents.get(i).finalComponentNumber = -1;
				addedComponents.get(i).finalControllerNumber = -1;
			}

			for (int i = 0; i < addedComponents.size(); i++) {

				for (int b = 0; b < con.length; b++) {
					if (addedComponents.get(i).controller.equals(con[b].getName())) {
						addedComponents.get(i).finalControllerNumber = b;
						System.out.println(addedComponents.get(i).controller);
						for (int c = 0; c < con[addedComponents.get(i).finalControllerNumber]
								.getComponents().length; c++) {
							if (com[addedComponents.get(i).finalControllerNumber][c].getName()
									.equals(addedComponents.get(i).component)) {
								addedComponents.get(i).finalComponentNumber = c;
								System.out.println(addedComponents.get(i).component);
								break;
							}

						}
						break;
					}
				}

				if (addedComponents.get(i).finalComponentNumber == -1
						|| addedComponents.get(i).finalControllerNumber == -1) {
					return false;
				}

			}

			// channels = new byte[addedComponents.size()];
			sendChannels = new byte[1];
			return true;

		}

		public void run() {
			stopSendServer = false;
			joystickSetup();
			try {
				sendListener = new ServerSocket(sendPort);
				sendSocket = sendListener.accept();
				ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
				while (!stopSendServer) {
					oos.writeObject(temp);
					oos.flush();
				}
				sendListener.close();
				sendSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Server Stopped Unexpectedly, or Client Closed the Application.");
				stopSendServer = true;
			}
			System.out.println("Thread Finished");
		}

	}

	public static class RecieveServerThread extends Thread implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int recievePort;
		private static byte[] recieveChannels;
		private ServerSocket recieveListener = null;
		private Socket recieveSocket = null;

		RecieveServerThread(int recievePort) {
			this.recievePort = recievePort;
		}

		public void run() {
			stopRecieveServer = false;
			try {
				if (recievePort == SendServerThread.sendPort) {
					System.out.println("Please use different ports for Send/Recieve!");
					return;
				}
				recieveListener = new ServerSocket(recievePort);
				recieveSocket = recieveListener.accept();
				ObjectInputStream ois = new ObjectInputStream(recieveSocket.getInputStream());
				while (!stopRecieveServer) {
					recieveChannels = (byte[]) ois.readObject();
					System.out.println(recieveChannels[recieveChannels.length - 1]);
				}
				recieveSocket.close();
				recieveListener.close();

			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				System.out.println("Server Stopped Unexpectedly, or Client Closed the Application.");
			}
		}

	}

	/**
	 * This is to help with listing and sending devices
	 * 
	 * @author McGee
	 *
	 */
	public static class AddedComponent implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		boolean isButton;
		boolean isAxis;

		int deadzonePercentage = 0;
		int deadzoneIndex;

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

	// TEMPORARY-----------------------------------------------

	public static class SendClientThread extends Thread implements Serializable {
		private static final long serialVersionUID = 1L;
		private int sendPort;
		private Socket sendSocket;
		private ObjectOutputStream oos;

		private byte[] send = { 1, 2, 3, 4, 5 };

		private SendClientThread(int sendPort) {
			this.sendPort = sendPort;
		}

		public void run() {
			stopClientSend = false;
			try {
				sendSocket = new Socket(InetAddress.getLocalHost(), sendPort);
				oos = new ObjectOutputStream(sendSocket.getOutputStream());
				while (!stopClientSend) {
					oos.writeObject(send);
					oos.flush();
				}
				oos.close();
				sendSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static class RecieveClientThread extends Thread implements Serializable {
		private static final long serialVersionUID = 1L;
		int recievePort;
		Socket recieveSocket;
		ObjectInputStream ois;
		byte[] recievedByteArray;

		RecieveClientThread(int recievePort) {
			this.recievePort = recievePort;
		}

		public void run() {
			stopClientRecieve = false;
			try {
				recieveSocket = new Socket(InetAddress.getLocalHost(), recievePort);
				ois = new ObjectInputStream(recieveSocket.getInputStream());
				while (!stopClientRecieve) {
					recievedByteArray = (byte[]) ois.readObject();
					System.out.println(recievedByteArray[recievedByteArray.length - 1]);
				}
				ois.close();
				recieveSocket.close();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

	}
}