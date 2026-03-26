package me.eduard.musicplayer.Library;

import javafx.stage.FileChooser;

import java.io.File;

@SuppressWarnings("unused")
public class FileSelector {
    private FileChooser.ExtensionFilter[] extensions = new FileChooser.ExtensionFilter[]{};
    private String title;
    private String initialFileName = null;
    private File initialDirectory = null;
    private File result;

    public FileSelector(){}
    public FileSelector setExtensions(FileChooser.ExtensionFilter... extensions){
        this.extensions = extensions;
        return this;
    }
    public FileSelector setTitle(String title){
        this.title = title;
        return this;
    }
    public FileSelector setInitialFileName(String initialFileName){
        this.initialFileName = initialFileName;
        return this;
    }
    public FileSelector setInitialDirectory(File initialDirectory){
        this.initialDirectory = initialDirectory;
        return this;
    }
    public File getResult(){
        if(this.result == null)
            throw new IllegalStateException("Result file is null or missing.");
        return this.result;
    }

    public void launch(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(this.title);
        fileChooser.getExtensionFilters().addAll(this.extensions);
        fileChooser.setInitialDirectory(this.initialDirectory);
        fileChooser.setInitialFileName(this.initialFileName);
        this.result = fileChooser.showOpenDialog(null);
    }

}
