package me.eduard.musicplayer.Components.Player;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.EqualizerBand;
import javafx.stage.Stage;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.BasicKeyValuePair;
import me.eduard.musicplayer.Library.CustomComponents.WindowTitleBar;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;
import me.eduard.musicplayer.Utils.Settings;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;
import me.eduard.musicplayer.Utils.Utilities;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.*;


public class Equalizers implements Initializable {

    public enum TurnType{
        MAX(12.0), MIN(-24.0), FLAT(0);
        private final double gain;
        TurnType(double gain){
            this.gain = gain;
        }
        public double getGain(){
            return this.gain;
        }
    }

    @FXML private AnchorPane corePane;
    @FXML public Slider slider32, slider64, slider125, slider250, slider500, slider1000, slider2000, slider4000, slider8000, slider16000, slider20000;
    @FXML public Spinner<Double> field32, field64, field125, field250, field500, field1000, field2000, field4000, field8000, field16000, field20000;
    @FXML public Hyperlink reset32, reset64, reset125, reset250, reset500, reset1000, reset2000, reset4000, reset8000, reset16000, reset20000;
    @FXML public Button resetToDefault, turnAllMax, turnAllMin, turnAllFlat;

    private static final double MIN_GAIN = EqualizerBand.MIN_GAIN;
    private static final double MAX_GAIN = EqualizerBand.MAX_GAIN;

    private static final Logger LOGGER = Logger.getLogger("Audio-Equalizer");

    static {
        LoggerHandler handler = new LoggerHandler();
        handler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
    }

    private static boolean LOADED = false;

    public static EqualizerBand[] EQUALIZERS = {
            new EqualizerBand(32, 19, 0),
            new EqualizerBand(64, 39, 0),
            new EqualizerBand(125, 78, 0),
            new EqualizerBand(250, 156, 0),
            new EqualizerBand(500, 312, 0),
            new EqualizerBand(1000, 625, 0),
            new EqualizerBand(2000, 1250, 0),
            new EqualizerBand(4000, 2500, 0),
            new EqualizerBand(8000, 5000, 0),
            new EqualizerBand(16000, 10000, 0),
            new EqualizerBand(20000, 12500, 0)
    };

