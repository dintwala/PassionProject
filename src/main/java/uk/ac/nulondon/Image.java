package uk.ac.nulondon;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Image {
    private final List<Pixel> rows;
    private int width;
    private int height;

    public Image(BufferedImage img) {
        width = img.getWidth();
        height = img.getHeight();
        rows = new ArrayList<>();
        Pixel current = null;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Pixel pixel = new Pixel(img.getRGB(col, row));
                if (col == 0) {
                    rows.add(pixel);
                } else {
                    current.right = pixel;
                    pixel.left = current;
                }
                current = pixel;
            }
        }
    }

    public BufferedImage toBufferedImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < height; row++) {
            Pixel pixel = rows.get(row);
            int col = 0;
            while (pixel != null) {
                image.setRGB(col++, row, pixel.color.getRGB());
                pixel = pixel.right;
            }
        }
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Calculates the energy of a Pixel
     *
     * @param above   is the Pixel directly above the current pixel
     * @param current is the Pixel whose energy is being calculated
     * @param below   is the Pixel directly below the current pixel
     * @return the energy of the current pixel, given its neighbors
     */
    double energy(Pixel above, Pixel current, Pixel below) {
        if (above != null && below != null && current.left != null && current.right != null) {
            double horizEnergy = (above.left.brightness() + (2 * current.left.brightness()) + below.left.brightness()) - (above.right.brightness() + (2 * current.right.brightness()) + below.right.brightness());
            double vertEnergy = (above.left.brightness() + (2 * above.brightness()) + above.right.brightness()) - (below.left.brightness() + (2 * below.brightness()) + below.right.brightness());
            return Math.sqrt((horizEnergy * horizEnergy) + (vertEnergy * vertEnergy));
        } else {
            return current.brightness();
        }
    }

    /**
     * Calculates the energy of all pixels in the rows private variable
     */
    public void calculateEnergy() {
        Pixel above;
        Pixel current;
        Pixel below;
        if (rows.isEmpty()) {
            return;
        }
        for (int i = 0; i < rows.size(); i++) {
            current = rows.get(i);
            if (i == 0) {
                above = null;
                below = rows.get(i + 1);
                for (int k = 0; k < width; k++) {
                    current.energy = energy(above, current, below);
                    current = current.right;
                }
            } else if (i == rows.size() - 1) {
                above = rows.get(i - 1);
                below = null;
                for (int k = 0; k < width; k++) {
                    current.energy = energy(above, current, below);
                    current = current.right;
                }
            } else {
                above = rows.get(i - 1);
                below = rows.get(i + 1);
                for (int k = 0; k < width; k++) {
                    current.energy = energy(above, current, below);
                    current = current.right;
                    above = above.right;
                    below = below.right;
                }
            }
        }
    }

    /**
     * Highlights the given seam with the given color
     *
     * @param seam  is the seam that's meant to be highlighted
     * @param color is the color used to highlight the given seam
     * @return returns the original un-highlighted seam
     */
    public List<Pixel> higlightSeam(List<Pixel> seam, Color color) {
        for (int i = 0; i < seam.size(); i++) {
            Pixel original = seam.get(i);
            Pixel replacement = new Pixel(color);
            replacement.left = original.left;
            replacement.right = original.right;
            if (original.left != null) {
                original.left.right = replacement;
            } else {
                // First pixel in the row — update rows directly using i
                rows.set(i, replacement);
            }
            if (original.right != null) {
                original.right.left = replacement;
            }
        }
        return seam;
    }

    /**
     * Removes the given seam
     *
     * @param seam is the given seam meant to be removed
     */
    public void removeSeam(List<Pixel> seam) {
        width--;
        for (int row = 0; row < seam.size(); row++) {
            Pixel pixel = seam.get(row);
            if (pixel.left != null) {
                pixel.left.right = pixel.right;
            } else {
                rows.set(row, pixel.right);
            }
            if (pixel.right != null) {
                pixel.right.left = pixel.left;
            }
        }
    }

    /**
     * Adds the given seam
     *
     * @param seam is the given seam meant to be added
     */
    public void addSeam(List<Pixel> seam) {
        width++;
        for (int row = 0; row < seam.size(); row++) {
            Pixel seamPixel = seam.get(row);
            Pixel rowHead = rows.get(row);
            if (seamPixel.left == null) {
                seamPixel.right = rowHead;
                if (rowHead != null) rowHead.left = seamPixel;
                rows.set(row, seamPixel);
            } else {
                Pixel current = seamPixel.left.right;
                seamPixel.left.right = seamPixel;
                seamPixel.right = current;
                if (current != null) current.left = seamPixel;
            }
        }
    }

    /**
     * Calculates the vertical seam that has the max cumulative energy value based on a provided value function
     *
     * @param valueGetter a function that takes a Pixel and returns a double value
     *                    representing the pixel's contribution to the seam (e.g., green value or energy)
     * @return the list of Pixels representing the seam with the highest total energy value
     */
    private List<Pixel> getSeamMaximizing(Function<Pixel, Double> valueGetter) {
        double[] previousValues = new double[width];
        double[] currentValues = new double[width];
        List<List<Pixel>> previousSeams = new ArrayList<>();
        List<List<Pixel>> currentSeams = new ArrayList<>();
        Pixel currentPixel = rows.getFirst();
        int col = 0;
        while (currentPixel != null) {
            previousValues[col] = valueGetter.apply(currentPixel);
            List<Pixel> seam = new ArrayList<>();
            seam.add(currentPixel);
            previousSeams.add(seam);
            currentPixel = currentPixel.right;
            col++;
        }

        for (int row = 1; row < height; row++) {
            currentPixel = rows.get(row);
            col = 0;
            while (currentPixel != null) {
                double max = previousValues[col];
                int ref = col;
                if (col > 0 && previousValues[col - 1] > max) {
                    max = previousValues[col - 1];
                    ref = col - 1;
                }
                if (col < width - 1 && previousValues[col + 1] > max) {
                    max = previousValues[col + 1];
                    ref = col + 1;
                }
                currentValues[col] = max + valueGetter.apply(currentPixel);
                List<Pixel> newSeam = new ArrayList<>(previousSeams.get(ref));
                newSeam.add(currentPixel);
                currentSeams.add(newSeam);
                col++;
                currentPixel = currentPixel.right;
            }
            previousValues = currentValues;
            currentValues = new double[width];
            previousSeams = currentSeams;
            currentSeams = new ArrayList<>();
        }

        // Find the maximum value from the last row of previousValues
        double maxValue = previousValues[0];
        int maxValueIndex = 0;
        for (int i = 1; i < width; i++) {
            if (previousValues[i] > maxValue) {
                maxValue = previousValues[i];
                maxValueIndex = i;
            }
        }

        return previousSeams.get(maxValueIndex);
    }

    public List<Pixel> getGreenestSeam() {
        return getSeamMaximizing(Pixel::getGreen);
    }

    public List<Pixel> getLowestEnergySeam() {
        calculateEnergy();
        return getSeamMaximizing(pixel -> -pixel.energy);
    }
}
