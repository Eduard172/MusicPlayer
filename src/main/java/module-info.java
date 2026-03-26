module me.eduard.musicplayer {

    requires java.sql;
    requires jdk.jshell;
    requires java.management;
    requires jdk.management;
    requires java.naming;
    requires jdk.xml.dom;
    requires java.desktop;
    requires jdk.compiler;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;

    opens me.eduard.musicplayer to javafx.fxml;
    opens me.eduard.musicplayer.Components to javafx.fxml;
    opens me.eduard.musicplayer.Components.ManagePlaylist to javafx.fxml;
    opens me.eduard.musicplayer.Components.Player to javafx.fxml;
    opens me.eduard.musicplayer.Components.Notifications to javafx.fxml;
    opens me.eduard.musicplayer.Library.Cache to javafx.fxml;
    opens me.eduard.musicplayer.Library.Cache.Window to javafx.fxml;
    opens me.eduard.musicplayer.Library.Animations to javafx.fxml;
    opens me.eduard.musicplayer.Library to javafx.fxml;
    opens me.eduard.musicplayer.Library.CustomComponents to javafx.fxml;
    opens me.eduard.musicplayer.Library.FFmpeg to javafx.fxml;
    opens me.eduard.musicplayer.Dialogs to javafx.fxml;
    opens me.eduard.musicplayer.Utils.PlaylistRelated to javafx.fxml;
    opens me.eduard.musicplayer.Library.Navigation to javafx.fxml;

    exports me.eduard.musicplayer;
    exports me.eduard.musicplayer.Components;
    exports me.eduard.musicplayer.Components.Player;
    exports me.eduard.musicplayer.Utils;
    exports me.eduard.musicplayer.Library.Cache;
    exports me.eduard.musicplayer.Library.Cache.Window;
    exports me.eduard.musicplayer.Library.Animations;
    exports me.eduard.musicplayer.Utils.StageRelated;
    exports me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder;
    exports me.eduard.musicplayer.Library;
    exports me.eduard.musicplayer.Library.CustomComponents;
    exports me.eduard.musicplayer.Utils.PlaylistRelated;
    exports me.eduard.musicplayer.Library.FFmpeg;
    exports me.eduard.musicplayer.Library.Navigation;
    exports me.eduard.musicplayer.Components.VersionManagement;
    opens me.eduard.musicplayer.Components.VersionManagement to javafx.fxml;

}