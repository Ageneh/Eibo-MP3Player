package mvc.frame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mvc.view.View;
import mvc.view.enums.Dim;

public class Frame extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private View view;
    private Scene baseScene;


    /////////////////////// CONSTRUCTOR

    public Frame(View view){
        this.view = view;
    }

    @Override
    public void start(Stage primaryStage) {

//        primaryStage.setScene(this.view.getScene());

        primaryStage.setMinHeight(Dim.MIN_H_SCREEN.doubleVal());
        primaryStage.setMinWidth(Dim.MIN_W_SCREEN.doubleVal());
        primaryStage.setMaxWidth(Screen.getPrimary().getVisualBounds().getWidth());
        primaryStage.setMaxHeight(Screen.getPrimary().getVisualBounds().getHeight());
        primaryStage.setScene(this.baseScene);
        primaryStage.setOnCloseRequest(
                event -> System.exit(0)
        );
        primaryStage.show();


    }
}
