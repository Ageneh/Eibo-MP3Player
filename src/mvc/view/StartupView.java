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
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import misc.ANSI;
import misc.TimeConverter;
import mvc.controller.Controller;
import mvc.model.extension.enums.Skip;
import mvc.model.playlist.Playlist;
import mvc.model.playlist.Song;
import mvc.view.elements.ControlButton;
import mvc.view.elements.PlaylistSongCover;
import mvc.view.elements.PlaylistViewCell;
import mvc.view.elements.PlistListCell;
import mvc.view.enums.Dim;

import java.util.Observable;
import java.util.Observer;

public class StartupView extends Application implements Observer {

    /////////////////////// VARIABLES

    private final double MIN_OPACITY = 0.4;

    private volatile Controller controller;

    private Stage primaryStage;

    private String stageTitle;
    private Scene baseScene;
    private BorderPane root;

    private VBox leftPanel;
    private ListView<PlaylistViewCell> playlistListView;
    private ObservableList<Playlist> allPlaylists;

    private PlaylistSongCover currentCoverImg;
    private TableView<Song> currentSongsTable;
    private SimpleStringProperty currentSongTitle;

    /**
     * String property, used for binding and updates of certain elements.
     * <br>Will save the current position of the {@link mvc.model.MP3Player#player player}.</br>
     * @see #setUpBottom()
     * @see #updateBottom()
     */
    private SimpleLongProperty currentPosPropVal;
    private SimpleStringProperty currentPos;
    private SimpleLongProperty songLength;
    private SimpleDoubleProperty bottomWidth;
    /**
     * String property, used for binding and updates of certain elements.
     * {@link Controller#getCurrentSongLength()}
     * @see #setUpBottom()
     * @see #updateBottom()
     */
    private SimpleStringProperty currentTotalLength;
    private SimpleFloatProperty volumeValue;


    /////////////////////// CONSTRUCTOR

    public StartupView(){
        this("MP3-Player");
    }

    public StartupView(String stageTitle){
        controller = new Controller();
        controller.addObserver(this);

        this.stageTitle = stageTitle;
    }


    /////////////////////// PUBLIC METHODS

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

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

        //// SETTING UP SCENE
        this.root.setCenter(
                this.setUpRightView()
        );

        ////// BOTTOM PANEL AND CONTROLS
        setUpBottom();
        this.root.setBottom(bottomPane);
        bottomPane.setId("controlBtnPane");

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

        //// PARSE STYLESHEET
        this.baseScene.getStylesheets().add(getClass().getResource("/mvc/view/stylesheets/styles.css").toExternalForm());

