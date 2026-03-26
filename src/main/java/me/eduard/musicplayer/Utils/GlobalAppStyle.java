package me.eduard.musicplayer.Utils;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;

public class GlobalAppStyle {

    public static void applyToButtons(Button... buttons){
        final String defaultStyle = """
                        -fx-text-fill: white;
                        -fx-background-color: #363636;
                        -fx-background-radius: 5px;
                        -fx-padding: 0;
                        -fx-border-color: transparent;
                        -fx-border-width: 2px;
                        -fx-border-radius: 5px;
                    """;
        final String hoverStyle = """
                        -fx-text-fill: white;
                        -fx-padding: 0;
                        -fx-background-color: #363636;
                        -fx-border-color: #919191;
                        -fx-border-width: 2px;
                        -fx-border-radius: 5px;
                        -fx-background-color: #525252;
                        -fx-background-radius: 5px;
                """;
        for(Button b : buttons){
            b.setPrefHeight(25);
            b.setStyle(defaultStyle);
            b.setOnMouseMoved(e -> b.setStyle(hoverStyle));
            b.setOnMousePressed(e -> b.setStyle("""
                        -fx-text-fill: #1a1a1a;
                        -fx-background-color: #919191;
                        -fx-border-color: #919191;
                        -fx-padding: 0;
                        -fx-border-width: 2px;
                        -fx-border-radius: 5px;
                        -fx-background-radius: 5px;
                    """));
            b.setOnMouseClicked(e -> b.setStyle(hoverStyle));
            b.setOnMouseExited(e -> b.setStyle(defaultStyle));
            b.setOnMouseReleased(e -> b.setStyle(defaultStyle));
        }
    }

    public static void applyToHyperLinks(Hyperlink... hyperlinks){
        final String defaultStyle = "-fx-border-style: none;";
        for(Hyperlink hyperlink : hyperlinks){
            hyperlink.setStyle(defaultStyle);
            hyperlink.setOnMousePressed(e -> hyperlink.setStyle("-fx-border-color: transparent"));
            hyperlink.setOnMouseExited(e -> hyperlink.setStyle(defaultStyle));
            hyperlink.setOnMouseReleased(event -> hyperlink.setStyle("-fx-border: transparent"));
        }
    }

    public static void applyToCheckbox(CheckBox... checkBoxes){
        final String defaultStyle = """
                -fx-background-color: transparent;
                -fx-border-color: transparent;
                -fx-background-radius: 5px;
                -fx-border-width: 2px;
                -fx-border-radius: 5px;
                -fx-text-fill: white;
                -fx-padding: 0;
                """;
        final String hoverStyle = """
                -fx-padding: 0;
                -fx-text-fill: white;
                -fx-border-color: #919191;
                -fx-border-width: 2px;
                -fx-border-radius: 5px;
                """;
        for(CheckBox checkBox : checkBoxes){
            checkBox.setStyle(defaultStyle);
            checkBox.setOnMouseMoved(e -> checkBox.setStyle(hoverStyle));
            checkBox.setOnMousePressed(e -> checkBox.setStyle("""
                    -fx-text-fill: #1a1a1a;
                    -fx-background-color: #919191;
                    -fx-border-color: #919191;
                    -fx-padding: 0;
                    -fx-border-width: 2px;
                    -fx-border-radius: 5px;
                    -fx-background-radius: 5px;
                    """));
            checkBox.setOnMouseClicked(e -> checkBox.setStyle(hoverStyle));
            checkBox.setOnMouseExited(e -> checkBox.setStyle(defaultStyle));
            checkBox.setOnMouseReleased(e -> checkBox.setStyle(defaultStyle));
        }
    }

}
