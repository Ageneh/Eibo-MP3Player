package mvc.model;

import de.hsrm.mi.eibo.simpleplayer.SimpleAudioPlayer;
import de.hsrm.mi.eibo.simpleplayer.SimpleMinim;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import misc.ANSI;
import mvc.model.extension.enums.Interval;
import mvc.model.extension.enums.Skip;
import mvc.model.extension.enums.StandardValues;
import mvc.model.playlist.Playlist;
import mvc.model.playlist.PlaylistManager;
import mvc.model.playlist.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

/**
 * This class is used for the main model. It is used to create a mp3-player.
 * <br>Part of this class is a {@link PlayerThread private inner class}, which is curucial
 * for the continuing playback of a {@link Playlist}.</br>
 * <br>This contains four static variables: {@see #thread}, {@see #plistManager}, {@see #minim} and
 * {@see #player}. These are static so that the possiblity of having multiple {@link SimpleAudioPlayer players}
 * and {@link PlaylistManager playlist managers} running for one MP3Player.</br>
 * <br>This class extends {@link Observable}.</br>
 */
public class MP3Player extends Observable {

    /////////////////////// VARIABLES

    /**
     * @see PlaylistManager
     */
    private static PlaylistManager plistManager;
    private static Thread thread;

    /**
     * The base object which is needed to {@linkplain SimpleMinim#loadFile(String) load} and
     * {@linkplain SimpleAudioPlayer#play() play} via the {@link #player}.
     */
    private static volatile SimpleMinim minim;
    /**
     * The actual "player".
     */
    private static volatile SimpleAudioPlayer player;

    private final PlayerThread modelThreader;
    /**
     * Flag which is used to control the behaviour of the {@link #modelThreader}.
     * <br>Is initially {@code false}.</br>
     */
    private volatile boolean stop, skip, pause;
    /**
     * Saves the current volume for the {@link #player} and can be changed through {@link #setVolume(float)}.
     */
    private volatile float currentVol;


    /////////////////////// CONSTRUCTOR

    public MP3Player(){
        plistManager = new PlaylistManager();
        minim = new SimpleMinim(true);
        this.currentVol = StandardValues.BASE_VOLUME.getFloat();

        this.modelThreader = new PlayerThread();
        thread = new Thread(this.modelThreader, "Player_Thread");
        thread.setDaemon(true);
        thread.start();
    }


    /////////////////////// PUBLIC METHODS

    @Override
    public void notifyObservers() {
        super.notifyObservers();
    }

    /**
     * Will stop music playback through calling {@link SimpleMinim#stop()}.
     */
    public void stop(){
        if(pause) {
            modelThreader.resume();
        }
        stop = true;
        minim.stop();
        plistManager.getCurrentPlaylist().reset();
        setChanged();
        notifyObservers();
    }

    /**
     * <br>Loads the current song.</br>
     * Will invoke {@link #load(Song)} and hand down the {@link PlaylistManager#getCurrentSong() current song} of the
     * {@link PlaylistManager#getCurrentPlaylist() current playlist}.
     */
    public void load(){
        this.load(plistManager.getCurrentSong());
    }

