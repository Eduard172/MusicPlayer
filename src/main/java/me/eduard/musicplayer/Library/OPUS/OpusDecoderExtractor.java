package me.eduard.musicplayer.Library.OPUS;

import javafx.scene.control.Label;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.FilesUtils;

import java.io.File;

public class OpusDecoderExtractor {

    public static void extract(Label label) {
        if(new File(MainApp.APP_EXTERNAL_HELPERS+"\\opusdec.exe").exists()){
            return;
        }
        final String url = "https://archive.mozilla.org/pub/opus/win32/opus-tools-0.2-opus-1.3.zip";
        final String zipPath = MainApp.APP_EXTERNAL_HELPERS.concat("\\opusDecoder.zip");
        FilesUtils.downloadFromInternet(zipPath, url, "[Opus Decoder] Downloaded [p]% out of 100%", label);
        FilesUtils.unzipFile(zipPath, true, "opusdec.exe");
    }

}
