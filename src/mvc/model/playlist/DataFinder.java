package mvc.model.playlist;

import misc.ANSI;
import mvc.model.extension.enums.Filetype;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class DataFinder {

    private ArrayList<String> paths;

    public DataFinder(){
        this.paths = new ArrayList<>();
    }

    public ArrayList<String> findFiles(String rootpath, Filetype... type){
        this.searchForFiles(rootpath, type);
        HashSet<String> hashTemp = new HashSet<>();
        hashTemp.addAll(paths);
        paths.clear();
        return new ArrayList<>(hashTemp);
    }

    public boolean fileExist(String rootpath, String filename, Filetype... type){
        ArrayList<String> files = findFiles(rootpath, type);
        if(files.contains(filename)){
            return true;
        }
        else{
            ANSI.CYAN.println(filename + " DOESNT EXIST.");
            ANSI.CYAN.println("=====================\n");
            return false;
        }
    }

    private void searchForFiles(String path, Filetype... type){
        this.searchForFiles(path, 0, type);
    }

    private void searchForFiles(String path, int index, Filetype ... filetype){
        File tempFile = new File(path);
        File[] tempFiles = tempFile.listFiles();

        if(tempFiles == null){
            return;
        }
        for(File file : tempFiles){
            if(!file.isHidden()){
                if(file.isDirectory()) {
                    this.searchForFiles(file.getPath(), index + 1, filetype);
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
