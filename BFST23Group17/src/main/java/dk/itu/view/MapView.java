package dk.itu.view;

import dk.itu.controller.MapController;
import dk.itu.datastructure.DijkstraSPNew;
import dk.itu.model.*;
import dk.itu.utils.DirectionUtil;
import dk.itu.utils.HaversineUtil;
import dk.itu.utils.NnUtils;
import dk.itu.utils.ThemesFactory;
import dk.itu.view.components.DebugDetailsComponent;
import dk.itu.view.components.ScaleRatioComponent;
import dk.itu.view.components.ZoomButtonComponent;

import dk.itu.view.components.*;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;

import javafx.geometry.Pos;
import dk.itu.model.MapModel;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;

import javafx.stage.Stage;

import java.io.File;
import java.util.*;


public class MapView {
    private static final Canvas canvas = new Canvas(1200, 800);
    public int drawnWays = 0; // Set of drawn way ids, as to not redraw them
    public final GraphicsContext gc = canvas.getGraphicsContext2D();
    private static Affine trans = new Affine();
    private final MapModel mapModel;
    private static int zoomLevel = 5;
    double zoomLvl = 1;
    private final VBox scaleBox;
    private final DebugDetailsComponent debugDetailsComponent;

    List<OsmElement> directionChain;

    List<String> directionDescription;

    private double totalDistance;

    private double secondsToTraverse;

    private double zoomValue = 1.0;

    private boolean onRoundabout;

    //Values for roundabout
    private boolean enteringRA, leavingRA;

    private RouteTypeComponent routeTypeComponent;

    private RouteInstructionComponent routeInstructionComponent;

    private RoadNameComponent roadNameComponent;
    private PointOfInterestComponent pointOfInterestComponent;
    private ListOfPointOfInterestComponent listOfPointOfInterestComponent;
    private static Set<Long> drawnWaysIds = new HashSet<>();
    boolean firstScale = true;

    private final ScaleRatioComponent scaleRatioComponent;
    private final StackPane sPane;

    private SearchFieldComponent searchComponent;

    private static Font iconFont, textFont;

    private VBox UIContent = new VBox();

    private double zoomFactor = 1.5;

    private GraphicalZoomComponent graphicalZoomComponent;
    private double minZoom = -4;
    private double maxZoom = 14;

    private RightClickComponent rightClickComponent;

    public MapView(Stage stage, MapModel _mapModel) {
        mapModel = _mapModel;
        sPane = new StackPane();
        BorderPane pane = new BorderPane(canvas);

        rightClickComponent = new RightClickComponent(this);
        debugDetailsComponent = new DebugDetailsComponent(this);
        searchComponent = new SearchFieldComponent(this);
        routeTypeComponent = new RouteTypeComponent(this);
        roadNameComponent = new RoadNameComponent(this);
        scaleRatioComponent = new ScaleRatioComponent(this, zoomFactor);
        pointOfInterestComponent = new PointOfInterestComponent("Save a point", this);
        listOfPointOfInterestComponent = new ListOfPointOfInterestComponent("Saved points", this);
        VBox zoomAndRoadName = new VBox();
        drawnWaysIds = new HashSet<>();

        UIContent.setMaxSize(250, 0);
        UIContent.setSpacing(8);
        sPane.setAlignment(Pos.TOP_LEFT);

        UIContent.getChildren().addAll( searchComponent, routeTypeComponent,
                pointOfInterestComponent, listOfPointOfInterestComponent);

        sPane.getChildren().addAll(pane, UIContent, debugDetailsComponent);
      
        VBox cont = new VBox();
        cont.setStyle("-fx-background-color: transparent");
        cont.setMaxSize(25, 25);
        cont.setSpacing(8);
        ZoomButtonComponent zoomPlus = new ZoomButtonComponent(ZoomButtonComponent.ZoomButtonType.PLUS, this);
        ZoomButtonComponent zoomMinus = new ZoomButtonComponent(ZoomButtonComponent.ZoomButtonType.MINUS, this);
        cont.getChildren().addAll(zoomPlus, zoomMinus);
        sPane.getChildren().addAll(cont);
        scaleBox = new VBox();
        scaleBox.getChildren().addAll(scaleRatioComponent);
        scaleBox.setAlignment(Pos.BOTTOM_LEFT);
        StackPane.setMargin(scaleBox, new Insets(0, 0, 10, 10));
        sPane.getChildren().add(scaleBox);
        sPane.setAlignment(debugDetailsComponent, Pos.TOP_RIGHT);
        scaleBox.setPickOnBounds(false);
        sPane.setAlignment(cont, Pos.CENTER_RIGHT);
        Scene scene = new Scene(sPane);
        stage.setScene(scene);
        stage.show();
        loadEventHandlers();
        graphicalZoomComponent = new GraphicalZoomComponent();
        zoomAndRoadName.getChildren().addAll(graphicalZoomComponent, roadNameComponent);
        sPane.getChildren().add(zoomAndRoadName);
        sPane.setAlignment(zoomAndRoadName, Pos.BOTTOM_RIGHT);
        zoomAndRoadName.setAlignment(Pos.BOTTOM_RIGHT);
        zoomAndRoadName.setPickOnBounds(false);

        trans.prependTranslation(-0.56 * mapModel.getMinX(), mapModel.getMaxY());
        trans.prependScale(canvas.getHeight() / (mapModel.getMaxY() - mapModel.getMinY()), canvas.getHeight() / (mapModel.getMaxY() - mapModel.getMinY()));

        redraw();

        stage.widthProperty().addListener((observable, oldval, newval) -> {
            resizeCanvas(newval.doubleValue(), 0);
            System.out.println("resizing");
        });

        stage.heightProperty().addListener((observable, oldval, newval) -> {
            resizeCanvas(0, newval.doubleValue());
        });
    }

