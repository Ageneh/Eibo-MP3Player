package mvc.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mvc.controller.Controller;
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

    private volatile Controller controller;

    private Stage primaryStage;

    private String stageTitle;
    private Scene baseScene;
    private HBox baseHorizContainer;
    private BorderPane root;

    private VBox leftPanel;
    private GridPane leftPanelHeader;
    private Label playlistCount;
    private ListView<PlaylistViewCell> playlistListView;
    private ObservableList<Playlist> allPlaylists;

    private VBox rightPanel;
    private PlaylistSongCover currentCoverImg;
    private TableView<Song> currentSongsTable;

    private Label totalLength;
    private SimpleStringProperty currentPosProp;


    /////////////////////// CONSTRUCTOR

    public StartupView(){
        this("MP3-Player");
    }

    public StartupView(String stageTitle){
        controller = new Controller();
        controller.addObserver(this);

//        this.controller = new Controller();

        playlistCount = new Label("0");
        this.stageTitle = stageTitle;
    }


    /////////////////////// PUBLIC METHODS

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Platform.runLater(() -> {
            this.primaryStage = primaryStage;

            //// SETTING UP STAGE AND ROOT PANE
            this.root = new BorderPane();
            this.baseScene = new Scene(this.root, Dim.MIN_W_SCREEN.doubleVal(), Dim.MIN_H_SCREEN.doubleVal());
            primaryStage.setScene(this.baseScene);
            primaryStage.setTitle(this.stageTitle);
            primaryStage.setMinHeight(Dim.MIN_H_SCREEN.doubleVal());
            primaryStage.setMinWidth(Dim.MIN_W_SCREEN.doubleVal());
            primaryStage.setMaxWidth(Screen.getPrimary().getVisualBounds().getWidth());
            primaryStage.setMaxHeight(Screen.getPrimary().getVisualBounds().getHeight());
            primaryStage.setOnCloseRequest(
                    event -> System.exit(1)
            );

            //// BORDER PANE PROPERTIES
            StackPane sidesNull = new StackPane();
            sidesNull.setPrefHeight(0);
            sidesNull.setPrefWidth(0);
            this.root.setRight(sidesNull);
            this.root.getRight().setStyle("-fx-background-color: red");

            ////// LEFT CONTROLS
            this.setUpLeftPanel();
            this.root.setLeft(this.leftPanel);

            //// SETTING UP SCENE
            this.baseHorizContainer = new HBox();
            this.baseHorizContainer.getChildren().addAll(this.playingPanel());
            this.root.setCenter(this.baseHorizContainer);

            ////// BOTTOM CONTROLS
            Pane bottom = setUpBottom();
            this.root.setBottom(bottom);
            bottom.setId("controlBtnPane");
            bottom.setStyle("-fx-padding: 0 100pt;");

            //// PARSE STYLESHEET
            this.baseScene.getStylesheets().add(getClass().getResource("/mvc/view/stylesheets/styles.css").toExternalForm());

            this.setUpListeners();

            primaryStage.show();
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(() -> {
////            System.out.println("OBSDEREREREREERE");
            new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    playlistCount.setText("" + controller.getPlaylistCount());
                    updatePlaylistView();
                    updateBottom();
                    return null;
                }
            }.run();
        });
    }


    /////////////////////// PRIVATE METHODS

    private void setUpLeftPanel(){
        VBox left = new VBox();
        Label leftPanelHeaderLabel = new Label("Meine Playlists");
        leftPanelHeaderLabel.setPadding(new Insets(0, 0, 0, Dim.PAD_SIDE_LIST.doubleVal()));
        leftPanelHeaderLabel.setId("leftPanelHeaderLabel");

//        this.playlistCount = new Label(mp3.getPlaylists().size() + " Playlists");
        this.playlistCount.setPadding(new Insets(0, Dim.PAD_SIDE_LIST.doubleVal(), 0, 0));
        this.playlistCount.setAlignment(Pos.CENTER_RIGHT);
        try {
            this.playlistCount.setText(this.playlistListView.getItems().size() + "");
        }
        catch (NullPointerException npe){
            this.playlistCount.setText("0");
        }

        ControlButton addPlaylist = new ControlButton("");
        addPlaylist.setId("newPlaylist");
        addPlaylist.setOnMouseClicked(
                event -> controller.addSongs(primaryStage)
        );

        this.leftPanelHeader = new GridPane();
        this.leftPanelHeader.setId("leftPanelHeader");
        this.leftPanelHeader.addColumn(0, leftPanelHeaderLabel);
        this.leftPanelHeader.setHgap(50);
        this.leftPanelHeader.addColumn(2, this.playlistCount);
        this.leftPanelHeader.addRow(1, new Label(controller.getPlistPath()));
        this.leftPanelHeader.addRow(2, addPlaylist);
        this.leftPanelHeader.setMinHeight(75);
        this.leftPanelHeader.setPrefHeight(90);
        this.leftPanelHeader.setMaxHeight(100);
        this.leftPanelHeader.setMinWidth(200);
        this.leftPanelHeader.setAlignment(Pos.BOTTOM_CENTER);

        this.leftPanel = new VBox();
        this.leftPanel.getChildren().add(leftPanelHeader);

        this.playlistListView = new ListView<>();
        this.playlistListView.setEditable(false);
        this.playlistListView.setFixedCellSize(Dim.H_CELL.doubleVal());
        this.playlistListView.setPadding(new Insets(0));

        allPlaylists = this.controller.getPlaylistData();
        this.setPlaylistViewCells();

        this.playlistListView.setOnMouseClicked(
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if(event.getClickCount() == 1) {
                            controller.setCurrentSongsData(
                                    playlistListView.getSelectionModel().getSelectedItem().getPlaylist()
                            );
                            currentSongsTable.setItems(controller.getCurrentSongs());
                        }
                    }
                }
        );
        this.leftPanel.getChildren().add(this.playlistListView);

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
            cellBox.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    createSongTable(playlist);
