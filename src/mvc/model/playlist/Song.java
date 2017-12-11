package mvc.model.playlist;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import mvc.model.extension.enums.Filetype;
import mvc.model.extension.enums.StandardValues;

import java.io.File;
import java.io.IOException;

/**
 * A class for a mp3 file.
 * <br>Saves specific metadata such as title, album, cover image, length and filepath of a mp3 file.</br>
 * <br>This class extends {@link SongAssets}.</br>
 */
public class Song extends SongAssets {

    /////////////////////// VARIABLES

    /**
     * Saves the title of the {@link Song}.
     * <br>Gets the title from the {@linkplain #mp3File} and if the title should be {@code null} then the file name
     * will be used instead. {@see #getTitle()}</br>
     */
    private String title;
    /**
     * Saves the artist of the {@link Song}.
     * <br>Gets the artist name from the {@link #mp3File}.</br>
     */
    private String artist;
    /**
     * Saves the absolute path of the {@link Song}.
     * <br>{@linkplain #path} To save the absolute filepath, first a new {@link File} is created of which the
     * absolute path then is extracted.</br>
     */
    private String path;
    private long lengthMillis;
    /**
     * Save a the cover image data in a byte-array.
     * <br>If the {@link #mp3File} doesn't have a cover image, then a standard image will be used instead.
     * The path of the standard cover image is defined in {@link SongAssets}.</br>
     */
    private byte[] coverImg;

    /**
     * Saves the mp3-file of the {@link Song}.
     * <br>Is of type {@link Mp3File}</br>
     */
    private Mp3File mp3File;


    /////////////////////// CONSTRUCTOR

    /**
     * Creates a {@link Song} object.
     * <br>{@see #path}</br>
     * @param path Receives the absolute or relative path to the mp3-file, in the form of a {@link String}.
     */
    public Song(String path){
        try {
            this.path = new File(path).getAbsolutePath();
            this.mp3File = new Mp3File(path);
            this.lengthMillis = mp3File.getLengthInMilliseconds();
            this.title = mp3File.getId3v2Tag().getTitle();
            this.artist = mp3File.getId3v2Tag().getArtist();
            this.setAlbumCover();
        } catch (InvalidDataException e) {
            e.printStackTrace();
        } catch (UnsupportedTagException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@linkplain #Song(File)}
     * @param file Receives a {@link File} object, of which the path will be passed through.
     */
    Song(File file){
        this(file.getAbsolutePath());
    }


    /////////////////////// GETTERS

    /**
     * Getter for the total length of {@link Song} in milliseconds.
     * @return Returns a long value.
     */
    public long getLengthMillis(){
        return this.lengthMillis;
    }

    /**
     * Getter for the title of the {@link Song}.
     * @return Returns the {@see #title} which is extracted from the {@linkplain #mp3File}.
     * <br>If the {@see title} should be {@code null} the the file name will be used instead.</br>
     */
    public String getTitle(){
        if(title == null){
            // if song title does not exist, then return Filename, without extension
            return new File(this.path).getName().replace(Filetype.MP3.getSuffix(), "");
        }
        return title;
    }

    /**
     * Getter for the album title of the {@link Song}.
     * @return Returns a {@link String} object.
     */
    public String getAlbum(){
        return mp3File.getId3v2Tag().getAlbum();
    }

    /**
     * Getter for the artist of the {@link Song}.
     * @return Returns a {@link String} object.
     */
    public String getArtist(){
        if(this.artist == null || this.artist.equals("")){
            this.artist = StandardValues.UNKNOW_ARTIST.getString();
            return this.artist;
        }
        return mp3File.getId3v2Tag().getArtist();
    }

    /**
     * Getter for the absolute path of the {@link Song}.
     * @return Returns a {@link String} object.
     */
    public String getPath() {
        return path;
    }

    /**
     * Getter for the album title of the {@link Song}.
     * @return Returns a {@link String} object.
     */
    public byte[] getCover(){
        return this.coverImg;
    }


    /////////////////////// SETTERS

    /**
     * Sets the {@linkplain #coverImg}.
     * <br>If the {@linkplain #mp3File} doesn't have a cover image then the predefined standard image
     * {@see SongAssets#stdSongCover} will be used.</br>
     */
    private void setAlbumCover(){
        if(mp3File.getId3v2Tag().getAlbumImage() != null){
            this.coverImg = mp3File.getId3v2Tag().getAlbumImage();
        }
        else /*if(this.coverImg == null || this.coverImg != null && this.coverImg.length < 1)*/{
            this.coverImg = super.getSongCover();
        }
    }

}