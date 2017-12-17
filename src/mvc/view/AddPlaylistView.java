package mvc.view;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import misc.ANSI;
import mvc.model.extension.enums.StandardValues;
import mvc.model.extension.enums.Filetype;
import mvc.model.playlist.DataFinder;
import mvc.view.enums.Dimensions;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author Michael Heide
 * @author Henock Arega
 */
public class AddPlaylistView extends Application {

    /////////////////////// VARIABLES

    private final double MIN_OPACITY = 0.3;

    private Stage stage;

    private VBox root;
    private ArrayList<File> files;
    private ArrayList<String> filepaths;
    private SimpleStringProperty title;

    private boolean hasSongs;
    private ListView<String> listView;
    private PlaylistViewObservable observable;
    private Label supportedMessage;
    private Button load;
    private Button cancel;
    private boolean firstStart;

    private boolean isStarted;


    /////////////////////// CONSTRUCTOR

    public AddPlaylistView(boolean firstStart){
        this.firstStart = firstStart;
        ANSI.YELLOW.println("Drag and drop view created.");
        this.observable = new PlaylistViewObservable();
        this.title = new SimpleStringProperty("");
        this.stage = new Stage();
        this.filepaths = new ArrayList<>();
        this.isStarted = false;
    }


    /////////////////////// PUBLIC METHODS

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        root = new VBox();
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(
                new Insets(Dimensions.PAD_SIDE_LIST.intVal())
        );
        root.setFillWidth(true);

        HBox top = setupTop();
        top.setPadding(
                new Insets(
                        0, 0,
                        Dimensions.PAD_SIDE_LIST.intVal(),
                        0
                )
        );
        VBox.setVgrow(top, Priority.ALWAYS);
        root.getChildren().add(top);

        this.listView = new ListView<>();
        this.listView.setStyle(
                "-fx-background-color: rgba(45,45,45,0.15);"
        );
        root.getChildren().add(listView);

        this.supportedMessage = new Label(StandardValues.DRAG_MSG_STD.getString());
        this.supportedMessage.setMaxWidth(Dimensions.W_ADDPLAYLIST_WINDOW.intVal() - 20);
        this.supportedMessage.setTextAlignment(TextAlignment.CENTER);
        this.supportedMessage.setWrapText(true);
        this.supportedMessage.setMinHeight(70);
        this.supportedMessage.setTextAlignment(TextAlignment.CENTER);
        this.supportedMessage.setPadding(
                new Insets(
                        Dimensions.PAD_SIDE_LIST.intVal(),
                        0,
                        Dimensions.PAD_SIDE_LIST.intVal(),
                        0
                )
        );
        root.getChildren().add(supportedMessage);

        HBox bottom = setupButtons();
        root.getChildren().add(bottom);

        Scene scene = new Scene(root);
        setListeners();

        this.load.setOnMouseClicked(
                event -> {
                    System.out.println("ADDING PLAYLIST ---");
                    ANSI.YELLOW.println("Drag and drop view closing.");
                    if(hasSongs()) {
//                        title.set(titleField.getText());
                        observable.notifyObservers();
                        primaryStage.hide();
                    }
                }
        );
        this.cancel.setOnMouseClicked(
                event -> {
                    if(firstStart){
                        System.exit(0);
                    }
                    this.close(primaryStage);
                }
        );

