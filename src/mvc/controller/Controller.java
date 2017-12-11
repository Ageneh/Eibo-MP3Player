package mvc.controller;

import com.sun.javafx.beans.event.AbstractNotifyListener;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import misc.ANSI;
import mvc.model.MP3Player;
import mvc.model.extension.ImageConverter;
import mvc.model.extension.enums.Skip;
import mvc.model.playlist.Playlist;
import mvc.model.playlist.Song;
import mvc.view.AddPlaylistView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

public class Controller extends Observable {

    /////////////////////// VARIABLES

//    private volatile MP3Player model;

    private SimpleListProperty<Playlist> allPlaylists;
    /**
     * Saves the current, selected {@link Playlist}, which is not necessarily playing.
     * <br>Used to show its {@link Playlist#songs songs}.</br>
     */
    private SimpleObjectProperty<Playlist> currentPlaylist;
    private SimpleListProperty<Song> currentSongs;
    private SimpleObjectProperty<Song> currentSong;
    /**
     * A {@link SimpleStringProperty string property} which saves the length of the {@link MP3Player#getCurrentSong()
     * current song}.
     */
    private SimpleStringProperty currentSongLength;
    /**
     * A {@link SimpleStringProperty string property} which saves the position of the {@link MP3Player#player player}.
     */
    private SimpleStringProperty currentSongPosition;
    /**
     * A {@link SimpleStringProperty string property} which saves the current path of the folder where all playlists
     * are located.
     * @see mvc.model.playlist.PlaylistManager#rootPath
     */
    private SimpleStringProperty playlistFolderPath;
    /**
     * A {@link SimpleFloatProperty float property} which saves the volume of the {@link MP3Player#player player}.
     */
    private SimpleFloatProperty currentVolume;
    /**
     * An {@link SimpleIntegerProperty integer property} which saves the number of available {@link Playlist playlists}.
     * <br>Uses the {@link java.util.ArrayList#size()} of the {@link MP3Player#getPlaylists() array-list containing all playlists}.</br>
     */
    private SimpleIntegerProperty playlistCount;
    private IntegerBinding allPlaylistsSize;

    private MP3Player model;

    private byte[] coverImg;

    private static AddPlaylistView smallWindow;
    private static DragAndDrogObserver dndObserver;
    private static ModelObserver modelObserver;


    /////////////////////// CONSTRUCTOR

    public Controller(){
        this.model = new MP3Player();

        this.allPlaylists = new SimpleListProperty<>(FXCollections.observableList(model.getPlaylists()));

        this.playlistCount = new SimpleIntegerProperty(this.allPlaylists.size());
        this.playlistCount.bind(
                Bindings.size(this.allPlaylists)
        );

        this.setAllPlaylists();

        this.currentPlaylist = new SimpleObjectProperty<>(model.getCurrentPlaylist());
        this.currentPlaylist.addListener(
                (observable, oldValue, newValue) -> {
                    setChanged();
                    notifyObservers();
                }
        );

        this.currentSongLength = new SimpleStringProperty(this.setTimeFormat(0));

        this.currentSong = new SimpleObjectProperty<>(
                this.model.getCurrentSong()
        );
        try {
            this.coverImg = model.getCurrentSong().getCover();
        }
        catch (NullPointerException e){
            // DO NOTHING
        }

        this.currentSongs = new SimpleListProperty<>();
        try {
            this.currentSongs.bind(
                    this.currentPlaylist.get().getSongsObservable()
            );
        }
        catch (NullPointerException e){
            // DO NOTHING
        }

        this.currentVolume = new SimpleFloatProperty();
        this.currentVolume.set(this.model.getCurrentVol());
        this.currentVolume.addListener(
                (observable, oldValue, newValue) -> {
                    setChanged();
                    notifyChanges();
                }
        );

        this.currentSongPosition = new SimpleStringProperty(this.setTimeFormat(0));

        this.playlistFolderPath = new SimpleStringProperty(
                model.getPlistPath()
        );

        //// OBSERVERS & VIEWS
        modelObserver = new ModelObserver();
        dndObserver = new DragAndDrogObserver();
        smallWindow = new AddPlaylistView();
        smallWindow.addObservers(dndObserver);
    }

    public Controller( String a){
//        this.model = new MP3Player();
//
//        this.allPlaylists = new SimpleListProperty<>(FXCollections.observableArrayList(model.getPlaylists()));
//
//        this.playlistFolderPath = new SimpleStringProperty(model.getPlistPath());
////        this.currentPlaylist = this.model.getCurrentPlaylist();
//        this.playlistCount = new SimpleIntegerProperty(
//                this.allPlaylists.size()
//        );
//        this.allPlaylistsSize = Bindings.size(this.allPlaylists);
//        this.playlistCount.bind(
//                this.allPlaylistsSize
//        );
//
//        this.currentSongs = new SimpleListProperty<>(
//                FXCollections.observableList(this.model.getCurrentSongs())
//        );
//        this.currentSongLength = new SimpleStringProperty(
//                this.setTimeFormat(this.model.getCurrentSong().getLengthMillis())
//        );
//        this.currentSongPosition = new SimpleStringProperty(this.setTimeFormat(0));
//
//        this.currentVolume = new SimpleFloatProperty(this.model.getCurrentVol());
//
//        //// OBSERVERS & VIEWS
//        modelObserver = new ModelObserver();
//        smallWindow = new AddPlaylistView();
//        dndObserver = new DragAndDrogObserver();
    }


