package dk.itu.view.components;

import dk.itu.controller.MapController;
import dk.itu.view.MapView;
import javafx.geometry.Insets;
import javafx.scene.control.Button;

public class ZoomButtonComponent extends Button {
    public ZoomButtonComponent(ZoomButtonType zoomButtonType, MapView mapView) {
        super();
        setPadding(new Insets(6));
        setText(zoomButtonType.getTextIcon());
        setStyle("-fx-background-color: white; -fx-text-fill: black;");
        setMinSize(25, 25);
        if(zoomButtonType == ZoomButtonType.PLUS){
            this.setOnMouseClicked(MapController::handleZoomClickPos);
        } else{
            this.setOnMouseClicked(MapController::handleZoomClickNeg);
        }
    }

    public enum ZoomButtonType {
        PLUS("+"),
        MINUS("-");

        private final String textIcon;

        ZoomButtonType(String icon) {
            textIcon = icon;
        }

        public String getTextIcon() {
            return textIcon;
        }
    }
}