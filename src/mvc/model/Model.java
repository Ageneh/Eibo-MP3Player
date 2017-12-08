package mvc.model;

import de.hsrm.mi.eibo.simpleplayer.SimpleAudioPlayer;
import de.hsrm.mi.eibo.simpleplayer.SimpleMinim;
import javafx.collections.ObservableList;
import mvc.model.extension.enums.Filetype;
import mvc.model.extension.enums.Interval;
import mvc.model.extension.enums.Skip;
import mvc.model.playlist.Playlist;
import mvc.model.playlist.PlaylistManager;
import mvc.model.playlist.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Scanner;

public class Model extends Observable {

    /////////////////////// VARIABLES

    private static PlaylistManager plistManager;

    private volatile boolean stop, skip, pause;
    private volatile float currentVol;
    private volatile PlayerThread modelThreader;
    private volatile SimpleMinim minim;
    private static volatile SimpleAudioPlayer player;

    private boolean playlistChanged;
    private volatile long songPosition;


    /////////////////////// CONSTRUCTOR

    public Model(){
        plistManager = new PlaylistManager();
        this.minim = new SimpleMinim(true);
        this.modelThreader = new PlayerThread();
        Thread t = new Thread(this.modelThreader, "RUNNER");
        t.setDaemon(true);
        t.start();
        this.currentVol = 50;
        this.playlistChanged = false;
    }


    /////////////////////// PUBLIC METHODS

    @Override
    public void notifyObservers() {
        super.notifyObservers();
        this.playlistChanged = false;
    }

    public static void main(String args[]){
        Model pl = new Model();
//        pl.isInCurrentPlaylist("/view_assests/henock/Music/iTunes/iTunes Media/Music/Logic/Under Pressure (Deluxe Edition)/09 Metropolis.mp3");
        pl.play("/view_assests/henock/IdeaProjects/GitHub_MP3Player/MP3_v2/playlists/mvc.model.playlist.Playlist.m3u");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pl.skip(Skip.FORWARD);
        String a = new Scanner(System.in).nextLine();
        pl.skip(Skip.NEXT);
    }

    public synchronized boolean play(){
        try {
            if(!this.isPlaying()) {
                if (stop) {
                    modelThreader.go();
                    player.play();
                    return true;
                } else if (pause) {
                    this.pause();
                    return this.pause;
                }
                else{
                    modelThreader.go();
                    player.play();
                    return true;
                }
            }
            else if(this.isPlaying()){
                this.pause();
                return this.pause;
            }
//            this.pause();
        }
        catch (NullPointerException e){
            System.out.println("==================================");
            System.out.println("===== PLAYER NOT INITIALIZED =====");
            System.out.println("==================================");
        }
        return false;
    }

    public void play(String path){
        File temp = new File(path);
        if(!temp.exists()){
            return;
        }
        else if(temp.getName().endsWith(Filetype.MP3.getSuffix())){
            this.playSong(path);
        }
        else if(temp.getName().endsWith(Filetype.M3U.getSuffix())){
            this.playPlaylist(path);
        }
    }

    /**
     * This method will isInCurrentPlaylist a given {@link Song}.
     *
     * If {@link Song song} is already playing nothing will be done - except for when its
     * paused. In the latter case, the {@link Song song} will be unpaused and resumed.
     * If {@link Song song} is part of a playlist, the playlists index will be set with
     * the positional value of {@link Song song}.
     * @param song A song which is to be played.
     */
    public void play(Song song){
        try{
            if(plistManager.isCurrentSong(song)){
                if(!this.isPlaying()){
                    // if song is already loaded and NOT playing
                    System.out.println("SONG" + song.getTitle() + " IS NOW PLAYING");
                    modelThreader.go();
                    return;
                }
                else{
//                    this.playSong(plistManager.getCurrentSong());
                    System.out.println("SONG" + song.getTitle() + " IS ALREADY PLAYING");
                    return;
                }
            }
            else if(plistManager.isInCurrentPlaylist(song)){
                plistManager.getCurrentPlaylist().setCurrentSong(song);
                this.load(plistManager.getCurrentSong());
                this.modelThreader.go();
                return;
            }
            else{
                this.play();
                return;
            }
        }
        catch (Exception e){
            // DO NOTHING
        }
//
//        if(plistManager.getCurrentPlaylist().isInPlaylist(song)) {
//            this.playSong(plistManager.getCurrentSong());
//            return;
//        }
//        else if(plistManager.isInCurrentPlaylist(song)){
//            this.playSong(plistManager.getCurrentSong());
//            return;
//        }
    }

