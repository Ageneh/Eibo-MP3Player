package mvc.view.custom_elements;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import mvc.view.enums.Dimensions;

public class ControlButton extends Button {

    public ControlButton(String text){
        super(text);
        this.setId(text.toLowerCase());
        this.setMinHeight(Dimensions.SIZE_CTRL_BTN.intVal());
        this.setMaxHeight(Dimensions.SIZE_CTRL_BTN.intVal());
        this.setPrefHeight(Dimensions.SIZE_CTRL_BTN.intVal());
        this.setMinWidth(Dimensions.SIZE_CTRL_BTN.intVal());
        this.setPrefWidth(Dimensions.SIZE_CTRL_BTN.intVal());
        this.setMaxWidth(Dimensions.SIZE_CTRL_BTN.intVal());
        this.setAlignment(Pos.CENTER);
    }

}