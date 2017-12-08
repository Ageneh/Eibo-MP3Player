package mvc.model.playlist;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public abstract class SongAsset {

    private final String STD_SONG_COVER = "assets/covers/default-release-cd.png";
    private byte[] stdSongCover;

    public SongAsset(){
        this.stdSongCover = this.getSongCover();
    }

    public byte[] getStdSongCover() {
        return stdSongCover;
    }

    protected byte[] getSongCover(){
        return this.getStdSongCover(STD_SONG_COVER);
    }

    protected byte[] getSongCover(String path){
        return this.getStdSongCover(path);
    }

    private byte[] getStdSongCover(String path){
        File imgFile = new File(path);
        byte[] imgBytes = null;
        try {
            imgBytes = Files.readAllBytes(imgFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imgBytes;
    }

}
