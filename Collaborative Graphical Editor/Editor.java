import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.util.TreeMap;

import javax.swing.*;

/**
 * Client-server graphical editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 */

public class Editor extends JFrame {	
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch; 						// holds and handles all the completed objects
	private int movingId = -1;					// current shape id (if any; else -1) being moved
	private int paintingId = -1;				// current shape id (if any; else -1) being moved
	private int deletingId = -1;				// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged

	public int shapeID = 0; // if you have multiple editors you will override the shapeID


	// Communication
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};
		
		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});		

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});
		
		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	public void setShapeID(int shapeID){
		this.shapeID = shapeID;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		// TODO: YOUR CODE HERE
		TreeMap<Integer, Shape> shapeMap = sketch.getSketchMap();
		for (Integer shapeID : shapeMap.keySet()) {
			Shape shapeInfo = shapeMap.get(shapeID);
			shapeInfo.draw(g);
		}

		// draws shape as you making a request so client can see what they are doing
		if (curr != null){
			curr.draw(g);
		}
	}

	public void incrementShapeID(){
		shapeID += 1;
	}

	// Helpers for event handlers
	
	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		// TODO: YOUR CODE HERE
		System.out.println("Press: " + p);
		// Drawing mode
		if (mode == Mode.DRAW) {
			drawFrom = p;
			// Creates a new ellipse
			if (shapeType.equals("ellipse")) {
				curr = new Ellipse((int) drawFrom.getX(), (int) drawFrom.getY(), color);
			}
			// Creates a new rectangle
			if (shapeType.equals("rectangle")) {
				curr = new Rectangle((int) drawFrom.getX(), (int) drawFrom.getY(), color);
			}
			// Creates a new freehand drawing
			if (shapeType.equals("freehand")) {
				curr = new Polyline((int) drawFrom.getX(), (int) drawFrom.getY(), color);
			}
			// Creates a new segment
			if (shapeType.equals("segment")) {
				curr = new Segment((int) drawFrom.getX(), (int) drawFrom.getY(), color);
			}
		}
		// Moving mode
		else if (mode == Mode.MOVE) {
			if (!sketch.getSketchMap().isEmpty()) {
				movingId = sketch.getShapeID((int)(p.getX()),(int)(p.getY()));
				if (movingId != -1){
					moveFrom = p;
				}
			}
		}
		// Recoloring mode
		else if (mode == Mode.RECOLOR) {
			if (!sketch.getSketchMap().isEmpty()) {
				paintingId = sketch.getShapeID((int)(p.getX()),(int)(p.getY()));
				if (paintingId != -1){
					comm.send("RECOLOR" + "," + paintingId + "," + color.getRGB());
				}
			}
		}
		// Deleting mode
		else if (mode == Mode.DELETE) {
			if (!sketch.getSketchMap().isEmpty()) {
				deletingId = sketch.getShapeID((int)(p.getX()),(int)(p.getY()));
				if (deletingId != -1){
					comm.send("DELETE" + "," + deletingId);
				}
			}
		}
		repaint();
	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
		// TODO: YOUR CODE HERE
		System.out.println("Drag: " + p);
		if (mode == Mode.DRAW) {
			// Updates the current rectangle being drawn
			if (shapeType.equals("rectangle")) {
				((Rectangle) curr).setCorners((int)(drawFrom.getX()), (int)(drawFrom.getY()), (int)p.getX(), (int)p.getY());
				repaint();
			}
			// Updates the current freehand being drawn
			if (shapeType.equals("freehand")) {
				((Polyline) curr).addPoint(p);
				repaint();
			}
			// Updates the current ellipse being drawn
			if (shapeType.equals("ellipse")) {
				((Ellipse) curr).setCorners((int)(drawFrom.getX()), (int)(drawFrom.getY()), (int)(p.getX()), (int)(p.getY()));//pass in 4 numbers instead of 2
				repaint();
			}
			// Updates the current segment being drawn
			if (shapeType.equals("segment")) {
				((Segment) curr).setEnd((int)(p.getX()), (int)(p.getY()));
				repaint();
			}
		}
		else if (mode == Mode.MOVE) {
			// Sends request to move the current shape
			comm.send("MOVE" + "," + movingId + "," + (int)(p.getX()-moveFrom.getX()) + "," + (int)(p.getY()-moveFrom.getY()));
			moveFrom = p;
			repaint();
		}
	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it		
	 */
	private void handleRelease() {
		// TODO: YOUR CODE HERE
		System.out.println("Release!");
		if (mode == Mode.DRAW){
			if (curr != null){
				comm.send("DRAW" + "," + shapeID + "," + curr.toString());
			}
			curr = null;
			drawFrom = null;
		}
		moveFrom = null;
		repaint();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});	
	}
}
