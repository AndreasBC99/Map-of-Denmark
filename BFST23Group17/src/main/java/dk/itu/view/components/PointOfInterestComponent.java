package dk.itu.view.components;
import dk.itu.model.MapModel;
import dk.itu.view.MapView;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;



public class PointOfInterestComponent extends Button {

    private static float[] coordXY = new float[2];
    private boolean isSelected = false;
    private Stage addStage;

    private MapView mapView;

    public PointOfInterestComponent(String text, MapView _mapView) {
        super(text);
        this.mapView = _mapView;

        setPadding(new Insets(4));
        setStyle("-fx-background-color: white; -fx-background-radius: 8px;");
        setTranslateX(8);
        setTranslateY(8);
        setMinWidth(100);

        addStage = new Stage();

        setOnAction(e -> {
            isSelected = true;
            addElement(mapView);
        });

        setOnMouseEntered(e -> {
            setStyle("-fx-background-color: #ECECEC; -fx-background-radius: 8px;");
        });

        setOnMouseExited(e -> {
            setStyle("-fx-background-color: white; -fx-background-radius: 8px;");
        });

    }

    /*
     * Opens a new window where the user can add an element with a name and description
     */
    public void addElement(MapView mapView) {

        Label nameLabel = new Label("Name:");
        Label descLabel = new Label("Description:");
        TextField nameField = new TextField();
        TextField descField = new TextField();

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.addRow(0, nameLabel, nameField);
        gridPane.addRow(1, descLabel, descField);

        // Save button
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String name = nameField.getText();
            String desc = descField.getText();
            mapView.getMapModel().setPoiNameList(name);
            mapView.getMapModel().setPoIMap(name, coordXY[0], coordXY[1]);
            addStage.close();
        });


        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(gridPane, saveButton);


        Scene scene = new Scene(layout, 400, 200);
        addStage.setScene(scene);


    }
    public void add(){
        isSelected = true;
        addElement(mapView);
    }
    public void showStage() {
        addStage.show();
        isSelected = false;
    }

    public boolean getPoIIsSelected() {return isSelected;}

    public void setPoIX(float x, float y) {
        coordXY[0] = x;
        coordXY[1] = y;
    }


}