//                    System.out.println(playlistListView.getSelectionModel().getSelectedItem().getTitle());
                }
            });
            this.playlistListView.getItems().add(cellBox.getGrid());
        }
    }

    /**
     * This method creates a {@link TableView table}, which shows all songs, of the current playlist.
     */
    private void createSongTable(){
        this.createSongTable(null);
    }

    /**
     * This method creates a {@link TableView table}, which shows all songs, of the current playlist.
     * @param playlist
     */
    private void createSongTable(Playlist playlist){
        this.currentSongsTable = new TableView<>();

        this.currentSongsTable.setPrefHeight(
                Dim.MIN_H_SCREEN.doubleVal() -
                this.currentCoverImg.getViewport().getHeight()
        );

        TableColumn<Song, String> title = new TableColumn<>("Songtitle");
        title.setId("songsTable");
        title.setMinWidth(300);
        title.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<Song, String> artist = new TableColumn<>("Artist");
        artist.setMinWidth(125);
        artist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        TableColumn<Song, String> length = new TableColumn<>("Length");
        length.setMinWidth(80);
        length.setCellValueFactory(new PropertyValueFactory<>("length"));

        this.currentSongsTable.setItems(this.controller.getCurrentSongs());
        this.currentSongsTable.getColumns().addAll(title, artist, length);
        this.currentSongsTable.setOnMouseClicked(
                event ->  {
                    Platform.runLater(() -> {
                        new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                System.out.printf(event.getClickCount() + "");
                                if(event.getClickCount() == 2) {
                                    controller.play(currentSongsTable.getSelectionModel().getSelectedItem());
                                }
                                return null;
                            }
                        }.run();
                    });
                }
        );

        return;
    }

    private void setUpListeners(){
        baseScene.widthProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
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
                }
        );
        baseScene.heightProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
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
                }
        );
        playlistListView.setOnMouseClicked(
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        try {
                            controller.setCurrentSongsData(
                                    playlistListView.getSelectionModel().getSelectedItem().getPlaylist()
                            );
                        }
                        catch (NullPointerException e){
                            // do nothing
                            System.out.println("===== NOTHING TO BE SELECTED");
                        }
                        currentSongsTable.setItems(
                                controller.getCurrentSongs()
                        );
                        currentSongsTable.refresh();
                        playlistListView.refresh();
                    }
                }
        );
    }

    private AnchorPane setUpBottom(){
        AnchorPane rootAnchor = new AnchorPane();
        rootAnchor.setMinHeight(Dim.H_ROOT_BOTTOM.doubleVal());
        rootAnchor.setPrefHeight(Dim.H_ROOT_BOTTOM.doubleVal());
        rootAnchor.setMaxHeight(Dim.H_ROOT_BOTTOM.doubleVal());
        ControlButton play, pause, next, stop, mute, prev, shuffle;
        Slider volume;

        shuffle = new ControlButton("PLAY");
        shuffle.setId("newPlaylist");
        shuffle.setOnMouseClicked(
                event -> {
                    Platform.runLater(() -> {
                        new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                controller.shuffle();
                                return null;
                            }
                        }.run();
                    });
                }
        );

        play = new ControlButton("PLAY");
        play.addEventHandler(
                ActionEvent.ANY, event -> {
                    Platform.runLater(() -> {
                        controller.play();
                    });
                }
        );
        pause = new ControlButton("PAUSE");
        pause.addEventHandler(
                ActionEvent.ANY, event -> {
                    Platform.runLater(() -> {
                        controller.pause();
                    });
                }
        );
        stop = new ControlButton("STOP");
        stop.addEventHandler(
                ActionEvent.ANY, event -> {
                    Platform.runLater(() -> {
                        controller.stop();
                    });
                }
        );
        next = new ControlButton("NEXT");
        next.addEventHandler(
                ActionEvent.ANY, event -> {
                    Platform.runLater(() -> {
                        controller.next();
                    });
                }
        );
        prev = new ControlButton("PREV");
        prev.addEventHandler(
                ActionEvent.ANY, event -> {
                    Platform.runLater(() -> {
                        controller.prev();
                    });
                }
        );
        mute = new ControlButton("MUTE");
        mute.addEventHandler(
                ActionEvent.ANY, event -> {
                    Platform.runLater(() -> {
                        controller.mute();
                    });
                }
        );

        volume = new Slider();
        volume.setMin(0);
        volume.setMax(100);
        volume.setMajorTickUnit(50);
        volume.setMinorTickCount(0);
        volume.setShowTickMarks(true);
        volume.setSnapToTicks(false);
        volume.setOnMouseDragged(
                event -> controller.setVolume((float) volume.getValue())
        );

        //// GLOBAL VARS WHICH CAN BE UPDATED
        Label currPos = new Label();
        this.currentPosProp = new SimpleStringProperty();
        currPos.textProperty().bindBidirectional(this.currentPosProp);


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
        controlBtns.getChildren().addAll(prev, play, stop, next, mute, volume, songPos, currPos, shuffle);

        rootAnchor.getChildren().addAll(controlBtns);

        return rootAnchor;
    }

    private AnchorPane playingPanel(){
        AnchorPane cover = new AnchorPane();

        this.currentCoverImg = new PlaylistSongCover();
        this.currentCoverImg.setImage(new Image("assets/covers/default-release-cd.png"));
        System.out.println("==== " + this.currentCoverImg.getBoundsInParent().getWidth());
        System.out.println(this.currentCoverImg.getBoundsInParent().getHeight());
        this.currentCoverImg.setPreserveRatio(true);
        this.currentCoverImg.setViewport(new Rectangle2D(
                0,
                this.currentCoverImg.getBoundsInParent().getHeight() * 0.25,
                this.currentCoverImg.getBoundsInParent().getWidth(),
                this.root.getHeight() * 0.4
        ));
        this.currentCoverImg.setFitWidth(
                Dim.MIN_W_SCREEN.doubleVal() -
                        this.root.getLeft().getBoundsInParent().getWidth()
        );

        cover.getChildren().add(this.currentCoverImg);

        this.rightPanel = new VBox();
        this.rightPanel.getChildren().add(this.currentCoverImg);

        this.createSongTable();
        this.rightPanel.getChildren().add(this.currentSongsTable);
        cover.getChildren().add(this.rightPanel);
        return cover;
    }

    private void updatePlaylistView(){
        this.allPlaylists = FXCollections.observableArrayList(controller.getAllPlaylists());
        this.setPlaylistViewCells();
        this.playlistListView.refresh();
    }

    private void updateBottom(){
        System.out.printf("-- ");
        this.currentPosProp.set(
                this.controller.getCurrentSongPosition().get()
        );
    }


}
