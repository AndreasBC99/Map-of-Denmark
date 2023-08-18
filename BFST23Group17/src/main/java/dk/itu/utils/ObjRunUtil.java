package dk.itu.utils;

import dk.itu.App;
import dk.itu.model.MapModel;
import dk.itu.view.LoadingScreen;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;

import static dk.itu.controller.MapController.createMapView;

public class ObjRunUtil {
    static MapModel mapModel;
    public static void loadObj(String customFilePath, Stage loadingStage, boolean isDefault) {
        Stage loadingCompleteStage = new Stage(StageStyle.UTILITY);
        loadingCompleteStage.setAlwaysOnTop(true);

        // starts loading obj file
        Runnable loadingObj = (() -> {
            ObjectInputStream in;
            long startTimeParse = System.currentTimeMillis();
            try {
                if (isDefault) {
                    InputStream fileIn = App.class.getResourceAsStream("/data/".concat(customFilePath));
                    ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(fileIn));
                    zipIn.getNextEntry();
                    in = new ObjectInputStream(new BufferedInputStream((zipIn)));
                }
                else {
                    in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(Path.of(customFilePath))));
                }
                mapModel = (MapModel) in.readObject();
                Platform.runLater(loadingCompleteStage::show);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e.getMessage() + " or built-in filepaths modified, terminating program");
                System.exit(0);
            }
            System.out.println("Finished loading of " + customFilePath + " in " + (System.currentTimeMillis() - startTimeParse) + "ms");
        });
        Thread loadingObjThread = new Thread(null, loadingObj, "objLoadThread", Integer.MAX_VALUE);

        // lets you run an obj file
        Button runButton = new Button("press to show map");
        runButton.setFont(new Font(16));
        runButton.setOnAction(event -> {
            if (loadingObjThread.isAlive()) {
                System.out.println("please wait until loading is finished");
            } else {
                loadingStage.close();
                loadingCompleteStage.close();
                createMapView(mapModel);
            }
        });

        Text loadingCompleteText = new Text("Loading is complete" + "\n");
        loadingCompleteText.setFont(new Font(26));
        VBox loadingCompleteVBox = new VBox(loadingCompleteText, runButton);
        loadingCompleteVBox.setAlignment(Pos.CENTER);
        loadingCompleteVBox.setSpacing(10);
        Scene loadingCompleteScene = new Scene(loadingCompleteVBox, 300, 200);
        loadingCompleteStage.setScene(loadingCompleteScene);

        // shows loadingscreen
        LoadingScreen.createLoadingScreen(loadingStage);
        loadingStage.show();
        loadingObjThread.start();
    }

    // saves as obj file
    public static void save(String objName, MapModel mapModel) {
        new Thread(null, () -> {
            try (var out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(objName.replace(".osm", ".obj"))))) {
                out.writeObject(mapModel);
                System.out.println("saved obj");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }, "objsavethread", Integer.MAX_VALUE).start();
    }
}
