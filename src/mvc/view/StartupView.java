package mvc.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import misc.ANSI;
import misc.TimeConverter;
import mvc.controller.Controller;
import mvc.model.extension.enums.Skip;
import mvc.model.playlist.Playlist;
import mvc.model.playlist.Song;
import mvc.view.custom_elements.ControlButton;
import mvc.view.custom_elements.PlaylistSongCover;
import mvc.view.custom_elements.PlaylistViewCell;
import mvc.view.custom_elements.PlistListCell;
import mvc.view.enums.Dim;

import java.util.Observable;
import java.util.Observer;

public class StartupView extends Application implements Observer {

    /////////////////////// VARIABLES

    private final double MIN_OPACITY = 0.4;

    private Controller controller;

    private String stageTitle;
    private Scene baseScene;
    private BorderPane root;

    private VBox leftPanel;
    private ListView<PlaylistViewCell> playlistListView;
    private ObservableList<Playlist> allPlaylists;
    private SimpleStringProperty currentPlaylistTitle;

    private PlaylistSongCover currentCoverImg;
    private TableView<Song> currentSongsTable;
    private SimpleStringProperty currentSongTitle;
    private SimpleStringProperty currentSongArtist;
    private SimpleStringProperty currentSongAlbum;
    private SimpleStringProperty play_pauseStyle;
    /**
     * String property, used for binding and updates of certain custom_elements.
     * <br>Will save the current position of the {@link mvc.model.MP3Player#player player}.</br>
     * @see #setUpBottomPane()
     * @see #updateBottom()
     */
    private HBox bottomPane;
    private SimpleLongProperty currentPosPropVal;
    private SimpleStringProperty currentPos;
    private SimpleLongProperty songLength;
    private SimpleDoubleProperty bottomWidth;
    /**
     * String property, used for binding and updates of certain custom_elements.
     * {@link Controller#getCurrentSongLength()}
     * @see #setUpBottomPane()
     * @see #updateBottom()
     */
    private SimpleStringProperty currentSongLength;
    private SimpleFloatProperty volumeValue;
    private SimpleDoubleProperty sceneOpacity;



    /////////////////////// CONSTRUCTOR

    public StartupView(){
        this("MP3-Player");
    }

    public StartupView(String stageTitle){
        controller = new Controller();
        controller.addObserver(this);

        this.stageTitle = stageTitle;
        this.allPlaylists = this.controller.getPlaylistData();
        this.currentCoverImg = new PlaylistSongCover();
        this.currentPosPropVal = new SimpleLongProperty(
                controller.getCurrentSongPosition()
        );
        this.currentSongLength = new SimpleStringProperty();
        this.volumeValue = new SimpleFloatProperty(this.controller.getVolume());
        this.currentSongTitle = new SimpleStringProperty(
                controller.getCurrentSong().getTitle()
        );
        this.currentSongAlbum = new SimpleStringProperty(
                controller.getCurrentSong().getAlbum()
        );
        this.currentSongArtist = new SimpleStringProperty(
                controller.getCurrentSong().getArtist()
        );
        this.songLength = new SimpleLongProperty(
                controller.getCurrentSong().getLengthMillis()
        );
        play_pauseStyle = new SimpleStringProperty("play");
    }


    /////////////////////// PUBLIC METHODS

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        this.sceneOpacity = new SimpleDoubleProperty(1);

        //// BORDER PANE PROPERTIES
        this.root = new BorderPane();
        this.root.setRight(null);
        this.root.setTop(null);

        //// SETTING UP STAGE AND ROOT PANE
        this.baseScene = new Scene(
                this.root,
                Dim.MIN_W_SCREEN.intVal(),
                Dim.MIN_H_SCREEN.intVal()
        );

        ////// LEFT PANEL AND CONTROLS
        this.setUpLeftPanel();

        //// SETTING UP CURRENT PLAYBACK
        this.root.setCenter(
                this.setUpRightView()
        );

