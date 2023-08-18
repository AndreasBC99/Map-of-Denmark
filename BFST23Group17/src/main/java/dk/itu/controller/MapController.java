package dk.itu.controller;

import dk.itu.model.MapModel;
import dk.itu.model.OsmPathNode;
import dk.itu.parsers.ParserFactory;
import dk.itu.utils.NnUtils;

import dk.itu.utils.ObjRunUtil;
import dk.itu.utils.WebMercatorUtils;
import dk.itu.view.MapView;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;


public class MapController {
    private static double lastX, lastY;
    private static MapView mapView;
    private static float lastScrollNano = 0;
    private static double[] minWebMercator;
    private static double[] maxWebMercator;
    private static double mapWidthWebMercator;
    private static double kilometerLengthPx;
    private static double mapHeightWebMercator;

    public static void loadCustomOsmMap(String customFilePath, Boolean saveObj) {

        MapModel customMapModel = new MapModel(ParserFactory.getParser(customFilePath));
        if (saveObj) {
            System.out.println(".obj file is being saved, please do not close program");
            ObjRunUtil.save(customFilePath, customMapModel);
        }
        createMapView(customMapModel);
    }

    public static void createMapView (MapModel mapModel) {
        Platform.runLater(() -> {
            Stage primaryStage = new Stage();
            mapView = new MapView(primaryStage, mapModel);
            minWebMercator = WebMercatorUtils.toWebMercator(mapModel.getMinY(), mapModel.getMinX());
            maxWebMercator = WebMercatorUtils.toWebMercator(mapModel.getMaxY(), mapModel.getMaxX());
            mapWidthWebMercator = maxWebMercator[0] - minWebMercator[0];
            mapHeightWebMercator = maxWebMercator[1] - minWebMercator[1];
            kilometerLengthPx = 1000 / (mapWidthWebMercator / (mapView.getCanvas().getWidth() * (mapView.getCanvas().getHeight() / (mapModel.getMaxY() - mapModel.getMinY())) / (mapView.getCanvas().getWidth() / (mapModel.getMaxX() - mapModel.getMinX()))));
            mapView.setScaleRatio(kilometerLengthPx, true);
        });
    }

    public static void handleOnMousePressed(MouseEvent e) {
        lastX = e.getX();
        lastY = e.getY();
    }
    public static void handleOnMouseDragged(MouseEvent e) {
        double dx = e.getX() - lastX;
        double dy = e.getY() - lastY;
        mapView.pan(dx, dy, true);

        lastX = e.getX();
        lastY = e.getY();
    }

    public static void handleOnScroll(ScrollEvent e) {
        boolean zoomIn;
        zoomIn = e.getDeltaY() > 0; //zoomIn will be true or false according to the direction of the zoom event

        if (lastScrollNano == 0) {
            lastScrollNano = System.nanoTime();
            mapView.setScaleRatio(kilometerLengthPx, zoomIn);
            mapView.zoom(e.getX(), e.getY(), zoomIn);
            return;
        }

        long currentTimeNano = System.nanoTime();
        double milliSecPassed = (currentTimeNano - lastScrollNano) * 1.0e-6;

        if(milliSecPassed > 250){
            mapView.setScaleRatio(kilometerLengthPx, zoomIn);
            mapView.zoom(e.getX(), e.getY(), zoomIn);
            lastScrollNano = currentTimeNano;
        }
    }

    public static void handleOnMove(MouseEvent e) {
        double[] latLon = getLatLonFromMouse(e.getX(), e.getY());
        OsmPathNode nn = NnUtils.nn(mapView, new Point2D(latLon[1], latLon[0]), true, true, true);
        mapView.getRoadNameComponent().update(nn == null ? "No nearest road" : nn.getParentWayName());
    }

    public static void handleOnRightClick(ContextMenuEvent e){
        double[] latLon = getLatLonFromMouse(e.getX(), e.getY());
        mapView.getRightClickComponent().setXY( latLon[1], latLon[0]);
        mapView.getRightClickComponent().show(mapView.getCanvas(), e.getScreenX(), e.getScreenY());
    }

    public static void handleOnClick(MouseEvent e) {
        double[] latLon = getLatLonFromMouse(e.getX(), e.getY());
        if(e.getButton() != MouseButton.SECONDARY){
            mapView.getRightClickComponent().hide();
        }
        //Create navigation icons if search fields are currently focused
        if(mapView.getPointOfInterestComponent().getPoIIsSelected()) {
            mapView.getPointOfInterestComponent().setPoIX((float) (latLon[1]), (float) (latLon[0]));
            mapView.getPointOfInterestComponent().showStage();
        }
    }

    private static double[] getLatLonFromMouse(double x, double y) {
        Point2D mousePoint;
        try {
            mousePoint = mapView.getTrans().inverseTransform(x, y);
//            mousePoint = mapView.getTransformOrigin().inverseTransform(x, y);
        } catch (NonInvertibleTransformException ex) {
            throw new RuntimeException(ex);
        }

        return new double[]{-mousePoint.getY(), mousePoint.getX()/0.56};
//        double normalizedX = (mousePoint.getX()/0.56) / mapView.getCanvas().getWidth();
//        double normalizedY = mousePoint.getY() / mapView.getCanvas().getHeight();
//        double webMercatorX = minWebMercator[0] + normalizedX * mapWidthWebMercator;
//        double webMercatorY = maxWebMercator[1] - normalizedY * mapHeightWebMercator;
//
//        return WebMercatorUtils.toLatLon(webMercatorX, webMercatorY);
    }


    public static void handleZoomClickPos(MouseEvent e) {
        mapView.zoom(mapView.getCanvasCenter().getX(), mapView.getCanvasCenter().getY(), true);
        mapView.setScaleRatio(kilometerLengthPx, true);

    }
    public static void handleZoomClickNeg(MouseEvent e) {
        mapView.zoom(mapView.getCanvasCenter().getX(), mapView.getCanvasCenter().getY(), false);
        mapView.setScaleRatio(kilometerLengthPx, false);

    }
}