    private void loadEventHandlers() {
        canvas.setOnMousePressed(MapController::handleOnMousePressed);
        canvas.setOnMouseDragged(MapController::handleOnMouseDragged);
        canvas.setOnScroll(MapController::handleOnScroll);
        canvas.setOnMouseMoved(MapController::handleOnMove);
        canvas.setOnMouseClicked(MapController::handleOnClick);
        canvas.setOnContextMenuRequested(MapController::handleOnRightClick);

    }

    public void redrawNavigation(){
        //Draw the navigation route
        if(directionChain != null && !directionChain.isEmpty()){
            gc.beginPath();
            gc.moveTo(0.56*directionChain.get(0).getMinX(), -directionChain.get(0).getMinY());
            for(int i = 0; i < directionChain.size(); i++){
                gc.lineTo(0.56*directionChain.get(i).getMinX(), -directionChain.get(i).getMinY());
            }
            gc.setLineWidth(1/Math.sqrt(trans.determinant()) * 6);
            gc.setStroke(Color.YELLOW);
            gc.stroke();

            //Draw dashed lines from navigation icons to direction chain start / end
            gc.beginPath();
            gc.setLineWidth(1/Math.sqrt(trans.determinant()));
            gc.setStroke(Color.BLUE);
            gc.setLineDashes(2.25 * gc.getLineWidth());

            gc.moveTo(0.56*searchComponent.getFromX(), -searchComponent.getFromY());
            gc.lineTo(0.56*directionChain.get(0).getMinX(), -directionChain.get(0).getMinY());

            gc.moveTo(0.56*searchComponent.getToX(), -searchComponent.getToY());
            gc.lineTo(0.56*directionChain.get(directionChain.size()-1).getMinX(), -directionChain.get(directionChain.size()-1).getMinY());
            gc.stroke();
        }

        //Draw navigation icons
        gc.setFont(iconFont);
        if(searchComponent.getFromX() != 0.0f){
            gc.setFill(Color.GREEN);
            gc.fillText("\uf3c5", 0.56*searchComponent.getFromX(), -searchComponent.getFromY());
        }
        if(searchComponent.getToY() != 0.0f){
            gc.setFill(Color.RED);
            gc.fillText("\uf3c5", 0.56*searchComponent.getToX(), -searchComponent.getToY());
        }
    }

