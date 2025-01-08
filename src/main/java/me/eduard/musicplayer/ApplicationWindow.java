package me.eduard.musicplayer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import me.eduard.musicplayer.Utils.OutputUtilities;
import me.eduard.musicplayer.Utils.Utilities;

@SuppressWarnings("all") public abstract class ApplicationWindow extends OutputUtilities {

    private double xOffset = 0.0d;
    private double yOffset = 0.0d;

    @FXML public AnchorPane corePane;
    @FXML private Label taskbarTitle;
    @FXML public Button xButton, minimizeButton;
    @FXML public Rectangle taskBar;
    @FXML private ImageView xImage, minimizeImage;
    private Stage stage;

    /**
     * Sets the working stage
     * @param stage - Working stage
     * @return A chaining object
     */
    public ApplicationWindow setStage(Stage stage){
        this.stage = stage;
        return this;
    }

    /**
     * Sets the title of the top bar.
     * @param title The title to be set
     * @return A chaining object
     */
    public ApplicationWindow title(String title){
        this.taskbarTitle.setText(title);
        return this;
    }

    /**
     * Sets the X button icon for the top bar.
     * @param imagePath Icon to be set.
     * @return A chaining object
     */
    public ApplicationWindow setXImage(String imagePath){
        Utilities.setImageView(this.xImage, imagePath);
        return this;
    }

    /**
     * Sets the Minimize button icon for the top bar.
     * @param imagePath Icon to be set
     * @return A chaining object
     */
    public ApplicationWindow setMinimizeButton(String imagePath){
        Utilities.setImageView(this.minimizeImage, imagePath);
        return this;
    }

    /**
     * Sets the taskbar behaviours such as dragging
     * @return A chaining object
     */
    public ApplicationWindow updateTaskbarBehaviour(){
        this.taskBar.setOnMousePressed(this::onMousePress);
        this.taskBar.setOnMouseDragged(this::onTaskbarDrag);
        this.taskbarTitle.setOnMousePressed(this::onMousePress);
        this.taskbarTitle.setOnMouseDragged(this::onTaskbarDrag);
        return this;
    }

    /**
     * Sets the taskbar buttons behaviour
     * @return A chaining object
     */
    public ApplicationWindow setDefaultTaskbarButtonsBehaviour(){
        this.xButton.setOnAction(event -> MainApp.closeStage(this.stage, true));
        this.minimizeButton.setOnAction(event -> this.stage.setIconified(!this.stage.isIconified()));
        return this;
    }

    /**
     * Sets the default icons for the X and Minimize buttons.
     * @return A chaining object
     */
    public ApplicationWindow setDefaultTaskbarButtonsIcons(){
        Utilities.setImageView(this.xImage, "XButton2.png");
        Utilities.setImageView(this.minimizeImage, "MinimizeIcon.png");
        return this;
    }

    /**
     * Sets all default functionalities such as X and Minimize buttons default icons, dragging and the functionalities to the buttons.
     * @return A chaining object
     */
    public ApplicationWindow useDefaultFunctionalityPresets(){
        this.updateTaskbarBehaviour().setDefaultTaskbarButtonsIcons().setDefaultTaskbarButtonsBehaviour();
        return this;
    }

    /**
     * Saves the x and y offset points when pressing the top bar.
     * @param event The mouse event required to work
     */
    public void onMousePress(MouseEvent event){
        this.xOffset = event.getSceneX();
        this.yOffset = event.getSceneY();
    }

    /**
     * Updates the stage's X and Y coordinate based on the pointer position.
     * @param event The mouse event required to work.
      */
    public void onTaskbarDrag(MouseEvent event){
        this.stage.setX(event.getScreenX() - this.xOffset);
        this.stage.setY(event.getScreenY() - this.yOffset);
    }
}
