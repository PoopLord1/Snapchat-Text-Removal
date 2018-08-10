import java.util.*;

public class RemoveText {
	private PImage origImage = null;
	
	void setup() {
	  size(750, 1334);
	  origImage = loadImage("input_data/image3.PNG");
	  surface.setSize(origImage.width, origImage.height);
	  process();
	}
	
	
	//Simply copies the original snapchat image into the window.
	void copyImage() {
	  for (int i=0; i < origImage.pixels.length; i++) {
	    pixels[i] = origImage.pixels[i];
	  }
	}
	
	
	//identifyBar - returns two integer values - the start and end pixels of the snapchat text bar.
	//make sure to search for rows that show a drop in exposure and variance.
	int[] identifyBar() {
	  float[] saturationLevels = new float[origImage.height];
	  float[] exposureLevels = new float[origImage.height];
	  
	  int[] ret = {0, 0};
	  
	  //First, gather the saturation and exposure levels.
	  for (int y=0; y<origImage.height; y++) {
	    
	    float rowSaturation = 0.0;
	    float rowExposure = 0.0;
	    for (int x=0; x<origImage.width; x++) {
	      int location = x + y * origImage.width;
	      float r = red(origImage.pixels[location]);
	      float g = green(origImage.pixels[location]);
	      float b = blue(origImage.pixels[location]);
	      rowExposure += r + b + g;
	      
	      float avg = (r + b + g) / 3;
	      rowSaturation += abs(r - avg);
	      rowSaturation += abs(b - avg);
	      rowSaturation += abs(g - avg);
	    }
	    
	    saturationLevels[y] = rowSaturation / origImage.width;
	    exposureLevels[y] = rowExposure / (3*origImage.width);
	  }
	  
	  
	  //Now run a window down the image and try to find the black bar.
	  //We need a drop in exposure and a drop in saturation to detect it.
	  for (int yi=0; yi<origImage.height-30; yi++) {
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
	Integer[] removeBar(int starty, int endy) {
	  //The brightest we can get is (102, 102, 102) with the bar. 
	  //Anything above that is considered text.
	  ArrayList<Integer> textPix = new ArrayList<Integer>();
	  for (int y=starty; y<endy; y++) {
	    for (int x=0; x<origImage.width; x++) {
	      int location = x + y*origImage.width;
	      float r = red(origImage.pixels[location]);
	      float g = green(origImage.pixels[location]);
	      float b = blue(origImage.pixels[location]);
	      double avg = (r + g + b) / 3;
	      if (avg > 104.0) {
	        textPix.add(location);
	      }
	    }
	  }
	  
	  //Now remove all of the gray from the pixels 
	  for (int y=starty; y<endy; y++) {
	    for (int x=0; x<origImage.width; x++) {
	       int location = x + y*origImage.width;
	       if (!(textPix.contains(location))) {
	         //The regression is: Y = 0.36176*X + 5.1657
	         //(Where X is the brighter value and Y is the darker one.)
	         double r = red(origImage.pixels[location]);
	         r = (r - 5.388474) / .356637;
	         double g = green(origImage.pixels[location]);
	         g = (g - 5.388474) / .356637;
	         double b = blue(origImage.pixels[location]);
	         b = (b - 5.388474) / .356637;
	         pixels[location] = color((int) r, (int) g, (int) b);
	       }
	    }
	  }
	  
	  //And return the array of white pixels.
	  Integer[] pixArr = new Integer[textPix.size()];
	  pixArr = textPix.toArray(pixArr);
	  return pixArr;
	}
	
	//Remove text - continuously loops over the given list of text pixels. Each pixel is popped and set equal
	//to the average of some of its neighbors.
	void removeText(Integer[] pixs) {
	  
	  //Create a new hash that stores the locations of the text pixels. 
	  //We also add all 8 neighbors to the Hashmap.
	  HashMap<Integer,Integer> presenceHash = new HashMap<Integer,Integer>();
	  for (Integer i : pixs) {
	    int[] dx = {-2, -1, 0, 1, 2};
	    int[] dy = dx;
	    for (int y : dy) {
	      for (int x : dx) {
	        int newLoc = i + x + y*origImage.width;
	        presenceHash.put(newLoc, 1);
	      }
	    }
	  }
	  
	  //Create an ArrayList of TextPix objects and their neighbors.
	  ArrayList<TextPix> textpixs = new ArrayList<TextPix>();
	  for (Integer i : pixs) {
	    int[] dx = {-2, -1, 0, 1, 2};
	    int[] dy = dx;
	    for (int y : dy) {
	      for (int x : dx) {
	        int newLoc = i + x + y*origImage.width;
	        textpixs.add(new TextPix(newLoc, presenceHash, origImage.width));
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
	      pixels[t.getLocation()] = color(r, g, b);
	      
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
	  ys[0] = ys[0]+2;
	  ys[1] = ys[1]+1;
	  for (int x=0; x < origImage.width; x++) {
	    //Find the average in a 5x1 window centered at each pixel.
	    int[] dys = {-3, 3};
	
	    int r=0, g=0, b=0;
	    for (int dy : dys) {
	      int loc = x + ys[0]*origImage.width + dy*origImage.width;
	      r += 0.5 * red(pixels[loc]);
	      g += 0.5 * green(pixels[loc]);
	      b += 0.5 * blue(pixels[loc]);
	    }
	    int location1 = x + ys[0]*origImage.width;
	    pixels[location1] = color(r,g,b);
	    pixels[location1+origImage.width] = color(r,g,b);
	    pixels[location1-origImage.width] = color(r,g,b);
	    
	    r=0;
	    g=0;
	    b=0;
	    for (int dy : dys) {
	      int loc = x + ys[1]*origImage.width + dy*origImage.width;
	      r += 0.5 * red(pixels[loc]);
	      g += 0.5 * green(pixels[loc]);
	      b += 0.5 * blue(pixels[loc]);
	    }
	    int location2 = x + ys[1]*origImage.width;
	    pixels[location2] = color(r,g,b);
	    pixels[location2+origImage.width] = color(r,g,b);
	    pixels[location2-origImage.width] = color(r,g,b);
	  }
	}
	
	
	void process() {
	  loadPixels();
	  copyImage();
	  int[] ys = identifyBar();
	  
	  Integer[] textpix = removeBar(ys[0]+3, ys[1]+1);
	  
	  removeText(textpix);
	  
	  removeLines(ys);  
	  
	  updatePixels();
	  save("out.jpg");
	}
}