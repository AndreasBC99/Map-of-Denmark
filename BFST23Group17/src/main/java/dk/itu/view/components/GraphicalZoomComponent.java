package dk.itu.view.components;

import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class GraphicalZoomComponent extends VBox {
    private final TextArea zoomPercent;

    public GraphicalZoomComponent(){
        zoomPercent = new TextArea();
        zoomPercent.setEditable(false);
        zoomPercent.setText(100 + "%");
        zoomPercent.setPickOnBounds(false);
        zoomPercent.setMouseTransparent(true);
        this.setPickOnBounds(false);
        this.setMaxWidth(100);
        this.setMaxHeight(30);
        this.getChildren().add(zoomPercent);
        this.setStyle("-fx-background-color: transparent");

    }
    public void updateZoom(double _zoomLevel, double maxZoom, double minZoom){
        if(_zoomLevel <= maxZoom && _zoomLevel >= minZoom) {
            double newZoomValue = ((_zoomLevel - minZoom) / (maxZoom - minZoom)) * 100;
            zoomPercent.setText((int) newZoomValue * 2 + "%");
        }
    }
}
