import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;

import javafx.util.Pair;

public class RemoveText {
	// Our image memoryspace. We directly edit this to generate our new image.
	private BufferedImage origImage = null;
	
	void setup() {
		// Create a new empty image
		//origImage = new BufferedImage(750, 1334, BufferedImage.TYPE_BYTE_INDEXED);
	  
	  // Fill it with the data from our file on disk.
		try {
			origImage = ImageIO.read(new File("input_data/image3.PNG"));
		} catch (IOException e) {
			System.err.println("Error attempting to read from file " + "input_data/image3.PNG");
			e.printStackTrace();
		}
//	  origImage = loadImage("input_data/image3.PNG");
//	  surface.setSize(origImage.width, origImage.height);
	  process();
	}
	
	
	//Simply copies the original snapchat image into the window.
//	void copyImage() {
//	  for (int i=0; i < origImage.pixels.length; i++) {
//	    pixels[i] = origImage.pixels[i];
//	  }
//	}
	
	
	//identifyBar - returns two integer values - the start and end pixels of the snapchat text bar.
	//make sure to search for rows that show a drop in exposure and variance.
	int[] identifyBar() {
	  double[] saturationLevels = new double[origImage.getHeight()];
	  double[] exposureLevels = new double[origImage.getHeight()];
	  
	  int[] ret = {0, 0};
	  
	  //First, gather the saturation and exposure levels.
	  for (int y=0; y<origImage.getHeight(); y++) {
	    
	    double rowSaturation = 0.0;
	    double rowExposure = 0.0;
	    for (int x=0; x<origImage.getWidth(); x++) {
	      int rgb_packed = origImage.getRGB(x, y);
	      int r = (rgb_packed >> 16) & 0xFF;
	      int g = (rgb_packed >> 8) & 0xFF;
	      int b = rgb_packed & 0xFF;
	      rowExposure += r + b + g;
	      
	      double avg = (r + b + g) / 3.0;
	      rowSaturation += Math.abs(r - avg);
	      rowSaturation += Math.abs(b - avg);
	      rowSaturation += Math.abs(g - avg);
	    }
	    
	    saturationLevels[y] = rowSaturation / origImage.getWidth();
	    exposureLevels[y] = rowExposure / (3*origImage.getWidth());
	  }
	  
	  
	  //Now run a window down the image and try to find the black bar.
	  //We need a drop in exposure and a drop in saturation to detect it.
	  for (int yi=0; yi<origImage.getHeight()-30; yi++) {
	   //Fix these threshold values plsss
	
	   if ((exposureLevels[yi] - exposureLevels[yi+3] > 40) && (ret[0] == 0)) {
	     System.out.println("Found a new start of line!");
	     ret[0] = yi;
	   }
	   
	   if (exposureLevels[yi+3] - exposureLevels[yi] > 40) {
	     System.out.println("Found a new end of line!");
	     ret[1] = yi;
	   }
	  }
	
	  return ret;
	}
	
	
	
	//removeBar - removes the gray snapchat bar, and keeps the white text in place. 
	//Returns an array of locations that make up the white text.
	ArrayList<Point> removeBar(int starty, int endy) {
	  //The brightest we can get is (102, 102, 102) with the bar. 
	  //Anything above that is considered text.
	  ArrayList<Point> textPix = new ArrayList<Point>();
	  for (int y=starty; y<endy; y++) {
	    for (int x=0; x<origImage.getWidth(); x++) {
	      int rgb_packed = origImage.getRGB(x, y);
	      int r = (rgb_packed >> 16) & 0xFF;
	      int g = (rgb_packed >> 8) & 0xFF;
	      int b = rgb_packed & 0xFF;
	      double avg = (r + g + b) / 3.0;
	      if (avg > 104.0) {
	        textPix.add(new Point(x, y));
	      }
	    }
	  }
	  
	  //Now revert the darkened pixels back to their rightful color 
	  for (int y=starty; y<endy; y++) {
	    for (int x=0; x<origImage.getWidth(); x++) {
	    	
	    	// Only modify the pixel if it is not a text character
	       if (!(textPix.contains(new Pair<Integer, Integer>(x, y)))) {
	         //The regression is: 
	    	 // 	(dark_value) = 0.36176 * (regular_value) + 5.1657
	    	 int rgb_packed = origImage.getRGB(x, y);
	         int r = (rgb_packed >> 16) & 0xFF;
	         r = (int) ((r - 5.388474) / .356637);
	         int g = (rgb_packed >> 8) & 0xFF;
	         g = (int) ((g - 5.388474) / .356637);
	         int b = rgb_packed & 0xFF;
	         b = (int) ((b - 5.388474) / .356637);
	         origImage.setRGB(x, y, new Color(r, g, b).getRGB());
	       }
	    }
	  }
	  
	  //And return the array of white pixels.
	  return textPix;
	}
	
