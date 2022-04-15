import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 */
public class Polyline implements Shape {
	ArrayList<Point> polylinePoints = new ArrayList<>();
	private Color color;

	/**
	 * An "empty" ellipse, with only one point set so far
	 */
	public Polyline(int x1, int y1, Color color) {
		Point newPoint = new Point(x1,y1);
		polylinePoints.add(newPoint);
		this.color = color;
	}

	public Polyline(ArrayList<Point> polylinePoints, Color color){
		this.polylinePoints = polylinePoints;
		this.color = color;
	}

	public void addPoint(Point p){
		polylinePoints.add(p);
	}

	@Override
	public void moveBy(int dx, int dy) {
		for (Point point: polylinePoints){
			point.move((int)(point.getX())+dx, (int)(point.getY())+dy);
		}
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public boolean contains(int x, int y) {
		boolean contains = false;
		for (int i=0; i < polylinePoints.size()-2; i++){
			int x1 = (int)polylinePoints.get(i).getX();
			int y1 = (int)polylinePoints.get(i).getY();
			int x2 = (int)polylinePoints.get(i+1).getX();
			int y2 = (int)polylinePoints.get(i+1).getY();
			if (Segment.pointToSegmentDistance(x, y, x1, y1, x2, y2) <= 3){
				contains = true;
			}
		}
		return contains;
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		for (int i=0; i<polylinePoints.size()-2; i++){
			g.drawLine((int)polylinePoints.get(i).getX(),(int)polylinePoints.get(i).getY(),(int)polylinePoints.get(i+1).getX(),(int)polylinePoints.get(i+1).getY());
		}

	}

	@Override
	public String toString() {
		String formattedPoints = "";
		for (int i = 0; i < polylinePoints.size() - 1; i++) {
			formattedPoints += (int)(polylinePoints.get(i).getX()) + "," + (int)(polylinePoints.get(i).getY()) + ",";
		}
		formattedPoints += (int)(polylinePoints.get(polylinePoints.size() - 1).getX()) + "," + (int)(polylinePoints.get(polylinePoints.size() - 1).getY());
		return "polyline" + "," + color.getRGB() + "," + formattedPoints;
	}
}
