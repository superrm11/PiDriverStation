package frame;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import hardware.Devices;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/**
 * 
 * @author McGee and Daniel
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
	private static JButton btnAddAsButton;
	private static JButton btnAddAsAxis;
	private static JButton cancelAddItem;
	private static TextArea componentList;
	private static JComboBox<String> comboBox;
		
	private static Window frame_minimized;
	private static Window frame_maximized;
		
	private static FileOutputStream fis;
	private static ObjectOutputStream oos;
	
	private static String deviceSelected;
	private static String saveName;
	private static int deviceSelectedIndex;
	private static int displayComponentNum = 0;
	private static ArrayList<Component> addedAxisComponents = new ArrayList<Component>();
	private static ArrayList<Component> addedButtonComponents = new ArrayList<Component>();
	private static ArrayList<String> addedDevices = new ArrayList<String>();
	//TODO add all arraylists to a single array for input/outputStream
	private static Object[] saveArrayLists = new Object[3];
	

	
	private enum windowState{
		MINIMIZE, MAXIMIZE
	}
	private enum controllerType{
		LIMITED, ALL, NON_DISPLAY
	}
	private enum state{
		ENABLED, DISABLED
	}


	/**
	 * Launch the application.
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
	}

	/**
	 * Create the frame.
	 */
	public Window() {
		//------------------------| Begin the Main Frame |---------------------------
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
			setBounds(100, 100, 450, 300);
			
			JMenuBar menuBar = new JMenuBar();				
			setJMenuBar(menuBar);
			
			//------------------------------------FILE TAB------------------------------------
			
			JMenu mnFile = new JMenu("File");
			menuBar.add(mnFile);
			//TODO create file that can be loaded with different channels and devices
			
			mntmSave = new JMenuItem("Save");
			mnFile.add(mntmSave);
			mntmSave.setEnabled(false);
			
			mntmSaveAs = new JMenuItem("Save As");
			mnFile.add(mntmSaveAs);
			
			mntmOpen = new JMenuItem("Open");
			mnFile.add(mntmOpen);
			
			mntmExit = new JMenuItem("Exit");
			mnFile.add(mntmExit);
			
			
			//----------------------------ADD TAB------------------------------------------
			
			mnAdd = new JMenu("Add");
			menuBar.add(mnAdd);
			
			mnDevice = new JMenu("Device");
			mnAdd.add(mnDevice);
			
			showAllControllers = new JRadioButtonMenuItem("Show All Controllers");
			
			mnAdd.add(showAllControllers);
			
			
			//-------------------------------VIEW TAB---------------------------------------
			JMenu mnView = new JMenu("View");
			menuBar.add(mnView);
			
			mntmMinimize = new JMenuItem("Minimize");
			mnView.add(mntmMinimize);
			
			
			mntmMaximize = new JMenuItem("Maximize");
			mnView.add(mntmMaximize);
			
			showComponentList = new JRadioButtonMenuItem("Show Component List");
			mnView.add(showComponentList);
			
			
			
			
			
			//-------------------------------SETUP TAB------------------------------------
			
			//TODO add preferences like customization(color and stuff) and wireless settings
			//as well as adding channels that will be mapped to components added
			JMenu mnSetup = new JMenu("Setup");
			menuBar.add(mnSetup);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			contentPane.setLayout(null);

			//-------------------------------Add Component Panel--------------------------
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
			
			//-----------------------------Component Listing Panel-------------------------
			
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
	public static void beginListeners(){
		
		
		//-------------------Add>Show All Controllers Radio Button----------------
		
		showAllControllers.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				mnAdd.doClick();
				if(showAllControllers.isSelected()){
					refreshDeviceList(controllerType.ALL);
				}else{
					refreshDeviceList(controllerType.LIMITED);
				}
			}
			
		});
		
		//--------------------File Buttons----------------------------------------
		
		mntmExit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				System.exit(1);
				
			}
			
		});
		
		mntmOpen.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				
				int rVal = fileChooser.showOpenDialog(fileChooser);
				
			}
		});
		
		mntmSave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				
			}
		});
		 mntmSaveAs.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Configuration Files", "cfg");
				fileChooser.setFileFilter(filter);
				
				
				int rVal = fileChooser.showSaveDialog(fileChooser);
				if(rVal == JFileChooser.APPROVE_OPTION){
					mntmSave.setEnabled(true);
					saveName = fileChooser.getSelectedFile().getAbsolutePath();
					System.out.println(saveName);
					try {
						fis = new FileOutputStream(saveName);
					} catch (FileNotFoundException e) {
						System.out.println("File Not Found!");
					}
					try {
						oos = new ObjectOutputStream(fis);
					} catch (IOException e) {
						e.printStackTrace();
					}
					saveArrayLists[0] = addedAxisComponents;
					saveArrayLists[1] = addedButtonComponents;
					saveArrayLists[2] = addedDevices;
					
					try {
						oos.writeObject(saveArrayLists);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
				
			}
			
		});
		
		//----------------------Add Item Panel Buttons---------------------------
		btnAddAsButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				addedButtonComponents.add(Devices.com[deviceSelectedIndex][comboBox.getSelectedIndex()]);
				addItemPanel(state.DISABLED);
				addedDevices.add(deviceSelected);
				displayComponents();
			}
			
		});
		
		btnAddAsAxis.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				addedAxisComponents.add(Devices.com[deviceSelectedIndex][comboBox.getSelectedIndex()]);
				addItemPanel(state.DISABLED);
				addedDevices.add(deviceSelected);
				displayComponents();
			}
			
		});
		
		cancelAddItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				addItemPanel(state.DISABLED);
			}
		});
		//-----------------------View Tab----------------------------------------
		mntmMaximize.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				setWindow(windowState.MAXIMIZE);
				refreshDeviceList(controllerType.LIMITED);
			}
		});
		
		mntmMinimize.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				setWindow(windowState.MINIMIZE);
				refreshDeviceList(controllerType.LIMITED);
			}
			
		});
		
		showComponentList.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				if(showComponentList.isSelected()){
					componentPanel.setVisible(true);
				}else{
					componentPanel.setVisible(false);
				}
			}
		});
		
	}
	
	
	
	/**
	 * sets the state of the window(minimized or maximized)
	 * @param w the window state that it will be changed to
	 */
	private static void setWindow(windowState w){
		switch(w){
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
		 * @param s describes whether the frame is being enabled or disabled
		 */
		static void addItemPanel(state s){
			refreshDeviceList(controllerType.NON_DISPLAY);
		
			switch(s){
			case ENABLED:
			
				panel.setVisible(true);
			
				//get index of device selected
				for (int i = 0; i < Devices.con.length; i++){
					if(Devices.con[i].getName() == deviceSelected){
						deviceSelectedIndex = i;
						break;
					}	
				}
				//clear comboBox if it has items
				if(comboBox.getItemCount() > 0){
					comboBox.removeAllItems();
				}	
			
		
				//populate ComboBox
				for(int i = 0; i < Devices.com[deviceSelectedIndex].length; i++){
					comboBox.addItem(Devices.com[deviceSelectedIndex][i].getName());
				} 
				break;
			
			case DISABLED:
			
				panel.setVisible(false);
				break;
			
			}
			if(showAllControllers.isSelected()){
				refreshDeviceList(controllerType.ALL);
			}else{
				refreshDeviceList(controllerType.LIMITED);
			}
		
	}
	/*
	 * Refreshes the list of devices under Add > Device
	 */
	public static void refreshDeviceList(controllerType r){

		
		Devices.con = ControllerEnvironment.getDefaultEnvironment().getControllers();
		Devices.com = new Component[Devices.con.length][];
		
		for(int i = 0; i < Devices.con.length; i++){
			Devices.com[i] = Devices.con[i].getComponents();
		}
		if(mnDevice.getItemCount() > 0){
			mnDevice.removeAll();
		}
		switch(r){
		case LIMITED:
			for(int i = 0; i < Devices.con.length; i++){
				if(Devices.con[i].getType() == Controller.Type.GAMEPAD || Devices.con[i].getType() == Controller.Type.KEYBOARD){
					JMenuItem controller = new JMenuItem();
					controller.setText(Devices.con[i].getName());
					mnDevice.add(controller);
					controller.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent arg0) {
							deviceSelected = controller.getText();
							addItemPanel(state.ENABLED);
						}
					});
				}
				
			}
			break;
		case ALL:
			for(int i = 0; i < Devices.con.length; i++){
				JMenuItem controller = new JMenuItem();
				controller.setText(Devices.con[i].getName());
				mnDevice.add(controller);
				controller.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent arg0) {
						deviceSelected = controller.getText();
						addItemPanel(state.ENABLED);
					}
				});
			}
			break;
			
		case NON_DISPLAY:
			for(int i = 0; i < Devices.con.length; i++){
				Devices.com[i] = Devices.con[i].getComponents();
			}
			break;
		}
	}
	
	/*
	 * Creates the TextArea that displays the axis and buttons added and sorts them at the same time.
	 */
	public static void displayComponents(){
		componentList.setText("");
		componentList.append("    Axis:\n");
		displayComponentNum = 0;
		for(int i = 0; i < addedAxisComponents.size(); i++){
			componentList.append(displayComponentNum + "   " + addedDevices.get(i) + "\n" + "    > " + addedAxisComponents.get(i).getName() + "\n");
			displayComponentNum++;
		}
		componentList.append("    Buttons:\n");
		for(int i = 0; i < addedButtonComponents.size(); i++){
			componentList.append(displayComponentNum + "   " + addedDevices.get(i) + "\n" + "    > " + addedButtonComponents.get(i).getName() + "\n");
			displayComponentNum++;
		}
	}
}
