package mvc.view.custom_elements;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import mvc.model.playlist.Playlist;
import mvc.view.enums.Dim;

public class PlistListCell extends GridPane {

    private Label plisstTitle;
    private Playlist playlist;
    private ImageView coverSm;
    private Label songCountLabel;
    private PlaylistViewCell cellBox = null;

    public PlistListCell(Playlist playlist){
        this.playlist = playlist;
        this.plisstTitle = new Label(playlist.getTitle());
        this.plisstTitle.setId("playlistTitle");
        this.plisstTitle.setStyle("-fx-font-family: \"SF Pro Display Medium\";\n" +
                "    -fx-font-style: oblique;");
        this.coverSm = new ImageView(new Image("/mvc/view/stylesheets/icons/playlist.png"));
        this.coverSm.setPreserveRatio(true);
        this.coverSm.setFitWidth(Dim.H_LIST_CELL.intVal() - 10);
        this.coverSm.setSmooth(true);
        this.coverSm.setOpacity(0.5);
        this.songCountLabel = new Label();
        this.createSongLabel();
        this.songCountLabel.setStyle("-fx-font-family: \"SF Pro Display Light\";\n" +
                "    -fx-font-size: 10pt;\n");
        this.setupGrid();
    }

    private void setupGrid(){
        this.cellBox = new PlaylistViewCell(playlist);
        this.cellBox.setId("cellbox");

        this.cellBox.setGridLinesVisible(false);
        this.cellBox.setPadding(new Insets(0));
        this.cellBox.addColumn(0, this.coverSm);
        this.cellBox.setHgap(25);
        this.cellBox.addColumn(1, this.plisstTitle);
        this.cellBox.setHgap(10);
        this.cellBox.addColumn(2, this.songCountLabel);
        this.cellBox.setMinWidth(250);
    }

    public PlaylistViewCell getGrid() {
        return cellBox;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public ImageView getCover(){
        return this.coverSm;

    }

    public Label getSongCount(){
        return this.songCountLabel;
    }

    private void createSongLabel(){
        this.songCountLabel.setText(this.playlist.getSongs().size() + "");
    }

    public Label getPlisstTitle() {
        this.plisstTitle.setMinWidth(10);
        return this.plisstTitle;
    }

}
