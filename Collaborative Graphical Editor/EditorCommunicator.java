import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Handles communication to/from the server for the editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author Chris Bailey-Kellogg; overall structure substantially revised Winter 2014
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 */
public class EditorCommunicator extends Thread {
	private PrintWriter out;		// to server
	private BufferedReader in;		// from server
	protected Editor editor;		// handling communication for

	/**
	 * Establishes connection and in/out pair
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			Socket sock = new Socket(serverIP, 4242);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		}
		catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends message to the server
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the server
	 */
	public void run() {
		try {
			// Handle messages (from SketchServerCommunicator)
			// TODO: YOUR CODE HERE
			String line;
			while ((line = in.readLine()) != null){
				// Updates each local sketch
				String[] request = line.split(",");
				// Grabs the mode of the request
				String mode = request[0];
				// Grabs the shapeID of the request
				int shapeID = Integer.parseInt(request[1]);
				// Drawing Mode
				if (mode.equals("DRAW")){
					// Grabs the shapeType
					String shapeType = request[2];
					// Grabs and creates the color
					int rgbValue = Integer.parseInt(request[3]);
					Color newColor = new Color(rgbValue);
					// Adds the new ellipse to the editor's local sketch
					if (shapeType.equals("ellipse")){
						Ellipse newEllipse = new Ellipse(Integer.parseInt(request[4]),Integer.parseInt(request[5]),Integer.parseInt(request[6]),Integer.parseInt(request[7]),newColor);
						editor.getSketch().addToSketch(shapeID,newEllipse);
					}
					// Adds the new rectangle to the editor's local sketch
					if (shapeType.equals("rectangle")){
						Rectangle newRectangle = new Rectangle(Integer.parseInt(request[4]),Integer.parseInt(request[5]),Integer.parseInt(request[6]),Integer.parseInt(request[7]),newColor);
						editor.getSketch().addToSketch(shapeID,newRectangle);
					}
					// Adds the new segment to the editor's local sketch
					if (shapeType.equals("segment")){
						Segment newSegment = new Segment(Integer.parseInt(request[4]),Integer.parseInt(request[5]),Integer.parseInt(request[6]),Integer.parseInt(request[7]),newColor);
						editor.getSketch().addToSketch(shapeID,newSegment);
					}
					// Adds the new polyline to the editor's local sketch
					if (shapeType.equals("polyline")){
						ArrayList<Point> polylinePoints = new ArrayList<>();
						for (int i=4; i< request.length; i++){
							if (i % 2 == 0){
								Point newPoint = new Point(Integer.parseInt(request[i]),Integer.parseInt(request[i+1]));
								polylinePoints.add(newPoint);
							}
						}
						Polyline newPolyline = new Polyline(polylinePoints, newColor);
						editor.getSketch().addToSketch(shapeID,newPolyline);
					}
					// Increments the editor's shapeID so that the next shape will have a unique shapeID
					editor.incrementShapeID();
					System.out.println("Editor Shape ID: " + editor.shapeID);
				}
				// Moving mode
				if (mode.equals("MOVE")) {
					editor.getSketch().getSketchMap().get(shapeID).moveBy(Integer.parseInt(request[2]),Integer.parseInt(request[3]));
				}
				// Recoloring mode
				if (mode.equals("RECOLOR")){
					Color newColor = new Color(Integer.parseInt(request[2]));
					editor.getSketch().getSketchMap().get(shapeID).setColor(newColor);
				}
				// Deleting mode
				if (mode.equals("DELETE")){
					editor.getSketch().getSketchMap().remove(shapeID);
				}
				// newClient mode - draws in the current sketch for a new editor
				if (mode.equals("newClient")){
					String shapeType = request[2];
					int rgbValue = Integer.parseInt(request[3]);
					Color newColor = new Color(rgbValue);
					if (shapeType.equals("ellipse")){
						Ellipse newEllipse = new Ellipse(Integer.parseInt(request[4]),Integer.parseInt(request[5]),Integer.parseInt(request[6]),Integer.parseInt(request[7]),newColor);
						editor.getSketch().addToSketch(shapeID,newEllipse);
					}
					if (shapeType.equals("rectangle")){
						Rectangle newRectangle = new Rectangle(Integer.parseInt(request[4]),Integer.parseInt(request[5]),Integer.parseInt(request[6]),Integer.parseInt(request[7]),newColor);
						editor.getSketch().addToSketch(shapeID,newRectangle);
					}
					if (shapeType.equals("segment")){
						Segment newSegment = new Segment(Integer.parseInt(request[4]),Integer.parseInt(request[5]),Integer.parseInt(request[6]),Integer.parseInt(request[7]),newColor);
						editor.getSketch().addToSketch(shapeID,newSegment);
					}
					if (shapeType.equals("polyline")){
						//loop through all the string x/y's and build add to a new arraylist in a new polyline
						ArrayList<Point> polylinePoints = new ArrayList<>();
						for (int i=4; i< request.length; i++){
							if (i % 2 == 0){
								Point newPoint = new Point(Integer.parseInt(request[i]),Integer.parseInt(request[i+1]));
								polylinePoints.add(newPoint);
							}
						}
						Polyline newPolyline = new Polyline(polylinePoints, newColor);
						editor.getSketch().addToSketch(shapeID,newPolyline);
					}
				}
				// Sets the shapeID of the new editor to that of the other editors so shapes are not overridden
				if (mode.equals("settingShapeID")){
					editor.setShapeID(shapeID);
				}
				editor.repaint();
			}
		}
		catch (IOException e) {
		e.printStackTrace();
		}
		finally {
			System.out.println("server hung up");
		}

	}
}
