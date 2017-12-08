package mvc.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import mvc.model.Model;
import mvc.model.extension.enums.Skip;
import mvc.model.playlist.Playlist;
import mvc.model.playlist.Song;
import mvc.view.AddPlaylistView;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import static javafx.application.Application.launch;

public class Controller extends Observable {

    /////////////////////// VARIABLES

    private volatile Model model;

    private ObservableList<Playlist> allPlaylists;
    private SimpleListProperty<Song> currentSongs;
    private SimpleStringProperty currentSongLength;
    private SimpleStringProperty currentSongPosition;
    private SimpleFloatProperty currentVolume;
    private SimpleIntegerProperty playlistCount;
    private SimpleStringProperty plistPath;
    private IntegerBinding allPlaylistsSize;

    private static ModelObserver modelObserver;
    private static AddPlaylistView smallWindow;
    private static DragAndDrogObserver dndObserver;

    boolean playlistChanged = false;


    /////////////////////// CONSTRUCTOR

    public Controller(){
        this.model = new Model();
        this.currentSongs = new SimpleListProperty<>();
//        this.allPlaylists = new SimpleListProperty<>();
        this.currentVolume = new SimpleFloatProperty();
        this.currentSongLength = new SimpleStringProperty(this.setTimeFormat(0));
        this.currentSongPosition = new SimpleStringProperty(this.setTimeFormat(0));
        this.playlistCount = new SimpleIntegerProperty(0);
        System.out.println(model);
        this.plistPath = new SimpleStringProperty(model.getPlistPath());

        this.currentSongs.set(this.model.getCurrentSongsObservable());
        this.setAllPlaylists();
        this.allPlaylistsSize = Bindings.size(this.allPlaylists);
        this.currentVolume.set(this.model.getCurrentVol());

        //// OBSERVERS & VIEWS
        modelObserver = new ModelObserver();
        smallWindow = new AddPlaylistView();
        dndObserver = new DragAndDrogObserver();
    }


    //////////////////////// MODEL COMMUNICATION
    public void play(){
        model.play();
    }

    public void play(Song songToPlay){
        this.model.play(songToPlay);
    }

    public void pause(){
        this.model.pause();
    }

    public void stop(){
        this.model.stop();
    }

    public void mute(){
        this.model.mute();
    }

    public void backward(){
        this.skip(Skip.BACKWARD);
    }

    public void forward(){
        this.skip(Skip.FORWARD);
    }

    public void prev(){
        this.skip(Skip.PREVIOUS);
    }

    public void next(){
        this.skip(Skip.NEXT);
    }

    private void skip(Skip value){
        this.model.skip(value);
    }

    public void shuffle(){
        this.model.toggleShuffle();
    }

    public void setVolume(float val){
        this.model.setVol(val);
    }

    public void addSongs(Stage primaryStage){
        smallWindow.addObservers(dndObserver);
        smallWindow.start();
    }

    /////////////////////// MODEL COMMUNICATION END


    /////////////////////// PUBLIC METHODS

    private void notifyChanges(){
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        setChanged();
                        notifyObservers();
//                        System.out.println("==========");
//                        System.out.println("==========");
//                        System.out.println("==========");
//                        System.out.println("214365787");
//                        System.out.println("==========");
//                        System.out.println("==========");
                    }
                }
        ).start();
    }


    /////////////////////// PRIVATE METHODS

    private void setAllPlaylists(){
        this.allPlaylists = FXCollections.observableArrayList(this.model.getPlaylistsObservable());
        for(int i = 0; i > allPlaylists.size(); i++){
            System.out.println(i+1 + ".) " + allPlaylists.get(i).getTitle());
        }
    }

    private String setTimeFormat(long milliseconds){
        String string =  String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.SECONDS.toSeconds(
                        TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds))
                )
        );
//        System.out.printf(string);
        return string;
    }


    /////////////////////// GETTERS

    public int getPlaylistCount() {
        return playlistCount.get();
    }

    public final SimpleStringProperty getPlayerPosition(){
        return this.currentSongPosition;
    }

    public final SimpleStringProperty getCurrentSongLength() {
        return this.currentSongLength;
    }

    public final SimpleStringProperty getCurrentSongPosition() {
        return this.currentSongPosition;
    }

    public final ObservableList<Playlist> getAllPlaylists() {
        return allPlaylists;
    }

    public final ObservableList<Song> getCurrentSongs() {
        return currentSongs.get();
    }

    public final ObservableList<Song> getSongsData(Playlist playlist){
        try {
            ObservableList<Song> songs = FXCollections.observableArrayList(
                    this.currentSongs
            );
            return songs;
        }
        catch (NullPointerException npe){
            return null;
        }
    }

    public final ObservableList<Playlist> getPlaylistData(){
        ObservableList<Playlist> playlists = FXCollections.observableArrayList(
                this.model.getPlaylists()
        );
        return playlists;
    }

    public final String getPlistPath() {
        return plistPath.get();
    }

    /////////////////////// SETTERS

    public void setCurrentSongsData(Playlist selected){
        this.currentSongs.removeAll();
        this.currentSongs.setAll(selected.getSongs());

//        this.model.addPlaylist(selected);
    }


    /////////////////////// PRIVATE CLASSES

    private class ModelObserver implements Observer{

        private ModelObserver(){
            model.addObserver(this);
        }

        @Override
        public void update(Observable o, Object arg) {
            currentSongs.set(
                    model.getCurrentSongsObservable()
            );
            currentVolume.set(
                    model.getCurrentVol()
            );
            currentSongLength.set(
                    setTimeFormat(model.getCurrentSong().getLengthSeconds())
            );
            currentSongPosition.set(
                    setTimeFormat(
                            model.getCurrentPosition()
                    )
            );
            playlistCount.bind(
                    allPlaylistsSize
            );

            playlistChanged = true;

            if(playlistChanged) {
                setAllPlaylists();
                playlistChanged = false;
            }
            notifyChanges();
        }
    }

    /**
     * This class is an addon to the {@link Controller}. With this class an
     * {@link Observer} will be added to fot the {@link AddPlaylistView
     * Drag'n'Drop window}, where the user can create a new {@link Playlist}
     * via drag and drop.
     */
    private class DragAndDrogObserver implements Observer{
        @Override
        public void update(Observable o, Object arg) {
            Platform.runLater(() -> {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (smallWindow.hasSongs()) {
                            model.newPlaylist(
                                    smallWindow.getTitle(),
                                    smallWindow.getFiles()
                            );
                            notifyObservers();
                        }
                    }
                }).start();
            });
        }
    }

}