    /**
     * Will invoke {@link #load(String)} handing down the path of the given {@param song}.
     * @param song The {@link Song} which is to be loaded in the {@link #player}.
     */
    public void load(Song song){
        if(plistManager.hasSong(song)) {
            this.stop();
            plistManager.setCurrentSong(song);
            player = minim.loadMP3File(plistManager.getCurrentSong().getPath());
            setVolume(currentVol);
            player.skip(-player.position());
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Will invoke {@link #load(String)} handing down the path of the given {@param song}.
     * @param playlist The {@link Song} which is to be loaded in the {@link #player}.
     */
    public void load(Playlist playlist, Song song){
        plistManager.load(playlist);
        load(song);
        setChanged();
        notifyObservers();
    }

    /**
     * Will initialize the {@link #player} using the given {@param filename}.
     * <br>After the initialization the {@link #currentVol} will be set applied to the {@link #player}.
     * @param filename The filepath of the mp3 file which is to be loaded.
     */
    private void load(String filename){
        this.stop();
        player = minim.loadMP3File(filename);
        setVolume(currentVol);
        setChanged();
        notifyObservers();
    }

    /**
     * This method will mute the {@link SimpleAudioPlayer player}, by calling {@link #setVolume(float)}
     * if the player is not muted and will unmute by calling {@link #setVolume(float)} with the {@link #currentVol}.
     */
    public void mute(){
        if(player.isMuted()){
            player.unmute();
        }
        else {
            player.mute();
        }
        setChanged();
        notifyObservers();
    }

    /**
     * Creates a new {@link Playlist}.
     * <br>Invokes {@see PlaylistManager#newPlaylist(String, ArrayList)}.</br>
     * @param title The title of the {@link Playlist new playlist}.
     * @param files The files being {@link mvc.model.extension.enums.Filetype#MP3 mp3s} or
     * {@link mvc.model.extension.enums.Filetype#M3U m3us} which will be added to the {@link Playlist new playlist}.
     */
    public void newPlaylist(String title, ArrayList<File> files){
        plistManager.newPlaylist(title, files);
        setChanged();
        notifyObservers();
    }

    /**
     * Will pause current music playback.
     */
    public void pause(){
        pause = pause ? false : true;
        try {
            if (pause) {
                player.pause();
                return;
            } else {
                this.modelThreader.resume();
            }
        } catch (NullPointerException e) {
            player = minim.loadMP3File(plistManager.getCurrentSong().getPath());
        } finally {
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        setChanged();
        notifyObservers();
    }

    /**
     * Will resume current music playback.
     * <br>If there is a {@link Song} already playing then {@link #pause} will be called.</br>
     * <br>Else the current {@link Song} will resume playing. Depending on the reason why the {@link Song}
     * is not playing {@link #pause()} will be invoked if the {@link #pause} is {@code true} or {@link Song}
     * <br>If it happens to be the case that the {@link #player} should be {@code null} then {@link #load()}
     * will be called.</br>
     */
    public void play(){
        try {
            if(!this.isPlaying()) {
                // if the current song is not playing
                if(stop){
                    load();
                    this.modelThreader.resume();
                }
                this.modelThreader.resume();
            }
            else{
                this.pause();
            }
        }
        catch (NullPointerException e){
            ANSI.MAGENTA.println("\n==================================");
            ANSI.MAGENTA.println("===== PLAYER NOT INITIALIZED =====");
            ANSI.MAGENTA.println("==================================\n");
//            this.load();
        }
        finally {
            setChanged();
            notifyObservers();
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
//            if(plistManager.isCurrentSong(song)){
//                if(!this.isPlaying()){
//                    // if current song and NOT playing
//                    System.out.println("SONG" + song.getTitle() + " IS NOW PLAYING");
//                    play();
//                }
//                else{
////                    this.playSong(plistManager.getCurrentSong());
//                    System.out.println("SONG" + song.getTitle() + " IS ALREADY PLAYING");
//                }
//            }
//            else if(plistManager.getCurrentPlaylist().setCurrentSongIndx(song)){
//                this.load(plistManager.getCurrentSong());
//            }
//            else{
//                this.play();
//            }
            if(plistManager.isCurrentSong(song)){
                // if the song is already the current/loaded song
                this.play();
                setChanged();
                notifyObservers();
                return;
            }
            else {
                this.load(song);
                this.modelThreader.resume();
                setChanged();
                notifyObservers();
            }
        }
        catch (Exception e){
            // DO NOTHING
        }
    }

    public void play(Playlist playlist, Song song){
        this.load(playlist, song);
        this.play();
    }

    /**
     * Will call either {@link PlayerThread#skipPosition(Skip)} or {@link PlayerThread#skipSong(Skip)} depending on
     * the given argument.
     * @param skipVal Is of type {@link Skip} and depending on its specific value the current Song will be skipped
     *                or the position of the {@link #player} will be changed.
     */
    public void skip(Skip skipVal){
        switch (skipVal) {
            case FORWARD:
            case BACKWARD:
                skipPosition(skipVal);
                break;

            case NEXT:
            case PREVIOUS:
                skipSong(skipVal);
                break;
        }
        setChanged();
        notifyObservers();
    }

    /**
     *
     * @return
     */
    public void toggleShuffle(){
        plistManager.toggleShuffle();
        setChanged();
        notifyObservers();
    }


    /////////////////////// PRIVATE METHODS

    /**
     * Will convert any given dB-Value to its linear value.
     * @param x
     * @return
     */
    private float dBToLinear(float x){
        return (float) Math.pow(10, (x / 20));
    }

    /**
     * @return Returns the current status of the {@link #player}. <br>If the {@link #player} is uninitialized
     * return value will be {@code false}.
     */
    public boolean isPlaying(){
        try {
            return player.isPlaying();
        }
        catch (NullPointerException e){
            load();
            return false;
        }
    }

    /**
     * Will convert any given linear value to its corresponding dB-Value value.
     * @param x The value in the {@code interval [0, 100]} which is to be converted into its corresponding dB value.
     * @return Returns a float value, which represents the converted dB value.
     */
    private float linearToDB(float x){
        return (float) (Math.log10(x / 50) * 20);
    }

    /**
     * Will fast forward or rewind the {@link PlaylistManager#getCurrentSong() current Song} of the
     * {@link PlaylistManager#getCurrentPlaylist() current playlist} via the {@link #player}.
     * @param val The amount of seconds which are to be skipped.
     * <br> Is of type {@link Skip#FORWARD} or {@link Skip#BACKWARD}.</br>
     */
    private void skipPosition(Skip val){
        pause();
        player.skip(val.getSkipVal());
        pause();
        setChanged();
        notifyObservers();
    }

    /**
     * Will skip the {@link PlaylistManager#getCurrentSong() current Song} of the
     * {@link PlaylistManager#getCurrentPlaylist() current playlist}.
     * @param val The amount of {@link Song songs} which are to be skipped.
     * <br> Is of type {@link Skip#NEXT} or {@link Skip#PREVIOUS}.</br>
     */
    private void skipSong(Skip val){
        if(plistManager.setNextSong(val.getSkipVal())){
            skip = true;
            if(pause){
                load();
                return;
            }
//            plistManager.setNextSong(val.getSkipVal());
            load();
            modelThreader.resume();
        }
        else{
            ANSI.CYAN.print("Cannot skip. ");
            ANSI.CYAN.println(plistManager.getCurrentSong().getTitle() + " by " + plistManager.getCurrentSong().getArtist());
        }
    }


    /////////////////////// GETTERS

    /**
     * @see PlaylistManager#isShuffle()
     */
    public boolean isShuffle(){
        return plistManager.isShuffle();
    }

    /**
     * @return Returns the position of the {@link #player}.
     * <br>If the {@link #player} is stopped or {@code null} then the return value will be {@literal 0},
     * else the position will be returned.
     */
    public long getCurrentPosition(){
        try {
//            if(stop){
//                return 0;
//            }
            return player.position();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    /**
     * @return Returns {@link #currentVol}
     */
    public float getCurrentVol() {
        return currentVol;
    }

    /**
     * @return {@link PlaylistManager#getRootPath()}
     * @see PlaylistManager#rootPath
     */
    public String getPlistPath(){
        return plistManager.getRootPath();
    }

    /**
     * @see PlaylistManager#getCurrentSong()
     */
    public Song getCurrentSong(){
        return plistManager.getCurrentSong();
    }

    /**
     * @return {@link PlaylistManager#getPlaylists()}
     */
    public ObservableList<Playlist>  getPlaylists(){
        return plistManager.getPlaylists();
    }

    /**
     * @return {@link PlaylistManager#getPlaylists()}
     */
    public ArrayList<Playlist>  getPlaylistsArray(){
        return plistManager.getPlaylistsArray();
    }

    /**
     * @return {@link PlaylistManager#getCurrentPlaylistSongs()}
     */
    public ArrayList<Song> getCurrentSongs(){
        return plistManager.getCurrentPlaylistSongs();
    }

    public Playlist getCurrentPlaylist(){
        return plistManager.getCurrentPlaylist();
    }


    /////////////////////// SETTERS

    /**
     * This method will set the volume.
     * @param val The new value which is to be set as the volume of the {@see #player}. Will be saved in {@see #currentVol}.
     */
    public synchronized void setVolume(float val){
        this.currentVol = val;
        if(player != null) {
            player.setGain(this.linearToDB(val));
            if(player.isMuted()){
                mute();
            }
        }
        setChanged();
        notifyObservers();
    }

    public void setPosition(long position){
        this.pause();
        player.skip(-player.position());
        player.skip(
                Math.toIntExact((
                    position
                ))
        );
        setChanged();
        notifyObservers();
    }


    /////////////////////// INNER CLASSES

    /**
     * Plays a whole {@link Playlist} till the end. When the {@linkplain PlaylistManager#getCurrentSong()
     * current Song} is done {@link PlayerThread} will play the the next one till the end of the {@link
     * PlaylistManager#getCurrentPlaylist() current Playlist}.
     * <br>Implements {@link Runnable}.</br>
     */
    private class PlayerThread implements Runnable {

        private PlayerThread(){
            this.reset();
            stop = true;
        }

        @Override
        public void run() {
            //BUGFIX: synchronised musste weg
            long posMod;
            long last;
            long songPosition;
            while(plistManager.setNextSong(Skip.NEXT.getSkipVal())){
                //// PLAY SONG AND NOTIFY OBSERVERS
                if(skip){
                    if (pause){
                        load();
                        waiter();
                    }
                    System.out.println("SKIPPING");
                    waiter();
                    System.out.println("RESUME SK");
                }
                else if(stop || !(!pause && !skip)){
                    waiter();
                    System.out.println("RESUME ST");
                }
//                else if(pause && stop){
//                    reset();
//                    waiter();
//                    resume();
//                }
                else{
                    skipSong(Skip.NEXT);
                    resume();
                }
                setChanged();
                notifyObservers();

                reset();
                last = -1;
                while (player.isPlaying() ){
                    songPosition = player.position();
                    posMod =  songPosition % Interval.REFRESH_RATE.getVal();
                    if(posMod <= Interval.LATENCY.getVal() && posMod >= 0 && posMod != last){
                        last = posMod;
                        ANSI.CYAN.println("UP PLAY");
                        setChanged();
                        notifyObservers();
                    }
                    if(pause){
                        waiter();
                        if(!player.isPlaying()){
                            System.out.println("BREAKING");
                            break;
                        }
                    }
                    else if(skip || stop){
                        break;
                    }
                }
                if(!skip && !stop) {
                    skipSong(Skip.NEXT);
                }
            }
        }

        /**
         * Will notify the current {@link PlayerThread}.
         * <br>Invokes {@link Thread#notify()}.</br>
         */
        private synchronized void go(){
            setChanged();
            notifyObservers();
            notify();
        }

        /**
         * Resets all the flags to false.
         * {@linkplain #play}
         * {@linkplain #pause}
         * {@linkplain #skip}
         * {@linkplain #stop}
         */
        private void reset(){
            pause = stop = skip = false;
        }

        /**
         * Will resume playback through invoking {@link SimpleAudioPlayer#play()} and resetting all flags.
         */
        private synchronized void resume(){
            reset();
            player.play();
            notifyAll();
            System.out.println("notified");
            setChanged();
            notifyObservers();
        }

        /**
         *
         * Is called so that the {@link #modelThreader current thread} waits.
         * <br>Invoke {@link Thread#notify()}.</br>
         */
        private void waiter(){
            synchronized (modelThreader) {
                setChanged();
                notifyObservers();
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}


