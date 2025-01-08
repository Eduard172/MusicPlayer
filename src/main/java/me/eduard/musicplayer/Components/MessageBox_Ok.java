package me.eduard.musicplayer.Components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import me.eduard.musicplayer.Library.ApplicationWindow;

@SuppressWarnings("UnusedReturnValue")
public class MessageBox_Ok extends ApplicationWindow {
    @FXML private Label header;
    @FXML public Label message;
    @FXML public Button okButton, cancelButton;
    @SuppressWarnings("unused") @FXML private AnchorPane corePane;
    public MessageBox_Ok setHeader(String header){
        this.header.setText(header);
        return this;
    }
    public MessageBox_Ok setMessage(String message){
        this.message.setText(message);
        return this;
    }
}
