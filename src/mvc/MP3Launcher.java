package mvc;

import javafx.application.Application;
import mvc.misc.ANSI;
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
     * down from the main - to start the {@link StartupView}.
     * @param args The initial arguments handed down from the main.
     */
    public MP3Launcher(String[] args){
        this.init();
        Application.launch(StartupView.class, args);
    }

    /**
     * Will run a small check on start of {@link MP3Launcher}.
     */
    public void init() {
        System.out.print("\n***********************************************\nChecking ");
        int count = (int)(Math.random() * 5 + 20);
        for(int i = 0; i < count; i++){
            System.out.print(".");
            try {
                Thread.currentThread().sleep(50 + (int)(Math.random() * 50));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println();
        this.checkFolder();
        System.out.println("***********************************************\n");
    }

    /**
     * Will check, whether the {@code playlists folder}
     * relative to the src-folder exists. The {@code playlists folder} is
     * crucial when it comes to the creation and loading of playlists.
     * <br>If {@code playlists} does not exist, it will be created.
     */
    private void checkFolder() {
        File playlistDir;
        playlistDir = new File(StandardValues.STD_PLAYLIST_ROOT.getVal());

        if(playlistDir.exists() && playlistDir.isDirectory()){
            System.out.println(ANSI.GREEN.colorize("Playlist folder \""+ playlistDir.getAbsolutePath() +"\" exists."));
        }
        else{
            playlistDir.mkdir();
            System.out.println(ANSI.YELLOW.colorize("Playlist folder \"" + StandardValues.TEMP_PLIST_TITLE.getVal()
                    + "\" has been created."));
            System.out.println(ANSI.YELLOW.colorize("From now on, you can find all your playlists, in this folder:"));
            System.out.println(ANSI.YELLOW.colorize("\"" + playlistDir.getAbsolutePath()));
        }
    }
}
