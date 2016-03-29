package frame;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.border.EmptyBorder;

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
	private JPanel contentPane;
	private static JRadioButtonMenuItem showAllControllers;
	private static JRadioButtonMenuItem showComponentLists;
	private static JMenuItem mntmMinimize;
	private static JMenuItem mntmMaximize;
	private static JMenuItem mntmExit;
	private static JButton btnAddAsButton;
	private static JButton btnAddAsAxis;
	private static JList<String> buttonList;
	private static JList<String> axisList;
	
	private static Window frame_minimized;
	private static Window frame_maximized;
		
	private static JPanel panel;
	private static JComboBox<String> comboBox;
	
	private static String deviceSelected;
	private static int deviceSelectedIndex;
	private static ArrayList<Component> addedAxisComponents = new ArrayList<Component>();
	private static ArrayList<Component> addedButtonComponents = new ArrayList<Component>();
	
	private enum windowState{
		MINIMIZE, MAXIMIZE
	}
	private enum controllerTypes{
		LIMITED, ALL
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
		refreshDeviceList(controllerTypes.LIMITED);
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
			JMenuItem mntmSave = new JMenuItem("Save");
			mnFile.add(mntmSave);
			
			JMenuItem mntmSaveAs = new JMenuItem("Save As");
			mnFile.add(mntmSaveAs);
			
			JMenuItem mntmOpen = new JMenuItem("Open");
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
			
			showComponentLists = new JRadioButtonMenuItem("Show Component List");
			mnView.add(showComponentLists);
			
			
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
			panel.setBounds(0, 0, 139, 88);
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
			
			//-----------------------------Component Listing Panel-------------------------
			
			JPanel componentPanel = new JPanel();
			componentPanel.setBackground(Color.LIGHT_GRAY);
			componentPanel.setBounds(173, 0, 259, 227);
			contentPane.add(componentPanel);
			componentPanel.setLayout(null);
			
			buttonList = new JList<String>();
			buttonList.setBounds(12, 13, 116, 201);
			componentPanel.add(buttonList);
			
			axisList = new JList<String>();
			axisList.setBounds(129, 13, 118, 201);
			componentPanel.add(axisList);
			
			
	}
	
	
public static void beginListeners(){
		
		//---------Minimize/Maximize Buttons--------------------------------------
		
		mntmMaximize.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				setWindow(windowState.MAXIMIZE);
			}
		});
		
		mntmMinimize.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				setWindow(windowState.MINIMIZE);
			}
			
		});
		
		//-------------------Add>Show All Controllers Radio Button----------------
		
		showAllControllers.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				mnAdd.doClick();
				if(showAllControllers.isSelected()){
					refreshDeviceList(controllerTypes.ALL);
				}else{
					refreshDeviceList(controllerTypes.LIMITED);
				}
			}
			
		});
		
		//--------------------File>Exit Button------------------------------------
		
		mntmExit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				System.exit(1);
				
			}
			
		});
		
		//----------------------Add Item Panel Buttons---------------------------
		btnAddAsButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				addedButtonComponents.add(Devices.com[deviceSelectedIndex][comboBox.getSelectedIndex()]);
				addItemPanel(false);
				
				
			}
			
		});
		
		btnAddAsAxis.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				addedAxisComponents.add(Devices.com[deviceSelectedIndex][comboBox.getSelectedIndex()]);
				addItemPanel(false);
				
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
			
			beginListeners();
			break;
		}
	}
		 static void addItemPanel(boolean state){
		refreshDeviceList(controllerTypes.ALL);
		if(state == true){
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
			
			
			
		}else{
			panel.setVisible(false);
			
		 
		}
	}
	
	public static void refreshDeviceList(controllerTypes r){

		
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
							addItemPanel(true);
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
						addItemPanel(true);
					}
				});
			}
			break;
		}
	}
}
