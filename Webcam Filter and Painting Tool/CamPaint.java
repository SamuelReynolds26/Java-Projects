import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.*;

/**
 * Webcam-based drawing 
 * Dartmouth CS 10, Winter 2022
 *
 * @author Sam Reynolds
 */
public class CamPaint extends Webcam {
	private char displayMode = 'w';			// what to display: 'w': live webcam, 'r': recolored image, 'p': painting
	private RegionFinder finder;			// handles the finding
	private Color targetColor;          	// color of regions of interest (set by mouse press)
	private Color paintColor = Color.blue;	// the color to put into the painting from the "brush"
	private BufferedImage painting;			// the resulting masterpiece

	/**
	 * Initializes the region finder and the drawing
	 */
	public CamPaint() {
		finder = new RegionFinder();
		clearPainting();
		displayMode = 'w';
	}

	/**
	 * Resets the painting to a blank image
	 */
	protected void clearPainting() {
		painting = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * DrawingGUI method, here drawing one of live webcam, recolored image, or painting,
	 * depending on display variable ('w', 'r', or 'p')
	 */
	@Override
	public void draw(Graphics g) {
		//w to go back to webcam feed
		if ((targetColor == null) || (displayMode == 'w')) {
			g.drawImage(image, 0, 0, null);
			// click on object then press r to color
		} else {
			if (displayMode == 'r') {
				g.drawImage(finder.getRecoloredImage(), 0, 0, null);
				clearPainting();
				//click to create blank screen and paint
			} else if (displayMode == 'p') {
				g.drawImage(painting, 0, 0, null);
			}
		}
	}

	/**
	 * Webcam method, here finding regions and updating the painting.
	 */
	@Override
	public void processImage() {
		if (targetColor != null){
			finder.setImage(image);
			//find region of target color
			finder.findRegions(targetColor);
			//create arraylist with all points of largest region
			ArrayList<Point> largestRegion = finder.largestRegion();
			finder.recolorImage();
			// loop through largest region, get x and y coordinates and set rbg to paint color
			for (int i = 0; i < finder.largestRegion().size()-1; i++){
				int x = (int)(largestRegion.get(i).getX());
				int y = (int)(largestRegion.get(i).getY());
				painting.setRGB(x,y,paintColor.getRGB());
			}
		}
	}

	/**
	 * Overrides the DrawingGUI method to set the track color.
	 */
	@Override
	//creates target color where mouse is pressed
	public void handleMousePress(int x, int y) {
		if (image != null) {
			targetColor = new Color(image.getRGB(x, y));
		}
	}

	/**
	 * DrawingGUI method, here doing various drawing commands
	 */
	@Override
	public void handleKeyPress(char k) {
		if (k == 'p' || k == 'r' || k == 'w') { // display: painting, recolored image, or webcam
			displayMode = k;
		}
		else if (k == 'c') { // clear
			clearPainting();
		}
		else if (k == 'o') { // save the recolored image
			saveImage(finder.getRecoloredImage(), "pictures/recolored.png", "png");
		}
		else if (k == 's') { // save the painting
			saveImage(painting, "pictures/painting.png", "png");
		}
		else {
			System.out.println("unexpected key "+k);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CamPaint();
			}
		});
	}
}
