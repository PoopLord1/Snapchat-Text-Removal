import java.util.ArrayList;
import java.util.HashMap;

/* 
 * A class that describes a Pixel that belongs to the text in a Snapchat-generated image.
 */
class TextPix implements Comparable<TextPix> {
  int xLocation;
  int yLocation;
  int imgWidth;
  HashMap<Integer, Integer> selfPixelHash;
  
  public TextPix(int xLocation, int yLocation, HashMap<Integer, Integer> ph, int imgWidth) {
	  this.xLocation = xLocation;
	  this.yLocation = yLocation;
    this.selfPixelHash = ph;
    this.imgWidth = imgWidth;
  }

  public int getXLocation() {
    return xLocation;
  }
  
  public int getYLocation() {
    return yLocation;
  }

  public void setSelfPixelHash(HashMap<Integer, Integer> h) {
    this.selfPixelHash = h;
  }

  //Returns an ArrayList of locations in a 5x5 area that were not used in the image's text.  
  public ArrayList<Integer> getRelevantPixels(HashMap<Integer, Integer> pixelHash) {
    int[] dx = {-2, -1, 0, 1, 2};
    int[] dy = {-2, -1, 0, 1, 2};
    
    if (pixelHash == null) {
      pixelHash = selfPixelHash;
    }
    
    //Make an ArrayList of locations for each pixel.
    ArrayList<Integer> pixs = new ArrayList<Integer>();
    //First, iterate over the neighboring pixels. 
    for (int a : dx) {
      for (int b : dy) {
        int currPixelX = getXLocation() + a;
        int currPixelY = getYLocation() + b;
        Integer i = pixelHash.get(loc);
        //If it isn't in the textpixels array, we use it to compute the new value.
        if ((i == null) || (i == 0)) {
          pixs.add(loc);
        }
      }
    }
    return pixs;
  }
  
  //get number of relevant pixels
  public int getNumRelevantPixels(HashMap<Integer, Integer> pixelHash) {
    return getRelevantPixels(pixelHash).size();
  }

  // Compare one pixel to another
  public int compareTo(TextPix o) {
    return this.getNumRelevantPixels(null) - ((TextPix) o).getNumRelevantPixels(null); 
  }
}
