package dk.itu.view;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ObjSavePrompt {
    public static void saveAsObjChooser(String customFilePath, Stage loadingStage) {
        Stage objStage = new Stage(StageStyle.UTILITY);
        Text title = new Text("Save as OBJ file?");
        title.setFont(new Font(32));
        Text underTitle = new Text("(saves .osm file as .obj after parsing)");
        underTitle.setFont(Font.font("Default", FontWeight.BOLD, 13));
        Text warningText = new Text("PLEASE DO NOT SAVE FILES OVER 4GB, AS THIS MIGHT CAUSE MEMORY ISSUES");
        warningText.setFont(Font.font("Default", FontWeight.BOLD, 13));
        warningText.setFill(Color.RED);
        Button yesButton = new Button("YES");
        Button noButton = new Button("NO");
        yesButton.setPrefSize(50, 30);
        noButton.setPrefSize(50, 30);

        yesButton.setOnAction(event -> Platform.runLater(() -> {
            objStage.close();
            loadingStage.show();
            StageView.executeMap(customFilePath, true);
        }));

        noButton.setOnAction(event -> Platform.runLater(() -> {
            objStage.close();
            loadingStage.show();
            StageView.executeMap(customFilePath, false);
        }));

        VBox vbox = new VBox(title, underTitle, warningText);
        HBox hbox = new HBox(yesButton, noButton);
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(10);
        vbox.getChildren().add(hbox);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        Scene objScene = new Scene(vbox, 550, 300);
        objStage.setScene(objScene);
        objStage.show();

    }

}
