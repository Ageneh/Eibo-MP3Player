package mvc.model.playlist;

import exceptions.NotAvailableException;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import misc.ANSI;
import mvc.model.extension.enums.StandardValues;
import mvc.model.extension.enums.Filetype;
import mvc.model.extension.m3u.M3UProcessor;

import java.io.File;
import java.util.ArrayList;

/**
 * A class used for the management of all {@link Playlist playlists} and the connection between the {@link mvc.MP3Launcher player}
 * and the actual {@link Playlist playlists} and their {@link Song songs}.
 */
public class PlaylistManager {

    /**
     * Saves the path of the folder where all {@link Playlist playlists} will be found and saved at.
     */
    private static String rootPath;

    /**
     * An {@code ArrayList<Playlist>} of all {@link Playlist playlists} which are found in the {@link #rootPath}.
     */
    private ArrayList<Playlist> playlists;
    private ObservableList<Playlist> playlistsObservable;
    private ObservableList<Song> playlistSongsObservable;
    private int currentPlistIndex;

    private SimpleObjectProperty<File> playlistDir;

    /**
     * A flag which will be set {@code true} if the {@link #getCurrentPlaylist() current playlist}
     * is to be played in toggleShuffle mode.
     * <br>Can be changed via {@link #toggleShuffle()} and accessed via {@link #isShuffle()}.</br>
     */
    private boolean isShuffle;


    /////////////////////// CONSTRUCTORS

    public PlaylistManager(){
        this(StandardValues.STD_PLAYLIST_ROOT.getString());
    }

    public PlaylistManager(String rootPath){
        PlaylistManager.rootPath = rootPath;

        this.playlistDir = new SimpleObjectProperty<>(
                new File(rootPath)
        );

        this.playlistSongsObservable = new SimpleListProperty<>();
        this.playlistsObservable = new SimpleListProperty<>();
        this.currentPlistIndex = 0;
        this.setAllPlaylists();
        this.isShuffle = false;

//        this.load(this.getCurrentPlaylist());
    }


    /////////////////////// PUBLIC METHODS

    /**
     * @return {@link Playlist#hasNext(int)}
     */
    public boolean hasNext(){
        return hasNext(1);
    }

    /**
     * @return {@link Playlist#hasNext(int)}
     */
    private boolean hasNext(int val){
        try {
            return this.getCurrentPlaylist().hasNext(val);
        }
        catch (NullPointerException e){
            return false;
        }
    }

    public boolean hasSong(Song song){
        return this.getCurrentPlaylist().hasSong(song);
    }

    /**
     * {@link Playlist#isCurrentSong(Song)}
     */
    public boolean isCurrentSong(Song songToPlay){
        return this.playlists.get(this.currentPlistIndex).isCurrentSong(songToPlay);
    }

    public void load(Playlist playlist){
        try {
            if (this.getCurrentPlaylist().equals(playlist)) {
                ANSI.BLUE.println("ALREADY LOADED PLAYLIST");
                return;
            } else {
                this.getCurrentPlaylist().reset();
                this.currentPlistIndex = this.playlists.indexOf(playlist);
            }
        }
        catch (NullPointerException e){
            this.playlists.add(playlist);
            this.currentPlistIndex = this.playlists.indexOf(playlist);
            this.getCurrentPlaylist().reset();
            this.setAllPlaylists();
        }
    }

    /**
     * Will create a new {@link Playlist} and saves it via the {@link M3UProcessor}.
     * <br>Will then in the end update the {@link #playlists arraylist of playlists} by calling
     * {@link #setAllPlaylists()}.<br>
     * @param title The title of the {@link Playlist new playlist}.
     * @param files The files being {@link mvc.model.extension.enums.Filetype#MP3 mp3s} or
     * {@link mvc.model.extension.enums.Filetype#M3U m3us} which will be added to the {@link Playlist new playlist}.
     */
    public void newPlaylist(String title, ArrayList<File> files){
        M3UProcessor processor = new M3UProcessor();

        File playlistFile = processor.writePlaylist(title, rootPath, files);
        this.playlists.add(new Playlist(playlistFile.getAbsolutePath()));
        this.playlistsObservable = FXCollections.observableList(this.playlists);

//        this.setAllPlaylists();
    }

    /**
     * Sets the next {@link Song} of the current playlist.
     * @param val The number of songs which are to be skipped.
     * @return Returns a boolean which is true if the requested song is inside the bounds of the
     * {@link #getCurrentPlaylist()} current playlist.
     */
    public boolean setNextSong(int val) {
        try {
           return this.getCurrentPlaylist().setNextSong(val);
        } catch (NullPointerException e) {
//            this.getCurrentPlaylist().getCurrentSong();
            return false;
        } catch (NotAvailableException e) {
//            this.getCurrentPlaylist().getCurrentSong();
            return false;
        }
    }

