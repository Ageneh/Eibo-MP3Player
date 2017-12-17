package mvc.view.custom_elements;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import mvc.view.enums.Dim;

public class ControlButton extends Button {

    public ControlButton(String text){
        super(text);
        this.setId(text.toLowerCase());
        this.setMinHeight(Dim.SIZE_CTRL_BTN.intVal());
        this.setMaxHeight(Dim.SIZE_CTRL_BTN.intVal());
        this.setPrefHeight(Dim.SIZE_CTRL_BTN.intVal());
        this.setMinWidth(Dim.SIZE_CTRL_BTN.intVal());
        this.setPrefWidth(Dim.SIZE_CTRL_BTN.intVal());
        this.setMaxWidth(Dim.SIZE_CTRL_BTN.intVal());
        this.setAlignment(Pos.CENTER);
    }

}