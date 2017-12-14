package mvc.controller;

import javafx.beans.InvalidationListener;
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

    private SimpleObjectProperty<MP3Player> model;
    private byte[] coverImg;

    private static AddPlaylistView smallWindow;
    private static DragAndDrogObserver dndObserver;
    private static ModelObserver modelObserver;


    /////////////////////// CONSTRUCTOR

    public Controller(){
        this.model = new SimpleObjectProperty<>(new MP3Player());

        this.coverImg = model.get().getCurrentSong().getCover();

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

    /**
     * Starts and shows the drag'n'drop view where the user is able to create {@link Playlist playlists}.
     * @see AddPlaylistView
     */
    public void addSongs(){
        smallWindow = new AddPlaylistView();
        smallWindow.addObservers(dndObserver);
        smallWindow.show();
    }

    public void loadPlaylist(Playlist selectedPlaylist){
        this.model.get().load(selectedPlaylist);
    }

    /**
     * {@link MP3Player#mute()}
     */
    public void mute(){
        this.model.get().mute();
    }

    /**
     * {@link MP3Player#pause()}
     */
    public void pause(){
        this.model.get().pause();
    }

    /**
     * {@link MP3Player#play()}
     */
    public void play(){
        model.get().play();
    }

    /**
     * {@link MP3Player#play(Song)}
     */
    public void play(Song songToPlay){
        model.get().play(songToPlay);
    }

    public void playCurrentSong(Playlist selectedPlaylist, Song song){
        this.model.get().play(selectedPlaylist, song);
    }

    /**
     * {@link MP3Player#setVolume(float)}
     */
    public void setVolume(float val){
        this.model.get().setVolume(val);
    }

    /**
     * {@link MP3Player#skip(Skip)}
     */
    public void skip(Skip val){
        model.get().skip(val);
    }

    /**
     * {@link MP3Player#stop()}
     */
    public void stop(){
        this.model.get().stop();
    }

    /**
     * {@link MP3Player#toggleShuffle()}
     */
    public void toggleShuffle(){
        this.model.get().toggleShuffle();
    }

    public void setPosition(long position){
        model.get().setPosition(position);
    }


    /////////////////////// GETTERS

    public boolean isShuffle(){
        return model.get().isShuffle();
    }

    public long getCurrentSongPosition(){
        return this.model.get().getCurrentPosition();
    }

    public float getVolume(){
        return model.get().getCurrentVol();
    }

    /**
     * @return Returns the {@link #coverImg current cover image} of the {@link
     * MP3Player#getCurrentSong() current song}.
     */
    public Image getCoverImg(){
        try {
            return ImageConverter.convertToJavaFXImage(
                    this.model.get().getCurrentSong().getCover()
            );
        }
        catch (NullPointerException e){
            return null;
        }
    }

    public Song getCurrentSong() {
        return this.model.get().getCurrentSong();
    }

    public String getPlaylistFolderPath() {
        return this.model.get().getPlistPath();
    }

    public ArrayList<Playlist> getAllPlaylists() {
        return model.get().getPlaylistsArray();
    }

    public SimpleListProperty<Song> getCurrentSongs() {
        return new SimpleListProperty<>(
                FXCollections.observableList(
                        this.model.get().getCurrentSongs()
                )
        );
    }

    public SimpleObjectProperty<Playlist> getCurrentPlaylist() {
        return new SimpleObjectProperty<>(
                this.model.get().getCurrentPlaylist()
        );
    }

    /**
     * @return Returns a {@link SimpleStringProperty} of the current length song length.
     */
    public SimpleStringProperty getCurrentSongLength() {
        return new SimpleStringProperty(TimeConverter.setTimeFormatStd(
                this.model.get().getCurrentSong().getLengthMillis())
        );
    }

    public ObservableList<Playlist> getPlaylistData(){
        return FXCollections.observableArrayList(
                this.model.get().getPlaylists()
        );
    }


    /////////////////////// PUBLIC METHODS

    private void notifyChanges(){
        setChanged();
        notifyObservers();
    }


    /////////////////////// PRIVATE CLASSES

    /**
     * An observer specifically for the {@link MP3Player}.
     * <br>Implements {@linkplain Observer}.</br>
     */
    private class ModelObserver implements Observer{

        private ModelObserver(){
            model.get().addObserver(this);
        }

        @Override
        public void update(Observable o, Object arg) {
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
                model.get().newPlaylist(
                        smallWindow.getTitle(),
                        smallWindow.getFiles()
                );
                notifyObservers();
            }
        }
    }

}
