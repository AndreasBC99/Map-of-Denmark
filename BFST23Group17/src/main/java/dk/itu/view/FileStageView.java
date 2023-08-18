package dk.itu.view;

import dk.itu.utils.ConfigurationFactory;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FileStageView {
    public static void showFileStageView() {
        Stage fileStage = new Stage(StageStyle.UTILITY);
        Text title = new Text("Please choose File Format:");
        title.setFont(new Font(32));
        Text underTitle = new Text("(for custom file, osm, osm.zip and previously saved obj files are supported)" + "\n");
        underTitle.setFont(Font.font("Default", FontWeight.BOLD, 13));

        Button denmarkButton = new Button("load map of Denmark");
        denmarkButton.setPrefSize(140, 20);
        Button bornholmButton = new Button("load map of Bornholm");
        bornholmButton.setPrefSize(140, 20);
        Button customButton = new Button("load custom file");
        customButton.setPrefSize(140, 20);
        // creates buttons for choosing between default and custom
        denmarkButton.setOnAction(event -> {
            fileStage.close();
            StageView.chooseMap(false, (String) ConfigurationFactory.getConfig().get("resources.data.denmarkZippedObj"));
        });

        bornholmButton.setOnAction(event -> {
            fileStage.close();
            StageView.chooseMap(false, (String) ConfigurationFactory.getConfig().get("resources.data.bornholmZippedObj"));
        });

        customButton.setOnAction(event -> {
            fileStage.close();
            String customFilePath = ChooseFiles.chooseFilePath();
           if (customFilePath == null) {
               fileStage.show();
           } else {
               Platform.runLater(() -> StageView.chooseMap( true, customFilePath));
           }


        });

        VBox titleAndButtons = new VBox(title, underTitle, denmarkButton, bornholmButton, customButton);
        titleAndButtons.setAlignment(Pos.CENTER);
        titleAndButtons.setSpacing(15);
        Scene fileScene = new Scene(titleAndButtons, 550, 300);
        fileStage.setScene(fileScene);
        fileStage.show();


    }
}
