package dk.itu.view.components;

import dk.itu.view.MapView;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;


public class ScaleRatioComponent extends StackPane {
    private Text scaleText;
    private MapView mapView;

    private double zoomDouble = 1000;
    private double zoomSize;

    private double zoomFactor;
    private double zoomScale = 1;
    private double maxZoom;
    private double minZoom;

    public ScaleRatioComponent(MapView mapView, double _zoomFactor){
        this.mapView = mapView;
        scaleText = new Text("1 km");
        this.getChildren().add(scaleText);
        this.zoomFactor = _zoomFactor;
    }
    public void setSize(double trueZoom, VBox vbox, double maxZoom, double minZoom){
        zoomSize = trueZoom;
        this.maxZoom = maxZoom;
        this.minZoom = minZoom;
        if(zoomSize > mapView.getCanvas().getWidth()/4){
            zoomScale = (mapView.getCanvas().getWidth()/6)/zoomSize;
        }else {
            zoomScale = Math.rint(250 / zoomSize);
        }
        if(zoomScale != 0) {
            zoomSize = zoomSize * zoomScale;
            zoomDouble = zoomDouble * zoomScale;
        }
        vbox.setMaxWidth(zoomSize);
        vbox.setPrefWidth(zoomSize);
        vbox.setMinWidth(zoomSize);
        vbox.setMaxHeight(30);
        scaleText.setText((int) zoomDouble + " m");
        vbox.setBorder(new Border(new BorderStroke(Color.CORAL, Color.CORAL, Color.CORAL, Color.CORAL,
                BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY, new BorderWidths(3), Insets.EMPTY)));


    }

    public void setScale(double zoomLevel, boolean zoomIn, VBox vbox){

        if(zoomIn && zoomLevel <maxZoom) {

            zoomDouble = zoomDouble*(1/zoomFactor);
        }        else if(!zoomIn && zoomLevel > minZoom) {
            zoomDouble = zoomDouble*zoomFactor;
        }
        scaleText.setText((int) zoomDouble + " m");
    }
}
