package mvc.view.elements;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import mvc.view.enums.Dim;

public class ControlButton extends Button {

    public ControlButton(String text){
        super(text);
        this.setId(text.toLowerCase());
        this.setMinHeight(Dim.SIZE_CTRL_BTN.doubleVal());
        this.setMaxHeight(Dim.SIZE_CTRL_BTN.doubleVal());
        this.setPrefHeight(Dim.SIZE_CTRL_BTN.doubleVal());
        this.setMinWidth(Dim.SIZE_CTRL_BTN.doubleVal());
        this.setPrefWidth(Dim.SIZE_CTRL_BTN.doubleVal());
        this.setMaxWidth(Dim.SIZE_CTRL_BTN.doubleVal());
        this.setAlignment(Pos.CENTER);
    }

}