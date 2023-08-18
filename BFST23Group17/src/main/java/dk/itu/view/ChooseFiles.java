package dk.itu.view;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class ChooseFiles {
    public static String chooseFilePath() {
        FileChooser fileChooser = new FileChooser();
        Stage fileChooserStage = new Stage();
        fileChooser.setTitle("Select a file");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("OSM Files", "*.osm"),
                new FileChooser.ExtensionFilter("OBJ Files", "*.obj"),
                new FileChooser.ExtensionFilter("ZIP Files", "*.zip"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));



        // Show the file choose dialog
        File selectedFile = fileChooser.showOpenDialog(fileChooserStage);
        if (selectedFile == null) return null;

        // get file if supported

        if (selectedFile.getName().endsWith(".osm")) return selectedFile.getAbsolutePath();
        else if (selectedFile.getName().endsWith(".zip")) return selectedFile.getAbsolutePath();
        else if (selectedFile.getName().endsWith(".obj")) return selectedFile.getAbsolutePath();
        else {
            System.out.println("Unsupported Filetype");
            return chooseFilePath();
        }

    }
}
