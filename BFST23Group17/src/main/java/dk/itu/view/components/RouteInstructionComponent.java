package dk.itu.view.components;

import dk.itu.model.MapModel;
import dk.itu.view.MapView;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.File;
import java.util.List;

public class RouteInstructionComponent extends VBox {

    private static MapView mapView;
    List<String> strings;
    ListView<String> listView;

    private Button copyButton;

    private Text distText, timeText;

    public RouteInstructionComponent(MapView _mapView) {
        super();

        mapView = _mapView;
        strings = mapView.getDirectionDescription();
        HBox hbox = new HBox();
        copyButton = new Button("Copy");
        copyButton.setPrefWidth(60);
        hbox.getChildren().add(copyButton);
        copyButton.setOnMouseClicked(e -> {copyInstructions();});

        setMaxSize(180, 0);
        setSpacing(6);
        setStyle("-fx-background-color: white; -fx-background-radius: 8px;");
        setTranslateX(8);
        setTranslateY(8);
        setPadding(new Insets(4));

        // Transportation type button
        String iconFontURL = System.getProperty("user.dir").concat("/src/main/resources/assets/fonts/fontawesome.ttf");
        String textFontURL = System.getProperty("user.dir").concat("/src/main/resources/assets/fonts/mulish.ttf");
        File iconFontFile = new File(iconFontURL);
        File textFontFile = new File(textFontURL);
        Font iconFont = Font.loadFont(iconFontFile.toURI().toString(), 14);
        Font textFont = Font.loadFont(textFontFile.toURI().toString(), 14);

        HBox routeInfo = new HBox();

        Text distIcon = new Text();
        distIcon.setFont(iconFont);
        distIcon.setText("\uf4d7");

        distText = new Text();
        distText.setFont(textFont);
        updateDistanceField();

        Text timeIcon = new Text();
        timeIcon.setFont(iconFont);
        timeIcon.setText("\uf2f2");

        timeText = new Text();
        timeText.setFont(textFont);
        updateTimeField();

        routeInfo.getChildren().addAll(distIcon, distText, timeIcon, timeText);

        listView = new ListView<>();
        if(strings != null){
            listView.getItems().addAll(strings);
            listView.setMinHeight(150);
            listView.setPrefHeight(30*strings.size());
            listView.setMaxHeight(200);
        }
      
      getChildren().addAll(routeInfo, listView, hbox);
    }

    public void copyInstructions(){
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        StringBuilder sb = new StringBuilder();
        for(String s : strings){
            sb.append(s);
            sb.append(System.lineSeparator());
            content.putString(sb.toString());
            clipboard.setContent(content);
        }
    }

    //Update the component
    public void update(){
        updateDistanceField();
        updateTimeField();
        listView.getItems().clear();
        strings = mapView.getDirectionDescription();
        listView.getItems().addAll(strings);
    }

    private void updateDistanceField(){
        double totalDistance = mapView.getTotalDistance();
        distText.setText(totalDistance < 1 ? " " + (int) (totalDistance * 1000) + " m " : " " + Math.round(totalDistance * 100.0) / 100.0 + " km ");
    }

    private void updateTimeField(){
        double secondsToTraverse = mapView.getSecondsToTraverse();
        timeText.setText(formatTimeField(secondsToTraverse));
    }

    //Convert an amount of seconds to hh:mm:ss format
    private String formatTimeField(double _seconds){
        int hours = 0, minutes = 0, seconds = 0;
        while (_seconds > 0){
            if(_seconds >= 3600){
                hours++;
                _seconds -= 3600;
                continue;
            }
            if(_seconds >= 60){
                minutes++;
                _seconds -= 60;
                continue;
            }
            seconds += _seconds;
            _seconds -= _seconds;
        }
        return " " + ((hours < 10) ? "0" + hours : hours) + ":" + ((minutes < 10) ? "0" + minutes : minutes) + ":" + ((seconds<10) ? "0" + seconds : seconds);
    }
}
