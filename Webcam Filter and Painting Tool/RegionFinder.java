import java.awt.*;
import java.awt.image.*;
import java.util.*;

/**
 * Region growing algorithm: finds and holds regions in an image.
 * Each region is a list of contiguous points with colors similar to a target color.
 * PS-1, Dartmouth CS 10, Winter 2022
 *
 * @author Sam Reynolds, Winter 2022
 */
public class RegionFinder {
	private static final int maxColorDiff = 750;				// how similar a pixel color must be to the target color, to belong to a region
	private static final int minRegion = 50; 				// how many points in a region to be worth considering

	private BufferedImage image;                            // the image in which to find regions
	private BufferedImage recoloredImage;                   // the image with identified regions recolored

	private ArrayList<ArrayList<Point>> regions = new ArrayList<>();			// a region is a list of points
	// so the identified regions are in a list of lists of points

	public RegionFinder() {
		this.image = null;
	}

	public RegionFinder(BufferedImage image) {
		this.image = image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public BufferedImage getImage() {
		return image;
	}

	public BufferedImage getRecoloredImage() {
		return recoloredImage;
	}

	/**
	 * Sets regions to the flood-fill regions in the image, similar enough to the trackColor.
	 */
	public void findRegions(Color targetColor) {
		regions = new ArrayList<>();

		BufferedImage visited = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

		// finding the first point that is similar to the target color
		for(int y=0; y < image.getHeight(); y++){
			for(int x=0; x < image.getWidth(); x++){
				if (colorMatch(new Color(image.getRGB(x,y)), targetColor) && (visited.getRGB(x,y)==0)){
					//create array list toVisit and one for similarRegion
					ArrayList<Point> similarRegion = new ArrayList<>();
					ArrayList<Point> toVisit = new ArrayList<>();
					//add points that match color and where visited.RGB == 0
					toVisit.add(new Point(x,y));
					// searching the neighbors of the points in toVisit
					while (toVisit.size() != 0){
						Point firstInQueue = toVisit.get(0);
						visited.setRGB((int)(firstInQueue.getX()),(int)(firstInQueue.getY()),1);
						similarRegion.add(toVisit.remove(0));
						// i is Y-coordinate
						for(int i = (int)(Math.max(0, firstInQueue.getX()-1)); i <= Math.min(image.getWidth() - 1, firstInQueue.getX()+1); i++) {
							// j is X-coordinate
							for (int j = (int)(Math.max(0, firstInQueue.getY() - 1)); j <= Math.min(image.getHeight() - 1, firstInQueue.getY() + 1); j++) {
								Color currNeighborColor = new Color(image.getRGB(i,j));
								if (visited.getRGB(i,j) == 0) {
									visited.setRGB(i,j,1);
									if (colorMatch(currNeighborColor, targetColor)){
										toVisit.add(new Point(i,j));
									}
								}
							}
						}
					}

					if (similarRegion.size() >= minRegion){
						regions.add(similarRegion);
					}
				}
			}
		}
		System.out.println("end");
	}

	/**
	 * Tests whether the two colors are "similar enough" (your definition, subject to the maxColorDiff threshold, which you can vary).
	 */
	private static boolean colorMatch(Color c1, Color c2) {
		// testing for color threshold between two colors
		int redDif = Math.abs(c1.getRed()-c2.getRed());
		int greenDif = Math.abs(c1.getGreen()-c2.getGreen());
		int blueDif = Math.abs(c1.getBlue()-c2.getBlue());
		int sumDif = (redDif*redDif) + (greenDif*greenDif) + (blueDif*blueDif);
		// if sum is less than or equal to diff threshold it is similar enough
		if (sumDif <= maxColorDiff) {
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * Returns the largest region detected (if any region has been detected)
	 */
	public ArrayList<Point> largestRegion() {
		int largestRegion = 0;
		int largestRegionIndex = 0;
		// loop through all region and return index of the largest one
		for (int i = 0; i < regions.size()-1; i++){
			if (regions.get(i).size() > largestRegion){
				largestRegion = regions.get(i).size();
				largestRegionIndex = i;
			}
		}
		return regions.get(largestRegionIndex);
	}

	/**
	 * Sets recoloredImage to be a copy of image,
	 * but with each region a uniform random color,
	 * so we can see where they are
	 */
	public void recolorImage() {
		// First copy the original
		recoloredImage = new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);
		// Now recolor the regions in it
		for (int i = 0; i < regions.size()-1; i++){
			int newRed = (int)(Math.random()*255);
			int newGreen = (int)(Math.random()*255);
			int newBlue = (int)(Math.random()*255);
			//create new color with random RGB values
			Color newColor = new Color(newRed, newGreen, newBlue);
			//get all x and y coordinates and set them to new color
			for (int j = 0; j < regions.get(i).size()-1; j++){
				int x = (int)(regions.get(i).get(j).getX());
				int y = (int)(regions.get(i).get(j).getY());
				recoloredImage.setRGB(x,y,newColor.getRGB());
			}
		}
	}
}
