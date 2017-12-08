package mvc.model.playlist;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public abstract class SongAsset {

    private static final String STD_SONG_COVER = "playlist/covers/default-release-cd.png";
    private byte[] stdSongCover;

    public SongAsset(){
        this.stdSongCover = this.setStdSongCover(STD_SONG_COVER);
    }

    public byte[] setStdSongCover() {
        return stdSongCover;
    }

    protected byte[] getSongCover(){
        return this.setStdSongCover(STD_SONG_COVER);
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
        }
        return imgBytes;
    }

}
