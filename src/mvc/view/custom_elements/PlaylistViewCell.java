package mvc.view.custom_elements;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import mvc.model.playlist.Playlist;

public class PlaylistViewCell extends GridPane {

    private Playlist playlist;
    private Label title;
    private GridPane cellBox = null;

    public PlaylistViewCell(Playlist playlist){
        this.playlist = playlist;
        this.title = new Label(playlist.getTitle());
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public Label getTitle() {
        return title;
    }

}
