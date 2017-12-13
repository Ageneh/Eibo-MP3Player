package mvc.view;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Observable;

/**
 * This class will show the visual mp3-player.
 */
public abstract class View extends Observable {

//    private Scene baseScene;
//    private BorderPane root;
    private StartupView start;


    /////////////////////// CONSTRUCTOR

    public View(){
//        //// SETTING UP STAGE AND ROOT PANE
//        this.root = new BorderPane();
//        this.root.setMinSize(
//                Dim.MIN_W_SCREEN.intVal(),
//                Dim.MIN_H_SCREEN.intVal()
//        );
//        this.baseScene = new Scene(this.root);
//        this.show = new StartupView();
        this.start = new StartupView();
    }
//
//
//    /////////////////////// PUBLIC METHODS
//
//    public static void main(String args[]){
//        launch(args);
//    }
//
////    public void init() {
//////        ControlButton btn = new ControlButton("Btton");
//////        btn.setOnMouseClicked(
//////                new EventHandler<MouseEvent>() {
//////                    @Override
//////                    public void handle(MouseEvent event) {
//////
//////                    }
//////                }
//////        );
//////        this.root.setBottom(btn);
////    }
//
//    @Override
//    public void show(Stage primaryStage) throws Exception {
//        this.show.show(primaryStage);
//    }

    private class ViewApplication extends Application{


        @Override
        public void start(Stage primaryStage) throws Exception {

        }
    }

}