        ////// BOTTOM PANEL AND CONTROLS
        this.bottomPane = setUpBottomPane();
        bottomPane.setAlignment(Pos.CENTER_LEFT);
        this.root.setBottom(bottomPane);
        this.root.getBottom().setStyle("-fx-padding: " + Dim.PAD_PLAYLIST_WINDOW.intVal() + ", 0, 0, 0;");

        //// PARSE STYLESHEET
        this.baseScene.getStylesheets().add(getClass().getResource("/mvc/view/stylesheets/styles.css").toExternalForm());

        primaryStage.setScene(this.baseScene);
        primaryStage.setTitle(this.stageTitle);
        primaryStage.setMinHeight(Dim.MIN_H_SCREEN.intVal());
        primaryStage.setMinWidth(Dim.MIN_W_SCREEN.intVal());
        primaryStage.setMaxWidth(Screen.getPrimary().getVisualBounds().getWidth());
        primaryStage.setMaxHeight(Screen.getPrimary().getVisualBounds().getHeight());
        primaryStage.setOnCloseRequest(
                event -> System.exit(0)
        );
        primaryStage.centerOnScreen();
        primaryStage.sizeToScene();
        primaryStage.setWidth(baseScene.getWidth());
        primaryStage.setHeight(baseScene.getHeight());
        primaryStage.opacityProperty().bind(this.sceneOpacity);
        primaryStage.show();

        this.update(null, null);

