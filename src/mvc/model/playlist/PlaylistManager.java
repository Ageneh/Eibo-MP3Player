package mvc.model.playlist;

import exceptions.NotAvailableException;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mvc.StandardValues;
import mvc.model.extension.enums.Filetype;
import mvc.model.extension.m3u.M3UProcessor;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class PlaylistManager {

    private static int TEMP_PLAYLIST_IDX = 0;
    private static String rootPath;

    private Playlist tempPlaylist;
    private ArrayList<Playlist> playlists;
    private ObservableList<Playlist> playlistsObservable;
    private ObservableList<Song> playlistSongsObservable;
    private int currentPlistIndex;

    private DataFinder finder;


    /////////////////////// CONSTRUCTORS

    public PlaylistManager(){
        this(StandardValues.STD_PLAYLIST_ROOT.getVal());
    }

    public PlaylistManager(String rootPath){
        this.setRoot(rootPath);
        this.rootPath = StandardValues.STD_PLAYLIST_ROOT.getVal();
        this.playlistSongsObservable = new SimpleListProperty<>();
        this.playlistsObservable = new SimpleListProperty<>();
        this.playlists = new ArrayList<>();
        this.currentPlistIndex = 0;
        this.finder = new DataFinder();
        this.setAllPlaylists();
    }


    /////////////////////// PUBLIC METHODS

    /**
     * Returns true if the song is part of the/a playlist.
     * @param song
     * @return
     */
    public boolean isInCurrentPlaylist(Song song){
        return findSong(song);
    }

    /**
     * Return true if the given Playlist is not playing. If it it, then false will be returned
     * @param path
     * @return
     */
    public boolean addPlaylist(String path){
        if(!this.playlistIsPlaying(path)) {
            Playlist temp = new Playlist(path);
            this.playlists.add(temp);
            this.currentPlistIndex = this.playlists.indexOf(temp);
            return true;
        }
        return false;
    }

    public void playPlaylist(Playlist playlist){
        playlist.setCurrentSong(0);
        this.currentPlistIndex = this.playlists.indexOf(playlist);
    }

    public boolean toggleShuffle(){
        return this.playlists.get(this.currentPlistIndex).toggleShuffle();
    }

    public boolean setNextSong(int val) {
        try {
            this.playlists.get(this.currentPlistIndex).setNext(val);
            return true;
        } catch (NotAvailableException e) {
            return false;
        }
    }

    public void resetRoot(){
        rootPath = StandardValues.STD_PLAYLIST_ROOT.getVal();
    }

    public void createPlaylist(){
        // TODO
        this.playlists.add(
                new Playlist(rootPath)
        );
    }

    public void toTempPlaylist(String path){
        this.tempPlaylist = new Playlist(rootPath +
                        StandardValues.TEMP_PLIST_TITLE.getVal() +
                        Filetype.M3U.getSuffix());
        this.tempPlaylist.addSongs(path);
        this.playlists.remove(TEMP_PLAYLIST_IDX);
        TEMP_PLAYLIST_IDX = this.playlists.indexOf(this.tempPlaylist);
        this.playlists.add(this.tempPlaylist);
        this.currentPlistIndex = TEMP_PLAYLIST_IDX;
    }

    public void writeTempPlistToSys(){
        new M3UProcessor().writePlaylist("UnnamedPlaylist", StandardValues.STD_PLAYLIST_ROOT.getVal(), this.playlists.get(TEMP_PLAYLIST_IDX).getSongs());
    }

    public void newPlaylist(String str, ArrayList<File> files){
        M3UProcessor processor = new M3UProcessor();
        ArrayList<Song> songs = new ArrayList<>();
        ArrayList<File> m3us = new ArrayList<>();

        for(File file : files){
            if(file.getName().endsWith(Filetype.MP3.getSuffix())) {
                songs.add(new Song(file));
            }
            else if(file.getName().endsWith(Filetype.M3U.getSuffix())){
                m3us.add(file);
            }
        }
        processor.writePlaylist(str, rootPath, songs, m3us);
        //###########################################################################################
        this.setAllPlaylists();
    }

    public boolean notDone(){
        return this.playlists.get(currentPlistIndex).hasNext();
    }

    public boolean isCurrentSong(Song songToPlay){
        return this.playlists.get(this.currentPlistIndex).isCurrentSong(songToPlay);
    }


    /////////////////////// PRIVATE METHODS

    private void setAllPlaylists() {
        ArrayList<String> tempPlaylistsString = finder.findFiles(rootPath, Filetype.M3U);
        this.playlists = new ArrayList<>();

        Playlist temp;
        for(String playlistPath : tempPlaylistsString){
            temp = new Playlist(playlistPath);
            this.playlists.add(temp);
        }
        if(!this.playlists.isEmpty()) {
            this.playlistsObservable = FXCollections.observableList(this.playlists);
        }
    }

    /**private void shuffle(){

        int[] songIndeces = new int[this.playlists.get(currentPlistIndex).getSongs().size()];

        int i = 0;
        songIndeces[0] = this.playlists.get(currentPlistIndex).getSongIndex(
                this.playlists.get(currentPlistIndex).getCurrentSong()
        );
        for(i = 1; i < songIndeces.length; i++){
            if(i == songIndeces[0]){
                continue;
            }
            songIndeces[i] = i % songIndeces.length ;
        }

        int pick, partner;
        int saveIndex;
        int[] nums = new int[this.playlists.get(currentPlistIndex).getSongs().size()];
        for(i = 1; i < nums.length; i++){
            nums[i] = i + 1;
        }

        i = 0;
        int shuffleAmount = (int)((Math.random() * songIndeces.length + songIndeces.length));
        while (i < shuffleAmount){
            i++;
            for(int n = 1; n < nums.length; n++){
                pick = (int)(Math.random() * (nums.length - 1) + 1);
                partner = (int)(Math.random() * (nums.length - 1) + 1);
                saveIndex = songIndeces[pick];
                songIndeces[pick] = songIndeces[partner];
                songIndeces[partner] = saveIndex;
            }

            this.shufflePlaylist = new ArrayList<>();
            for(int s : songIndeces){
                this.shufflePlaylist.add(s);
            }
            System.out.println();
        }
    }*/

    private boolean playlistIsPlaying(String path){
        File temp = new File(path);
        try {
            if(this.playlists.get(currentPlistIndex).getPlaylistFile().equals(temp)){
                return true;
            }
        }
        catch (IndexOutOfBoundsException e){
            return false;
        }
        return false;
    }

    private boolean findSong(Song song){
        Playlist temp;
        for(Playlist playlist : playlists){
            if(playlist.isInPlaylist(song)){
                this.playlists.get(currentPlistIndex).reset();
                this.currentPlistIndex = this.playlists.indexOf(playlist);
                temp = this.playlists.get(this.currentPlistIndex);
                temp.setCurrentSong(temp.getSongIndex(song));
//                this.currentSongIndex = this.playlists.get(this.currentPlistIndex).getSongIndex(song);
                return true;
            }
        }
//        if(this.playlists.get(currentPlistIndex).isInPlaylist(song)){
//            this.playlists.get(currentPlistIndex).reset();
//            this.currentPlistIndex = this.playlists.indexOf(playlist);
//            temp = this.playlists.get(this.currentPlistIndex);
//            temp.setCurrentSong(temp.getSongIndex(song));
////                this.currentSongIndex = this.playlists.get(this.currentPlistIndex).getSongIndex(song);
//            return true;
//        }
        return false;
    }

    private String newPlaylistTitle(){
        String title;
        LocalDateTime ld = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM_HH:mm:ss");
        title = "NewPlaylist_" + dtf.format(ld);
        return title;
    }


    /////////////////////// GETTERS

    public Song getCurrentSong(){
        return this.playlists.get(this.currentPlistIndex).getCurrentSong();
    }

    public Playlist getCurrentPlaylist(){
        return this.playlists.get(currentPlistIndex);
    }

    public ArrayList<Song> getCurrentPlaylistSongs(){
        try {
            return this.playlists.get(currentPlistIndex).getSongs();
        }
        catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    public ArrayList<Playlist> getPlaylists() {
        return playlists;
    }

    public ObservableList<Song> getObservableCurrentPlaylistSongs(){
        try {
            ArrayList<Song> sPropL = this.playlists.get(this.currentPlistIndex).getSongs();
            this.playlistSongsObservable = FXCollections.observableArrayList(sPropL);
            return this.playlistSongsObservable;
        }
        catch (IndexOutOfBoundsException e){
            return FXCollections.observableArrayList();
        }
    }

    public ObservableList<Playlist> getObservablePlaylists(){
        return this.playlistsObservable;
    }

    public static String getRootPath() {
        return rootPath;
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

}
