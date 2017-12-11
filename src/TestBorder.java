import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import mvc.model.MP3Player;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TestBorder extends Application {

    ImageView coverImg;
    MP3Player mp3;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
//        mp3 = new MP3Player("/view_assests/henock/IdeaProjects/GitHub_MP3Player/MP3_v2/playlists/temp");
        BorderPane root = new BorderPane();

//        root.setBottom(new HBox(new Label("BOTTOM")));
//        root.getBottom().setStyle("-fx-background-color: aqua;");
////        ImageView a = new ImageView((Element) convertToJavaFXImage(mp3.getPlaylists().get(1).readSongs().get(2).getCover(), 300, 300));A
//        ArrayList<Song> ar = mp3.getPlaylists().get(0).readSongs();
//        byte[] br = ar.get(2).getCover();
//        Image a = convertToJavaFXImage(mp3.getPlaylists().get(1).readSongs().get(0).getCover(), 300, 300);
//        ImageView aView = new ImageView(a);
//        aView.setPreserveRatio(true);
//        root.setCenter(aView);

        HBox hbox = new HBox();
        root.getChildren().add(hbox);

        primaryStage.setScene(new Scene(root, 500, 375));
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(375);
        primaryStage.show();

        ImageView im = new ImageView();
        im.setPreserveRatio(true);
        im.setFitWidth(primaryStage.getWidth());
        im.setFitHeight(primaryStage.getHeight());
//        im.setViewport(new Rectangle2D(0, 0, 300, 150));
        im.setViewport(new Rectangle2D(0, 100, im.getBoundsInParent().getWidth(), im.getFitHeight()));
        Button next = new Button("KJNSCKA");
        int max = mp3.getCurrentSongs().size();
        next.setOnMouseClicked(
                new EventHandler<MouseEvent>() {
                    int i = 0;
                    @Override
                    public void handle(MouseEvent event) {
                        im.setImage(convertToJavaFXImage(mp3.getCurrentSongs().get((i++) % (max - 1)).getCover(), 300, 300));
                    }
                }
        );
        hbox.getChildren().add(next);
        hbox.getChildren().add(im);

        primaryStage.getScene().heightProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                        aView.setFitHeight(root.getHeight() - 100);
                        im.setFitHeight(newValue.doubleValue());
//                        im.setViewport(new Rectangle2D(0, 0, 300, 250));
                    }
                }
        );

        primaryStage.getScene().widthProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                        aView.setFitWidth(root.getWidth() - 100);
                        im.setFitWidth(newValue.doubleValue());
                    }
                }
        );

    }

    private static Image convertToJavaFXImage(byte[] raw, final int width, final int height) {
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
