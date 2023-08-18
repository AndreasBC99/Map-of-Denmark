package dk.itu.view;

import dk.itu.controller.MapController;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static dk.itu.utils.ObjRunUtil.loadObj;


public class StageView {
    private static Stage loadingStage;

    public static void makeStartStage() {
        //loading screen stage here since it works with UI
        loadingStage = new Stage(StageStyle.UNDECORATED);
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        loadingStage.setX(bounds.getWidth() / 2);
        loadingStage.setY(bounds.getHeight() / 5);

        FileStageView.showFileStageView();

    }

    public static void chooseMap(boolean isCustom, String customFilePath) {
        if (isCustom) {
            // obj files should not be savable
            if (customFilePath.endsWith(".obj")) {
                loadObj(customFilePath, loadingStage, false);
            }
            // if not obj asks if user wants file saved as obj
            else {
                LoadingScreen.createLoadingScreen(loadingStage);
                if (customFilePath.endsWith(".zip")) {
                    loadingStage.show();
                    executeMap(customFilePath, false);
                } else {
                    ObjSavePrompt.saveAsObjChooser(customFilePath, loadingStage);
                }
            }
        }
        // if is default file uses loadObj
        else {
                loadObj(customFilePath, loadingStage, true);
        }

    }

    public static void executeMap(String customFilePath, boolean saveObj) {
     // update the UI on the JavaFX application thread
        Platform.runLater(() -> {
            MapController.loadCustomOsmMap(customFilePath, saveObj);
            loadingStage.close();
        });
    }
}
