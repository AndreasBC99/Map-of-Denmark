package dk.itu.view.components;

import dk.itu.view.MapView;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashMap;


public class ListOfPointOfInterestComponent extends Button {
    private MapView mapView;
    private Scene scene;

    private Stage stage;
    public ListOfPointOfInterestComponent (String text, MapView _mapview) {
        super(text);
        this.mapView = _mapview;
        setPadding(new Insets(4));
        setStyle("-fx-background-color: white; -fx-background-radius: 8px;");
        setTranslateX(8);
        setTranslateY(8);
        setMinWidth(100);
        setOnMouseEntered(e -> {
            setStyle("-fx-background-color: #ECECEC; -fx-background-radius: 8px;");
        });

        setOnMouseExited(e -> {
            setStyle("-fx-background-color: white; -fx-background-radius: 8px;");
        });

        setOnAction(e -> {
            // Create a ListView and add some dummy items to it
            ListView<String> listView = new ListView<>();


            listView.setItems(FXCollections.observableArrayList(mapView.getMapModel().getPoiMap().keySet()));
            listView.setCellFactory(param -> new ListCell<String>() {
                        private final HBox hbox = new HBox();
                        private final Text text = new Text();
                        private final Button fromButton = new Button("Set From");
                        private final Button toButton = new Button("Set To");

                        {
                            hbox.setAlignment(Pos.CENTER_LEFT);
                            hbox.setSpacing(10);
                            HBox.setHgrow(text, Priority.ALWAYS);
                            hbox.getChildren().addAll(text, fromButton, toButton);
                            setGraphic(hbox);
                        }
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        text.setText(item);
                        if (mapView.getMapModel().getPoiMap().containsKey(item)) {
                            setGraphic(hbox);
                            fromButton.setOnAction(e -> {
                                if (mapView.getMapModel().getPoiMap().containsKey(item)) {
                                    HashMap<Float, Float> coordMap = mapView.getMapModel().getPoiMap().get(item);
                                    for (float x : coordMap.keySet()) {
                                        float y = coordMap.get(x);
                                        mapView.getSearchComponent().setFrom((float) x, (float) y);
                                        getListView().setDisable(true);
                                        stage.close();
                                    }

                                }
                            });

                            toButton.setOnAction(e -> {
                                if (mapView.getMapModel().getPoiMap().containsKey(item)) {
                                    HashMap<Float, Float> coordMap = mapView.getMapModel().getPoiMap().get(item);
                                    for (float x : coordMap.keySet()) {
                                        float y = coordMap.get(x);
                                        mapView.getSearchComponent().setTo((float) x, (float) y);
                                        getListView().setDisable(true);
                                        stage.close();

                                    }

                                }
                            });
                        } else {
                            setGraphic(text);
                        }
                    }
                }
            });
            listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    if (mapView.getMapModel().getPoiMap().containsKey(newValue)) {
                        System.out.println("CHOSEN FROM FAVOURITE LIST: " + newValue);
                        System.out.print(newValue + " ");
                        HashMap<Float, Float> coordMap = mapView.getMapModel().getPoiMap().get(newValue);
                        for (float x : coordMap.keySet()) {
                            float y = coordMap.get(x);
                            System.out.println("X: " + x + ", Y: " + y);
                        }

                    }
                }
            });


            // Create a VBox to hold the ListView
            VBox vbox = new VBox(listView);

            // Create a new scene with the VBox and show it in a new window
            scene = new Scene(vbox, 400, 300);
            stage = new Stage();
            stage.setScene(scene);
            stage.show();
        });


    }
}
