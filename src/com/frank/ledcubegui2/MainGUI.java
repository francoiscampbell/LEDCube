/*
 * Application to control my 7x7x7 LED cube through USB virtual serial port.
 * This is my personal application, so it's only ever been run on my Mac, I haven't
 * even tried running it on Windows.
 * The application requires the Java serial library to be installed in /Library/Java/Extensions.
 * 
 * The software works as-is, but is very simple and not very foolproof.
 * For example, if a serial port is not chosen at startup, the application will crash.
 * I'm in the process of building v2 of the physical cube, and I will update
 * the software when the cube PCB arrives and is fully assembled.
 * The point of this software was not to have a permanent cube control application, but
 * rather to be able to light up arbitrary LEDs in the cube as simply as possible,
 * hence the 343 checkboxes representing each LED.
 * 
 * The source is very comment-sparse as I wrote this application quickly to get the cube into
 * a working state as fast as possible. I haven't put this on my Github because I
 * don't think it's ready for a public release. If I ever do release the new version publicly, 
 * I will add relevant comments where they are needed.
 * 
 * EDIT Jan 24 2014: I've decided to put it on my Github anyways...
 * 
 * Essentially, the user chooses which LEDs to light and for how long, and this consists of a frame
 * (like a display frame). Many frames can be chained together and uploaded to the ATmega microcontroller.
 * The list of frames can be saved to file in order to save cool-looking patterns.
 * The frame limit is due the micro's 2K of memory. The next version of the software will
 * feature two-way communication between the micro and the application, enabling an unlimited
 * number of frames and continuous animation.
 */