        this.primaryStage = primaryStage;
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
//                        selectedPlaylist.set(
//                                newValue.getPlaylist()
//                        );
//                        ANSI.GREEN.println(controller.getCurrentPlaylist().get().getTitle());
                        updateSongTable(
                                newValue.getPlaylist()
                        );
                        playlistListView.refresh();
                    }
                }
        );
        currentSongsTable.setOnMouseClicked(
                event -> {
                    if (event.getClickCount() == 2) {
                        // load the playlist and its first song
                        controller.playCurrentSong(
                                playlistListView.getFocusModel().getFocusedItem().getPlaylist(),
                                currentSongsTable.getSelectionModel().getSelectedItem()
                        );
                        currentSongsTable.refresh();
                    }
                    else {
                        currentSongsTable.getFocusModel().focus(
                                currentSongsTable.getSelectionModel().getFocusedIndex()
                        );
                    }
                }
        );
    }

    private void setUpLeftPanel(){
        this.allPlaylists = this.controller.getPlaylistData();

        //// HEADER
        Label leftPanelHeaderLabel = new Label("Meine Playlists");
        leftPanelHeaderLabel.setId("leftPanelHeaderLabel");
        leftPanelHeaderLabel.setWrapText(false);
        leftPanelHeaderLabel.setMinWidth(80);
        leftPanelHeaderLabel.setAlignment(Pos.BOTTOM_LEFT);

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
                    primaryStage.setOpacity(0.8);
                    addPlaylist.setDisable(true);
                    controller.addSongs();
                    addPlaylist.setDisable(false);
                    primaryStage.setOpacity(1);
                }
        );
        addPlaylist.setOpacity(MIN_OPACITY);
        addPlaylist.setWrapText(false);
        addPlaylist.setMinWidth(40);
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

        this.setPlaylistViewCells();

        leftPanel.getChildren().add(this.playlistListView);
        this.root.setLeft(leftPanel);
    }

    private void setPlaylistViewCells(){
        PlistListCell cellBox;
        this.playlistListView.getItems().clear();
        this.playlistListView.refresh();
        for (Playlist playlist : allPlaylists){
            if(playlist == null){
                continue;
            }
            cellBox = new PlistListCell(playlist);
            cellBox.setId("cellbox");
            this.playlistListView.getItems().add(cellBox.getGrid());
        }

        this.playlistListView.getSelectionModel().select(0);
        this.playlistListView.getFocusModel().focus(0);
        this.playlistListView.scrollTo(0);
//        this.playlistListView.getSelectionModel().select(0);
//        this.controller.setSelectedPlaylist(
//                this.playlistListView.getSelectionModel().getSelectedItem().getPlaylist()
//        );
    }

    /**
     * This method creates a {@link TableView table}, which shows all songs, of the current playlist.
     */
    private void updateSongTable(){
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
                title,
                artist,
                album,
                length
        );
        this.currentSongsTable.setPrefHeight(
                Dim.MIN_H_SCREEN.intVal() -
                        this.currentCoverImg.getViewport().getHeight()
        );
    }

    /**
     * This method creates a {@link TableView table}, which shows all songs, of the {@param playlist given playlist}.
     * @param playlist The selected {@link Playlist} which is to be shown in the table.
     */
    private void updateSongTable(Playlist playlist){
//        this.currentSongsTable = new TableView<>();

        this.currentSongsTable.getItems().setAll(
                playlist.getSongs()
        );
        this.currentSongsTable.getSelectionModel().select(
                currentSongsTable.getSelectionModel().getSelectedIndex()
        );
        this.currentSongsTable.refresh();
    }

    HBox bottomPane;
    private HBox setUpBottom(){
        bottomWidth = new SimpleDoubleProperty(Dim.MIN_W_SCREEN.intVal());
        bottomPane = new HBox();
        bottomPane.setAlignment(Pos.CENTER_LEFT);
        bottomPane.setPrefHeight(Dim.H_BORDERP_BOTTOM.intVal());
//        bottomPane.setGridLinesVisible(true);

        ControlButton play_pause = new ControlButton("PLAY");
        play_pause.addEventHandler(
                ActionEvent.ANY, event -> controller.play()
        );
//        bottomPane.addColumn(0, play_pause);
        ControlButton stop = new ControlButton("STOP");
        stop.addEventHandler(
                ActionEvent.ANY, event -> controller.stop()
        );
//        bottomPane.addColumn(2, stop);
        ControlButton next = new ControlButton("NEXT");
        next.addEventHandler(
                ActionEvent.ANY, event -> controller.skip(Skip.NEXT)
        );
//        bottomPane.addColumn(3, next);
        ControlButton prev = new ControlButton("PREV");
        prev.addEventHandler(
                ActionEvent.ANY, event -> controller.skip(Skip.PREVIOUS)
        );
//        bottomPane.addColumn(4, prev);
        ControlButton mute = new ControlButton("MUTE");
        mute.addEventHandler(
                ActionEvent.ANY, event -> controller.mute()
        );
//        bottomPane.addColumn(10, mute);
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

        //// VOLUME SLIDER
        Slider volume = new Slider();
        volume.setMinWidth(50);
        volume.maxWidth(50);
        volume.setMin(0);
        volume.setMax(100);
        volume.setMajorTickUnit(50);
        volume.setMinorTickCount(0);
        volume.setShowTickMarks(true);
        volume.setSnapToTicks(false);
        this.volumeValue = new SimpleFloatProperty(this.controller.getVolume());
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
        this.currentTotalLength = new SimpleStringProperty();
        this.currentTotalLength = this.controller.getCurrentSongLength();
        songLen.textProperty().bindBidirectional(this.currentTotalLength);

        //// CURRENT SONG TITLE
        Label songTitle = new Label();
        songTitle.minWidth(100);
        songTitle.prefWidth(100);
        songTitle.maxWidth(100);
        this.currentSongTitle = new SimpleStringProperty(
                controller.getCurrentSong().getTitle()
        );
        songTitle.textProperty().bind(
                this.currentSongTitle
        );

        //// CURRENT SONG POSITION
        Label currPos = new Label(
                TimeConverter.setTimeFormatStd(0)
        );
        currPos.setMinWidth(Dim.W_SONGTITLE.intVal());
        currPos.setMaxWidth(Dim.W_SONGTITLE.intVal());
        this.currentPos = new SimpleStringProperty();
        this.currentPosPropVal = new SimpleLongProperty(
                controller.getCurrentSongPosition()
        );
        currentPosPropVal.addListener(
                (observable, oldValue, newValue) -> currPos.setText(
                        TimeConverter.setTimeFormatStd(
                                newValue.longValue()
                        )
                )
        );
        ////// SLIDER
        Slider posSlider = new Slider();
        this.songLength = new SimpleLongProperty(
                controller.getCurrentSong().getLengthMillis()
        );
        posSlider.minWidth(400);
        posSlider.maxWidth(350);
        posSlider.maxProperty().bind(
                songLength
        );
        posSlider.valueProperty().bindBidirectional(
                currentPosPropVal
        );
        posSlider.maxWidth(Dim.W_MAX_SLIDER.intVal());

        ////// CURRENT SONG CONTAINER
        HBox currentSongCtrl = new HBox();
        currentSongCtrl.setMaxWidth(Dim.W_ADDPLAYLIST_WINDOW.intVal());
        HBox.setHgrow(posSlider, Priority.SOMETIMES);
        HBox.setHgrow(currentSongCtrl, Priority.ALWAYS);
        currentSongCtrl.getChildren().addAll(
                currPos,
                posSlider,
                songLen
        );
        VBox currentSongContainer = new VBox(songTitle, currentSongCtrl);
        currentSongContainer.setFillWidth(true);

        HBox controlBtns = new HBox();
        controlBtns.setSpacing(5);
        controlBtns.setMaxWidth(1500);
        controlBtns.setPrefHeight(Dim.SIZE_CTRL_BTN.intVal());
        controlBtns.setMaxHeight(Dim.SIZE_CTRL_BTN.intVal());
        controlBtns.setMinHeight(Dim.SIZE_CTRL_BTN.intVal());
        controlBtns.setAlignment(Pos.CENTER);
        controlBtns.getChildren().addAll(
                prev,
                play_pause,
                stop,
                next,
                shuffle,
                mute,
                volume,
                currentSongContainer
        );

        HBox.setHgrow(controlBtns, Priority.ALWAYS);
        bottomPane.getChildren().addAll(controlBtns);


        return bottomPane;
    }

    private StackPane setUpRightView(){
        StackPane rightPanel = new StackPane();

        this.currentCoverImg = new PlaylistSongCover();
        this.currentCoverImg.setImage(controller.getCoverImg());
        this.currentCoverImg.setPreserveRatio(true);
        this.currentCoverImg.setViewport(
                new Rectangle2D(
                    0,
                    this.currentCoverImg.getBoundsInParent().getHeight() * 0.25,
                    this.currentCoverImg.getBoundsInParent().getWidth(),
                        this.baseScene.getHeight() * 0.4
        ));
        this.currentCoverImg.setFitWidth(
                Dim.MIN_W_SCREEN.intVal() - Dim.W_LEFT_PANEL.intVal()
        );
        this.currentCoverImg.resize(
                100,
                this.baseScene.getHeight() * 0.4
            );
        rightPanel.getChildren().add(this.currentCoverImg);

        VBox verticalSeperator = new VBox();
        verticalSeperator.getChildren().add(this.currentCoverImg);

        this.updateSongTable();
        verticalSeperator.getChildren().add(this.currentSongsTable);

        rightPanel.getChildren().add(verticalSeperator);

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

    private void updateSongView(){
        this.currentCoverImg.setImage(
                this.controller.getCoverImg()
        );

    }

    private void updateBottom(){
        this.currentPosPropVal.set(
                this.controller.getCurrentSongPosition()
        );
        this.currentTotalLength.set(
                this.controller.getCurrentSongLength().get()
        );
        this.volumeValue.set(
                this.controller.getVolume()
        );
        this.currentSongTitle.set(
                controller.getCurrentSong().getTitle()
        );
        this.songLength.set(
                controller.getCurrentSong().getLengthMillis()
        );
    }

}
