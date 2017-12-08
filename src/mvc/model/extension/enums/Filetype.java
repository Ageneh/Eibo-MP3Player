package mvc.model.extension.enums;

public enum Filetype {

    M3U("M3U", ".m3u"),
    MP3("MP3", ".mp3");

    private String type;
    private String suffix;

    Filetype(String type, String suffix){
        this.type = type;
        this.suffix = suffix;
    }

    public String getSuffix() {
        return this.suffix;
    }
}
