package mvc.view.misc;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import mvc.model.Model;
import mvc.model.playlist.Playlist;

import java.util.ArrayList;

public class GUI extends Application {

    private Scene scene;
    private Model player;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

//        player = new Model("/view_assests/henock/IdeaProjects/GitHub_MP3Player/MP3_v2/playlists");

        StackPane pane = new StackPane();
        pane.getChildren().add(playlistView());
        scene = new Scene(pane);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private ListView playlistView(){
        class ListElements {

            int max = 30;
            ListView playlists;
            AnchorPane coverImage;

            public ListElements(){
                playlists = new ListView();
                coverImage = new AnchorPane();
            }

            protected ArrayList<HBox> createElement(ArrayList<Playlist> playlists){
                ArrayList<HBox> items = new ArrayList<>();
                HBox baseSeperator = null;

                for(Playlist playlist : playlists) {
                    if(playlist == null){
                        continue;
                    }
                    // TITLE, ARTIST AND TIME
                    Label title = new Label(playlist.getTitle());
                    Text songCount = new Text(playlist.getSongs().size() + " Songs");
                    VBox rightHeightSeperator = new VBox();
                    rightHeightSeperator.getChildren().addAll(title, songCount);

                    // BASE HBOX
                    baseSeperator = new HBox();
                    baseSeperator.setId("COVER");
                    baseSeperator.getChildren().addAll(rightHeightSeperator);

                    items.add(baseSeperator);
                }
                return items;
            }
        }

        ListView<HBox> playlists = new ListView<>();
        ListElements elements = new ListElements();

        playlists.getItems().addAll(elements.createElement(player.getPlaylists()));

        return playlists;
    }


}
