package mvc.model.playlist;

import misc.ANSI;
import mvc.model.extension.enums.StandardValues;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SongAssets {

    private byte[] stdSongCover;

    public byte[] getSongCover(){
        return this.setStdSongCover(StandardValues.STD_SONG_COVER.getString());
    }

    public byte[] getSongCover(String path){
        return this.setStdSongCover(path);
    }

    private byte[] setStdSongCover(String path){
        File imgFile = new File(path);
        byte[] imgBytes = null;
        try {
            imgBytes = Files.readAllBytes(imgFile.toPath());
        } catch (IOException e) {
            System.out.println(ANSI.YELLOW.colorize("\n===========\n"));
        }
        return imgBytes;
    }

}
