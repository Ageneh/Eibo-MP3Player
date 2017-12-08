package exceptions;

public class NotInitialized extends Exception {

    public NotInitialized(String str){
        super(str + " has not been initialized.");
    }

}
