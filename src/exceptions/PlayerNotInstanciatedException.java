package exceptions;

/**
 *
 * @author Michael Heide
 * @author Henock Arega
 */
public class PlayerNotInstanciatedException extends Exception {

    public PlayerNotInstanciatedException(){
        this("PLAY IS NOT INSTACIATED YET.");
    }
    public PlayerNotInstanciatedException(String msg){
        super(msg);
    }

}
