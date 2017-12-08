package mvc;

import exceptions.NotAvailableException;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import mvc.view.StartupView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;

import static javafx.application.Application.launch;

public class MP3Launcher {

    public static void main(String[] args){
        new MP3Launcher(args);
    }

    public MP3Launcher(String[] args){
        this.init();
        Application.launch(StartupView.class, args);
    }

    public void init() {
        System.out.println("\n***********************************************\n");
        System.out.print("Checking ");
        int count = (int)(Math.random() * 5 + 20);
        for(int i = 0; i < count; i++){
            System.out.print(".");
            try {
                Thread.currentThread().sleep(50 + (int)(Math.random() * 50));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("\n");
        this.checkFolder();
        System.out.println("\n***********************************************\n");
    }

    private void checkFolder() {
        File playlistDir = null;
        playlistDir = new File(StandardValues.STD_PLAYLIST_ROOT.getVal());

        if(playlistDir.exists() && playlistDir.isDirectory()){
            System.out.println("Playlist folder \""+ playlistDir.getAbsolutePath() +"\" exists.");
        }
        else{
            playlistDir.mkdir();
            System.out.println("Playlist folder \"" + StandardValues.TEMP_PLIST_TITLE.getVal()
                    + "\" has been created.");
            System.out.println("From now on, you can find all your playlists, in this folder:");
            System.out.println("\"" + playlistDir.getAbsolutePath() + "\n");
        }
    }
}
