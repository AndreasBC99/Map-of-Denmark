package dk.itu.view.components;

import dk.itu.model.MapModel;
import dk.itu.utils.NnUtils;
import dk.itu.view.MapView;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.File;
import java.util.List;

public class RoadNameComponent extends VBox {
    private static MapView mapView;

    private Text roadName;

    public RoadNameComponent(MapView _mapView) {
        super();
        mapView = _mapView;

        roadName = new Text();
        roadName.setText("Nearest road");
        roadName.setFill(Color.RED);

        setMaxSize(180, 0);
        setSpacing(6);
        setStyle("-fx-background-color: white; -fx-background-radius: 8px 0px 0px 0px;");
        setPadding(new Insets(10));

        String textFontURL = System.getProperty("user.dir").concat("/src/main/resources/assets/fonts/mulish.ttf");
        File textFontFile = new File(textFontURL);
        Font textFont = Font.loadFont(textFontFile.toURI().toString(), 14);

        getChildren().add(roadName);
    }

    public void update(String name){
        roadName.setText(name);
    }
}