        //// SETTING UP LISTENERS
        this.setUpListeners();
    }


    /////////////////////// PRIVATE METHODS

    private void setUpListeners(){
        baseScene.widthProperty().addListener(
                (observable, oldValue, newValue) -> {
                    currentCoverImg.setFitWidth(
                            newValue.doubleValue() -
                                    root.getLeft().getBoundsInParent().getWidth()
                    );
                    currentSongsTable.setPrefWidth(
                            root.getWidth()
                                    - root.getLeft().getBoundsInParent().getWidth()
                                    - root.getLeft().getBoundsInParent().getWidth()
                    );
                    currentSongsTable.setPrefHeight(
                            root.getHeight()
                                    - Dim.H_BORDERP_BOTTOM.intVal()
                                    - currentCoverImg.getBoundsInParent().getHeight()
                    );
                    bottomPane.resize(newValue.doubleValue(), baseScene.getHeight());
                    bottomPane.setMinWidth(newValue.doubleValue());
                    bottomPane.setPrefWidth(newValue.doubleValue());
                    System.out.println(bottomPane.getWidth());
                }
        );
        baseScene.heightProperty().addListener(
                (observable, oldValue, newValue) -> {
                    playlistListView.setPrefHeight(
                            root.getHeight()
                                    - Dim.H_BORDERP_BOTTOM.intVal()
                                    - Dim.H_LEFT_PANEL_HEADER.doubleVal()
                    );

                    currentSongsTable.setPrefHeight(
                            root.getHeight()
                                    - Dim.H_BORDERP_BOTTOM.intVal()
                                    - currentCoverImg.getBoundsInParent().getHeight()
                    );
                }
        );
        playlistListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if(newValue != null){
                        ANSI.YELLOW.println("NEW PLAYLIST " + newValue.getTitle());
                        playlistListView.getFocusModel().focus(playlistListView.getSelectionModel().getSelectedIndex());
                        updateSongTable(
                                newValue.getPlaylist()
                        );
                        controller.setSelectedPlaylist(newValue.getPlaylist());
                        playlistListView.refresh();
                    }
                }
        );
        currentSongsTable.setOnMouseClicked(
                event -> {
                    if (event.getClickCount() == 2) {
                        // load the playlist and its first song
                        controller.playCurrentSong(
                                currentSongsTable.getSelectionModel().getSelectedItem()
                        );
                        currentSongsTable.refresh();

                        updateCurrentProperties();
                    }
                    else {
                        currentSongsTable.getSelectionModel().select(
                                currentSongsTable.getSelectionModel().getFocusedIndex()
                        );
                    }
                }
        );
    }

    private void setUpLeftPanel(){
        //// HEADER
        Text leftPanelHeaderLabel = new Text("Meine Playlists");
        leftPanelHeaderLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        leftPanelHeaderLabel.setTextAlignment(TextAlignment.LEFT);

        Label playlistCount = new Label();
        IntegerBinding plistCount = Bindings.size(this.allPlaylists);
        playlistCount.setAlignment(Pos.CENTER_RIGHT);
        playlistCount.textProperty().bind(StringBinding.stringExpression(plistCount));

        AnchorPane headerRow1 = new AnchorPane(leftPanelHeaderLabel, playlistCount);
        headerRow1.setPadding(
                new Insets(Dim.PAD_SIDE_LIST.intVal())
        );
        headerRow1.setMinHeight(Dim.H_LEFT_PANEL_HEADER.doubleVal());
        headerRow1.maxHeight(Dim.H_LEFT_PANEL_HEADER.doubleVal());
        headerRow1.setLeftAnchor(leftPanelHeaderLabel, 0.0);
        headerRow1.setTopAnchor(leftPanelHeaderLabel, 0.0);
        headerRow1.setRightAnchor(playlistCount, 0.0);
        headerRow1.setTopAnchor(playlistCount, 0.0);

        //// SECOND ROW
        ControlButton addPlaylist = new ControlButton("");
        addPlaylist.setId("newPlaylist");
        addPlaylist.setOnMouseClicked(
                event -> {
                    sceneOpacity.set(0.8);
                    addPlaylist.setDisable(true);
                    controller.addSongs();
                    addPlaylist.setDisable(false);
                    sceneOpacity.set(1);
                }
        );
        addPlaylist.setOpacity(MIN_OPACITY);
        addPlaylist.setWrapText(false);
        addPlaylist.setMinWidth(Dim.SIZE_CTRL_BTN.intVal());
        StackPane addPlaylistBox = new StackPane(addPlaylist);
        addPlaylistBox.setMaxHeight(30);
        addPlaylistBox.setPadding(
                new Insets(0, 0, Dim.PAD_SIDE_LIST.intVal(), 0)
        );

        VBox leftPanelHeader = new VBox();
        leftPanelHeader.setFillWidth(true);
        leftPanelHeader.setAlignment(Pos.TOP_CENTER);
        leftPanelHeader.getChildren().add(headerRow1);
        leftPanelHeader.getChildren().add(addPlaylistBox);

        leftPanel = new VBox();
        leftPanel.getChildren().add(leftPanelHeader);

        this.playlistListView = new ListView<>();
        this.playlistListView.setEditable(false);
        this.playlistListView.setFixedCellSize(Dim.H_LIST_CELL.intVal());
        this.playlistListView.setPadding(new Insets(0));
        VBox.setVgrow(playlistListView, Priority.ALWAYS);

        this.setPlaylistViewCells();
        this.currentPlaylistTitle = new SimpleStringProperty();
        try {
            System.out.println(this.playlistListView.getItems().get(0).getPlaylist().getTitle());
            System.out.println(controller.getSelectedPlaylist().getTitle());
            this.currentPlaylistTitle.set(
                    controller.getSelectedPlaylist().getTitle()
            );
        }
        catch (IndexOutOfBoundsException e){
            System.out.println();
        }


        leftPanel.getChildren().add(this.playlistListView);
        this.root.setLeft(leftPanel);
    }

    private void setPlaylistViewCells(){
        PlistListCell cellBox;
        this.playlistListView.getItems().clear();

        for (Playlist playlist : allPlaylists){
            if(playlist == null){
                continue;
            }
            cellBox = new PlistListCell(playlist);
            cellBox.setId("cellbox");
            this.playlistListView.getItems().add(cellBox.getGrid());
        }
        playlistListView.getSelectionModel().select(0);
        this.playlistListView.refresh();
    }

    /**
     * This method creates a {@link TableView table}, which shows all songs, of the current playlist.
     */
    private void setSongTable(){
        TableColumn<Song, String> songNr = new TableColumn<>("Nr.");
        songNr.setMinWidth(40);
        songNr.setMaxWidth(40);
        songNr.setCellValueFactory(new PropertyValueFactory<>("songNr"));

        TableColumn<Song, String> title = new TableColumn<>("Titel");
        title.setMinWidth(300);
        title.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Song, String> artist = new TableColumn<>("Künstler");
        artist.setMinWidth(150);
        artist.setCellValueFactory(new PropertyValueFactory<>("artist"));

        TableColumn<Song, String> album = new TableColumn<>("Album");
        album.setMinWidth(225);
        album.setCellValueFactory(new PropertyValueFactory<>("album"));

        TableColumn<Song, String> length = new TableColumn<>("Dauer");
        length.setId("length_col");
        length.setMinWidth(100);
        length.setCellValueFactory(new PropertyValueFactory<>("lengthConverted"));

        this.currentSongsTable = new TableView<>();
        this.currentSongsTable.getColumns().addAll(
                songNr,
                title,
                artist,
                album,
                length
        );
        this.playlistListView.getSelectionModel().select(0);
        this.updateSongTable(
                this.playlistListView.getSelectionModel().getSelectedItem().getPlaylist()
        );
        this.currentSongsTable.getSelectionModel().select(0);
    }

    private HBox setUpBottomPane(){
        HBox bottomPane = new HBox();
        bottomPane.setId("controlBtnPane");
        bottomPane.setAlignment(Pos.CENTER_LEFT);
        bottomPane.setPrefHeight(Dim.H_BORDERP_BOTTOM.intVal());
        bottomWidth = new SimpleDoubleProperty(Dim.MIN_W_SCREEN.intVal());

        ControlButton play_pause = new ControlButton("PLAY");
        play_pause.addEventHandler(
                ActionEvent.ANY, event -> {
                    controller.play();
                }
        );
        play_pause.idProperty().bind(
                play_pauseStyle
        );
        ControlButton stop = new ControlButton("STOP");
        stop.addEventHandler(
                ActionEvent.ANY, event -> {
                    controller.stop();
                    currentSongsTable.scrollTo(0);
                }
        );
        ControlButton next = new ControlButton("NEXT");
        next.addEventHandler(
                ActionEvent.ANY, event -> {
                    controller.skip(Skip.NEXT);
                    currentSongsTable.getSelectionModel().select(
                            currentSongsTable.getItems().indexOf(controller.getCurrentSong())
                    );
                    currentSongsTable.scrollTo(controller.getCurrentSong());

                    updateCurrentProperties();
                }
        );
        ControlButton prev = new ControlButton("PREV");
        prev.addEventHandler(
                ActionEvent.ANY, event -> {
                    controller.skip(Skip.PREVIOUS);
                    currentSongsTable.getSelectionModel().select(
                            currentSongsTable.getItems().indexOf(controller.getCurrentSong())
                    );
                    currentSongsTable.scrollTo(controller.getCurrentSong());

                    updateCurrentProperties();
                }
        );
        ControlButton mute = new ControlButton("MUTE");
        mute.addEventHandler(
                ActionEvent.ANY, event -> controller.mute()
        );
        ControlButton shuffle = new ControlButton("SHUFFLE");
        if(controller.isShuffle()) shuffle.setOpacity(1);
        else shuffle.setOpacity(MIN_OPACITY);
        shuffle.addEventHandler(
                ActionEvent.ACTION, event -> {
                    controller.toggleShuffle();
                    if(controller.isShuffle()) shuffle.setOpacity(1);
                    else shuffle.setOpacity(MIN_OPACITY);
                }
        );

        HBox btnsContainer = new HBox();
        btnsContainer.setSpacing(Dim.PAD_SIDE_LIST.intVal());
        btnsContainer.setFillHeight(true);
        btnsContainer.getChildren().addAll(
                prev,
                play_pause,
                stop,
                next,
                shuffle,
                mute
        );

        //// VOLUME SLIDER
        Slider volume = new Slider();
        volume.setMinWidth(50);
        volume.maxWidth(50);
        volume.setMin(Dim.VOL_MIN.intVal());
        volume.setMax(Dim.VOL_MAX.intVal());
        volume.setMajorTickUnit(50);
        volume.setMinorTickCount(0);
        volume.setShowTickMarks(false);
        volume.setSnapToTicks(false);
        volume.valueProperty().bindBidirectional(
                this.volumeValue
        );
        volume.setOnMouseDragged(
                event -> controller.setVolume((float) volume.getValue())
        );
        volume.setOnMouseClicked(
                event -> controller.setVolume((float) volume.getValue())
        );

        //// CURRENT SONG LENGTH
        Label songLen = new Label();
        songLen.setMinWidth(Dim.W_SONGTITLE.intVal());
        songLen.setMaxWidth(Dim.W_SONGTITLE.intVal());
        this.currentSongLength = this.controller.getCurrentSongLength();
        songLen.textProperty().bindBidirectional(this.currentSongLength);

        //// CURRENT SONG TITLE
        Label songTitle = new Label();
        songTitle.minWidth(100);
        songTitle.prefWidth(100);
        songTitle.maxWidth(100);
        songTitle.textProperty().bind(
                this.currentSongTitle
        );

        ////// SLIDER
        Slider posSlider = new Slider();
        posSlider.setId("posSlider");
        posSlider.minWidth(400);
        posSlider.maxWidth(3050);
        posSlider.maxProperty().bind(
                songLength
        );
        posSlider.valueProperty().bindBidirectional(
                currentPosPropVal
        );
        posSlider.setStyle("-fx-background-color: transparent;");
        posSlider.maxWidth(Dim.W_MAX_SLIDER.intVal());
        HBox.setHgrow(posSlider, Priority.ALWAYS);

        HBox volumeCtrl = new HBox(mute, volume);
        volumeCtrl.setAlignment(Pos.CENTER);

        //// CURRENT SONG POSITION
        Label currPos = new Label(
                TimeConverter.setTimeFormatStd(0)
        );
        currPos.setMinWidth(Dim.W_SONGTITLE.intVal());
        currPos.setMaxWidth(Dim.W_SONGTITLE.intVal());
        this.currentPos = new SimpleStringProperty();
        currentPosPropVal.addListener(
                (observable, oldValue, newValue) -> {
                    currPos.setText(
                            TimeConverter.setTimeFormatStd(
                                    newValue.longValue()
                            )
                    );
                }
        );

        ////// CURRENT SONG CONTAINER
        HBox currentSongCtrl = new HBox();
//        currentSongCtrl.setMaxWidth(Dim.W_ADDPLAYLIST_WINDOW.intVal());
        HBox.setHgrow(currentSongCtrl, Priority.ALWAYS);
        currentSongCtrl.getChildren().addAll(
                currPos,
                posSlider,
                songLen
        );
        VBox currentSongContainer = new VBox(songTitle, currentSongCtrl);
        currentSongContainer.setFillWidth(true);
        HBox.setHgrow(currentSongContainer, Priority.ALWAYS);

        //// CONTAINER: CONTROL BUTTONS
        bottomPane.setSpacing(5);
        bottomPane.setMinWidth(Dim.MIN_W_SCREEN.intVal());
        bottomPane.setMaxWidth(1500);
        bottomPane.setPrefHeight(Dim.H_BORDERP_BOTTOM.intVal());
        bottomPane.setMaxHeight(Dim.H_BORDERP_BOTTOM.intVal());
        bottomPane.setMinHeight(Dim.H_BORDERP_BOTTOM.intVal());
        bottomPane.setSpacing(Dim.H_BORDERP_BOTTOM.intVal());
        bottomPane.setStyle("-fx-background-color: aqua;");
        bottomPane.setPadding(
                new Insets(Dim.PAD_SIDE_LIST.intVal(), 0, 0, 0)
        );
        bottomPane.getChildren().addAll(
                btnsContainer,
                volumeCtrl,
                currentSongContainer
        );

        HBox.setHgrow(bottomPane, Priority.ALWAYS);

        return bottomPane;
    }

    private StackPane setUpRightView(){
        StackPane rightPanel = new StackPane();

        Text bigPlaylist = new Text(
                controller.getSelectedPlaylist().getTitle()
        );
        bigPlaylist.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        bigPlaylist.textProperty().bind(this.currentPlaylistTitle);
        bigPlaylist.setId("playlistBig");
        Text bigTitle = new Text(controller.getCurrentSong().getTitle());
        bigTitle.textProperty().bind(this.currentSongTitle);
        bigTitle.setId("titleBig");
        Text bigAlbum = new Text(controller.getCurrentSong().getAlbum());
        bigAlbum.textProperty().bind(this.currentSongAlbum);
        bigAlbum.setId("albumBig");
        Text bigArtist = new Text(controller.getCurrentSong().getAlbum());
        bigArtist.textProperty().bind(this.currentSongArtist);
        bigArtist.setId("artistBig");
        Text bigLength = new Text(controller.getCurrentSongLength().get());
        bigLength.textProperty().bind(this.currentSongLength);
        bigLength.setId("lengthBig");

        VBox bigSongTextContainer = new VBox(bigPlaylist, bigTitle, bigArtist, bigAlbum, bigLength);
        bigSongTextContainer.setFillWidth(true);
        AnchorPane anchorPane = new AnchorPane(bigSongTextContainer);
        AnchorPane.setBottomAnchor(bigSongTextContainer, 0.0);

        this.currentCoverImg.setImage(controller.getCoverImg());
        this.currentCoverImg.setPreserveRatio(true);
        this.currentCoverImg.setFitHeight(
                Dim.MIN_H_SCREEN.intVal() - 300
        );
        rightPanel.getChildren().add(this.currentCoverImg);

        HBox cover_bigElements = new HBox(currentCoverImg, anchorPane);
        cover_bigElements.setAlignment(Pos.BOTTOM_LEFT);
        cover_bigElements.setSpacing(Dim.PAD_SIDE_LIST.intVal());
        cover_bigElements.setFillHeight(true);

        VBox verticalSeperator = new VBox();
        verticalSeperator.getChildren().addAll(cover_bigElements);

        this.setSongTable();
        this.playlistListView.getSelectionModel().select(0);
        verticalSeperator.getChildren().add(this.currentSongsTable);
        verticalSeperator.setPadding(
                new Insets(
                        Dim.PAD_SIDE_LIST.intVal(),
                        0, 0 ,
                        Dim.PAD_SIDE_LIST.intVal()
                )
        );

        rightPanel.getChildren().add(verticalSeperator);
        rightPanel.setAlignment(Pos.BOTTOM_LEFT);
        StackPane.setAlignment(cover_bigElements, Pos.BOTTOM_LEFT);

        return rightPanel;
    }


    /////////////////////// UPDATE METHODS

    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(() -> {
                    updatePlaylistView();
                    updateSongView();
                    updateBottom();
                }
        );
    }

    private void updatePlaylistView(){
        this.allPlaylists.setAll(
                controller.getAllPlaylists()
        );
        this.setPlaylistViewCells();
        this.playlistListView.refresh();
    }

    /**
     * This method creates a {@link TableView table}, which shows all songs, of the {@param playlist given playlist}.
     * @param playlist The selected {@link Playlist} which is to be shown in the table.
     */
    private void updateSongTable(Playlist playlist){
        this.currentSongsTable.getItems().setAll(
                playlist.getSongs()
        );
        this.currentSongsTable.refresh();
    }

    private void updateSongView(){
        this.currentCoverImg.setImage(
                this.controller.getCoverImg()
        );
    }

    private void updateBottom(){
        this.currentPosPropVal.set(
                this.controller.getCurrentSongPosition()
        );
        this.volumeValue.set(
                this.controller.getVolume()
        );
        if(controller.isPlaying()){
            play_pauseStyle.set("pause");
        }
        else{
            play_pauseStyle.set("play");
        }
     }

    private void updateCurrentProperties(){
        currentPlaylistTitle.set(
                controller.getSelectedPlaylist().getTitle()
        );
        currentSongTitle.set(
                controller.getCurrentSong().getTitle()
        );
        songLength.set(
                controller.getCurrentSong().getLengthMillis()
        );
        currentSongAlbum.set(
                controller.getCurrentSong().getAlbum()
        );
        currentSongArtist.set(
                controller.getCurrentSong().getArtist()
        );
        currentSongLength.set(
                controller.getCurrentSongLength().get()
        );
    }

}
