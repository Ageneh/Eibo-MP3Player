package testing;

import ddf.minim.AudioListener;
import de.hsrm.mi.eibo.simpleplayer.SimpleAudioPlayer;
import de.hsrm.mi.eibo.simpleplayer.SimpleMinim;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.Observable;

public class MP3 extends Observable {

    SimpleMinim minim;
    SimpleAudioPlayer player;
    SimpleIntegerProperty pos;


    public MP3(String str){
        minim = new SimpleMinim(true);
        player = minim.loadMP3File(str);
        pos = new SimpleIntegerProperty(player.position());
    }
    void play() {
        player.play();
        setChanged();
        System.out.println("PLAYlPALYPLAYPLAY");
        pos = new SimpleIntegerProperty(player.position());
        notifyObservers();
    }
    public final SimpleIntegerProperty posProp(){
        return this.pos;
    }

    public int getPos() {
        return pos.get();
    }
}
