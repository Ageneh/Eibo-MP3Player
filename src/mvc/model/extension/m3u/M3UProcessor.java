package mvc.model.extension.m3u;

import mvc.model.extension.enums.Filetype;
import mvc.model.playlist.Playlist;
import mvc.model.playlist.Song;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class M3UProcessor {

    private static final String EXT_M3U = "#EXTM3U";
    private static final String EXT_SONG = "#EXTINF";

    private boolean isEXT;
    private BufferedReader breader;
    private String filePath;
    private File file;


    /////////////////////// CONSTRUCTOR

    public M3UProcessor(){
        this.breader = null;
    }


    /////////////////////// PUBLIC METHODS

    public File writePlaylist(String title,
                              String directoryPath,
                              ArrayList<File> files){
        ArrayList<Song> playlistSongs = this.consolidateToArray(files);
        this.writeEXTM3U(title, directoryPath, playlistSongs);
        return file;
    }

    /**
     * Reads every line of an .m3u file and returns the corresponding {@link Song}.
     * @param pathname The location where the selected .m3u file is located.
     * @return Returns an array-list containing {@link Song songs} which can be found in the selected .m3u file.
     */
    public ArrayList<Song> readSongs(String pathname){
        try {
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

    private FileWriter initWriter(String title, String directoryPath, Filetype filetype){
        FileWriter fw = null;
        try {
            if(title == null || title.equals("")){
                LocalDateTime ld = LocalDateTime.now();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM_HH:mm:ss");
                title = "Playlist_" + dtf.format(ld);

                System.out.println("NEW PLAYLIST: " + title);
            }
            filePath = directoryPath + title + filetype.getSuffix();
            file = new File(filePath);
            fw = new FileWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fw;
    }

    /**
     * Creates an m3u file with all songs from {@param playlistSongs}.
     * @param title The title which is usd as the filename.
     * @param directoryPath The location where to write the file at.
     * @param playlistSongs The content - being {@link Song songs} - of the output m3u.
     */
    private void writeEXTM3U(String title, String directoryPath, List<Song> playlistSongs){
        FileWriter writer = null;
        File file;
        try {
            writer = initWriter(title, directoryPath, Filetype.M3U);
            writer.append(EXT_M3U);
            writer.append(System.lineSeparator());
            for (Song song : playlistSongs){
                writer.append(EXT_SONG);
                writer.append(":");
                writer.append(String.format(
                        "%s",
                        TimeUnit.MILLISECONDS.toSeconds(song.getLengthMillis()))
                );
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
            String line = breader2.readLine();
            if(line.equals(EXT_M3U) || line.startsWith(EXT_M3U)){
                return true;
            }
        }
        catch (NullPointerException ignored) { }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ArrayList<Song> readM3U(){
        ArrayList<Song> temp = new ArrayList<>();
        String line;

        try {
            line = this.breader.readLine();
            while (line != null) {
                if(line.startsWith(".") || line.startsWith("/")){
                    temp.add(new Song(line));
                }
                line = this.breader.readLine();
            }
            return temp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }

    private ArrayList<Song> consolidateToArray(ArrayList<File> files){
        ArrayList<Song> songs = new ArrayList<>();

        int songInd = 0;
        for(File file : files){
            if(file.getName().endsWith(Filetype.MP3.getSuffix())) {
                songs.add(
                        new Song(
                                file.getAbsolutePath()
                        )
                );
            }
            else if(file.getName().endsWith(Filetype.M3U.getSuffix())){
                songs.addAll(
                        new Playlist(
                                file.getAbsolutePath()
                        ).getSongs()
                );
            }
        }

        return songs;
    }


}
