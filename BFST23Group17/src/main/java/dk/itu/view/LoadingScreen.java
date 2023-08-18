package dk.itu.view;

import dk.itu.App;

import dk.itu.utils.ConfigurationFactory;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Objects;

public class LoadingScreen {

    public static void createLoadingScreen(Stage loadingStage) {

        // creates image URL for easier use with both macOS and windows, untested for linux+others
        String imageURL = ConfigurationFactory.getConfig().get("resources.assets.loadingScreenPicture").toString();
        // Creates backgroundImage
        Image ourLoadingImage = new Image(
                Objects.requireNonNull(App.class.getResourceAsStream("/assets/".concat(imageURL))),
                600, 350, true, true);
      
        // Create a border pane to hold the loading label
        StackPane container = new StackPane(new ImageView(ourLoadingImage));
        loadingStage.setTitle("Loading...");
        Scene loadingScene = new Scene(container);
        loadingStage.centerOnScreen();
        loadingStage.setScene(loadingScene);
    }
}