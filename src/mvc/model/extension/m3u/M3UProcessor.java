package mvc.model.extension.m3u;

import mvc.model.extension.enums.Filetype;
import mvc.model.playlist.Playlist;
import mvc.model.playlist.Song;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class M3UProcessor {

    private static final String EXT_M3U = "#EXTM3U";
    private static final String EXT_SONG = "#EXTINF";

    private String pathname;
    private boolean isEXT;
    private BufferedReader breader;


    /////////////////////// CONSTRUCTOR

    public M3UProcessor(){
        this.breader = null;
    }


    /////////////////////// PUBLIC METHODS

    public void writePlaylist(String title, String directoryPath, ArrayList<Song> playlistSongs){
        if(!new File(directoryPath + title + Filetype.M3U.getSuffix()).exists()){
            FileWriter fw = null;
            try {
                if(title == null || title.equals("")){
                    LocalDateTime ld = LocalDateTime.now();
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM_HH:mm:ss");
                    title = "NewPlaylist_" + dtf.format(ld);

                    System.out.println("NEW PLAYLIST: " + title);
                }
                fw = new FileWriter(new File(directoryPath + title + Filetype.M3U.getSuffix()));

                fw.flush();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.writeExtended(title, directoryPath, playlistSongs);
//        if(this.isEXT){
//            this.writeExtended(title, directoryPath, playlistSongs);
//        } else{
//            this.writeSimple(title, directoryPath, playlistSongs);
//        }
    }

    public void writePlaylist(String title, String directoryPath, ArrayList<Song> playlistSongs, ArrayList<File> files){
        playlistSongs = this.consolidateM3Us(playlistSongs, files);
        this.writePlaylist(title, directoryPath, playlistSongs);
    }

    public ArrayList<String> getSongs(String pathname){
        try {
            this.pathname = pathname;
            this.breader = new BufferedReader(
                    new FileReader(
                            new File(pathname)
                    )
            );
            this.isEXT = checkM3UType();
            return this.readM3U();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /////////////////////// PRIVATE METHODS

    private void writeExtended(String title, String directoryPath, List<Song> playlistSongs){
        FileWriter writer = null;
        try {
            writer = new FileWriter(directoryPath + title + Filetype.M3U.getSuffix());
            writer.append(EXT_M3U);
            writer.append(System.lineSeparator());
            for (Song song : playlistSongs){
                writer.append(EXT_SONG);
                writer.append(":");
                writer.append("" + song.getLengthSeconds());
                writer.append(",");
                writer.append(song.getTitle());
                writer.append(" - ");
                writer.append(song.getArtist());
                writer.append(System.lineSeparator());
                writer.append(song.getPath());
                writer.append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkM3UType(){
        BufferedReader breader2 = breader;
        try {
            if(breader2.readLine().equals(EXT_M3U)){
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ArrayList<String> readM3U(){
        ArrayList<String> temp = new ArrayList<>();
        String line = null;

        try {
            this.breader = new BufferedReader(
                    new FileReader(
                            new File(pathname)
                    )
            );
            line = this.breader.readLine();
            while (line != null) {
                if(line.startsWith(".") || line.startsWith("/")){
                    temp.add(line);
                }
                line = this.breader.readLine();
            }
            return temp;
        } catch (IOException e) {
            e.printStackTrace();
        }


        return temp;
    }

    private ArrayList<Song> consolidateM3Us(ArrayList<Song> songs, ArrayList<File> files){
        // TODO
        Playlist playlist;

        for(File file : files){
            playlist = new Playlist(file.getAbsolutePath());
            songs.addAll(playlist.getSongs());
        }

        return songs;
    }

}
