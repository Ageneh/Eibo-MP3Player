package exceptions;

public class NotAvailableException extends Exception {

    public NotAvailableException(String msg){
        super(msg);
    }
    public NotAvailableException(){
        super();
    }

}
