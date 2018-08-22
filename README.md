# Snapchat Text Removal
###### Remove default text from Snapchat images

This project is a quick implementation of basic computer vision concepts to identify and remove default text on Snapchat-generated images. Though the core functionality is finished, the code should still be reworked majorly. This was written before I had any experience with any professional projects, so a large of amount of necessary organization is missing. Other ideas for development are listed at the bottom of the README. Here's a quick run-down of the core functionality:

### Gray Overlay

First, I construct two row-based histograms: one for saturation and another for exposure. I then pass over these histograms and look for significant dips in saturation and exposure. When I find a large dip in both metrics at the same row, I note that as the beginning of the gray overlay. I keep iterating until I see these metrics pick up again. This would be the bottom of the gray overlay. Here is where I construct an `ArrayList` of pixels that are used as text in this overlay. 

I had taken a few samples of pixel values before and after being covered by this overlay. So, I was able to find a basic linear equation that relates original pixel values to these "grayed-out" values. So, I use this equation to revert those pixels to their original values.

### Text Pixels

I define a pixel's "valid" neighbors as the neighbors that were not used for making the white text. So, I find the number of "valid" neighbors for each text pixel and sort them in descending order. Then, I just replace each text pixel with the average of its valid neighbors. This effectively just blurs the original colors into the space that was occupied by the text. If the original image was compressed or the edges of the text were blurred over a few pixels, this algorithm can lead to some imperfect results. 


## Running the Software

The Git repository should be set up as an Eclipse project, able to imported and run immediately. Tested with Java 1.8. 

## Future Work

Future work includes a major refactoring of the codebase, including dedicated Manager classes for the gray bar and the text. The software will also be made into an executable that is able to accept command line arguments for paths to images. Perhaps it can recursively iterate over a folder and batch process many images at once? The sky is the limit for this small pet project that I will forget about in a week! 