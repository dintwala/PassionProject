package uk.ac.nulondon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class ImageEditor {
    private final Deque<Command> actionDeque = new ArrayDeque<>();
    private Image image;
    private int counter = 1;
    private List<Pixel> lastHighlightedSeam;
    private Color lastHighlightColor;

    public void load(String filePath) throws IOException {
        File originalFile = new File(filePath);
        BufferedImage img = ImageIO.read(originalFile);
        image = new Image(img);
    }

    public void save(String filePath) throws IOException {
        BufferedImage img = image.toBufferedImage();
        ImageIO.write(img, "png", new File(filePath));
    }

    /**
     * Highlights the greenest column in the image using command pattern, pushes action to deque, and creates updated new image in target directory.
     *
     * @throws IOException if any errors occur with image
     **/
    public void highlightGreenest() throws IOException {
        try {
            List<Pixel> seam = image.getGreenestSeam();
            Command highlightGreenest = new HighlightGreenest(seam);
            highlightGreenest.execute();
            actionDeque.push(highlightGreenest);
            lastHighlightedSeam = seam;
            lastHighlightColor = Color.GREEN;
            save("target/tmp" + counter++ + ".png");
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Highlights the column with the lowest energy in the image using command pattern, pushes action to deque, and creates new updated image in target directory.
     *
     * @throws IOException if any errors occur with image
     **/
    public void highlightLowestEnergySeam() throws IOException {
        try {
            List<Pixel> seam = image.getLowestEnergySeam();
            Command highlight = new HighlightLowestEnergySeam(seam);
            highlight.execute();
            actionDeque.push(highlight);
            lastHighlightedSeam = seam;
            lastHighlightColor = Color.RED;
            save("target/tmp" + counter++ + ".png");
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Removes highlighted seam, as long there is one, pushes action to deque, and saves updated image in target directory.
     *
     * @throws IOException           if any errors occur with image
     * @throws IllegalStateException if there is no highlighted seam
     **/
    public void removeHighlighted() throws IOException, IllegalStateException {
        try {
            if (lastHighlightedSeam == null || lastHighlightColor == null) {
                throw new IllegalStateException("No highlighted seam to remove.");
            }
            Command remove = new RemoveHighlighted(lastHighlightedSeam, lastHighlightColor);
            remove.execute();
            actionDeque.push(remove);
            save("target/tmp" + counter++ + ".png");
            lastHighlightedSeam = null;
            lastHighlightColor = null;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Undoes most recent action by accessing deque, and produces updated image in target directory.
     *
     * @throws IOException if any errors occur with image
     **/
    public void undo() throws IOException {
        try {
            if (!actionDeque.isEmpty()) {
                Command command = actionDeque.pop();
                command.undo();
                save("target/tmp" + counter++ + ".png");
            } else {
                System.out.println("Nothing to undo");
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Command interface that defines execute and undo operations for implementations of Command interface.
     */
    interface Command {
        /**
         * Executes a specific command
         */
        void execute();

        /**
         * Undoes the most recent command
         */
        void undo();
    }

    /**
     * Implementation class of Command that highlights the greenest seam in the image.
     */
    class HighlightGreenest implements Command {
        private final List<Pixel> seam;

        /**
         * Constructs a HighlightGreenest command with the specified seam.
         *
         * @param seam the list of pixels representing the greenest seam to highlight
         */
        public HighlightGreenest(List<Pixel> seam) {
            this.seam = seam;
        }

        /**
         * Executes the command by highlighting the greenest seam in the image.
         */
        @Override
        public void execute() {
            image.higlightSeam(seam, Color.GREEN);
        }

        /**
         * Undoes the highlighting operation by removing the highlighted seam and adding the original pixels back.
         */
        @Override
        public void undo() {
            image.removeSeam(seam);
            image.addSeam(seam);
        }
    }

    /**
     * Implementation class of Command that highlights the seam with the lowest energy in the image.
     */
    class HighlightLowestEnergySeam implements Command {
        private final List<Pixel> seam;

        /**
         * Constructs a HighlightLowestEnergySeam command with the specified seam.
         *
         * @param seam the list of pixels representing the lowest energy seam to highlight
         */
        public HighlightLowestEnergySeam(List<Pixel> seam) {
            this.seam = seam;
        }

        /**
         * Executes the command by highlighting the lowest energy seam in the image.
         */
        @Override
        public void execute() {
            image.higlightSeam(seam, Color.RED);
        }

        /**
         * Undoes the highlighting operation by removing the highlighted seam and adding the original pixels back.
         */
        @Override
        public void undo() {
            image.removeSeam(seam);
            image.addSeam(seam);
        }
    }

    /**
     * Implementation class of Command that removes a highlighted/specified seam in the image.
     */
    class RemoveHighlighted implements Command {
        private final List<Pixel> seam;
        private final Color color;

        /**
         * Constructs a RemoveHighlighted command with the specified seam and color.
         *
         * @param seam  the list of pixels representing the seam to remove
         * @param color the color of the highlighted seam to restore when undoing
         */
        public RemoveHighlighted(List<Pixel> seam, Color color) {
            this.seam = seam;
            this.color = color;
        }

        /**
         * Executes the command by removing the highlighted seam from the image.
         */
        @Override
        public void execute() {
            image.removeSeam(seam);
        }

        /**
         * Undoes the removal operation by adding the seam back and re-highlighting it with the specified color.
         */
        @Override
        public void undo() {
            image.addSeam(seam);
            image.higlightSeam(seam, color);
        }
    }
}
