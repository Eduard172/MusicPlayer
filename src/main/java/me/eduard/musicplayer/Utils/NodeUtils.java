package me.eduard.musicplayer.Utils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class NodeUtils {
    public enum ViewType{
        ABOVE, NORMAL
    }

    public static void disableNodeActivity(ButtonBase node){
        node.setOnAction(event -> doNothing());
    }
    public static void setNodeActiveMethod(ButtonBase node, EventHandler<ActionEvent> eventHandler){
        node.setOnAction(eventHandler);
    }
    public static void setTooltip(Control node, String message, int millisToShow){
        Tooltip tooltip = new Tooltip(message);
        tooltip.setFont(Font.font(14));
        tooltip.setShowDelay(Duration.millis(millisToShow));
        tooltip.setHideDelay(Duration.millis(0));
        tooltip.setHideOnEscape(true);
        node.setTooltip(tooltip);
    }
    public static void addSameTooltipToMultipleNodes(Control[] nodes, String message, int millisToShow){
        for(Control node : nodes){
            setTooltip(node, message, millisToShow);
        }
    }
    public static void setStyle(Node node, String style){
        String string = node.getStyle();
        if(style.equals(string))
            return;
        node.setStyle(style);
    }
    public static void addBasicHyperLinkMouseEvents(Hyperlink... hyperlinks){
        for(Hyperlink hyperlink : hyperlinks){
            setStyle(hyperlink, "-fx-border-style: none");
            hyperlink.setOnMousePressed(event -> setStyle(hyperlink, "-fx-border-color: transparent"));
            hyperlink.setOnMouseReleased(event -> setStyle(hyperlink, "-fx-border: transparent"));
        }
    }
    public static void setNodeViewType(ViewType viewType, Node node){
        switch (viewType){
            case ABOVE -> node.setViewOrder(-1);
            case NORMAL -> node.setViewOrder(0);
        }
    }
    private static void doNothing(){}

}
