package mvc.model.extension.enums;

import misc.ANSI;

import java.io.File;

/**
 * An enum of different standard values and messages.
 */
public enum StandardValues {

    STD_PLAYLIST_ROOT("playlists/"),
    STD_SONG_COVER(
            new File("covers/default_light.png")),
    TEMP_PLIST_TITLE("temp-playlist"),
    DRAG_MSG_STD("Please drag a " + Filetype.MP3 + " or "
            + Filetype.M3U + " file or folder containing"
            + Filetype.MP3 + " or " + Filetype.M3U + "s into this window."),
    DRAG_MSG_ERR("Your file is not valid."),
    UNKNOW_ARTIST("Unknown Artist"),
    UNNAMED_PLAYLIST("Unnamed Playlist"),
    BASE_VOLUME(50);

    private String strVal;
    private float intVal;

    StandardValues(String str){
        this.strVal = str;
    }

    StandardValues(float floatVal){
        this(Float.toString(floatVal));
        this.intVal = floatVal;
    }

    StandardValues(File file){
        this(file.getAbsolutePath());
        ANSI.GREEN.println(file.exists() + "");
        ANSI.GREEN.println(file.getAbsolutePath() + "");
    }

    public String getString() {
        return strVal;
    }

    public float getFloat() {
        return intVal;
    }
}
