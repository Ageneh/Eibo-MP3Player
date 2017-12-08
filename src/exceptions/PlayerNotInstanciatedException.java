package exceptions;

public class PlayerNotInstanciatedException extends Exception {

    public PlayerNotInstanciatedException(){
        this("PLAY IS NOT INSTACIATED YET.");
    }
    public PlayerNotInstanciatedException(String msg){
        super(msg);
    }

}
