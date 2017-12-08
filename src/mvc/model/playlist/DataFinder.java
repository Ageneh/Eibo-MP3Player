package mvc.model.playlist;

import mvc.model.extension.enums.Filetype;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class DataFinder {

    private File rootDirectory;
    private ArrayList<String> paths;

    public DataFinder(){
        this.paths = new ArrayList<>();
    }

    public ArrayList<String> findFiles(String rootpath){
        return this.findFiles(rootpath, Filetype.M3U);
    }

    public ArrayList<String> findFiles(String rootpath, Filetype ... type){
        this.searchForPlaylists(rootpath, type);
        HashSet<String> hashTemp = new HashSet<>();
        hashTemp.addAll(paths);
        paths.clear();
        return new ArrayList<>(hashTemp);
    }

    public void searchForPlaylists(String path){
        this.searchForPlaylists(path, Filetype.MP3);
    }

    public void searchForPlaylists(String path, Filetype ... type){
        this.searchForPlaylists(path, 0, type);
    }

    private void searchForPlaylists(String path, int index, Filetype ... filetype){
        File tempFile = new File(path);
        File[] tempFiles = tempFile.listFiles();

        if(tempFiles == null){
            return;
        }
        for(File file : tempFiles){
            if(!file.isHidden()){
                if(file.isDirectory()) {
                    this.searchForPlaylists(file.getPath(), index + 1, filetype);
                }
                else{
                    for(Filetype type : filetype) {
                        if (file.getName().endsWith(type.getSuffix())) {
                            this.paths.add(file.getPath());
                        }
                    }
                }
            }
        }

    }

}
