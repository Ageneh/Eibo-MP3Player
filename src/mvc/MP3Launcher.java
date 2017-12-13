package mvc;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import misc.ANSI;
import mvc.model.extension.enums.StandardValues;
import mvc.view.StartupView;

import java.io.File;

/**
 * This class starts the MP3.
 * The main of this class will create a new instance of itself.
 */
public class MP3Launcher {

    public static void main(String[] args){
        new MP3Launcher(args);
    }

    /**
     * The main constructor. Will call its init method and uses the arguments - handed
     * down from the main - to show the {@link StartupView}.
     * @param args The initial arguments handed down from the main.
     */
    public MP3Launcher(String[] args){
//        StartupView show = new StartupView();
        System.out.println("Started background");
        Application.launch(StartupView.class, args);
        System.out.println("ended background");

        System.out.println("Started init");
        this.init();
    }

    /**
     * Will run a small check on show of {@link MP3Launcher}.
     */
    public void init() {
        ANSI.GREEN.println("\n***********************************************\nChecking ");
        int count = (int)(Math.random() * 5 + 20);
        for(int i = 0; i < count; i++){
            System.out.print(".");
            try {
                Thread.currentThread().sleep(50 + (int)(Math.random()) * (int)(Math.random() * 100 + 35));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println();
        this.checkFolder();
        ANSI.GREEN.println("***********************************************\n");
    }

    /**
     * Will check, whether the {@code playlists folder}
     * relative to the src-folder exists. The {@code playlists folder} is
     * crucial when it comes to the creation and loading of playlists.
     * <br>If {@code playlists} does not exist, it will be created.
     */
    private void checkFolder() {
        File playlistDir;
        playlistDir = new File(StandardValues.STD_PLAYLIST_ROOT.getString());

        if(playlistDir.exists() && playlistDir.isDirectory()){
            ANSI.GREEN.println("Playlist folder \""+ playlistDir.getAbsolutePath() +"\" exists.");
        }
        else{
            playlistDir.mkdir();
            ANSI.YELLOW.println("Playlist folder \"" + StandardValues.TEMP_PLIST_TITLE.getString() + "\" has been created.");
            ANSI.YELLOW.println("From now on, you can find all your playlists, in this folder:");
            ANSI.YELLOW.println("\"" + playlistDir.getAbsolutePath());
        }
    }
}