    public void redraw(){
        gc.setStroke(null);
        drawnWaysIds.clear();
        long startTime = System.currentTimeMillis();
        drawnWays = 0; // Resets the drawn way count
        gc.setTransform(new Affine());

        if(ThemesFactory.getColorMode()==2) gc.setFill(Color.web("#552c20")); // Set inverted "water" background for canvas if inverted colors chosen
        else gc.setFill(Color.web("#aad3df")); // Set "water" background for canvas as standard

        gc.fillRect(0,0,canvas.getWidth(), canvas.getHeight());
        gc.setTransform(trans);
        gc.setLineWidth(1/Math.sqrt(trans.determinant()));

        // Debug - only draws if allowed
        if (debugDetailsComponent.canDraw()) {
            //Load fontawesome
            String iconFontURL = System.getProperty("user.dir").concat("/src/main/resources/assets/fonts/fontawesome.ttf");
            String textFontURL = System.getProperty("user.dir").concat("/src/main/resources/assets/fonts/mulish.ttf");
            File iconFontFile = new File(iconFontURL);
            File textFontFile = new File(textFontURL);
            iconFont = Font.loadFont(iconFontFile.toURI().toString(), MapView.getDrawingSize()*12);
            textFont = Font.loadFont(textFontFile.toURI().toString(), MapView.getDrawingSize()*12);

            for (int i = 0 ; i<mapModel.getAllNewPhTrees().size() ; i++) {
                if (mapModel.getAllNewPhTrees().get(i) == null) continue;
                if (i < debugDetailsComponent.getMinLayer() || i > debugDetailsComponent.getMaxLayer()) continue;

                if (ThemesFactory.getTheme().getLayerBounds(i) < getScreenWidthX()) continue;
                List<OsmElement> queryList = mapModel.getAllNewPhTrees().get(i).windowQuery(getScreenLeftX(), getScreenRightX(), getScreenBottomY(), getScreenTopY());
                Iterator<OsmElement> query = queryList.iterator();


                while (query.hasNext()) {
                    OsmElement e = query.next();
                    if (!e.shouldBeDrawn()) continue;
                    if(e.getType().equals(OsmElement.OsmElementType.RELATION_MULTIPOLYGON) || e.getType().equals(OsmElement.OsmElementType.NODE_INFO)){
                        e.draw(this, Optional.empty(), Optional.empty());
                    } else if(e.getType().equals(OsmElement.OsmElementType.WAY)){
                        if(!e.getIndependent()){
                            continue;
                        }
                        if (!drawnWaysIds.contains(e.getId())) {
                            e.draw(this, Optional.empty(), Optional.empty());
                            drawnWaysIds.add(e.getId());
                        }
                        drawnWays++;//For debug purposes
                    }

                    if (debugDetailsComponent.canShowElementBoundBox()) {
                        gc.beginPath();
                        gc.setLineDashes(null);
                        gc.setLineWidth(getDrawingSize() * 3);
                        gc.moveTo(0.56 * e.getMinX(), -e.getMinY());
                        gc.lineTo(0.56 * e.getMaxX(), -e.getMinY());
                        gc.lineTo(0.56 * e.getMaxX(), -e.getMaxY());
                        gc.lineTo(0.56 * e.getMinX(), -e.getMaxY());
                        gc.lineTo(0.56 * e.getMinX(), -e.getMinY());
                        gc.setStroke(Color.RED);
                        gc.stroke();
                    }

//                    break;
                }
            }
            redrawNavigation();
        }

        if (debugDetailsComponent.canShowKdBox()) {
            gc.beginPath();
            gc.setLineDashes(null);
            gc.setLineWidth(getDrawingSize()*3);
            gc.moveTo(0.56*getScreenLeftX(), -getScreenTopY());
            gc.lineTo(0.56*getScreenRightX(), -getScreenTopY());
            gc.lineTo(0.56*getScreenRightX(), -getScreenBottomY());
            gc.lineTo(0.56*getScreenLeftX(), -getScreenBottomY());
            gc.lineTo(0.56*getScreenLeftX(), -getScreenTopY());
            gc.setStroke(Color.BLUE);
            gc.stroke();
        }

        long endTime = System.currentTimeMillis()-startTime;
        double rps = Math.min(Math.floor((1.0/(endTime/1000.0))), 120); // RPS or redraws per second
        debugDetailsComponent.setFpsText("RPS: " + rps);
        System.out.println("Redrew " + drawnWays + " ways in " + (endTime) + "ms with " + rps + "RPS");
    }

