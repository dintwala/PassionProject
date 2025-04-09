package uk.ac.nulondon;

import java.awt.*;

public class Pixel {
    Pixel left;
    Pixel right;

    double energy;

    final Color color;

    public Pixel(int rgb) {
        this.color = new Color(rgb);
    }

    public Pixel(Color color) {
        this.color = color;
    }

    /**
     * Calculates brightness of pixel using RGB values
     *
     * @return brightness of this Pixel
     */
    public double brightness() {
        return (double) (color.getRed() + color.getGreen() + color.getBlue()) / 3;
    }

    public double getGreen() {
        return color.getGreen();
    }
}