    /**
     * {@link Playlist#toggleShuffle()}
     */
    public void toggleShuffle(){
        this.isShuffle = this.playlists
                .get(this.currentPlistIndex)
                .toggleShuffle();
    }


    /////////////////////// PRIVATE METHODS

    private boolean findSong(Song song){
        Playlist temp;
        for(Playlist playlist : playlists){
            if(playlist.isInPlaylist(song)){
                this.playlists.get(currentPlistIndex).reset();
                this.currentPlistIndex = this.playlists.indexOf(playlist);
                temp = this.playlists.get(this.currentPlistIndex);
                temp.setCurrentSongIndx(temp.getSongIndex(song));
//                this.currentSongIndex = this.playlists.get(this.currentPlistIndex).getSongIndex(song);
                return true;
            }
        }
        return false;
    }

    /**
     * Used to initialize and update the list containing all {@link Playlist playlists}
     * which are in the {@see #rootPath}.
     * @see DataFinder
     */
    private void setAllPlaylists() {
        ArrayList<String> tempPlaylistsString = new DataFinder().findFiles(rootPath, Filetype.M3U);
        if(this.playlists == null) {
            this.playlists = new ArrayList<>();
        }

        Playlist temp;

        for(String playlistPath : tempPlaylistsString){
            temp = new Playlist(playlistPath);
            this.playlists.add(temp);
        }
        if(!this.playlists.isEmpty()) {
            this.playlistsObservable = FXCollections.observableList(this.playlists);
        }
    }


    /////////////////////// GETTERS

    /**
     * @return Returns a boolean value which tells whether the {@link #getCurrentPlaylist() current playlist}
     * is being played in toggleShuffle or normal playback. The value value is saved in {@link #isShuffle}.
     */
    public boolean isShuffle() {
        return isShuffle;
    }

    public boolean setCurrentSong(Song song){
        return this.getCurrentPlaylist().setCurrentSong(song);
    }

    /**
     * @return Returns the {@linkplain #rootPath} where all {@link Playlist playlists}
     * are/are to be saved.
     */
    public String getRootPath() {
        return rootPath;
    }

    /**
     * @return Returns the current song.
     * @see Playlist#getCurrentSong()
     */
    public Song getCurrentSong(){
        try {
            return this.getCurrentPlaylist().getCurrentSong();
        }
        catch (NullPointerException ignored){
            return null;
        }
    }

    /**
     * @return Returns the current playlist.
     */
    public Playlist getCurrentPlaylist(){
        try {
            return this.playlists.get(currentPlistIndex);
        }
        catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    public ArrayList<Song> getCurrentPlaylistSongs(){
        try {
            return this.playlists.get(currentPlistIndex).getSongs();
        }
        catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    /**
     * @return Returns an array-list which contains all {@link Playlist playlists} found at the {@link #rootPath}.
     * @see #playlists
     */
    public ObservableList<Playlist> getPlaylists() {
        return playlistsObservable;
    }

    /**
     * @return Returns an array-list which contains all {@link Playlist playlists} found at the {@link #rootPath}.
     * @see #playlists
     */
    public ArrayList<Playlist> getPlaylistsArray() {
        return playlists;
    }

    public ObservableList<Song> getObservableCurrentPlaylistSongs(){
        try {
//            ArrayList<Song> sPropL = this.playlists.get(this.currentPlistIndex).getSongs();
            this.setAllPlaylists();
            this.playlistSongsObservable = FXCollections.observableArrayList(
                    this.getCurrentPlaylist().getSongs()
            );
            return this.playlistSongsObservable;
        }
        catch (IndexOutOfBoundsException e){
            return FXCollections.observableArrayList();
        }
    }

    public ObservableList<Playlist> getObservablePlaylists(){
        return this.playlistsObservable;
    }


    /////////////////////// SETTERS

    /**
     * The root path where all {@link Playlist Playlists} will be saved can be
     * set but only if ne the root is different from the current.
     * @param newRoot A new root path which is to be set as{@link Playlist} root.
     */
    public void setRoot(String newRoot){
        if(!newRoot.equals(rootPath)) {
            rootPath = newRoot;
        }
    }

    /**
     * Resets the {@link PlaylistManager#rootPath} to its standard value:
     * {@linkplain StandardValues#STD_PLAYLIST_ROOT}.
     */
    public void resetRoot(){
        rootPath = StandardValues.STD_PLAYLIST_ROOT.getString();
    }

}