    public synchronized void pause(){
        try {
            this.modelThreader.pause();
        }
        catch (NullPointerException e){
            this.player = minim.loadMP3File(plistManager.getCurrentSong().getPath());
        }
        finally {
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop(){
        stop = true;
    }

    public void mute(){
        this.setVol(0);
    }

    public void load(Song song){
        this.load(song.getPath());
    }

    public void load(String filename){
            this.stop();
            player = minim.loadMP3File(filename);
            setVol(currentVol);
            setChanged();
            notifyObservers();
    }

    public void skip(Skip skipVal){
        switch (skipVal) {
            case FORWARD:
            case BACKWARD:
                modelThreader.skipPosition(skipVal);
                break;

            case NEXT:
            case PREVIOUS:
                modelThreader.skipSong(skipVal);
                break;
        }
    }

    public void addSongs(String ... songpaths){
        plistManager.getCurrentPlaylist().addSongs(songpaths);
    }

    public void addSongs(Song ... songs){
        plistManager.getCurrentPlaylist().addSongs(songs);
    }

    public void addSongs(File ... songs){
        plistManager.getCurrentPlaylist().addSongs(songs);
    }

    public void newPlaylist(ArrayList<File> files){
        plistManager.newPlaylist(null, files);
    }

    public void newPlaylist(String title, ArrayList<File> files){
        plistManager.newPlaylist(title, files);
        setChanged();
        notifyObservers();
    }

    public boolean toggleShuffle(){
        boolean temp = plistManager.toggleShuffle();
        setChanged();
        notifyObservers();
        return temp;
    }

    public void writeTempPlistToSys(){
        this.playlistChanged = true;
        plistManager.writeTempPlistToSys();
        notifyObservers();
    }

    public void writeTempPlistToSys(File ... files){
        this.playlistChanged = true;
        plistManager.writeTempPlistToSys();
    }

    public void createPlaylist(){
        this.playlistChanged = true;
        plistManager.createPlaylist();
    }


    /////////////////////// PRIVATE METHODS

    /**
     * Plays a {@link Song} sent by the {@link PlayerThread}.
     * @param song
     */
    private void playSong(Song song){
        this.load(song);
        System.out.println(plistManager.getCurrentPlaylist().getSongs().indexOf(
                plistManager.getCurrentSong()
        ));
        System.out.println(
                plistManager.getCurrentSong().getTitle()
        );
        this.play();
    }

    /**
     * Plays a {@link Song} sent by the {@link PlayerThread}.
     * @param path
     */
    private void playSong(String path){
        plistManager.toTempPlaylist(path);
//        this.load(plistManager.getCurrentSong());
        this.load(path);
    }

    private void playPlaylist(String path){
        if(plistManager.addPlaylist(path)){
            this.load(plistManager.getCurrentSong());
        }
        else{
            System.out.println("PLAYLIST ALREADY PLAYING");
        }
    }

    public void playPlaylist(Playlist playlist){
        plistManager.playPlaylist(playlist);
//        this.load(plistManager.getCurrentSong());
    }

    /**
     * Will convert any given dB-Value to its linear value.
     * @param x
     * @return
     */
    private float dBToLinear(float x){
//        linear-to-db(x) = log(x) * 20
//        db-to-linear(x) = 10^(x / 20
//        double value = Math.pow(10, (val/20));
        return (float) Math.pow(10, (x / 20));
    }

    /**
     * Will convert any given linear to its dB-Value value.
     * @param x
     * @return
     */
    private float linearToDB(float x){
//        linear-to-db(x) = log(x) * 20
//        db-to-linear(x) = 10^(x / 20
//        double value = Math.pow(10, (val/20));
        return (float) (Math.log10(x / 50) * 20);

    }

    private synchronized boolean isPlaying(){
        try {
            return player.isPlaying();
        }
        catch (NullPointerException e){
            return false;
        }
    }


    /////////////////////// GETTERS

    public boolean getPlaylistStatus(){
        return playlistChanged;
    }

    public long getCurrentPosition(){
        synchronized (modelThreader) {
            try {
                return player.position();
            } catch (NullPointerException e) {
                return 0;
            }
        }
    }

    public float getCurrentVol() {
        return currentVol;
    }

    public String currentPlaylistTitle(){
        return plistManager.getCurrentPlaylist().getTitle();
    }

    public ArrayList<Playlist> getPlaylists(){
        return plistManager.getPlaylists();
    }

    public ArrayList<Song> getCurrentSongs(){
        return plistManager.getCurrentPlaylistSongs();
    }

    public ObservableList<Playlist> getPlaylistsObservable(){
        return plistManager.getObservablePlaylists();
    }

    public ObservableList<Song> getCurrentSongsObservable(){
        return plistManager.getObservableCurrentPlaylistSongs();
    }

    public Song getCurrentSong(){
        return plistManager.getCurrentSong();
    }

    public String getPlistPath(){
        return plistManager.getRootPath();
    }


    /////////////////////// SETTERS

    /**
     * This method will set the volume.
     * @param val
     */
    public void setVol(float val){
        try {
            if(val == 0){
                if (!this.player.isMuted()) {
                    this.player.mute();
                }
                else{
                    this.player.unmute();
                }
                setChanged();
                notifyObservers();
                return;
            }
            else{
                this.player.unmute();
            }

            this.currentVol = val;
            this.player.setGain(this.linearToDB(val));
            setChanged();
        }
        catch (NullPointerException e){
            this.currentVol = val;
            setChanged();
        }
        notifyObservers();
        return;
    }


    /////////////////////// INNER CLASSES

    /**
     * Plays a whole mvc.model.playlist.Playlist till the end. When song is done PlayerThread will isInCurrentPlaylist the next one.
     */
    private class PlayerThread implements Runnable {

        @Override
        public void run() {
            //BUGFIX: synchronised musste weg
            long posMod;
            long last;
            waiter();
            while(plistManager.notDone()){
                //// PLAY SONG AND NOTIFY OBSERVERS
                playSong(plistManager.getCurrentSong());
                setChanged();
                notifyObservers();

                reset();
                printCurrent();
                last = -1;
                while (!stop && player.isPlaying()){
                    songPosition = player.position();
                    posMod =  songPosition % Interval.REFRESH_RATE.getVal();
                    if(posMod <= Interval.LATENCY.getVal() && posMod >= 0 && posMod != last){
                        System.out.printf("\n*** ** " + posMod + " ** ***\n");
                        setChanged();
                        last = posMod;
                        notifyObservers();
                    }
                    if(stop){
                        break;
                    }
                    else if(pause){
                        waiter();
                        if(stop){
                            break;
                        }
                    }
                }
                if(!skip && stop){
                    waiter();
                }
                else if(skip && stop){
                    // go on an isInCurrentPlaylist song which has been set in skip(Skip val)
                    // go on an isInCurrentPlaylist song which has been set in skip(Skip val)
                    continue;
                }
                else{
                    skipSong(Skip.NEXT);
                }
                reset();
            }
        }

        private synchronized void waiter(){
            setChanged();
            notifyObservers();
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private synchronized void skipPosition(Skip val){
            synchronized (this) {
                pause();
                System.out.println("OLD POS: " + player.position());
                player.skip(val.getSkipVal());
                setChanged();
                System.out.println("NEW POS: " + player.position());
                pause();
                notifyObservers();
            }
        }

        private synchronized void skipSong(Skip val){
            if(plistManager.setNextSong(val.getSkipVal())){
                if(pause || player == null){
                    return;
                }
            }
            skip = true;
            stop = true;
            setChanged();
            notifyObservers();
        }

        private synchronized void pause(){
            pause = pause ? false : true;
            if(pause){
                player.pause();
                this.reset();
            }
            else{
                play();
                this.reset();
            }
            go();
        }

        private synchronized void go(){
            setChanged();
            notifyObservers();
            this.notify();
        }

        private synchronized void reset(){
            pause = stop = skip = false;
        }

        private void printCurrent(){
            System.out.println("===============================");
            System.out.println("=======CURRENTLY PLAYING=======");
            System.out.println("===============================");
            System.out.println("TITEL:  " + plistManager.getCurrentSong().getTitle());
            System.out.println("ARTIST: " + plistManager.getCurrentSong().getArtist());
            System.out.println("ALBUM:  " + plistManager.getCurrentSong().getAlbum());
            System.out.println("LENGTH:  " + plistManager.getCurrentSong().getLength());
            System.out.println("===============================");
            System.out.println("===============================");
            System.out.println("===============================");
        }
    }

}