    //////////////////////// MODEL COMMUNICATION

    public void loadPlaylist(){
        this.model.load(currentPlaylist.get());
    }

    /**
     * {@link MP3Player#play()}
     */
    public void play(){
        model.play();
    }

    /**
     * {@link MP3Player#play(Song)}
     */
    public void play(Song songToPlay){
        model.play(songToPlay);
    }

    /**
     * {@link MP3Player#pause()}
     */
    public void pause(){
        this.model.pause();
    }

    /**
     * {@link MP3Player#stop()}
     */
    public void stop(){
        this.model.stop();
    }

    /**
     * {@link MP3Player#mute()}
     */
    public void mute(){
        this.model.mute();
    }

    /**
     * {@link MP3Player#skip(Skip)}
     */
    public void skip(Skip val){
        model.skip(val);
    }

    /**
     * {@link MP3Player#setVolume(float)}
     */
    public void setVolume(float val){
        this.model.setVolume(val);
        this.currentVolume.set(model.getCurrentVol());
    }

    /**
     * Starts and shows the drag'n'drop view where the user is able to create {@link Playlist playlists}.
     * @see AddPlaylistView
     */
    public void addSongs(){
        smallWindow.start();
    }

    /**
     * {@link MP3Player#toggleShuffle()}
     */
    public void toggleShuffle(){
        this.model.toggleShuffle();
    }


    /////////////////////// PUBLIC METHODS

    private void notifyChanges(){
        setChanged();
        notifyObservers();
    }


    /////////////////////// PRIVATE METHODS

    private void setAllPlaylists(){
        this.allPlaylists = new SimpleListProperty<>(FXCollections.observableList(model.getPlaylists()));
//        this.allPlaylists.setAll(
//                FXCollections.observableList(model.getPlaylists())
//        );
    }

    private String setTimeFormat(long milliseconds){
        return String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.SECONDS.toSeconds(
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(milliseconds)
                        )
                )
        );
    }


    /////////////////////// GETTERS

    public boolean isShuffle(){
        return model.isShuffle();
    }

    public float getVolume(){
        return model.getCurrentVol();
    }

    /**
     * @return Returns the number of songs inside the current playlist.
     */
    public SimpleIntegerProperty getPlaylistCount() {
        return playlistCount;
    }

    public String getPlaylistFolderPath() {
        return playlistFolderPath.get();
    }

    /**
     * @return Returns the {@link #coverImg current cover image} of the {@link
     * MP3Player#getCurrentSong() current song}.
     */
    public Image getCoverImg(){
        try {
            return ImageConverter.convertToJavaFXImage(
                    currentSong.get().getCover()
            );
        }
        catch (NullPointerException e){
            return null;
        }
    }

    /**
     * @return Returns a {@link SimpleStringProperty} of {@link #currentSongLength}.
     */
    public SimpleStringProperty getCurrentSongLength() {
        return this.currentSongLength;
    }

    public SimpleStringProperty getCurrentSongPosition() {
        return this.currentSongPosition;
    }

    public ObservableList<Playlist> getAllPlaylists() {
        return allPlaylists;
    }

    public ObservableList<Song> getCurrentSongs() {
        return currentSongs.get();
    }

    public ObservableList<Playlist> getPlaylistData(){
        return FXCollections.observableArrayList(
                this.model.getPlaylists()
        );
    }

    public SimpleObjectProperty<Playlist> getCurrentPlaylist() {
        return currentPlaylist;
    }


    /////////////////////// SETTERS

    public void setCurrentPlaylist(Playlist playlist){
        this.currentPlaylist.set(playlist);
    }

    public void setCurrentSong(Song song){
        this.model.getCurrentPlaylist().setCurrentSong(song);
    }


    /////////////////////// PRIVATE CLASSES

    /**
     * An observer specifically for the {@link MP3Player}.
     * <br>Implements {@linkplain Observer}.</br>
     */
    private class ModelObserver implements Observer{

        private ModelObserver(){
            model.addObserver(this);
        }

        @Override
        public void update(Observable o, Object arg) {
            System.out.print(model.getCurrentSong().getTitle());
            System.out.println(", " + model.getCurrentPosition());

            allPlaylists.set(FXCollections.observableList(model.getPlaylists()));
            currentPlaylist.set(model.getCurrentPlaylist());
            currentSong.set(currentPlaylist.get().getCurrentSong());

            setChanged();
            notifyChanges();
        }
    }

    /**
     * This class is an addon to the {@link Controller}. With this class an
     * {@link Observer} will be added to fot the {@link AddPlaylistView
     * Drag'n'Drop window}, where the user can create a new {@link Playlist}
     * via drag and drop.
     * <br>Implements {@linkplain Observer}.</br>
     */
    private class DragAndDrogObserver implements Observer{
        @Override
        public void update(Observable o, Object arg) {
            if (smallWindow.hasSongs()) {
                model.newPlaylist(
                        smallWindow.getTitle(),
                        smallWindow.getFiles()
                );
                notifyObservers();
            }
        }
    }

}
