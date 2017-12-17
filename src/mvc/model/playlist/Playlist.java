package mvc.model.playlist;

import exceptions.NotAvailableException;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import misc.ANSI;
import mvc.model.extension.enums.Filetype;
import mvc.model.extension.m3u.M3UProcessor;

import java.io.File;
import java.util.ArrayList;

public class Playlist {

    /////////////////////// VARIABLES

    /**
     * An array-list containing all songs of a playlist.
     * @see Song
     */
    private ArrayList<Song> songs;
    private String path;
    private String title;
    private File playlistFile;
    private int currentSongIndx;
    private long totalLength;

    /**
     * An array-list of integer values which contains pointer in a random order,
     * which will be used for the shuffle playback.
     */
    private boolean playShuffle;
    /**
     * Is a pointer/value from {@link #playShuffle}.
     * A <it>pointer</it> which points to the current position of the {@link #playShuffle}.
     * <br>If the next song is to be played this <it>pointer</it> will be incremented and decremented
     * if the previous song is requested.</br>
     */
    private int shuffleIndex;
    private ArrayList<Integer> shufflePlaylist;


    /////////////////////// CONSTRUCTOR

    public Playlist(String path){
        this(path, null);
        this.title = this.playlistFile
                .getName()
                .replace(Filetype.M3U.getSuffix(), "");
    }

    public Playlist(String path, String title){
        this.title = title;
        this.path = path;
        this.playlistFile = new File(path);
        this.currentSongIndx = 0;
        this.songs = new ArrayList<>();
        this.totalLength = 0;
        if(this.playlistFile.exists()){
            this.addSongs(new M3UProcessor().readSongs(path));
        }
        this.shufflePlaylist = null;
        this.playShuffle = false;
        this.shuffleIndex = 0;
    }


    /////////////////////// PUBLIC & PACKAGE METHODS

    void addSongs(ArrayList<Song> songs){
        Song tempSong;
        int songNr = 0;
        for(Song song : songs){
            if(song == null){
                continue;
            }
            song.setSongNr(songNr++);
            this.songs.add(song);
        }
        this.calcTotalLength();
    }

    public void deleteSongs(ArrayList<Song> songpaths){
        for(Song song : songpaths){
            this.songs.remove(song);
        }
        this.calcTotalLength();
    }

    /**
     * Resets the {@literal currentSongIndx} to {@literal 0}.
     */
    public void reset(){
        this.currentSongIndx = 0;
    }

    /**
     * Will set the {@literal playShuffle} flag and based on its state will
     * toggleShuffle the current {@link Playlist}.
     * @return Returns the value of {@literal playShuffle}.
     * <br>Return is true, if the current {@link Playlist} is shuffled and
     * will be false if its in regular play.</br>
     */
    boolean toggleShuffle(){
        this.playShuffle = this.playShuffle ? false : true;

        if(this.playShuffle){
            this.shuffle();

            for(int i = 0; i < shufflePlaylist.size(); i++){
                System.out.print(
                        ANSI.MAGENTA.colorize(
                                i+1 + ". " +
                                        this.songs.get(i).getTitle() + " => "
                        )
                );
                ANSI.BLUE.println(this.songs.get(this.shufflePlaylist.get(i)).getTitle() + " ");
            }
            this.shuffleIndex = 0;
        }
        else{
            this.shufflePlaylist = null;
            this.shuffleIndex = 0;
        }

        return this.playShuffle;
    }

    /**
     * Checks whether the given {@link Song} is part of the selected {@link Playlist}. Will
     * be called before a {@link Song} is played.
     * @param songToPlay The {@link Song} which is to be played.
     * @return Returns a boolean which will be {@code true}, if the {@link Song
     * songToPlay} is part of the {@link Playlist}. <br>Return false {@code false} if the {@link Song
     * songToPlay} is not part of the {@link Playlist}.</br><br>If the given {@link Song} is already
     * playing {@code false} will be returned.</br>
     */
    boolean isInPlaylist(Song songToPlay){
        for (Song song : songs) {
            if(song.getTitle().equals(songToPlay.getTitle())) return true;
        }
        if(!this.songs.contains(songToPlay)){
            return false;
        }
        else{
            return true;
//            System.out.printf("CONTAINS------------------>>>>>>>>>>><");
//            this.setCurrentSongIndx(
//                    this.songs.indexOf(songToPlay)
//            );
        }
    }

