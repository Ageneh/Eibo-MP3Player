package mvc.controller;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import misc.ANSI;
import misc.TimeConverter;
import mvc.model.MP3Player;
import mvc.model.extension.ImageConverter;
import mvc.model.extension.enums.Skip;
import mvc.model.playlist.Playlist;
import mvc.model.playlist.Song;
import mvc.view.AddPlaylistView;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class Controller extends Observable {

    /////////////////////// VARIABLES

//    private volatile MP3Player model;

    private MP3Player model;

    private SimpleListProperty<Playlist> allPlaylists;
    /**
     * Saves the current, selected {@link Playlist}, which is not necessarily playing.
     * <br>Used to show its {@link Playlist#songs songs}.</br>
     */
    private SimpleObjectProperty<Playlist> currentPlaylist;

    private SimpleListProperty<Song> currentSongs;

    private Song currentSong;
    /**
     * A {@link SimpleStringProperty string property} which saves the length of the {@link MP3Player#getCurrentSong()
     * current song}.
     */
    private SimpleLongProperty currentSongLength;
    /**
     * A {@link SimpleStringProperty string property} which saves the position of the {@link MP3Player#player player}.
     */
    private SimpleLongProperty currentSongPosition;
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

    private byte[] coverImg;

    private static AddPlaylistView smallWindow;
    private static DragAndDrogObserver dndObserver;
    private static ModelObserver modelObserver;


    /////////////////////// CONSTRUCTOR

    public Controller(){
        this.model = new MP3Player();
        ANSI.CYAN.println("=== Player initialized");
        this.currentSong = model.getCurrentSong();
        ANSI.CYAN.println("=== Current Song initialized");
        ANSI.CYAN.println("=== " + currentSong);

        //// PROPERTIES
        this.allPlaylists = new SimpleListProperty<>(
                model.getPlaylists()
        );
        System.out.println(model.getCurrentSong().getTitle());
        ANSI.CYAN.println("=== PlaylistCount initialized");
        ANSI.CYAN.println("=== Count " + allPlaylists.size());

        System.out.println(model.getCurrentSong().getTitle());
        this.currentPlaylist = new SimpleObjectProperty<>(
                model.getCurrentPlaylist()
        );
        System.out.println(model.getCurrentSong().getTitle());
        this.currentPlaylist.addListener(
                (observable, oldValue, newValue) -> {
                    this.setSongs();
                }
        );
        ANSI.CYAN.println("=== CurrentPlaylist initialized");
        ANSI.CYAN.println("=== " + currentPlaylist.get().getTitle());
        this.currentSongs = new SimpleListProperty<>(
                FXCollections.observableList(this.currentPlaylist.get().getSongs())
        );
        this.setSongs();
        ANSI.CYAN.println("=== All Songs initialized");
        this.currentSongLength = new SimpleLongProperty(
                currentSong.getLengthMillis()
        );
        ANSI.CYAN.println("=== Current Song length initialized");


        this.currentSongLength.bind(
                currentSong.getLengthMillisProp()
        );
        this.currentSongPosition = new SimpleLongProperty(
                    model.getCurrentPosition()
        );
        ANSI.CYAN.println("=== Current position initialized");
        this.playlistFolderPath = new SimpleStringProperty(
                model.getPlistPath()
        );
        ANSI.CYAN.println("=== Path initialized");
        this.currentVolume = new SimpleFloatProperty(
                model.getCurrentVol()
        );
        ANSI.CYAN.println("=== Volume initialized");

        this.coverImg = currentSong.getCover();


        //// OBSERVERS & VIEWS
        modelObserver = new ModelObserver();
        dndObserver = new DragAndDrogObserver();
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
//                this.setTimeFormatStd(this.model.getCurrentSong().getLengthMillis())
//        );
//        this.currentSongPosition = new SimpleStringProperty(this.setTimeFormatStd(0));
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
        smallWindow = new AddPlaylistView();
        smallWindow.addObservers(dndObserver);
        smallWindow.show();
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

    private void setSongs(){
        this.currentSongs.setAll(
                FXCollections.observableList(this.currentPlaylist.get().getSongs())
        );
    }


    /////////////////////// GETTERS

    public boolean isShuffle(){
        return model.isShuffle();
    }

    public float getVolume(){
        return model.getCurrentVol();
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
                    currentSong.getCover()
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
        return new SimpleStringProperty(TimeConverter.setTimeFormatStd(
                this.currentSongLength.get()));
    }

    public SimpleLongProperty getCurrentSongPosition() {
        return this.currentSongPosition;
    }

    public ArrayList<Playlist> getAllPlaylists() {
        return model.getPlaylistsArray();
    }

    public SimpleListProperty<Song> getCurrentSongs() {
        return currentSongs;
    }

    public ObservableList<Playlist> getPlaylistData(){
        return FXCollections.observableArrayList(
                this.model.getPlaylists()
        );
    }

    public SimpleObjectProperty<Playlist> getCurrentPlaylist() {
        return currentPlaylist;
    }

    public SimpleObjectProperty<Song> getCurrentSong() {
        return new SimpleObjectProperty<Song>(currentSong);
    }

    /////////////////////// SETTERS

    public void setSelectedPlaylist(Playlist playlist){
        this.currentPlaylist.set(playlist);
//        this.setSongs();
    }

    public void playCurrentSong(Song song){
        this.model.play(currentPlaylist.get(), song);
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
//            System.out.print(model.getCurrentSong().getTitle());
//            System.out.println(", " + model.getCurrentPosition());

            allPlaylists = new SimpleListProperty<>(
                    FXCollections.observableList(model.getPlaylists())
            );

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
