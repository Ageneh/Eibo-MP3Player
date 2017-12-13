package misc;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.concurrent.TimeUnit;

public abstract class TimeConverter {

    public static String setTimeFormatStd(long milliseconds){
        return String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.SECONDS.toSeconds(
                        TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                                TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(milliseconds)
                                )
                )
        );
    }

    public static SimpleStringProperty setTimeFormatStd(SimpleLongProperty milliseconds){
        return new SimpleStringProperty(setTimeFormatStd(milliseconds.get()));
    }

}
