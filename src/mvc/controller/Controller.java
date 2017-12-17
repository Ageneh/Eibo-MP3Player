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
import mvc.model.playlist.SongAssets;
import mvc.view.AddPlaylistView;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class Controller extends Observable {
    /////////////////////// VARIABLES

    private SimpleObjectProperty<MP3Player> model;
    private byte[] coverImg;
    private Playlist playlist;

    private static AddPlaylistView smallWindow;
    private static DragAndDrogObserver dndObserver;
    private static ModelObserver modelObserver;


    /////////////////////// CONSTRUCTOR

    public Controller(){
        this.model = new SimpleObjectProperty<>(new MP3Player());
        //// OBSERVERS & VIEWS
        modelObserver = new ModelObserver();
        dndObserver = new DragAndDrogObserver();
        this.checkForSongs();

        try {
            this.coverImg = model.get().getCurrentSong().getCover();
        }
        catch (NullPointerException e){
            this.coverImg = new SongAssets().getSongCover();
        }
    }


    //////////////////////// MODEL COMMUNICATION

    public void checkForSongs(){
        if(model.get().getPlaylists().isEmpty()){
            this.addSongs(true);
        }
        this.setSelectedPlaylist(model.get().getCurrentPlaylist());
    }

    /**
     * Starts and shows the drag'n'drop view where the user is able to create {@link Playlist playlists}.
     * @see AddPlaylistView
     */
    public void addSongs(){
        addSongs(false);
    }

    public void addSongs(boolean firstStart){
        smallWindow = new AddPlaylistView(firstStart);
        smallWindow.addObservers(dndObserver);
        smallWindow.show();
    }

    /**
     * {@link MP3Player#mute()}
     */
    public void mute(){
        this.model.get().mute();
    }

    /**
     * {@link MP3Player#play()}
     */
    public void play(){
        model.get().play();
    }

    public void playCurrentSong(Song song){
        this.model.get().play(playlist, song);
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

    public ArrayList<Playlist> getAllPlaylists() {
        return model.get().getPlaylistsArray();
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

    public Playlist getSelectedPlaylist() {
        return playlist;
    }

    public boolean isPlaying(){
        return model.get().isPlaying();
    }

    /////////////////////// SETTERS

    public void setSelectedPlaylist(Playlist playlist){
        this.playlist = playlist;
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
            Controller.this.setChanged();
            Controller.this.notifyChanges();
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
                setChanged();
                notifyObservers();
            }
        }
    }

}
