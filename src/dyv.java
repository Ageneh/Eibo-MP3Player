import javafx.beans.property.SimpleIntegerProperty;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

public class dyv extends Observable {

    public static void main(String[] args){
        long l = 23425;
        System.out.println(
                String.format("%2d:%2d",
                        TimeUnit.MILLISECONDS.toMinutes(l),
                        TimeUnit.MILLISECONDS.toSeconds(l))
        );
        l = 123;
        System.out.println(TimeUnit.SECONDS.toMinutes(l));
        System.out.println((TimeUnit.SECONDS.toMinutes(l) - TimeUnit.SECONDS.toSeconds(l)));

        System.out.println(
                String.format("%02d:%02d",
                        TimeUnit.SECONDS.toMinutes(l),
                        TimeUnit.SECONDS.toSeconds(TimeUnit.SECONDS.toSeconds(l) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(l))))
        );

        dyv d = new dyv();
        Observer o = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                ;
                System.out.println(d.getX());
            }
        };
        d.addObserver(o);

        d.start();
        System.out.println("STARTED\n");

    }

    private Thread thread;
    private String str;
    private volatile int i;
    private SimpleIntegerProperty sipI;
    private SimpleIntegerProperty x;

    public dyv(){
        str = "Hello";
        i = 1;

        x = new SimpleIntegerProperty(i);
        sipI = new SimpleIntegerProperty(i);

        x.bind(sipI);

        thread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (true && i < 20){
                            try {
                                Thread.sleep(2000);
//                                System.out.println(str + " " + i++ + "x");
                                setChanged();
                                i++;
                                sipI.set(i);
                                x.multiply(i* 9);
                                notifyObservers();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
        thread.setDaemon(true);
    }

    public int getX() {
        return x.get();
    }

    public void start(){
        this.thread.run();
    }

}
