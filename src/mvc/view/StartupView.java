package mvc.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import misc.ANSI;
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
    private GridPane leftPanelHeader;
    private ListView<PlaylistViewCell> playlistListView;
    private ObservableList<Playlist> allPlaylists;

    private PlaylistSongCover currentCoverImg;
    private TableView<Song> currentSongsTable;

    /**
     * String property, used for binding and updates of certain elements.
     * <br>Will save the current position of the {@link mvc.model.MP3Player#player player}.</br>
     * @see #setUpBottom()
     * @see #updateBottom()
     */
    private SimpleStringProperty currentPosProp;
    /**
     * String property, used for binding and updates of certain elements.
     * {@link Controller#getCurrentSongLength()}
     * @see #setUpBottom()
     * @see #updateBottom()
     */
    private SimpleStringProperty currentTotalLength;
    private SimpleStringProperty playlistCountProp;
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
                Dim.MIN_W_SCREEN.doubleVal(),
                Dim.MIN_H_SCREEN.doubleVal()
        );

        ////// LEFT CONTROLS
        this.setUpLeftPanel();

        //// SETTING UP SCENE
        this.root.setCenter(
                this.setUpRightView()
        );

        ////// BOTTOM CONTROLS
        Pane bottom = setUpBottom();
        this.root.setBottom(bottom);
        bottom.setId("controlBtnPane");

        this.setUpListeners();

        primaryStage.setScene(this.baseScene);
        primaryStage.setTitle(this.stageTitle);
        primaryStage.setMinHeight(Dim.MIN_H_SCREEN.doubleVal());
        primaryStage.setMinWidth(Dim.MIN_W_SCREEN.doubleVal());
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

        this.update(null, null);

        primaryStage.show();
    }


    /////////////////////// PRIVATE METHODS

    private void setUpLeftPanel(){
        Label leftPanelHeaderLabel = new Label("Meine Playlists");
        leftPanelHeaderLabel.setPadding(new Insets(0, 0, 0, Dim.PAD_SIDE_LIST.doubleVal()));
        leftPanelHeaderLabel.setId("leftPanelHeaderLabel");

        Label playlistCount = new Label();
        playlistCount.setPadding(new Insets(0, Dim.PAD_SIDE_LIST.doubleVal(), 0, 0));
        playlistCount.setAlignment(Pos.CENTER_RIGHT);
        this.playlistCountProp = new SimpleStringProperty(
                this.controller.getPlaylistCount() + "");
        playlistCount.textProperty().bind(playlistCountProp);

        ControlButton addPlaylist = new ControlButton("");
        addPlaylist.setId("newPlaylist");
        addPlaylist.setOnMouseClicked(
                event -> {
                    primaryStage.setOpacity(0.8);
                    controller.addSongs();
                    primaryStage.setOpacity(1);
                }
        );

        this.leftPanelHeader = new GridPane();
        this.leftPanelHeader.setId("leftPanelHeader");
        this.leftPanelHeader.addColumn(0, leftPanelHeaderLabel);
        this.leftPanelHeader.setHgap(50);
        this.leftPanelHeader.addColumn(2, playlistCount);
        this.leftPanelHeader.addRow(1, new Label(controller.getPlaylistFolderPath()));
        this.leftPanelHeader.addRow(2, addPlaylist);
        this.leftPanelHeader.setPrefHeight(90);
        this.leftPanelHeader.setMaxWidth(Dim.W_LEFT_PANEL.doubleVal());
        this.leftPanelHeader.setAlignment(Pos.BOTTOM_CENTER);

        this.leftPanel = new VBox();
        this.leftPanel.getChildren().add(leftPanelHeader);

        this.playlistListView = new ListView<>();
        this.playlistListView.setEditable(false);
        this.playlistListView.setFixedCellSize(Dim.H_CELL.doubleVal());
        this.playlistListView.setPadding(new Insets(0));

        allPlaylists = this.controller.getPlaylistData();
        this.setPlaylistViewCells();

        this.leftPanel.getChildren().add(this.playlistListView);
        this.root.setLeft(this.leftPanel);
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
//        this.playlistListView.getSelectionModel().select(0);
//        this.controller.setCurrentPlaylist(
//                this.playlistListView.getSelectionModel().getSelectedItem().getPlaylist()
//        );
    }

    /**
     * This method creates a {@link TableView table}, which shows all songs, of the current playlist.
     */
    private void createSongTable(){
        this.createSongTable(null);
    }

    /**
     * This method creates a {@link TableView table}, which shows all songs, of the {@param playlist given playlist}.
     * @param playlist The selected {@link Playlist} which is to be shown in the table.
     */
    private void createSongTable(Playlist playlist){
        this.currentSongsTable = new TableView<>();

        this.currentSongsTable.setPrefHeight(
                Dim.MIN_H_SCREEN.doubleVal() -
                this.currentCoverImg.getViewport().getHeight()
        );

//        title.setId("songsTable");
        TableColumn<Song, String> title = new TableColumn<>("Songtitle");
        title.setMinWidth(300);
        title.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<Song, String> artist = new TableColumn<>("Artist");
        artist.setMinWidth(125);
        artist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        TableColumn<Song, String> length = new TableColumn<>("Length");
        length.setMinWidth(100);
        length.setCellValueFactory(new PropertyValueFactory<>("length"));

        this.currentSongsTable.setItems(this.controller.getCurrentSongs());
        this.currentSongsTable.getColumns().addAll(title, artist, length);
    }

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
                            - leftPanelHeader.getBoundsInParent().getWidth()
                    );
                    currentSongsTable.setPrefHeight(
                            root.getHeight()
                            - Dim.H_ROOT_BOTTOM.doubleVal()
                            - currentCoverImg.getBoundsInParent().getHeight()
                    );
                }
        );
        baseScene.heightProperty().addListener(
                (observable, oldValue, newValue) -> {
                    playlistListView.setPrefHeight(
                            root.getHeight()
                            - Dim.H_ROOT_BOTTOM.doubleVal()
                            - leftPanelHeader.getHeight()
                    );

                    currentSongsTable.setPrefHeight(
                            root.getHeight()
                            - Dim.H_ROOT_BOTTOM.doubleVal()
                            - currentCoverImg.getBoundsInParent().getHeight()
                    );
                }
        );
        playlistListView.setOnMouseClicked(
                event -> {
                    try {
                        controller.setCurrentPlaylist(
                                playlistListView.getSelectionModel().getSelectedItem().getPlaylist()
                        );
                    }
                    catch (NullPointerException e){

                    }
                    ANSI.GREEN.println(controller.getCurrentPlaylist().get().getTitle());
                    currentSongsTable.setItems(
                            controller.getCurrentPlaylist().get().getSongsObservable()
                    );
                    currentSongsTable.refresh();
                    playlistListView.refresh();
                }
        );
        currentSongsTable.setOnMouseClicked(
                (MouseEvent event) ->  {
                    if(event.getClickCount() == 2) {
                        // load the playlist and its first song
                        controller.loadPlaylist();
                        controller.setCurrentSong(
                                currentSongsTable.getSelectionModel().getSelectedItem()
                        );
                        ANSI.MAGENTA.println(controller.getCurrentPlaylist().get().getTitle());
                        controller.play(
                        );
                        currentSongsTable.refresh();
                    }
                }
        );
    }

    private AnchorPane setUpBottom(){
        AnchorPane rootAnchor = new AnchorPane();
        rootAnchor.setMinHeight(Dim.H_ROOT_BOTTOM.doubleVal());
        rootAnchor.setPrefHeight(Dim.H_ROOT_BOTTOM.doubleVal());
        rootAnchor.setMaxHeight(Dim.H_ROOT_BOTTOM.doubleVal());

        ControlButton play = new ControlButton("PLAY");
        play.addEventHandler(
                ActionEvent.ANY, event -> controller.play()
        );
        ControlButton pause = new ControlButton("PAUSE");
        pause.addEventHandler(
                ActionEvent.ANY, event -> controller.pause()
        );
        ControlButton stop = new ControlButton("STOP");
        stop.addEventHandler(
                ActionEvent.ANY, event -> controller.stop()
        );
        ControlButton next = new ControlButton("NEXT");
        next.addEventHandler(
                ActionEvent.ANY, event -> controller.skip(Skip.NEXT)
        );
        ControlButton prev = new ControlButton("PREV");
        prev.addEventHandler(
                ActionEvent.ANY, event -> controller.skip(Skip.PREVIOUS)
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

        Slider volume = new Slider();
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

        Label currPos = new Label();
        this.currentPosProp = new SimpleStringProperty();
        this.currentPosProp = this.controller.getCurrentSongPosition();
        currPos.textProperty().bindBidirectional(this.currentPosProp);

        Label songLen = new Label();
        this.currentTotalLength = new SimpleStringProperty();
        this.currentTotalLength = this.controller.getCurrentSongLength();
        songLen.textProperty().bindBidirectional(this.currentTotalLength);


        ProgressBar pos = new ProgressBar();
        pos.setMinWidth(200);

//        pos.setProgress(this.controller.getCurrentSongPosition().get());

        Slider songPos = new Slider();

        HBox controlBtns = new HBox();
        controlBtns.setSpacing(10);
        controlBtns.setPrefHeight(Dim.SIZE_CTRL_BTN.doubleVal());
        controlBtns.setMaxHeight(Dim.SIZE_CTRL_BTN.doubleVal());
        controlBtns.setMinHeight(Dim.SIZE_CTRL_BTN.doubleVal());
        controlBtns.setAlignment(Pos.CENTER);
        controlBtns.getChildren().addAll(prev, play, stop, next, mute, volume, songPos, songLen, currPos, shuffle);

        rootAnchor.getChildren().addAll(controlBtns);

        return rootAnchor;
    }

    private AnchorPane setUpRightView(){
        AnchorPane rightPanel = new AnchorPane();

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
                Dim.MIN_W_SCREEN.doubleVal() - Dim.W_LEFT_PANEL.doubleVal()
        );
        this.currentCoverImg.resize(
                100,
                this.baseScene.getHeight() * 0.4
            );
        rightPanel.getChildren().add(this.currentCoverImg);

        VBox rightPanelBox = new VBox();
        rightPanelBox.getChildren().add(this.currentCoverImg);

        this.createSongTable();
        rightPanelBox.getChildren().add(this.currentSongsTable);
        rightPanel.getChildren().add(rightPanelBox);

        return rightPanel;
    }


    /////////////////////// UPDATE METHODS

    @Override
    public void update(Observable o, Object arg) {
        new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                            playlistCountProp.set(controller.getPlaylistCount() + "");
                            updateBottom();
                            updatePlaylistView();
                            updateCoverView();
                        }
                );
                return null;
            }
        }.run();
    }

    private void updatePlaylistView(){
        this.allPlaylists = (controller.getAllPlaylists());
        this.setPlaylistViewCells();
        this.playlistCountProp.set(
                allPlaylists.size() + ""
        );
        this.playlistListView.refresh();
    }

    private void updateCoverView(){
        ANSI.MAGENTA.println("LISTEN LISTEN LSIETEN");
        this.currentCoverImg.setImage(
                this.controller.getCoverImg()
        );
    }

    private void updateBottom(){
//        System.out.printf("-- ");
        this.currentPosProp.set(
                this.controller.getCurrentSongPosition().get()
        );
        this.currentTotalLength.set(
                this.controller.getCurrentSongLength().get()
        );
        this.volumeValue.set(
                this.controller.getVolume()
        );
    }

}