    /**
     * Old values: 0, 1.3, 1, 1.6, 0.75 * 4, 2.8, 4
     */
    private static final double[] DEFAULT_GAINS = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    }; //Remake

    public static final String[] SETTING_VALUES = {
            "freq32: "+ DEFAULT_GAINS[0],
            "freq64: "+DEFAULT_GAINS[1],
            "freq125: "+DEFAULT_GAINS[2],
            "freq250: "+DEFAULT_GAINS[3],
            "freq500: "+DEFAULT_GAINS[4],
            "freq1000: "+DEFAULT_GAINS[5],
            "freq2000: "+DEFAULT_GAINS[6],
            "freq4000: "+DEFAULT_GAINS[7],
            "freq8000: "+DEFAULT_GAINS[8],
            "freq16000: "+DEFAULT_GAINS[9],
            "freq20000: "+DEFAULT_GAINS[10]
    };

    private void initializeSpinners(){
        this.setupSpinnerValueFactory(this.field32, this.slider32);
        this.setupSpinnerValueFactory(this.field64, this.slider64);
        this.setupSpinnerValueFactory(this.field125, this.slider125);
        this.setupSpinnerValueFactory(this.field250, this.slider250);
        this.setupSpinnerValueFactory(this.field500, this.slider500);
        this.setupSpinnerValueFactory(this.field1000, this.slider1000);
        this.setupSpinnerValueFactory(this.field2000, this.slider2000);
        this.setupSpinnerValueFactory(this.field4000, this.slider4000);
        this.setupSpinnerValueFactory(this.field8000, this.slider8000);
        this.setupSpinnerValueFactory(this.field16000, this.slider16000);
        this.setupSpinnerValueFactory(this.field20000, this.slider20000);

        //Remove each increment/decrement arrow
        this.field32.getStyleClass().clear();
        this.field64.getStyleClass().clear();
        this.field125.getStyleClass().clear();
        this.field250.getStyleClass().clear();
        this.field500.getStyleClass().clear();
        this.field1000.getStyleClass().clear();
        this.field2000.getStyleClass().clear();
        this.field4000.getStyleClass().clear();
        this.field8000.getStyleClass().clear();
        this.field16000.getStyleClass().clear();
        this.field20000.getStyleClass().clear();
    }

    private void setupSpinnerValueFactory(Spinner<Double> spinner, Slider slider){
        spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                -24, 12, Utilities.getValueOfFixedLimits(MIN_GAIN, MAX_GAIN, slider.getValue()), 0.01)
        );
    }

    private void addNecessaryListenerToSpinner(Spinner<Double> field, Slider belongingSlider, int playerBandIndex){
        try {
            field.setOnKeyPressed(event -> {
                if(field.valueProperty().get() > MAX_GAIN){
                    field.getValueFactory().setValue(MAX_GAIN);
                }else if(field.valueProperty().get() < MIN_GAIN){
                    field.getValueFactory().setValue(MAX_GAIN);
                }
                if(event.getCode() == KeyCode.ENTER){
                    belongingSlider.setValue(
                            Utilities.getPercentageOfFixedLimits(MIN_GAIN, MAX_GAIN, field.getValueFactory().getValue())
                    );
                    double val = field.getValueFactory().getValue();
                    equalizers.saveSetting(getEqualizerSettingByIndex(playerBandIndex), val);
                }
            });
        }catch (NumberFormatException exception){
            field.getValueFactory().setValue(0d);
        }
    }

    private void addNecessaryListenerToSlider(Slider slider, Spinner<Double> belongingSpinner, int playerBandIndex){
        Player player = Player.instance;
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double val = Utilities.getValueOfFixedLimits(MIN_GAIN, MAX_GAIN, slider.getValue());
            belongingSpinner.getValueFactory().setValue(val);
            player.audioPlayer.getAudioEqualizer().getBands().get(playerBandIndex).setGain(val);
            equalizers.saveSetting(getEqualizerSettingByIndex(playerBandIndex), val);
        });
    }

    private static String getEqualizerSettingByIndex(int index){
        return switch (index) {
            case 0 -> "freq32";
            case 1 -> "freq64";
            case 2 -> "freq125";
            case 3 -> "freq250";
            case 4 -> "freq500";
            case 5 -> "freq1000";
            case 6 -> "freq2000";
            case 7 -> "freq4000";
            case 8 -> "freq8000";
            case 9 -> "freq16000";
            case 10 -> "freq20000";
            default -> throw new IllegalArgumentException("Index range should be between 0 and 10.");
        };
    }

    private BasicKeyValuePair<Spinner<Double>, Slider> getSpinnerSliderPairBasedOnIndex(int index){
        return switch (index){
            case 0 -> BasicKeyValuePair.of(this.field32, this.slider32);
            case 1 -> BasicKeyValuePair.of(this.field64, this.slider64);
            case 2 -> BasicKeyValuePair.of(this.field125, this.slider125);
            case 3 -> BasicKeyValuePair.of(this.field250, this.slider250);
            case 4 -> BasicKeyValuePair.of(this.field500, this.slider500);
            case 5 -> BasicKeyValuePair.of(this.field1000, this.slider1000);
            case 6 -> BasicKeyValuePair.of(this.field2000, this.slider2000);
            case 7 -> BasicKeyValuePair.of(this.field4000, this.slider4000);
            case 8 -> BasicKeyValuePair.of(this.field8000, this.slider8000);
            case 9 -> BasicKeyValuePair.of(this.field16000, this.slider16000);
            case 10 -> BasicKeyValuePair.of(this.field20000, this.slider20000);
            default -> throw new IllegalArgumentException("Index range should be between 0 and 10.");
        };
    }

    private EqualizerBand getEqualizerByIndex(int index){
        return EQUALIZERS[index];
    }

    public static final Settings equalizers = Settings.of("Equalizers.yml");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.initializeBasicComponents();
        this.initializeValues();
    }
    private void initializeBasicComponents(){
        if(!equalizers.isSettingsFileExists()){
            equalizers.setupSettingsFile(true, SETTING_VALUES);
        }
        this.initializeSpinners();
        field32.getValueFactory().setValue(getEqualizerByIndex(0).getGain());
        field64.getValueFactory().setValue(getEqualizerByIndex(1).getGain());
        field125.getValueFactory().setValue(getEqualizerByIndex(2).getGain());
        field250.getValueFactory().setValue(getEqualizerByIndex(3).getGain());
        field500.getValueFactory().setValue(getEqualizerByIndex(4).getGain());
        field1000.getValueFactory().setValue(getEqualizerByIndex(5).getGain());
        field2000.getValueFactory().setValue(getEqualizerByIndex(6).getGain());
        field4000.getValueFactory().setValue(getEqualizerByIndex(7).getGain());
        field8000.getValueFactory().setValue(getEqualizerByIndex(8).getGain());
        field16000.getValueFactory().setValue(getEqualizerByIndex(9).getGain());
        field20000.getValueFactory().setValue(getEqualizerByIndex(10).getGain());

        this.turnAllMax.setOnAction(event -> this.turnGainsToExtremity(TurnType.MAX, this));
        this.turnAllMin.setOnAction(event -> this.turnGainsToExtremity(TurnType.MIN, this));
        this.turnAllFlat.setOnAction(event -> this.turnGainsToExtremity(TurnType.FLAT, this));

    }
    private void initializeValues(){
        //Sliders
        this.slider32.setValue(Utilities.getPercentageOfFixedLimits(MIN_GAIN, MAX_GAIN, getEqualizerByIndex(0).getGain()));
        this.slider64.setValue(Utilities.getPercentageOfFixedLimits(MIN_GAIN, MAX_GAIN, getEqualizerByIndex(1).getGain()));
        this.slider125.setValue(Utilities.getPercentageOfFixedLimits(MIN_GAIN, MAX_GAIN, getEqualizerByIndex(2).getGain()));
        this.slider250.setValue(Utilities.getPercentageOfFixedLimits(MIN_GAIN, MAX_GAIN, getEqualizerByIndex(3).getGain()));
        this.slider500.setValue(Utilities.getPercentageOfFixedLimits(MIN_GAIN, MAX_GAIN, getEqualizerByIndex(4).getGain()));
        this.slider1000.setValue(Utilities.getPercentageOfFixedLimits(MIN_GAIN, MAX_GAIN, getEqualizerByIndex(5).getGain()));
        this.slider2000.setValue(Utilities.getPercentageOfFixedLimits(MIN_GAIN, MAX_GAIN, getEqualizerByIndex(6).getGain()));
        this.slider4000.setValue(Utilities.getPercentageOfFixedLimits(MIN_GAIN, MAX_GAIN, getEqualizerByIndex(7).getGain()));
        this.slider8000.setValue(Utilities.getPercentageOfFixedLimits(MIN_GAIN, MAX_GAIN, getEqualizerByIndex(8).getGain()));
        this.slider16000.setValue(Utilities.getPercentageOfFixedLimits(MIN_GAIN, MAX_GAIN, getEqualizerByIndex(9).getGain()));
        this.slider20000.setValue(Utilities.getPercentageOfFixedLimits(MIN_GAIN, MAX_GAIN, getEqualizerByIndex(10).getGain()));


        //Spinners
        this.addNecessaryListenerToSpinner(this.field32, this.slider32, 0);
        this.addNecessaryListenerToSpinner(this.field64, this.slider64, 1);
        this.addNecessaryListenerToSpinner(this.field125, this.slider125, 2);
        this.addNecessaryListenerToSpinner(this.field250, this.slider250, 3);
        this.addNecessaryListenerToSpinner(this.field500, this.slider500, 4);
        this.addNecessaryListenerToSpinner(this.field1000, this.slider1000, 5);
        this.addNecessaryListenerToSpinner(this.field2000, this.slider2000, 6);
        this.addNecessaryListenerToSpinner(this.field4000, this.slider4000, 7);
        this.addNecessaryListenerToSpinner(this.field8000, this.slider8000, 8);
        this.addNecessaryListenerToSpinner(this.field16000, this.slider16000, 9);
        this.addNecessaryListenerToSpinner(this.field20000, this.slider20000, 10);

        //Sliders
        this.addNecessaryListenerToSlider(this.slider32, this.field32, 0);
        this.addNecessaryListenerToSlider(this.slider64, this.field64, 1);
        this.addNecessaryListenerToSlider(this.slider125, this.field125, 2);
        this.addNecessaryListenerToSlider(this.slider250, this.field250, 3);
        this.addNecessaryListenerToSlider(this.slider500, this.field500, 4);
        this.addNecessaryListenerToSlider(this.slider1000, this.field1000, 5);
        this.addNecessaryListenerToSlider(this.slider2000, this.field2000, 6);
        this.addNecessaryListenerToSlider(this.slider4000, this.field4000, 7);
        this.addNecessaryListenerToSlider(this.slider8000, this.field8000, 8);
        this.addNecessaryListenerToSlider(this.slider16000, this.field16000, 9);
        this.addNecessaryListenerToSlider(this.slider20000, this.field20000, 10);
    }
    private void resetValuesToDefault(Equalizers instance){
        Player player = Player.instance;
        for(int i = 0; i < EQUALIZERS.length; i++){
            equalizers.saveSetting(getEqualizerSettingByIndex(i), DEFAULT_GAINS[i]);
            player.audioPlayer.getAudioEqualizer().getBands().get(i).setGain(DEFAULT_GAINS[i]);
            instance.getSpinnerSliderPairBasedOnIndex(i).getValue().setValue(
                    Utilities.getPercentageOfFixedLimits(MIN_GAIN, MAX_GAIN, DEFAULT_GAINS[i])
            );
        }
    }

    public void turnGainsToExtremity(TurnType turnType, Equalizers instance){
        Player player = Player.instance;
        for(int i = 0; i < DEFAULT_GAINS.length; i++){
            equalizers.saveSetting(getEqualizerSettingByIndex(i), turnType.getGain());
            instance.getSpinnerSliderPairBasedOnIndex(i).getValue().setValue(Utilities.getPercentageOfFixedLimits(MIN_GAIN, MAX_GAIN, turnType.getGain()));
            player.audioPlayer.getAudioEqualizer().getBands().get(i).setGain(turnType.getGain());
        }
    }

    public void setEventForSpecificResetButtons(Equalizers instance){
        instance.reset32.setOnAction(event -> instance.resetSpecificBand(0, instance));
        instance.reset64.setOnAction(event -> instance.resetSpecificBand(1, instance));
        instance.reset125.setOnAction(event -> instance.resetSpecificBand(2, instance));
        instance.reset250.setOnAction(event -> instance.resetSpecificBand(3, instance));
        instance.reset500.setOnAction(event -> instance.resetSpecificBand(4, instance));
        instance.reset1000.setOnAction(event -> instance.resetSpecificBand(5, instance));
        instance.reset2000.setOnAction(event -> instance.resetSpecificBand(6, instance));
        instance.reset4000.setOnAction(event -> instance.resetSpecificBand(7, instance));
        instance.reset8000.setOnAction(event -> instance.resetSpecificBand(8, instance));
        instance.reset16000.setOnAction(event -> instance.resetSpecificBand(9, instance));
        instance.reset20000.setOnAction(event -> instance.resetSpecificBand(10, instance));
    }

    public void resetSpecificBand(int index, Equalizers instance){
        Player player = Player.instance;
        equalizers.saveSetting(getEqualizerSettingByIndex(index), DEFAULT_GAINS[index]);
        player.audioPlayer.getAudioEqualizer().getBands().get(index).setGain(DEFAULT_GAINS[index]);
        instance.getSpinnerSliderPairBasedOnIndex(index).getValue().setValue(
                Utilities.getPercentageOfFixedLimits(MIN_GAIN, MAX_GAIN, DEFAULT_GAINS[index])
        );
    }

    public static void launchWindow(){
        new Equalizers().launch();
    }

    private void launch(){
        try {
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("Equalizers")
                    .withStageBuilder(
                            StageBuilder.newBuilder()
                                    .styleSheet("ApplicationWindow")
                                    .removeUpperBar()
                                    .title("Audio Equalizer")
                                    .resizable(false)
                                    .icon("icons/icon.png")
                    ).addExitListenerWithEscape().bindAllStagesCloseKeyCombination().finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();
            WindowTitleBar titleBar = new WindowTitleBar(stage).useDefaultPresets();

            Equalizers equalizers = fxmlStageBuilder.getFxmlLoader().getController();
            equalizers.setEventForSpecificResetButtons(equalizers);

            equalizers.resetToDefault.setOnAction(event -> equalizers.resetValuesToDefault(equalizers));
            titleBar.setTitleString("Equalizer");
            titleBar.linkToStage();
            titleBar.applyAfterLinkPresets();
            MainApp.openStage(stage, Player.ANIMATIONS, true);
        }catch (IllegalStateException exception){
            ErrorHandler.launchWindow(exception);
        }
    }

    public static EqualizerBand[] getEqualizers() {
        if (!LOADED) {
            //Q Factor: 1.2, default: 1.6
            //Old: 19, 39, 78, 156, 312, 625, 1250, 2500, 5000, 10000
            EQUALIZERS = new EqualizerBand[]{
                    new EqualizerBand(32, 32, Double.parseDouble(equalizers.getSettingValue("freq32", false))),
                    new EqualizerBand(64, 64, Double.parseDouble(equalizers.getSettingValue("freq64", false))),
                    new EqualizerBand(125, 125, Double.parseDouble(equalizers.getSettingValue("freq125", false))),
                    new EqualizerBand(250, 250, Double.parseDouble(equalizers.getSettingValue("freq250", false))),
                    new EqualizerBand(500, 500, Double.parseDouble(equalizers.getSettingValue("freq500", false))),
                    new EqualizerBand(1000, 1000, Double.parseDouble(equalizers.getSettingValue("freq1000", false))),
                    new EqualizerBand(2000, 2000, Double.parseDouble(equalizers.getSettingValue("freq2000", false))),
                    new EqualizerBand(4000, 4000, Double.parseDouble(equalizers.getSettingValue("freq4000", false))),
                    new EqualizerBand(8000, 8000, Double.parseDouble(equalizers.getSettingValue("freq8000", false))),
                    new EqualizerBand(16000, 16000, Double.parseDouble(equalizers.getSettingValue("freq16000", false))),
                    new EqualizerBand(20000, 20000, Double.parseDouble(equalizers.getSettingValue("freq20000", false)))
            };
            LOADED = true;
            LOGGER.info("Loaded "+EQUALIZERS.length+" bands gain from 'Equalizers.yaml'.");
        }

        return EQUALIZERS;
    }
}
