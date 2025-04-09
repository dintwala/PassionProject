# AE3 - Image Compression Project using Seams

# Contributors

**Dylan Intwala, Sasmit Munagala, Ansh Rai**

# Description

This is an image editor that allows a user to remove seams, or consistent vertical patterns of pixels, from an image,
based on if they are the greenest seams, or the seams with the lowest energies (based on their RGB values). The project
also allows users to undo their last function.

# Functions

**Remove Greenest Seam**: Identifies and removes the seam with the highest green values.

**Remove Lowest Energy Seam**: Idenfities and removes the seam with the lowest energy, based on the pixel's RGB values.

**Undo**: Reverts the last operation performed.

# Files

**Main.java**: Contains the main program logic and user interface.

**ImageEditor.java**: Handles the image editing operations like removing seams, undo, and saving the image.

**Pixel.java**: Represents the individual pixels in the image.

**Image.java**: Contains the logic for managing the image, calculating energy, and handling the seams.

# Installation

Download and unzip the given zip file, open it in IntelliJ, and run the main method in the Main class

Java version 21

Generated at 2024-02-14 10:04:00