    /**
     * Checks whether a given {@link Song song} is the current {@link Song song} of the {@link Playlist}.
     * @param song The {@link Song} which is to be checked.
     * @return Returns a boolean which is true if the {@link Song song} is the {@literal currentSongIndx}
     * and false if not.
     */
    boolean isCurrentSong(Song song){
        if(this.getCurrentSong().equals(song)){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Will check whether the {@link Playlist} is playig the last song.
     * @return If the {@link Song current song} is not the last {@link Song} the return value is {@code true}.
     * Else {@code false} will be returned.
     */
    boolean hasNext(int val){
        if(playShuffle){
            if(shuffleIndex + val < this.songs.size() && shuffleIndex + val >= 0){
                return true;
            }
        }
        else if(currentSongIndx + val < this.songs.size() && currentSongIndx + val >= 0 ){
            return true;
        }
        return false;
    }

    boolean hasSong(Song song){
        return this.songs.contains(song);
    }


    /////////////////////// PRIVATE METHODS

    /**
     * Will calculate the {@literal total length} of a {@link Playlist}.
     */
    private void calcTotalLength(){
        totalLength = 0;
        for(Song song : this.songs){
            if(song == null){
                continue;
            }
            totalLength += song.getLengthMillis();
        }
    }

    /**
     * This method will toggleShuffle the indeces of the {@link Playlist}.
     * <br>The indeces will then be saved inside an {@link ArrayList}, with a randomized order,
     * named {@link #shufflePlaylist}. The integer values inside the {@link ArrayList array-list}
     * will be used as pointers for the {@link Playlist}.</br>
     * Depending on the flag {@link #playShuffle} either the original or the randomized {@link Playlist}
     * will be played.
     */
    private void shuffle(){
        int[] songIndeces = new int[this.songs.size()];
        int i;

        songIndeces[0] = this.currentSongIndx;
        for(i = 1; i < songIndeces.length; i++){
            if(i == songIndeces[0]){
                continue;
            }
            songIndeces[i] = i % songIndeces.length ;
        }

        int pick, partner;
        int saveIndex;
        int[] nums = new int[songIndeces.length];
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
    }


    /////////////////////// GETTERS

    /**
     * @param song The {@link Song} of which its index is to be found.
     * @return Returns the index of the given {@link Song}. Will be {@value -1} if the {@link Song}
     * is not part of the {@link Playlist}.
     */
    int getSongIndex(Song song){
        return this.songs.indexOf(song);
    }

    long getTotalLength(){
        return this.totalLength;
    }

    /**
     * Getter of the {@literal song title}.
     * @return Returns a {@link String} object.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter of {@link Song current song}.
     * @return Returns a {@link Song} object.
     */
    public Song getCurrentSong(){
        try {
            return this.songs.get(currentSongIndx);
        }
        catch (Exception e){
            System.out.println();
            return null;
        }
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public SimpleListProperty<Song> getSongsObservable(){
//        ObservableList<Song> songs = FXCollections.observableList(this.songs);
        return new SimpleListProperty<Song>(FXCollections.observableArrayList(songs));
    }


    /////////////////////// SETTERS
    // are only accessible via the playlistmanager

    /**
     * Setter for setting the {@literal currentSongIndx}.
     * @param val The given {@link Integer} value will be added onto the {@literal currentSongIndx}.
     *            Depending on the given {@param val} and the size of the {@link Playlist} the
     *            argument and {@literal currentSongIndx} will added together.
     */
    void setCurrentSongIndx(int val){
        if(val >= 0 && val < this.songs.size()) {
            this.currentSongIndx = val;
        }
        else{
            this.currentSongIndx = 0;
        }
    }

    /**
     * {@link #setCurrentSongIndx(int)} Only difference is that in this case a {@link Song} object is given.
     * @param song The song which is to be set as the new {@link Song current Song}.
     * @return Returns a boolean which says whether the given {@link Song} has been set as the current or not.
     */
    boolean setCurrentSong(Song song){
        if(playShuffle && this.songs.get(currentSongIndx).equals(song)){
            return true;
        }
        else if(this.isInPlaylist(song) || this.isCurrentSong(song)){
            this.currentSongIndx = this.songs.indexOf(song);
            if(playShuffle){
//                this.currentSongIndx = this.shufflePlaylist.indexOf(
//                        this.currentSongIndx
//                );
                this.shuffleIndex = this.shufflePlaylist.indexOf(this.currentSongIndx);
            }
            return true;
        }
        return false;
    }

    /**
     * Sets the next Song.
     * @param val The the direction and amount of steps to skip {@link Song songs}.
     * @return Returns the new {@link Song current song}.
     * @throws NotAvailableException
     */
    boolean setNextSong(int val) throws NotAvailableException {
        if(playShuffle){
            if(this.shuffleIndex + val >= 0
                    && this.shuffleIndex + val <= this.songs.size()) {
                this.shuffleIndex += val;
                this.currentSongIndx = this.shufflePlaylist.get(this.shuffleIndex);
//                return this.songs.get(this.currentSongIndx);
                return true;
            }
//            else{
//                this.shuffle();
//                this.setNextSong(val);
//            }
        }
        else if(currentSongIndx + val >= 0
                && currentSongIndx + val < this.songs.size()){
            this.currentSongIndx += val;
            return true;
        }
        else {
            throw new NotAvailableException();
        }
//        return this.songs.get(currentSongIndx);
        return false;
    }

}
