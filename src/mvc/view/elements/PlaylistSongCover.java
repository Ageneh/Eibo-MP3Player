package mvc.view.elements;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.swing.text.Element;
import java.awt.image.BufferedImage;

public class PlaylistSongCover extends ImageView {

    final double maxHeight = 500;

    public PlaylistSongCover(){
        super();
        this.maxHeight(maxHeight);
    }
    public PlaylistSongCover(String url){
//            super(url);
        this();
        this.setImage(new Image(url));
    }

    public PlaylistSongCover(BufferedImage image){
        super(new Image(String.valueOf(image)));

        this.maxHeight(maxHeight);
    }


    public double getScaleXRatio(){
        return this.getBoundsInParent().getWidth() / this.getImage().getWidth();
    }

    public double getScaleYRatio(){
        return this.getBoundsInParent().getHeight() / this.getImage().getHeight();
    }

}
