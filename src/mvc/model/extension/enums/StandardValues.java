package mvc;

import mvc.model.extension.enums.Filetype;

public enum StandardValues {

    STD_PLAYLIST_ROOT("playlists/"),
    TEMP_PLIST_TITLE("temp-playlist"),
    DRAG_MSG_STD("Please drag a " + Filetype.MP3 + " file or folder containing" + Filetype.MP3 + "s into this window."),
    DRAG_MSG_ERR("Your file is not valid.");

    private String val;

    StandardValues(String str){
        this.val = str;
    }

    public String getVal() {
        return val;
    }
}
