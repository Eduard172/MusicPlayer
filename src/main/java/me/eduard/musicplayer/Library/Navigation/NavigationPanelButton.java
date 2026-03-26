package me.eduard.musicplayer.Library.Navigation;

import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.List;

public class NavigationPanelButton {

    private Button button;
    private final AnchorPane leftSide;
    private static final List<Button> INSTANCES = new ArrayList<>();
    private double height = -1.0f;

    public NavigationPanelButton(AnchorPane leftSideInstance) {
        this.leftSide = leftSideInstance;
    }

    private void init() {
        this.button = new Button("Text");
    }

    public static void update() {
        for(int i = 0; i < INSTANCES.size(); i++) {
            //Continue from here.
        }
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void linkToStage() {
        this.addOrRemoveToRoot(this.button, true);
        INSTANCES.add(this.button);
    }

    public void remove() {
        this.addOrRemoveToRoot(this.button, false);
        INSTANCES.remove(this.button);
    }
    private void addOrRemoveToRoot(Button button, boolean add){
        if(add) this.leftSide.getChildren().add(button);
        else this.leftSide.getChildren().remove(button);
    }


}
