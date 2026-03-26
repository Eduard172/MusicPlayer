package me.eduard.musicplayer.Library;

import java.awt.*;
import java.io.IOException;

@SuppressWarnings("unused")
public class WindowsCommands {

    public static void executePowerShell(String command){
        try {
            Runtime.getRuntime().exec("powershell -Command \"& %command%\"".replace("%command%", command));
        }catch (IOException exception){
            exception.printStackTrace(System.err);
        }
    }

    public static void launchPowershellNotification(String title, String message){
        try {
            SystemTray systemTray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("");
            TrayIcon trayIcon = new TrayIcon(image);
            trayIcon.setImageAutoSize(true);
            systemTray.add(trayIcon);
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }catch (AWTException exception){
            exception.printStackTrace(System.err);
        }
    }

}
