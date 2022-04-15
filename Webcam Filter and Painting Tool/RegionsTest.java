import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

/**
 * Testing code for region finding in PS-1.
 * 
 * @author Sam Reynolds, Winter 2022
 */
public class RegionsTest extends DrawingGUI {
	private BufferedImage image;

	/**
	 * Test your RegionFinder by passing an image filename and a color to find.
	 * //@param filename
	 * //@param color
	 */
	public RegionsTest(String name, RegionFinder finder, Color targetColor) {
		super(name, finder.getImage().getWidth(), finder.getImage().getHeight());

		// Do the region finding and recolor the image.
		finder.findRegions(targetColor);
		finder.recolorImage();
		image = finder.getRecoloredImage();
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(image, 0, 0, null);
	}

	public static void main(String[] args) { 
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new RegionsTest("smiley", new RegionFinder(loadImage("CS10/PS1/smiley.png")), new Color(0, 0, 0));
				new RegionsTest("baker", new RegionFinder(loadImage("CS10/Pictures/baker.jpg")), new Color(130, 100, 100));
			}
		});
	}
}