package com.frank.ledcubegui2;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MainGUI implements WindowListener {

	public static final int CUBESIZE = 7;

	public final static String strNEXT = "-->";
	public final static String strPREV = "<--";
	public final static String strUPLOAD = "Upload";
	public final static String strUPLOADSTRING = "Upload string";
	public final static String strCLEAR = "Clear";
	public final static String strDELETE = "Remove";
	public final static String strSAVE = "Save";
	public final static String strLOAD = "Load";
	public final static String strRAIN = "Rain";

	private JFrame mainAppFrame;

	private List<JCheckBox> checkboxes;

	private List<CubeFrame> cubeFrames;
	private CubeFrame currentCubeFrame;
	private int currentCubeFrameIndex;
	private JTextField txtUploadAString;

	private JLabel lblFrameNumber;

	private boolean arduinoConnected;

	private SerialPort myPort;
	private String chosenPort;
	private InputStream inPort;
	private OutputStream outPort;

	private JSpinner spinnerDuration;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					MainGUI window = new MainGUI();
					window.mainAppFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainGUI() {
		checkboxes = new ArrayList<JCheckBox>(CUBESIZE * CUBESIZE * CUBESIZE);

		cubeFrames = new LinkedList<CubeFrame>();
		currentCubeFrame = new CubeFrame();
		cubeFrames.add(currentCubeFrame);
		currentCubeFrameIndex = 0;

		initialize();
	}

	public void toggleBit(int x, int y, int z) {
		currentCubeFrame.toggleBit(x, y, z);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		mainAppFrame = new JFrame();
		mainAppFrame.setBounds(100, 100, 1200, 260);
		mainAppFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainAppFrame.setResizable(false);
		mainAppFrame.addWindowListener(this);

		JPanel panelButtons = new JPanel();
		mainAppFrame.getContentPane().add(panelButtons, BorderLayout.NORTH);
		panelButtons.setLayout(new GridLayout(CUBESIZE, CUBESIZE * CUBESIZE));

		JCheckboxHandler jcbh = new JCheckboxHandler(this);

		for (int z = 0; z < CUBESIZE; z++) {
			for (int y = 0; y < CUBESIZE; y++) {
				for (int x = 0; x < CUBESIZE; x++) {
					JCheckBox cb = new JCheckBox("" + x + y + z);
					cb.addActionListener(jcbh);
					panelButtons.add(cb);
					checkboxes.add(cb);
				}
				Component horizontalStrut = Box.createHorizontalStrut(20);
				panelButtons.add(horizontalStrut);
			}

		}

		JPanel panelControls = new JPanel();
		mainAppFrame.getContentPane().add(panelControls, BorderLayout.CENTER);

		JButtonHandler jbh = new JButtonHandler(this);

		JButton btnPrevFrame = new JButton(strPREV);
		btnPrevFrame.addActionListener(jbh);
		panelControls.add(btnPrevFrame);

		JLabel lblFrame = new JLabel("Frame #:");
		panelControls.add(lblFrame);

		lblFrameNumber = new JLabel("" + currentCubeFrameIndex);
		panelControls.add(lblFrameNumber);

		JButton btnNextFrame = new JButton(strNEXT);
		btnNextFrame.addActionListener(jbh);
		panelControls.add(btnNextFrame);

		JButton btnClear = new JButton(strCLEAR);
		btnClear.addActionListener(jbh);
		panelControls.add(btnClear);

		JButton btnDelete = new JButton(strDELETE);
		btnDelete.addActionListener(jbh);
		panelControls.add(btnDelete);

		JButton btnSave = new JButton(strSAVE);
		btnSave.addActionListener(jbh);
		panelControls.add(btnSave);

		JButton btnLoad = new JButton(strLOAD);
		btnLoad.addActionListener(jbh);
		panelControls.add(btnLoad);

		JPanel panelUploadControls = new JPanel();
		mainAppFrame.getContentPane().add(panelUploadControls, BorderLayout.SOUTH);

		spinnerDuration = new JSpinner();
		spinnerDuration.setModel(new SpinnerNumberModel(0, 0, 4095, 1));
		spinnerDuration.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner sp = (JSpinner) e.getSource();
				currentCubeFrame.setDuration((Integer) sp.getValue());
			}
		});

		JLabel lblDuration = new JLabel("Duration:");
		panelUploadControls.add(lblDuration);
		panelUploadControls.add(spinnerDuration);

		JButton btnUpload = new JButton(strUPLOAD);
		btnUpload.addActionListener(jbh);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		panelUploadControls.add(horizontalStrut);
		panelUploadControls.add(btnUpload);

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		panelUploadControls.add(horizontalStrut_1);

		txtUploadAString = new JTextField();
		panelUploadControls.add(txtUploadAString);
		txtUploadAString.setText("Upload a string");
		txtUploadAString.setColumns(20);

		JButton btnUploadString = new JButton(strUPLOADSTRING);
		btnUploadString.addActionListener(jbh);
		panelUploadControls.add(btnUploadString);

		openSerialPort();
	}

	private void openSerialPort() {
		arduinoConnected = false;
		ArrayList<String> portNames = new ArrayList<String>();

		Enumeration portIdentifiers = CommPortIdentifier.getPortIdentifiers();

		while (portIdentifiers.hasMoreElements()) {
			CommPortIdentifier pid = (CommPortIdentifier) portIdentifiers.nextElement();
			portNames.add(pid.getName());
		}

		String[] nameArray = portNames.toArray(new String[portNames.size()]);
		String strChoose = "Please choose your Arduino's serial port:";
		String strSelect = "Select serial port";
		String chosenPort = (String) JOptionPane.showInputDialog(mainAppFrame, strChoose, strSelect, JOptionPane.PLAIN_MESSAGE, null, nameArray, nameArray[0]);

		CommPortIdentifier portId = null;
		portIdentifiers = CommPortIdentifier.getPortIdentifiers();
		while (portIdentifiers.hasMoreElements()) {
			CommPortIdentifier pid = (CommPortIdentifier) portIdentifiers.nextElement();
			if (pid.getName().equals(chosenPort)) {
				portId = pid;
				break;
			}
		}

		try {
			myPort = (SerialPort) portId.open("LEDCube Gui", 10000);
			myPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			inPort = myPort.getInputStream();
			outPort = myPort.getOutputStream();

			arduinoConnected = true;
		} catch (PortInUseException e) {
			System.err.println("Port already in use: " + e);
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Can't open input stream: write-only");
		}

	}

	public void clearCurrentFrame() {
		currentCubeFrame.clear();
		resetDisplayState();
	}

	public void removeCurrentFrame() {
		cubeFrames.remove(currentCubeFrameIndex);
		if (cubeFrames.size() == 0) {
			cubeFrames.add(new CubeFrame());
			currentCubeFrameIndex = 0;
			currentCubeFrame = cubeFrames.get(currentCubeFrameIndex);
			resetDisplayState();
		} else {
			loadPrevFrame();
		}
	}

	public void loadPrevFrame() {
		if (currentCubeFrameIndex > 0) {
			currentCubeFrameIndex--;
		}
		lblFrameNumber.setText("" + currentCubeFrameIndex);
		currentCubeFrame = cubeFrames.get(currentCubeFrameIndex);
		resetDisplayState();
	}

	public void loadNextFrame() {
		currentCubeFrameIndex++;
		if (currentCubeFrameIndex >= cubeFrames.size()) {
			cubeFrames.add(new CubeFrame());
		}
		lblFrameNumber.setText("" + currentCubeFrameIndex);
		currentCubeFrame = cubeFrames.get(currentCubeFrameIndex);
		resetDisplayState();
	}

	private void resetDisplayState() {
		for (JCheckBox cb : checkboxes) {
			String coords = cb.getActionCommand();
			int x = Character.getNumericValue(coords.charAt(0));
			int y = Character.getNumericValue(coords.charAt(1));
			int z = Character.getNumericValue(coords.charAt(2));
			cb.setSelected(currentCubeFrame.bitActive(x, y, z));
		}
		spinnerDuration.setValue(currentCubeFrame.getDuration());
	}

	public void upload() {
		upload(cubeFrames);
	}

	public void upload(List<CubeFrame> uploadFrames) {
		if (arduinoConnected) {
			Thread uploadThread = new Thread(new CubeFrameUploader(cubeFrames, outPort, inPort));
			uploadThread.start();
		} else {
			System.out.println("Arduino not connected");
		}
	}

	public void uploadString() {
	}

	void rain(int nFrames) {
		Random r = new Random();
		boolean[][][] frame = new boolean[CUBESIZE][CUBESIZE][CUBESIZE];

		List<CubeFrame> frameList = new ArrayList<CubeFrame>();
		CubeFrame tempFrame = new CubeFrame(frame, currentCubeFrame.getDuration());

		for (int i = 0; i < nFrames; i++) {
			tempFrame = tempFrame.shifted(CubeFrame.FrameDirection.MINUSZ);
			for (int x = 0; x < CUBESIZE; x++) {
				for (int y = 0; y < CUBESIZE; y++) {
					if ((r.nextInt() % 11) == 0) {
						tempFrame.toggleBit(x, y, CUBESIZE - 1);
					}
				}
			}
			frameList.add(tempFrame);
		}

		cubeFrames = frameList;
		currentCubeFrame = cubeFrames.get(0);
		upload(frameList);
	}

	public void loadFromFile() {
		File inFile = getFileFromDialog('o');

		BufferedReader input;
		try {
			input = new BufferedReader(new FileReader(inFile));

			int nFrames = Integer.parseInt(input.readLine());
			cubeFrames = new ArrayList<CubeFrame>(nFrames);
			for (int i = 0; i < nFrames; i++) {
				boolean[][][] frame = new boolean[CUBESIZE][CUBESIZE][CUBESIZE];
				for (int x = 0; x < CUBESIZE; x++) {
					for (int y = 0; y < CUBESIZE; y++) {
						for (int z = 0; z < CUBESIZE; z++) {
							frame[x][y][z] = (Integer.parseInt(input.readLine()) == 1);
						}
					}
				}
				int duration = Integer.parseInt(input.readLine());
				cubeFrames.add(new CubeFrame(frame, duration));
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		currentCubeFrameIndex = 0;
		currentCubeFrame = cubeFrames.get(currentCubeFrameIndex);
		resetDisplayState();
	}

	public void saveToFile() {
		File outFile = getFileFromDialog('s');
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(outFile));
			output.write("" + cubeFrames.size());
			output.newLine();
			for (CubeFrame f : cubeFrames) {
				for (int x = 0; x < CUBESIZE; x++) {
					for (int y = 0; y < CUBESIZE; y++) {
						for (int z = 0; z < CUBESIZE; z++) {
							if (f.bitActive(x, y, z)) {
								output.write("" + 1);
							} else {
								output.write("" + 0);
							}
							output.newLine();
						}
					}
				}
				output.write("" + f.getDuration());
				output.newLine();
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File getFileFromDialog(char action) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		final JFileChooser fc = new JFileChooser();

		int returnVal;
		if (action == 'o') {
			returnVal = fc.showOpenDialog(mainAppFrame);
		} else if (action == 's') {
			returnVal = fc.showSaveDialog(mainAppFrame);
		} else {
			return null;
		}

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			return selectedFile;
		} else {
			System.out.println("Command cancelled by user.");
			return null;
		}
	}

	@Override
	public void windowClosed(WindowEvent e) {
		try {
			outPort.close();
			inPort.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

}
