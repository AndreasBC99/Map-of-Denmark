package dk.itu.view.components;

import dk.itu.view.MapView;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.File;

public class RouteTypeComponent extends VBox {
    private static MapView mapView;

    private Button foot, bike, vehicle;

    public RouteTypeComponent(MapView _mapView) {
        super();
        mapView = _mapView;
        setMaxSize(180, 0);
        setSpacing(6);
        setStyle("-fx-background-color: white; -fx-background-radius: 8px;");
        setTranslateX(8);
        setTranslateY(8);
        setPadding(new Insets(4));

        // Transportation type button
        String iconFontURL = System.getProperty("user.dir").concat("/src/main/resources/assets/fonts/fontawesome.ttf");
        File iconFontFile = new File(iconFontURL);
        Font iconFont = Font.loadFont(iconFontFile.toURI().toString(), 14);

        VBox content = new VBox();
        HBox traversalOptions = new HBox();
        foot = new Button("\uf554");
        foot.setFont(iconFont);
        foot.setOnMouseClicked(e -> {
            //Load Dijkstra on foot
            mapView.loadNewDijkstra(options.FOOT);
        });

        bike = new Button("\uf206");
        bike.setFont(iconFont);
        bike.setOnMouseClicked(e -> {
            //Load Dijkstra on bike
            mapView.loadNewDijkstra(options.BIKE);
        });

        vehicle = new Button("\uf1b9");
        vehicle.setFont(iconFont);
        vehicle.setOnMouseClicked(e -> {
            //Load Dijkstra on vehicle
            mapView.loadNewDijkstra(options.VEHICLE);
        });

        traversalOptions.setSpacing(6);
        traversalOptions.getChildren().addAll(foot, bike, vehicle);
        getChildren().addAll(traversalOptions);
    }

    public enum options{
        FOOT,
        BIKE,
        VEHICLE
    }

    public void disableButtons(){
        foot.setDisable(true);
        bike.setDisable(true);
        vehicle.setDisable(true);
    }

    public void enableButtons(){
        foot.setDisable(false);
        bike.setDisable(false);
        vehicle.setDisable(false);
    }
}
