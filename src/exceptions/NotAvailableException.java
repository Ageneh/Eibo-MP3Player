package exceptions;

/**
 *
 * @author Michael Heide
 * @author Henock Arega
 */
public class NotAvailableException extends Exception {

    public NotAvailableException(String msg){
        super(msg);
    }
    public NotAvailableException(){
        super();
    }

}
