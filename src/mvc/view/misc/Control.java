package mvc.view.misc;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import mvc.model.Model;

import java.io.IOException;

public class Control extends Application {

    Model mp3;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.mp3 = new Model();
        Scene scene = null;
        AnchorPane stack;
        try {
//            stack = FXMLLoader.load(new File("Control.fxml").toURI().toURL());
            stack = FXMLLoader.load(getClass().getResource("Control.fxml"));

            ObservableList<Node> s = stack.getChildren();
            for (Node n : s){

                System.out.println(n.getId());

            }


            stack.getStylesheets().add(getClass().getResource("Stlye.css").toExternalForm());
            scene = new Scene(stack);
        } catch (IOException e) {
            e.printStackTrace();
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void play(){
        this.mp3.play("/view_assests/henock/IdeaProjects/GitHub_MP3Player/MP3_v2/playlists/Playlist.m3u");
    }

}
