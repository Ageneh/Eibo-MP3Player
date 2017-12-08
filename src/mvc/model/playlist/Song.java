package mvc.model.playlist;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import mvc.model.extension.enums.Filetype;

import java.io.File;
import java.io.IOException;
import java.sql.Time;

public class Song extends SongAsset {

    /////////////////////// VARIABLES

    private final long CONST_DEFICITE = 3600000; // 1hour

    private String title;
    private String path;
    private long lengthSeconds;
    private long lengthMillis;
    private Time length;
    private byte[] coverImg;

    private Mp3File mp3File;


    /////////////////////// CONSTRUCTOR

    public Song(String path){
        try {
            this.mp3File = new Mp3File(path);
            this.lengthSeconds = mp3File.getLengthInSeconds();
            this.lengthMillis = mp3File.getLengthInMilliseconds();
            this.length = new Time(lengthMillis - CONST_DEFICITE);
            this.path = path;
            this.title = mp3File.getId3v2Tag().getTitle();
            this.setAlbumCover();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedTagException e) {
            e.printStackTrace();
        } catch (InvalidDataException e) {
            e.printStackTrace();
        }
    }

    public Song(File file){
        this(file.getAbsolutePath());
    }


    /////////////////////// PUBLIC METHODS

    public static void main(String[] args) {
        Song s = new Song("/view_assests/henock/Music/iTunes/iTunes Media/Music/Logic/Under Pressure (Deluxe Edition)/09 Metropolis.mp3");
        System.out.println(s.getLength().toString());
        s = new Song("/view_assests/henock/Music/iTunes/iTunes Media/Music/Logic/Under Pressure (Deluxe Edition)/05 Buried Alive.mp3");
        System.out.println(s.getLength().toString());
    }


    /////////////////////// GETTERS

    public long getLengthSeconds(){
        return this.lengthSeconds;
    }

    public long getLengthMillis(){
        return this.lengthMillis;
    }

    public String getTitle(){
        if(title == null){
            // if song title does not exist, then return Filename, without extension
            return new File(this.path).getName().replace(Filetype.MP3.getSuffix(), "");
        }
        return title;
    }

    // TODO CHANGE TO LONG BECAUSE IT SHOULD BE MODULAR
    public Time getLength(){
        return this.length;
    }

    public String getAlbum(){
        return mp3File.getId3v2Tag().getAlbum();
    }

    public String getArtist(){
        return mp3File.getId3v2Tag().getArtist();
    }

    public String getPath() {
        return path;
    }

    public byte[] getCover(){
        return this.mp3File.getId3v2Tag().getAlbumImage();
    }


    /////////////////////// SETTERS

    private void setAlbumCover(){
        this.coverImg = mp3File.getId3v2Tag().getAlbumImage();

        if(this.coverImg == null || this.coverImg != null && this.coverImg.length < 1){
            this.setCoverImage(
                    super.getStdSongCover()
            );
        }
    }

    public void setCoverImage(String imagePath){
        this.setCoverImage(
                super.getSongCover(imagePath)
        );
    }

    public void setCoverImage(byte[] image){
        this.mp3File.getId3v2Tag().setAlbumImage(image, "IMAGE");
    }


}