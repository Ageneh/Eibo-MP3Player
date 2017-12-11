package mvc.model.extension.m3u;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import mvc.view.enums.Dim;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public abstract class ImageConverter {

    //// IMAGE CONVERTER
    public static Image convertToJavaFXImage(byte[] raw) {
        return convertToJavaFXImage(raw, Dim.W_COVER_IMG.doubleVal(), Dim.H_COVER_IMG.doubleVal());
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
