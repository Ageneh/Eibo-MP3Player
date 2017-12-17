package mvc.model.extension;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import mvc.view.enums.Dimensions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public abstract class ImageConverter {

    //// IMAGE CONVERTER
    public static Image convertToJavaFXImage(byte[] raw) {
        return convertToJavaFXImage(raw, Dimensions.W_COVER_IMG.intVal(), Dimensions.H_COVER_IMG.intVal());
    }

    public static Image convertToJavaFXImage(byte[] raw, final int width, final int height) {
        Image image = new WritableImage(width, height);
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(raw);
            BufferedImage read = ImageIO.read(bis);
            image = SwingFXUtils.toFXImage(read, null);
        } catch (IOException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        return image;
    }

}
