package dk.itu.view.components;

import dk.itu.view.MapView;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class RightClickComponent extends ContextMenu {

    private double x, y;
    private MenuItem coords;
    public RightClickComponent(MapView mapView) {
        MenuItem setFrom = new MenuItem("Set From");
        MenuItem setTo = new MenuItem("Set To");
        MenuItem createPOI = new MenuItem("Save point");
        coords = new MenuItem("");
        this.getItems().addAll(setFrom, setTo, createPOI, coords);
        setFrom.setOnAction(e -> {
            mapView.getSearchComponent().setFrom((float) x, (float) y);

        });
        setTo.setOnAction((event) -> {
            mapView.getSearchComponent().setTo((float) x, (float) y);
        });
        createPOI.setOnAction((event) -> {
            mapView.getPointOfInterestComponent().add();
            mapView.getPointOfInterestComponent().setPoIX((float) x, (float) y);
            mapView.getPointOfInterestComponent().showStage();
        });

    }
    public void setXY(double _x, double _y){
        this.x = _x;
        this.y = _y;
        coords.setText(x + ", " + y);
    }
}