	//Remove text - continuously loops over the given list of text pixels. Each pixel is popped and set equal
	//to the average of some of its neighbors.
	void removeText(ArrayList<Point> pixs) {
	  
	  //Create a new hash that stores the locations of the text pixels. 
	  //We also add all 8 neighbors to the Hashmap.
	  HashMap<Point, Integer> presenceHash = new HashMap<Point, Integer>();
	  for (Point i : pixs) {
	    int[] dx = {-2, -1, 0, 1, 2};
	    int[] dy = dx;
	    for (int y : dy) {
	      for (int x : dx) {
	    	int i_x = (int) i.getX();
	    	int i_y = (int) i.getY();
	        Point newPoint = new Point(i_x + x, i_y + y);
	        presenceHash.put(newPoint, 1);
	      }
	    }
	  }
	  
	  //Create an ArrayList of TextPix objects and their neighbors.
	  ArrayList<TextPix> textpixs = new ArrayList<TextPix>();
	  for (Point i : pixs) {
	    int[] dx = {-2, -1, 0, 1, 2};
	    int[] dy = dx;
	    for (int y : dy) {
	      for (int x : dx) {
	        textpixs.add(new TextPix(x, y, presenceHash, origImage.getWidth()));
	      }
	    }
	  }
	  Collections.sort(textpixs);
	  
	  //Repeat this process until all of the textpixels have been processed.
	  while (textpixs.size() != 0) {
	    
	    int i = 0;
	    while ((i < textpixs.size())) {
	    //First, iterate until we start hitting pixels with no relevant neighbors.
	      
	      //At each of these, take the average rgb values of their relevant neighbors.
	      TextPix t = textpixs.get(i);
	      int r = 0;
	      int g = 0;
	      int b = 0;
	      int count = 0;
	      for (Integer loc : t.getRelevantPixels(presenceHash)) {
	        r += red(pixels[loc]);
	        g += green(pixels[loc]);
	        b += blue(pixels[loc]);
	        count += 1;
	      }
	      
	      
	      if (count == 0) {
	       i += 1;
	       continue;
	      }
	      
	      r /= count;
	      g /= count;
	      b /= count;
	      
	      //And set this pixel equal to the average of those pixels. 
	      origImage.setRGB(t.getXLocation(), t.getYLocation(), new Color(r, g, b).getRGB());
	      
	      //Then remove the location from both the arraylist and the presenceHash.
	      textpixs.remove(t);
	      presenceHash.put(t.getLocation(), 0);
	    
	    }
	    
	    //Once we hit the end, update the presenceHash stored in each remaining pixel.
	    for (TextPix t : textpixs) {
	      t.setSelfPixelHash(presenceHash);
	    }
	    
	    //Sort, and loop over again.
	    Collections.sort(textpixs);
	  }
	}
	
	
	//removeLines - uses a similar Gaussian Filter to remove the lines left by removeBar.
	void removeLines(int[] ys) {
	  for (int x=0; x < origImage.getWidth(); x++) {
	    //Find the average in a 7x1 window centered at each pixel.
	    int[] dys = {-3, 3};
	
	    int r=0, g=0, b=0;
	    for (int dy : dys) {
	      int rgb_packed = origImage.getRGB(x, ys[0]);
	      r += 0.5 * ((rgb_packed >> 16) & 0xFF);
	      g += 0.5 * ((rgb_packed >> 8) & 0xFF);
	      b += 0.5 * (rgb_packed & 0xFF);
	    }
	    origImage.setRGB(x, ys[0], new Color(r,g,b).getRGB());
	    origImage.setRGB(x, ys[0] - 1, new Color(r,g,b).getRGB());
	    origImage.setRGB(x, ys[0] + 1, new Color(r,g,b).getRGB());

	    
	    r=0;
	    g=0;
	    b=0;
	    for (int dy : dys) {
	      int rgb_packed = origImage.getRGB(x, ys[1] + dy);
	      r += 0.5 * ((rgb_packed >> 16) & 0xFF);
	      g += 0.5 * ((rgb_packed >> 16) & 0xFF);
	      b += 0.5 * (rgb_packed & 0xFF);
	    }

	    origImage.setRGB(x, ys[1], new Color(r, g, b).getRGB());
	    origImage.setRGB(x, ys[1] + 1, new Color(r, g, b).getRGB());
	    origImage.setRGB(x, ys[1] - 1, new Color(r, g, b).getRGB());
	  }
	}
	
	
	void process() {
//	  loadPixels();
//	  copyImage();
	  int[] ys = identifyBar();
	  
	  ArrayList<Point> textpix = removeBar(ys[0]+3, ys[1]+1);
	  
	  removeText(textpix);
	  
	  removeLines(ys);  
	  
//	  updatePixels();
	  save("out.jpg");
	}
}