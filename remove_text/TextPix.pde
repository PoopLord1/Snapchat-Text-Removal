class TextPixel implements Comparable {
  int location;
  int imgWidth;
  HashMap<Integer, Integer> selfPixelHash;
  
  public TextPixel(int location, HashMap<Integer, Integer> ph, int imgWidth) {
    this.location = location;
    this.selfPixelHash = ph;
    this.imgWidth = imgWidth;
  }

  public int getLocation() {
    return location;
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
        int loc = location + a + b*imgWidth;
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

  int compareTo(Object o) {
    if (o.getClass() != this.getClass()) {
        throw new IllegalArgumentException();
    } else {
        return this.getNumRelevantPixels(null) - ((TextPix) o).getNumRelevantPixels(null); 
    }
  }
}
