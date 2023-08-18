package dk.itu.view.components;

import dk.itu.utils.ThemesFactory;
import dk.itu.view.MapView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.checkerframework.checker.units.qual.C;

import java.util.List;

public class DebugDetailsComponent extends VBox {

    private static MapView mapView;
    private final Button showDebug;
    private final Text textRPS;
    private ComboBox<Cities> citySelector;
    private final ComboBox<Integer> minLayer, maxLayer;
    private final CheckBox canDraw, showPhBox, showElementBoundBox;

    private static boolean experimentalFeaturesEnabled = false;
  
    public DebugDetailsComponent(MapView _mapView) {
        super();
        mapView = _mapView;
        setMaxSize(180, 0);
        setSpacing(6);
        setStyle("-fx-background-color: white; -fx-background-radius: 8px;");
        setTranslateX(-8);
        setTranslateY(8);
        setPadding(new Insets(4));

        // Shown Text
        textRPS = new Text("FPS: ");

        // See City Button
        Button seeCity = new Button("Load city");
        seeCity.setOnMouseClicked(e -> {
            Cities selectedCity = citySelector.getValue();
            mapView.resetTrans();
            mapView.getTrans().prependTranslation(selectedCity.getPrependTransTx(), selectedCity.getPrependTransTy());
            mapView.getTrans().prependScale(selectedCity.getPrependScaleFactor(), selectedCity.getPrependScaleFactor());
            mapView.redraw();
            mapView.redraw();
        });

        // City Selector
        citySelector = new ComboBox<>();
        citySelector.setStyle("-fx-background-color: -fx-shadow-highlight-color, -fx-outer-border, -fx-inner-border, white;" +
                "-fx-background-insets: 0 0 -1 0, 0, 1, 2;" +
                "-fx-background-radius: 3px, 3px, 2px, 1px;");
        citySelector.getItems().addAll(Cities.values());
        citySelector.setValue(Cities.ALL);

        // Container for City selection
        HBox seeCityCont = new HBox();
        seeCityCont.setSpacing(8);
        seeCityCont.getChildren().addAll(seeCity, citySelector);

        // Can draw checkbox
        canDraw = new CheckBox("Can draw");
        canDraw.setSelected(true);
        canDraw.setOnAction(e -> mapView.redraw());

        showPhBox = new CheckBox("Ph Tree Search box");
        showPhBox.setSelected(false);
        showPhBox.setOnAction(e -> mapView.redraw());

        showElementBoundBox = new CheckBox("Element bound box");
        showElementBoundBox.setSelected(false);
        showElementBoundBox.setOnAction(e -> mapView.redraw());

        HBox layers = new HBox();
        layers.setSpacing(8);
        minLayer = new ComboBox<>();
        maxLayer = new ComboBox<>();

        for (int i = mapView.getMapModel().getMinLayer() ; i<=mapView.getMapModel().getMaxLayer() ; i++) {
            minLayer.getItems().add(i);
            maxLayer.getItems().add(i);
        }
        minLayer.setValue(mapView.getMapModel().getMinLayer());
        maxLayer.setValue(mapView.getMapModel().getMaxLayer());

        layers.getChildren().addAll(minLayer, maxLayer);

        minLayer.setOnAction(e -> mapView.redraw());
        maxLayer.setOnAction(e -> mapView.redraw());

        HBox colorModeBox = new HBox();
        colorModeBox.setAlignment(Pos.CENTER_LEFT);
        colorModeBox.setSpacing(8);

        Text colorText = new Text("Select ColorMode:");

        ComboBox<Integer> colorMode = new ComboBox<>();

        // this converts integers to strings and vice versa, to better interact with colorMode as an integer
        StringConverter<Integer> converter = new StringConverter<>() {
            @Override
            public String toString(Integer value) {
                return switch (value) {
                    default -> "DEFAULT";
                    case 1 -> "HIGH CONTRAST, ONLY ROADS";
                    case 2 -> "REVERSED COLORS";
                    case 3 -> "SET RANDOM COLORS";
                    case 4 -> "CRAZY COLORS! (EPILEPSY WARNING)";
                };
            }

            @Override
            public Integer fromString(String string) {
                return switch (string) {
                    default -> 0;
                    case "HIGH CONTRAST, ONLY ROADS" -> 1;
                    case "REVERSED COLORS" -> 2;
                    case "SET RANDOM COLORS" -> 3;
                    case "CRAZY COLORS! (EPILEPSY WARNING)" -> 4;
                };
            }
        };
// Sets the stringconverter for the colorMode ComboBox
        colorMode.setConverter(converter);
// Add the integer values, therefore strings to the colorModeBox
        colorMode.getItems().addAll(List.of(0, 1, 2));
        colorMode.setValue(ThemesFactory.getColorMode());
        colorMode.setOnAction(e -> {
            ThemesFactory.setColorMode(colorMode.getValue());
            mapView.redraw();
        });

        // for enabling randomcolors and other experimental features
        CheckBox experimentalFeatures = new CheckBox("Enable experimental features");
        experimentalFeatures.setSelected(false);
            experimentalFeatures.setOnAction(e -> {
                if (experimentalFeatures.isSelected()) {
                    experimentalFeaturesEnabled = true;
                    colorMode.getItems().addAll(List.of(3, 4));
                }
                else {
                    experimentalFeaturesEnabled = false;
                    colorMode.getItems().removeAll(List.of(3, 4));
                    colorMode.setValue(0);
                }
            });
      
        colorModeBox.getChildren().addAll(colorText, colorMode);

        // Show Debug button
        showDebug = new Button("Debug mode");
        showDebug.setAlignment(Pos.CENTER);
        showDebug.setOnMouseClicked(e -> {
            if (getChildren().size() > 1) {
                // Is Shown
                getChildren().clear();
                getChildren().add(showDebug);
                showDebug.setText("Debug mode");
            } else {
                // Is hidden
                getChildren().clear();

                getChildren().addAll(showDebug, textRPS, seeCityCont, canDraw, showPhBox, showElementBoundBox, layers, colorModeBox, experimentalFeatures);

                showDebug.setText("Exit debug mode");
            }
        });

        getChildren().addAll(showDebug);

    }

    public void setFpsText(String newText) {
        textRPS.setText(newText);
    }

    public boolean canDraw() {
        return canDraw.isSelected();
    }

    public boolean canShowKdBox() {
        return showPhBox.isSelected();
    }

    public boolean canShowElementBoundBox() {
        return showElementBoundBox.isSelected();
    }

    public int getMinLayer() {
        return minLayer.getValue();
    }

    public int getMaxLayer() {
        return maxLayer.getValue();
    }

    // gets the value of experimental
    public static boolean getExperimentalBool() {
        return experimentalFeaturesEnabled;
    }

    public enum Cities {
        ALL(mapView.getMapModel().getMinX(), mapView.getMapModel().getMinY(), mapView.getMapModel().getMaxY(), "ALL"),
        CPH(12.5709, 55.6640, 55.6800, "CPH"),
        AAR(10.1900, 56.1363, 56.1668, "AAR"),
        BORN(14.6613, 54.9792, 55.3127, "BORN");

        private final double minX, minY, maxY;
        private final String name;

        Cities(double _minX, double _minY, double _maxY, String _name) {
            minX = _minX;
            minY = _minY;
            maxY = _maxY;
            name = _name;
        }

        public double getPrependTransTx() {
            return -0.56*minX;
        }
        public double getPrependTransTy() {
            return maxY;
        }
        public double getPrependScaleFactor() {
            return mapView.getCanvas().getHeight() / (maxY - minY);
        }

        public String getName() {
            return name;
        }
    }
}
