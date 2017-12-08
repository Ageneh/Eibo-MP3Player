package mvc.model.playlist;

import exceptions.NotAvailableException;
import mvc.model.extension.enums.Filetype;
import mvc.model.extension.m3u.M3UProcessor;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;

public class Playlist {
    private final long CONST_DEFICITE = 3600000; // 1hour

//    private ArrayList<Song> songs;
    private ArrayList<Song> songs;
    private String path;
    private String title;
    private File playlistFile;
    private Time totalTime;
    private int currentSong;
    private long totalLength;

    private boolean playShuffle;
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
        this.currentSong = 0;
        this.songs = new ArrayList<>();
        this.totalLength = 0;
        this.totalTime = new Time(0);
        if(this.playlistFile.exists()){
            this.addSongs(new M3UProcessor().getSongs(path));
        }
        this.shufflePlaylist = null;
        this.playShuffle = false;
        this.shuffleIndex = 0;
    }


    /////////////////////// PUBLIC METHODS

    public void addSongs(ArrayList<String> songpaths){
        Song tempSong;
        for(String path : songpaths){
            tempSong = new Song(path);
            this.songs.add(tempSong);
            tempSong = null;
        }
        this.calcTotalLength();
    }

    public void addSongs(String ... songpaths){
        for(String path : songpaths){
            System.out.println("Adding: " + path);
            this.songs.add(new Song(path));
        }
        this.calcTotalLength();
    }

    public void addSongs(Song... songs){
        for(Song song : songs){
            this.songs.add(song);
        }
        this.calcTotalLength();
    }

    public void addSongs(File... songs){
        for(File songFile : songs){
            this.songs.add(new Song(songFile));
        }
        this.calcTotalLength();
    }

    public void reset(){
        this.currentSong = 0;
    }

    public boolean toggleShuffle(){
        this.playShuffle = this.playShuffle ? false : true;

        if(this.playShuffle){
            this.shuffle();

            for(int i = 0; i < shufflePlaylist.size(); i++){
                System.out.println(
                        this.songs.get(
                                this.shufflePlaylist.get(i)
                        ).getTitle()
                );
            }
        }
        else{
            this.shufflePlaylist = null;
            this.shuffleIndex = 0;
        }

        System.out.println("########");
        System.out.println("SHUFFLE: " + this.playShuffle);
        System.out.println("########");

        return this.playShuffle;
    }

    /**
     * Checks whether the given {@link Song} is part of the selected {@link Playlist}. Will
     * be called before a {@link Song} is played.
     * @param songToPlay The {@link Song} which is to be played.
     * @return Returns a boolean which will be {@code true}, if the {@link Song
     * songToPlay} is part of the Playlist. <br>Return false {@code false} if the {@link Song
     * songToPlay} is not part of the {@link Playlist}.</br><br>If the given {@link Song} is already
     * playing {@code false} will be returned.</br>
     */
    public boolean isInPlaylist(Song songToPlay){
        if(!this.songs.contains(songToPlay)){
            return false;
        }
        else{
            System.out.printf("CONTAINS------------------>>>>>>>>>>><");
//            this.setCurrentSong(
//                    this.songs.indexOf(songToPlay)
//            );
        }
        return true;
    }

    public boolean isCurrentSong(Song song){
        if(this.songs.get(this.currentSong).equals(song)){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean hasNext(){
        if(currentSong < this.songs.size()){
            return true;
        }
        return false;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }


    /////////////////////// PRIVATE METHODS

    private void calcTotalLength(){
        totalLength = 0;
        for(Song song : this.songs){
            if(song == null){
                continue;
            }
            totalLength += song.getLengthMillis();
        }
//        totalLength *= 1000;
        this.totalTime = new Time(totalLength);
    }

    private void shuffle(){

        int[] songIndeces = new int[this.songs.size()];

        int i = 0;
        songIndeces[0] = this.currentSong;
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
//        FileWriter fw = null;
//        ArrayList<String> tempPlaylistSongs = new ArrayList<>();
//        try {
//            fw = new FileWriter("MP3_v2/playlists/shuffle.m3u");
//
//            for(int index : songs){
//                fw.write(this.playlists.get(indexCurrentPlaylist).getPlaylistSongs().get(index).getPath());
//                fw.write(System.lineSeparator());
//
//                tempPlaylistSongs.add(this.playlists.get(indexCurrentPlaylist).getPlaylistSongs().get(index).getPath());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        finally {
//            try {
//                fw.flush();
//                fw.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

    }


    /////////////////////// GETTERS

    public Song getCurrentSong(){
        return this.songs.get(currentSong);
    }

    public int getSongIndex(Song song){
        return this.songs.indexOf(song);
    }

    public String getTitle() {
        return title;
    }

    public File getPlaylistFile() {
        return playlistFile;
    }

    public int getCurrentSongIndex(){
        return this.currentSong;
    }

    public long getTotalLengthInMillis(){
        return totalLength;
    }

    public Time getTotalTime(){
        return this.totalTime;
    }


    /////////////////////// SETTERS

    public void setCurrentSong(int val){
        if(val >= 0 && val < this.songs.size()) {
            this.currentSong = val;
        }
        else{
            this.currentSong = 0;
        }
    }

    public void setCurrentSong(Song song){
        if(this.isInPlaylist(song) && !this.isCurrentSong(song)){
            this.currentSong = this.songs.indexOf(song);
        }
    }

    public Song setNext(int val) throws NotAvailableException {
        if(playShuffle){
            int shufflePos = this.shufflePlaylist.get(shuffleIndex);
            if(shufflePos + val >= 0 && shufflePos + val <= this.songs.size()) {
                this.songs.get(
                        this.shufflePlaylist.get((this.shuffleIndex += val) % this.shufflePlaylist.size())
                );
            }
            else{
                this.shuffle();
                this.setNext(val);
            }
        }
        else if(currentSong + val >= 0
                && currentSong + val < this.songs.size()){
            this.currentSong += val;
            return this.songs.get(currentSong);
        }
        throw new NotAvailableException("SONG INDEX DOES NOT EXIST.");
    }

}
