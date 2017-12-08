package testing;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Observable;
import java.util.Observer;

public class MP extends Application {

    public static void main(String[] args) {
        launch(args);
    }
int i = 0;
    @Override
    public void start(Stage primaryStage) {
        MP3 m = new MP3("/view_assests/henock/Music/iTunes/iTunes Media/Music/Kwabs/Unknown Album/Cheating On Me.mp3");
        Button btn = new Button("Hallo");
        btn.setOnMouseClicked(
                event -> m.play()
        );

        Observer o = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                btn.setText("HEY" + i++);
            }
        };
        m.addObserver(o);

        m.posProp().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        btn.setText(newValue.intValue() + "");
                    }
                }
        );

        Pane p = new Pane();
        p.getChildren().add(btn);
        Scene s = new Scene(p);
        primaryStage.setScene(s);
        primaryStage.show();

    }
}
