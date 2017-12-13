package mvc.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
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
    private GridPane leftPanelHeader;
    private ListView<PlaylistViewCell> playlistListView;
    private ObservableList<Playlist> allPlaylists;
    private SimpleObjectProperty<Playlist> selectedPlaylist;

    private PlaylistSongCover currentCoverImg;
    private TableView<Song> currentSongsTable;

    private SimpleObjectProperty<Song> currentSong;
    /**
     * String property, used for binding and updates of certain elements.
     * <br>Will save the current position of the {@link mvc.model.MP3Player#player player}.</br>
     * @see #setUpBottom()
     * @see #updateBottom()
     */
    private SimpleLongProperty currentPosPropVal;
    private SimpleStringProperty currentPosProp;
    /**
     * String property, used for binding and updates of certain elements.
     * {@link Controller#getCurrentSongLength()}
     * @see #setUpBottom()
     * @see #updateBottom()
     */
    private SimpleStringProperty currentTotalLength;
    private SimpleIntegerProperty playlistCountProp;
    private SimpleFloatProperty volumeValue;

    private SimpleBooleanProperty showStage;



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
        Pane bottom = setUpBottom();
        this.root.setBottom(bottom);
        bottom.setId("controlBtnPane");

        this.setUpListeners();

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
//        this.baseScene.getStylesheets().add(getClass().getResource("/mvc/view/stylesheets/bootstrap3.css").toExternalForm());

        this.update(null, null);

        this.primaryStage = primaryStage;
        primaryStage.show();
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
                                    - leftPanelHeader.getBoundsInParent().getWidth()
                    );
                    currentSongsTable.setPrefHeight(
                            root.getHeight()
                                    - Dim.H_ROOT_BOTTOM.intVal()
                                    - currentCoverImg.getBoundsInParent().getHeight()
                    );
                }
        );
        baseScene.heightProperty().addListener(
                (observable, oldValue, newValue) -> {
                    playlistListView.setPrefHeight(
                            root.getHeight()
                                    - Dim.H_ROOT_BOTTOM.intVal()
                                    - leftPanelHeader.getHeight()
                    );

                    currentSongsTable.setPrefHeight(
                            root.getHeight()
                                    - Dim.H_ROOT_BOTTOM.intVal()
                                    - currentCoverImg.getBoundsInParent().getHeight()
                    );
                }
        );
        playlistListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if(newValue != null){
                        ANSI.YELLOW.println("NEW PLAYLIST " + newValue.getTitle());
                        this.selectedPlaylist.set(
                                newValue.getPlaylist()
                        );
                        ANSI.GREEN.println(controller.getCurrentPlaylist().get().getTitle());
                        updateSongTable(
                                newValue.getPlaylist()
                        );
                    }
                    playlistListView.refresh();
                }
        );
