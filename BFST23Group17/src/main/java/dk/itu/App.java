package dk.itu;

import dk.itu.utils.ConfigurationFactory;
import dk.itu.view.StageView;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        ConfigurationFactory.loadConfigurations();
        launch(args);
    }
  
    @Override
    public void start(Stage ignored) {
        StageView.makeStartStage();
    }
}