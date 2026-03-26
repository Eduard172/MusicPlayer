package me.eduard.musicplayer.Library;

import javafx.stage.DirectoryChooser;

import java.io.File;

@SuppressWarnings("unused")
public class DirectorySelector {
    private String title;
    private File initialDirectory = null;
    private File result;

    public DirectorySelector(){}
    public DirectorySelector setTitle(String title){
        this.title = title;
        return this;
    }
    public DirectorySelector setInitialDirectory(File initialDirectory){
        this.initialDirectory = initialDirectory;
        return this;
    }
    public File getResult(){
        return this.result;
    }

    public void launch(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(this.title);
        directoryChooser.setInitialDirectory(this.initialDirectory);
        this.result = directoryChooser.showDialog(null);
    }
}