//        playlistListView.setOnMouseClicked(
//                event -> {
//                    this.selectedPlaylist.set(
//                            playlistListView.getSelectionModel().getSelectedItem().getPlaylist()
//                    );
//                    ANSI.GREEN.println(controller.getCurrentPlaylist().get().getTitle());
//                    updateSongTable(
//                            this.selectedPlaylist.get()
//                    );
//                    playlistListView.refresh();
//                }
//        );
//        currentSongsTable.getSelectionModel().selectedItemProperty().addListener(
//                (observable, oldValue, newValue) -> {
//                    if(!newValue.equals(oldValue)){
//                        this.currentSong.set(
//                                newValue
//                        );
//                    }
//                }
//        );
        currentSongsTable.setOnMouseClicked(
                (MouseEvent event) ->  {
                    if(event.getClickCount() == 2) {
                        this.currentSong.set(
                                currentSongsTable.getSelectionModel().getSelectedItem()
                        );
                        // load the playlist and its first song
                        ANSI.MAGENTA.println("ANDERS ANDERS ANDERS ANDERS");
                        System.out.println(currentSong.get().getTitle());
                        ANSI.MAGENTA.println("ANDERS ANDERS ANDERS ANDERS");
                        controller.playCurrentSong(
                                selectedPlaylist.get(),
                                currentSong.get()
                        );
                        ANSI.MAGENTA.println("CLICKY _> " + controller.getCurrentSong().get().getTitle());
                        currentSongsTable.refresh();
                    }
                }
        );
    }

    private void setUpLeftPanel(){
        this.allPlaylists = this.controller.getPlaylistData();

        Label leftPanelHeaderLabel = new Label("Meine Playlists");
        leftPanelHeaderLabel.setPadding(new Insets(0, 0, 0, Dim.PAD_SIDE_LIST.intVal()));
        leftPanelHeaderLabel.setId("leftPanelHeaderLabel");

        Label playlistCount = new Label();
        IntegerBinding plistCount = Bindings.size(this.allPlaylists);
        playlistCount.setPadding(new Insets(0, Dim.PAD_SIDE_LIST.intVal(), 0, 0));
        playlistCount.setAlignment(Pos.CENTER_RIGHT);
        playlistCount.textProperty().bind(StringBinding.stringExpression(plistCount));

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

        this.leftPanelHeader = new GridPane();
        this.leftPanelHeader.setId("leftPanelHeader");
        this.leftPanelHeader.addColumn(0, leftPanelHeaderLabel);
        this.leftPanelHeader.setHgap(50);
        this.leftPanelHeader.addColumn(2, playlistCount);
        this.leftPanelHeader.addRow(1, new Label(controller.getPlaylistFolderPath()));
        this.leftPanelHeader.addRow(2, addPlaylist);
        this.leftPanelHeader.setPrefHeight(90);
        this.leftPanelHeader.setMaxWidth(Dim.W_LEFT_PANEL.intVal());
        this.leftPanelHeader.setAlignment(Pos.BOTTOM_CENTER);

        this.leftPanel = new VBox();
        this.leftPanel.getChildren().add(leftPanelHeader);

        this.playlistListView = new ListView<>();
        this.playlistListView.setEditable(false);
        this.playlistListView.setFixedCellSize(Dim.H_CELL.intVal());
        this.playlistListView.setPadding(new Insets(0));

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

        this.playlistListView.getSelectionModel().select(0);
        this.playlistListView.getFocusModel().focus(0);
        this.playlistListView.scrollTo(0);
        this.selectedPlaylist = new SimpleObjectProperty<>(
                this.playlistListView.getSelectionModel().getSelectedItem().getPlaylist()
        );
//        this.playlistListView.getSelectionModel().select(0);
//        this.controller.setSelectedPlaylist(
//                this.playlistListView.getSelectionModel().getSelectedItem().getPlaylist()
//        );
    }

    /**
     * This method creates a {@link TableView table}, which shows all songs, of the current playlist.
     */
    private void updateSongTable(){
        this.currentSongsTable = new TableView<>();

        TableColumn<Song, String> title = new TableColumn<>("Songtitle");
        title.setMinWidth(300);
        title.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<Song, String> artist = new TableColumn<>("Artist");
        artist.setMinWidth(125);
        artist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        TableColumn<Song, String> length = new TableColumn<>("Length");
        length.setMinWidth(100);
        length.setCellValueFactory(new PropertyValueFactory<>("lengthConverted"));
        this.currentSongsTable.getColumns().addAll(title, artist, length);

        this.currentSongsTable.setPrefHeight(
                Dim.MIN_H_SCREEN.intVal() -
                        this.currentCoverImg.getViewport().getHeight()
        );

//        this.updateSongTable(null);
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
        this.currentSongsTable.getFocusModel().focus(
                this.currentSongsTable.getItems().indexOf(this.currentSong.get())
        );
        this.currentSong.set(
                this.currentSongsTable.getSelectionModel().getSelectedItem()
        );
        this.currentSongsTable.refresh();
    }

    private AnchorPane setUpBottom(){
        AnchorPane rootAnchor = new AnchorPane();
        rootAnchor.setMinHeight(Dim.H_ROOT_BOTTOM.intVal());
        rootAnchor.setPrefHeight(Dim.H_ROOT_BOTTOM.intVal());
        rootAnchor.setMaxHeight(Dim.H_ROOT_BOTTOM.intVal());

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
        this.currentPosPropVal = new SimpleLongProperty(
                controller.getCurrentSongPosition()
        );
        currentPosProp.bind(TimeConverter.setTimeFormatStd(currentPosPropVal));
        currPos.textProperty().bindBidirectional(this.currentPosProp);

        Label songLen = new Label();
        this.currentTotalLength = new SimpleStringProperty();
        this.currentTotalLength = this.controller.getCurrentSongLength();
        songLen.textProperty().bindBidirectional(this.currentTotalLength);

        Label songTitle = new Label();
        this.currentSong = this.controller.getCurrentSong();


        ProgressBar pos = new ProgressBar();
        pos.setMinWidth(200);

//        pos.setProgress(this.controller.getCurrentSongPosition().get());

        Slider songPos = new Slider();
//        songPos.valueProperty().bindBidirectional(
//                currentPosProp.ge
//        );

        HBox controlBtns = new HBox();
        controlBtns.setSpacing(10);
        controlBtns.setPrefHeight(Dim.SIZE_CTRL_BTN.intVal());
        controlBtns.setMaxHeight(Dim.SIZE_CTRL_BTN.intVal());
        controlBtns.setMinHeight(Dim.SIZE_CTRL_BTN.intVal());
        controlBtns.setAlignment(Pos.CENTER);
        controlBtns.getChildren().addAll(prev, play, stop, next, shuffle, mute, volume, songPos, songLen, currPos);

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
                Dim.MIN_W_SCREEN.intVal() - Dim.W_LEFT_PANEL.intVal()
        );
        this.currentCoverImg.resize(
                100,
                this.baseScene.getHeight() * 0.4
            );
        rightPanel.getChildren().add(this.currentCoverImg);

        VBox rightPanelBox = new VBox();
        rightPanelBox.getChildren().add(this.currentCoverImg);

        this.updateSongTable();
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
                            currentSong.set(controller.getCurrentSong().get());
                            updatePlaylistView();
                            updateCoverView();
                            updateBottom();
                        }
                );
                return null;
            }
        }.run();
    }

    private void updatePlaylistView(){
        this.allPlaylists.setAll(controller.getAllPlaylists());
        this.setPlaylistViewCells();
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
        this.currentPosPropVal.set(
                this.controller.getCurrentSongPosition()
        );
        this.currentTotalLength.set(
                this.controller.getCurrentSongLength().get()
        );
        this.volumeValue.set(
                this.controller.getVolume()
        );
    }

}
