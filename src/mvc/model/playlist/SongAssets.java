package mvc.model.playlist;

import misc.ANSI;
import mvc.model.extension.enums.StandardValues;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public abstract class SongAssets {

//    private static final String STD_SONG_COVER = "/Users/henock/IdeaProjects/EiboMP3/src/mvc/model/playlist/covers/default-release-cd.png";
    private byte[] stdSongCover;

    protected byte[] getSongCover(){
        return this.setStdSongCover(StandardValues.STD_SONG_COVER.getString());
//        return stdSongCover;
    }

    protected byte[] getSongCover(String path){
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