        primaryStage.setTitle("Add New Playlist");
        primaryStage.setMinWidth(Dimensions.W_ADDPLAYLIST_WINDOW.doubleVal());
        primaryStage.setMinHeight(Dimensions.H_ADDPLAYLIST_WINDOW.doubleVal());
        primaryStage.setOnCloseRequest(
                event -> {
                    if(firstStart){
                        System.exit(0);
                    }
                    this.close(primaryStage);
                }
        );
        primaryStage.setScene(scene);
        primaryStage.showAndWait();
    }

    /**
     * {@link PlaylistViewObservable}
     * @param o An {@link Observer} which will watch over the {@link AddPlaylistView} window.
     */
    public void addObservers(Observer o){
        this.observable.addObserver(o);
    }

    public void show(){
        this.start(this.stage);
    }

    /**
     * @return Returns the flag, which tells whether songs have been added or not.<br> If falls, then the {@link mvc.model.playlist.Playlist} will not be created.</br>
     */
    public boolean hasSongs(){
        if(files.size() == 0){
            this.hasSongs = false;
        }
        return this.hasSongs;
    }

    public String getTitle(){
        return this.title.get();
    }

    public ArrayList<File> getFiles() {
        return files;
    }


    /////////////////////// PRIVATE METHODS

    private void close(Stage primaryStage){
        ANSI.YELLOW.println("Drag and drop view closing.");
//        observable.notifyObservers();
        primaryStage.hide();
        primaryStage.close();
    }

    /**
     * Drag'n'Drop functionality.
     * @param event The {@link DragEvent} which will call this method.
     */
    private void DnD(DragEvent event){
        this.hasSongs = true;
        Dragboard db = event.getDragboard();

        String name = db.getFiles().get(0).getName();
        boolean isDir = db.getFiles().get(0).isDirectory();
        boolean isM3U = name.endsWith(Filetype.M3U.getSuffix());
        boolean isAccepted = name.endsWith(Filetype.MP3.getSuffix())
                || isM3U
                || isDir;

        DataFinder finder = new DataFinder();
        this.filepaths = finder.findFiles(db.getFiles().get(0).getAbsolutePath(), Filetype.MP3);

        if(files == null){
            files = new ArrayList<>();
        }

        for (String path : filepaths){
            this.files.add(new File(path));
        }

        if(isDir){
            filepaths = new ArrayList<>();
            for(int i = 0; i < files.size(); i++){
                filepaths.add(files.get(i).getName());
                System.out.println(filepaths.get(i));
            }
//            listView.getItems().removeAll();
            listView.setItems(FXCollections.observableArrayList(
                    filepaths
            ));
            listView.refresh();
        }
        else if(db.hasFiles() && isAccepted){
            files.add(db.getFiles().get(0));
            filepaths.add(db.getFiles().get(0).getAbsolutePath());
            listView.getItems().add(
                    db.getFiles().get(0).getName()
            );
        }
    }

    private void setListeners(){
        root.setOnDragOver(
                event -> {
                    System.out.println("DRAG OVER");
                    isStarted = true;
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
        );
        root.setOnDragDropped(
                event -> {
                    boolean success = false;
                    if(isStarted){
                        System.out.println("DRAG DROP");
                        DnD(event);
                        if(files.size() > 0) {
                            success = true;
                            isStarted = false;
                            load.setDisable(false);
                            load.setOpacity(1);
//                            load.setStyle("-fx-background-color: rgba(180,237,176,0.67); -fx-text-fill: #239421;" +
//                                    "-fx-border-color: #239421; -fx-border-width: 2pt; -fx-border-style: inset;");
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();
                }
        );
        root.setOnDragEntered(
                event -> {
                    System.out.println("DRAG ENTER");
                    root.setStyle("-fx-background-color: #7ccde5;");
                    event.consume();
                }
        );
        root.setOnDragExited(
                event -> {
                    System.out.println("DRAG EXIT");
                    root.setStyle("-fx-background-color: #f4f4f4;");
                    isStarted = false;
                    event.consume();
                }
        );
    }

    private HBox setupButtons(){
        this.load = new Button("Create Playlist");
        this.load.setOpacity(MIN_OPACITY);
        this.load.setDisable(true);

        this.cancel = new Button("Close");
        this.cancel.setDisable(false);
        this.cancel.setCancelButton(true);

        HBox load_cancel = new HBox(load, cancel);
        load_cancel.setMinWidth(Dimensions.H_BORDERP_BOTTOM.intVal());
        load_cancel.setAlignment(Pos.CENTER);
        load_cancel.setFillHeight(true);
        load_cancel.setSpacing(Dimensions.PAD_PLAYLIST_WINDOW.intVal());

        return load_cancel;
    }

    private HBox setupTop(){
        Label playlistTitle = new Label("Set Playlist title:");
        playlistTitle.setId("mainLabel");
        playlistTitle.setPadding(
                new Insets(0, 20, 0, 0)
        );

        TextField titleField = new TextField();
        titleField.setPrefColumnCount(15);
        HBox.setHgrow(titleField, Priority.ALWAYS);
        title.bind(titleField.textProperty());

        HBox inputLine = new HBox(playlistTitle, titleField);
        return inputLine;
    }


    /////////////////////// INNER CLASS

    private class PlaylistViewObservable extends Observable{

        @Override
        public void notifyObservers() {
            setChanged();
            super.notifyObservers();
        }
    }

}
