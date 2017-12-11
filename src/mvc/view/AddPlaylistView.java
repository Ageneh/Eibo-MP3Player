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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mvc.model.extension.enums.StandardValues;
import mvc.model.extension.enums.Filetype;
import mvc.view.enums.Dim;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class AddPlaylistView extends Application {

    private final double MIN_OPACITY = 0.3;

    private FlowPane root;
    private Label fileText;
    private ArrayList<File> files;
    private SimpleStringProperty title;

    private boolean hasSongs;
    private ListView<String> listView;
    private PlaylistViewObservable observable;
    private Label supportedMessage;
    private Button load;
    private VBox vertical;

    private ArrayList<String> filepaths;

    private Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    public AddPlaylistView(){
        this.observable = new PlaylistViewObservable();
        this.title = new SimpleStringProperty("");
        this.stage = new Stage();
        this.filepaths = new ArrayList<>();
    }

    public void start(){
        this.start(this.stage);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Add New Playlist");
        primaryStage.setResizable(false);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setOnCloseRequest(
                event -> {
                    primaryStage.close();
                }
        );
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        TextField titleField = new TextField();
        titleField.setPrefColumnCount(15);
        titleField.setOnAction(
                event -> {
                    title.set(titleField.getText());
                }
        );

        Text playlistTitle = new Text("Set Playlist title:");
        playlistTitle.setId("mainLabel");

        this.load = new Button("Create Playlist");
        this.load.setOpacity(MIN_OPACITY);
        this.load.setOnMouseClicked(
                event -> {
                    if(filepaths.size() > 0) {
                        title.set(titleField.getText());
                        observable.notifyObservers();
                        primaryStage.close();
                    }
                }
        );

        this.supportedMessage = new Label(StandardValues.DRAG_MSG_STD.getString());
        this.supportedMessage.setWrapText(true);

        this.files = new ArrayList<>();

        HBox inputLine = new HBox(playlistTitle, titleField, load);
        inputLine.setPadding(
                new Insets(Dim.PAD_PLAYLIST_WINDOW.doubleVal(),
                        0,
                        Dim.PAD_PLAYLIST_WINDOW.doubleVal(),
                        0)
        );
        inputLine.setAlignment(Pos.CENTER);

        this.listView = new ListView<>();
        this.listView.setPrefHeight(
                Dim.H_ADDPLAYLIST_WINDOW.doubleVal() -
                        inputLine.getBoundsInParent().getHeight() -
                        (2* Dim.PAD_PLAYLIST_WINDOW.doubleVal())
        );
        this.listView.setEditable(true);

        this.vertical = new VBox(inputLine, listView, supportedMessage);
        this.vertical.setFillWidth(true);
        this.vertical.setAlignment(Pos.CENTER);

        this.root = new FlowPane(vertical);

        setListeners();

        Scene scene = new Scene(
                this.root,
                Dim.W_ADDPLAYLIST_WINDOW.doubleVal(),
                Dim.H_ADDPLAYLIST_WINDOW.doubleVal()
        );

        this.listView.maxHeightProperty().bind(
                scene.heightProperty().subtract(
                        inputLine.getBoundsInParent().getHeight() +
                                (4* Dim.PAD_PLAYLIST_WINDOW.doubleVal()))
        );
        this.listView.prefWidthProperty().bind(
                scene.widthProperty().subtract(
                        2* Dim.PAD_PLAYLIST_WINDOW.doubleVal())
        );

        primaryStage.setScene(scene);
        primaryStage.showAndWait();
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

        searchForFiles(db.getFiles().get(0));

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

    private void searchForFiles(File dir){
        this.searchForFiles(dir, 0, Filetype.MP3, Filetype.M3U);
    }

    private void searchForFiles(File dir, int index, Filetype ... filetype){
        File[] tempFiles = dir.listFiles();

        if(tempFiles == null){
            return;
        }
        for(File file : tempFiles){
            if(!file.isHidden()){
                if(file.isDirectory()) {
                    this.searchForFiles(file, index + 1, filetype);
                }
                else{
                    for(Filetype type : filetype) {
                        if (file.getName().endsWith(type.getSuffix())) {
                            this.files.add(file);
                        }
                    }
                }
            }
        }

    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public String getTitle(){
        return this.title.get();
    }

    private boolean isStarted = false;
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
                        root.setStyle("-fx-background-color: #f4f4f4;");
                        DnD(event);
                        if(files.size() > 0) {
                            success = true;
                            isStarted = false;
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
                    root.setStyle("-fx-background-color: #eeeeee;");
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

    /**
     * {@link PlaylistViewObservable}
     * @param o An {@link Observer} which will watch over the {@link AddPlaylistView} window.
     */
    public void addObservers(Observer o){
        this.observable.addObserver(o);
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

    private class PlaylistViewObservable extends Observable{

        @Override
        public void notifyObservers() {
            setChanged();
            super.notifyObservers();
            System.out.println("LOAD BUTTON CLICKED");
        }
    }

}
