package me.eduard.musicplayer.Components.VersionManagement;

import me.eduard.musicplayer.AppState;
import me.eduard.musicplayer.Library.AppMode;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.FilesUtils;
import me.eduard.musicplayer.Utils.ValueParser;

import java.util.NoSuchElementException;

public class VersionHandler {

    public void manageIntegrity() {
        double version = getVersion();
        if(version < MainApp.VERSION){
            AppMode.set(AppState.WAITING_FOR_UPDATE_APPROVAL);
            new VersionUpdater();
        }
    }

    public void writeVersion() {
        FilesUtils.writeToFile(
                MainApp.MAIN_APP_PATH.concat("\\AppVersion.txt"), false,
                "Version: "+MainApp.VERSION
        );
    }

    private double getVersion() {
        final String[] versionContent = FilesUtils.getFileContents(MainApp.MAIN_APP_PATH.concat("\\AppVersion.txt"));
        if(versionContent == null) {
            return -1;
        }
        final String versionLabel = versionContent[0];
        final ValueParser parser = new ValueParser();
        parser.setFullString(versionLabel);
        double version = parseVersion(parser, versionLabel);
        if(version != -1) {
            return version;
        }
        final String realLabel = parser.getRealLabel(versionLabel);
        return parseVersion(parser, realLabel);
    }

    private double parseVersion(final ValueParser parser, final String value) {
        try {
            return parser.toDoubleValue(value);
        }catch (NoSuchElementException | NumberFormatException exception){
            return -1;
        }
    }

}