    public void loadNewDijkstra(RouteTypeComponent.options type) {
        //Disable buttons on start of dijkstra
        routeTypeComponent.disableButtons();
        directionDescription = new ArrayList<>();
        directionChain = new ArrayList<>();

        float fromX = searchComponent.getFromX();
        float fromY = searchComponent.getFromY();
        float toX = searchComponent.getToX();
        float toY = searchComponent.getToY();

        OsmPathNode startNode = NnUtils.nn(this, new Point2D(fromX, fromY), type.equals(RouteTypeComponent.options.VEHICLE), type.equals(RouteTypeComponent.options.BIKE), type.equals(RouteTypeComponent.options.FOOT));
        OsmPathNode endNode = NnUtils.nn(this, new Point2D(toX, toY), type.equals(RouteTypeComponent.options.VEHICLE), type.equals(RouteTypeComponent.options.BIKE), type.equals(RouteTypeComponent.options.FOOT));

        if((startNode == null) || (endNode == null)){
            routeTypeComponent.enableButtons();
            return;//Do not create the dijkstra if no points have been selected
        }

        Runnable createDijkstra = (() -> {
            DijkstraSPNew dijk = new DijkstraSPNew(startNode, endNode, type);

            totalDistance = 0;//Ensure reset
            secondsToTraverse = 0;//Ensure reset

            if(dijk.hasPathTo(endNode)){
                secondsToTraverse = endNode.getDist();
                Iterator<OsmPathNode> it = dijk.pathTo(endNode).iterator();
                List<OsmPathNode> edges = new ArrayList<>();

                while(it.hasNext()) edges.add(it.next());

                ListIterator<OsmPathNode> listIterator = edges.listIterator();

                //For route instructions
                OsmWay currentParent = null;
                double distance = 0;

                OsmPathNode currentPN = null;
                OsmPathNode previousPN = null;
                OsmPathNode previousPN2 = null;

                while(listIterator.hasNext()){
                    previousPN2 = previousPN;
                    previousPN = currentPN;
                    currentPN = listIterator.next();//Update the current node
                    directionChain.add(currentPN);

                    //Ensure that three nodes has been loaded
                    if(previousPN2 != null){
                        if(currentParent == null) currentParent = previousPN2.getParentWayTo(previousPN);//If first way

                        if(previousPN.getParentWayTo(currentPN) != currentParent){
                            //A new way has been hit, describe previous segment
                            //Test if leaving a roundabout
                            if(currentParent.getRoundAbout()) leavingRA = true;

                            if(!leavingRA) directionDescription.add(((distance < 1) ? (int) (distance*1000) + " m" : Math.round(distance * 100.0) / 100.0 + " km") + " on " + ((onRoundabout) ? "roundabout" : currentParent.getName()));
                            currentParent = previousPN.getParentWayTo(currentPN);//Update parent
                            if(currentParent.getRoundAbout()) enteringRA = true;//Test if entering a roundabout
                            totalDistance += distance;

                            //Determine whether there has been a turn left / right
                            double[] vector1 = {previousPN.getMinX() - previousPN2.getMinX(), previousPN.getMinY() - previousPN2.getMinY()};
                            double[] vector2 = {currentPN.getMinX() - previousPN.getMinX(), currentPN.getMinY() - previousPN.getMinY()};
                            double bearing1 = DirectionUtil.bearing(vector1);
                            double bearing2 = DirectionUtil.bearing(vector2);
                            String directionToTurn = DirectionUtil.getDirection(bearing1, bearing2, previousPN2, currentPN);

                            if(!directionToTurn.equals("Stay")) {//Do not add to description if it's the same road
                                if(enteringRA) directionDescription.add("Enter the roundabout");
                                else if(leavingRA) directionDescription.add("Exit roundabout on " + previousPN.getParentWayTo(currentPN).getName());
                                else directionDescription.add(directionToTurn + " on " + previousPN.getParentWayTo(currentPN).getName());
                            }

                            leavingRA = false;
                            enteringRA = false;
                            distance = HaversineUtil.pointsToKilometer(previousPN, currentPN);//Update distance
                        } else{
                            //Currently on the same road -> increase distance
                            distance += HaversineUtil.pointsToKilometer(previousPN2, previousPN);
                        }
                    } else if(previousPN != null) distance += HaversineUtil.pointsToKilometer(previousPN, currentPN);
                }

                //Construct final road when iterator is through
                if(currentParent != null) {
                    directionDescription.add(((distance < 1) ? (int) (distance * 1000) + " m" : Math.round(distance * 100.0) / 100.0 + " km") + " on " + currentParent.getName());
                }

                totalDistance += distance;
            } else{
                directionDescription.add("No available path for this transportation type");
            }


            startNode.reset();
            routeTypeComponent.enableButtons();

            Platform.runLater(() -> {
                loadDirections();
                redrawNavigation();
                redraw();
            });
        });

        Thread createDijkstraThread = new Thread(null, createDijkstra, "", Integer.MAX_VALUE);
        createDijkstraThread.start();

        //Update visuals
        redraw();
    }

    public void loadDirections(){
        if(routeInstructionComponent == null) routeInstructionComponent = new RouteInstructionComponent(this);
        else routeInstructionComponent.update();
        UIContent.getChildren().remove(routeInstructionComponent);
        UIContent.getChildren().add(routeInstructionComponent);
    }

    //Panning
    public void pan(double dx, double dy, boolean shouldRedraw){
        trans.prependTranslation(dx, dy);
        if (shouldRedraw) redraw();
    }

