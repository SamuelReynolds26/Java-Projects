import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client
	private PrintWriter out;				// to client
	private SketchServer server;			// handling communication for

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}
	
	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");
			
			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			// TODO: YOUR CODE HERE
			TreeMap<Integer, Shape> masterSketch = server.getSketch().getSketchMap();
			for (Integer shapeID: masterSketch.keySet()){
				System.out.println("New Client: " + masterSketch.get(shapeID).toString());
				send("newClient," + shapeID + "," + masterSketch.get(shapeID).toString());
			}
			int currentShapeID = 0;
			for (Integer shapeID : server.getSketch().getSketchMap().descendingKeySet()){
				currentShapeID = shapeID;
				break;
			}
			send("settingShapeID," + currentShapeID+1);

			// Keep getting and handling messages from the client
			// TODO: YOUR CODE HERE
			String line;
			while ((line = in.readLine()) != null){
				// Updates the masterSketch
				String[] request = line.split(",");
				// Grabs the mode of the request
				String mode = request[0];
				// Grabs the shapeID of the request
				int shapeID = Integer.parseInt(request[1]);
				// Drawing Mode
				if (mode.equals("DRAW")){
					// Grabs the requested shapeType
					String shapeType = request[2];
					// Grabs and creates the requested color
					int rgbValue = Integer.parseInt(request[3]);
					Color newColor = new Color(rgbValue);
					// Adds the new ellipse to the server's master sketch
					if (shapeType.equals("ellipse")){
						Ellipse newEllipse = new Ellipse(Integer.parseInt(request[4]),Integer.parseInt(request[5]),Integer.parseInt(request[6]),Integer.parseInt(request[7]),newColor);
						server.getSketch().addToSketch(shapeID,newEllipse);
					}
					// Adds the new rectangle to the server's master sketch
					if (shapeType.equals("rectangle")){
						Rectangle newRectangle = new Rectangle(Integer.parseInt(request[4]),Integer.parseInt(request[5]),Integer.parseInt(request[6]),Integer.parseInt(request[7]),newColor);
						server.getSketch().addToSketch(shapeID,newRectangle);
					}
					// Adds the new segment to the server's master sketch
					if (shapeType.equals("segment")){
						Segment newSegment = new Segment(Integer.parseInt(request[4]),Integer.parseInt(request[5]),Integer.parseInt(request[6]),Integer.parseInt(request[7]),newColor);
						server.getSketch().addToSketch(shapeID,newSegment);
					}
					// Adds the new polyline to the server's master sketch
					if (shapeType.equals("polyline")){
						ArrayList<Point> polylinePoints = new ArrayList<>();
						for (int i=4; i< request.length; i++){
							if (i % 2 == 0){
								Point newPoint = new Point(Integer.parseInt(request[i]),Integer.parseInt(request[i+1]));
								polylinePoints.add(newPoint);
							}
						}
						Polyline newPolyline = new Polyline(polylinePoints, newColor);
						server.getSketch().addToSketch(shapeID,newPolyline);
					}
				}
				// Moving Mode
				if (mode.equals("MOVE")) {
					server.getSketch().getSketchMap().get(shapeID).moveBy(Integer.parseInt(request[2]),Integer.parseInt(request[3]));
				}
				// Recoloring Mode
				if (mode.equals("RECOLOR")){
					Color newColor = new Color(Integer.parseInt(request[2]));
					server.getSketch().getSketchMap().get(shapeID).setColor(newColor);
				}
				// Deleting Mode
				if (mode.equals("DELETE")){
					server.getSketch().getSketchMap().remove(shapeID);
				}

				// Sends same exact request back to the Editor Communicators so the local sketches can be updated
				server.broadcast(line);
				System.out.println("Broadcasting: " + line);
			}
			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