    //Zooming
    public void zoom(double dx, double dy, boolean zoomIn) {
        if(zoomIn && zoomLevel+1 < maxZoom){
            pan(-dx, -dy, false);
            trans.prependScale(zoomFactor, zoomFactor);
            zoomLevel++;
            pan(dx, dy, false);
        } else if (!zoomIn && zoomLevel-1 > minZoom) {
            pan(-dx, -dy, false);
            trans.prependScale(1/zoomFactor, 1/zoomFactor);
            zoomLevel--;
            pan(dx, dy, false);
        }
        graphicalZoomComponent.updateZoom(zoomLevel, maxZoom, minZoom);
        redraw();
    }

    public float getScreenLeftX() {
        if (debugDetailsComponent.canShowKdBox()) {
            return (float) ((trans.getTx()/trans.getMxx())/-0.56 + (((trans.getTx()-canvas.getWidth())/trans.getMxx())/-0.56 - (trans.getTx()/trans.getMxx())/-0.56)*0.2);
        } else {
            return (float) ((trans.getTx()/trans.getMxx())/-0.56);
        }
    }

    public float getScreenRightX() {
        if (debugDetailsComponent.canShowKdBox()) {
            return (float) ((trans.getTx()/trans.getMxx())/-0.56 + (((trans.getTx()-canvas.getWidth())/trans.getMxx())/-0.56 - (trans.getTx()/trans.getMxx())/-0.56)*0.8);
        } else {
            return (float) (((trans.getTx()-canvas.getWidth())/trans.getMxx())/-0.56);
        }
    }

    public float getScreenBottomY() {
        if (debugDetailsComponent.canShowKdBox()) {
            return (float) (trans.getTy()/trans.getMyy() + ((trans.getTy()-canvas.getHeight())/trans.getMyy() - trans.getTy()/trans.getMyy())*0.6);
        } else {
            return (float) ((trans.getTy()-canvas.getHeight())/trans.getMyy());
        }
    }

    public float getScreenTopY() {
        if (debugDetailsComponent.canShowKdBox()) {
            return (float) (trans.getTy()/trans.getMyy() + ((trans.getTy()-canvas.getHeight())/trans.getMyy() - trans.getTy()/trans.getMyy())*0.2);
        } else {
            return (float) (trans.getTy()/trans.getMyy());
        }
    }
    public Point2D getCanvasCenter() {
        Bounds canvasCenter = canvas.getLayoutBounds();
        return new Point2D(canvasCenter.getCenterX(), canvasCenter.getCenterY());
    }
    public void setScaleRatio(double _kilometerLengthPx, boolean zoomIn) {
        if(firstScale) {
            scaleRatioComponent.setSize(_kilometerLengthPx, scaleBox, maxZoom, minZoom);
            sPane.setAlignment(scaleBox, Pos.BOTTOM_LEFT);
            firstScale = false;
            maxZoom = maxZoom + Math.rint(100/_kilometerLengthPx);
            minZoom = minZoom - Math.rint(100/_kilometerLengthPx);
        } else {
            scaleRatioComponent.setScale(zoomLevel, zoomIn, scaleBox);

        }

    }

    public RightClickComponent getRightClickComponent() { return rightClickComponent; }

    public Affine getTrans() {
        return trans;
    }

    public void resetTrans() {
        trans = new Affine();
    }

    public DebugDetailsComponent getDebugDetailsComponent() {
        return debugDetailsComponent;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public MapModel getMapModel() {
        return mapModel;
    }

    public static double getDrawingSize(){
        return 1/Math.sqrt(trans.determinant());
    }

    public static void setDrawn(long id){
        drawnWaysIds.add(id);
    }

    public static Font getIconFont(){return iconFont;}

    public static Font getTextFont(){return textFont;}

    public static int getZoomLevel(){return zoomLevel;}

    public List<OsmElement> getDirectionChain(){return directionChain;}

    public List<String> getDirectionDescription(){return directionDescription;}

    public double getTotalDistance(){return totalDistance;}
    public double getSecondsToTraverse(){return secondsToTraverse;}

    public SearchFieldComponent getSearchComponent(){return searchComponent;}

    public PointOfInterestComponent getPointOfInterestComponent() {return pointOfInterestComponent;}

    public RoadNameComponent getRoadNameComponent(){return roadNameComponent;}

    public float getScreenWidthX() {
        return Math.abs(getScreenRightX()-getScreenLeftX());
    }

    //Function for resizing the canvas
    public void resizeCanvas(double _width, double _height){
        if(_width != 0) canvas.setWidth(_width);
        if(_height != 0) canvas.setHeight(_height);
        redraw();
    }
}
