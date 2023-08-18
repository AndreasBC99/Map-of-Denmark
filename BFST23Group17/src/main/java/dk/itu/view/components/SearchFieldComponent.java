package dk.itu.view.components;
import dk.itu.datastructure.TernarySearchTree;
import dk.itu.model.MapModel;
import dk.itu.model.OsmNode;
import dk.itu.view.MapView;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;


public class SearchFieldComponent extends VBox {
    static TernarySearchTree tst = new TernarySearchTree();
    private boolean isSelectedFrom;
    private boolean isSelectedTo;
    List<String> suggestedWordsFrom;
    List<String> suggestedWordsTo;

    private static float fromX;
    private static float fromY;
    private static float toX;
    private static float toY;


    //ListView of addresses that shows suggestions from the trie algorithm
    private final ListView<String> addressSuggestionsListViewFrom = new ListView<>();
    private final ListView<String> addressSuggestionsListViewTo = new ListView<>();

    //TextFields
    TextField searchFieldFrom;
    TextField searchFieldTo;

    MapView mapView;
    public SearchFieldComponent(MapView _mapview) {
        this.mapView = _mapview;
        searchFieldFrom = new TextField();
        searchFieldTo = new TextField();
        setMaxSize(250, 200);
        setPadding(new Insets(4));
        setSpacing(10.0);
        setTranslateX(8);
        setTranslateY(8);
        searchFieldFrom.setPromptText("Search for a starting address ...");
        searchFieldTo.setPromptText("Search for a destination ...");
        searchFieldFrom.setPadding(new Insets(4));
        searchFieldTo.setPadding(new Insets(4));
        storeAddressesInAlgorithm(); //Inserts all the addresses to the Trie algorithm
        addressSuggestionsListViewTo.setMaxHeight(0);
        addressSuggestionsListViewFrom.setMaxHeight(0);

        searchFieldFrom.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.toCharArray().length >= 1) {
                if (!isSelectedFrom) {
                    String formattedInput = newValue.trim();
                    String[] words = formattedInput.split("\\s+");
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String word : words) {
                        if (word.length() > 0) {
                            stringBuilder.append(Character.toUpperCase(word.charAt(0)));
                            if (word.length() > 1) {
                                if (containsInteger(word)) {
                                    stringBuilder.append(word.substring(1, word.length() - 1)).append(Character.toUpperCase(word.charAt(word.length() - 1)));


                                } else {
                                    stringBuilder.append(word.substring(1).toLowerCase());
                                }
                            }
                            stringBuilder.append(" ");
                        }
                    }
                    formattedInput = stringBuilder.toString().trim();
                    suggestedWordsFrom = tst.suggest(formattedInput);
                    Platform.runLater(() -> addressSuggestionsListViewFrom.setMinHeight(suggestedWordsFrom.size() > 5 ? 5 * 30.0 : suggestedWordsFrom.size() * 30.0));
                    Platform.runLater(() -> addressSuggestionsListViewFrom.getItems().setAll(suggestedWordsFrom));
                }
            }
        });


        //Search for a destination
        searchFieldTo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.toCharArray().length >= 1) {
                if (!isSelectedTo) {
                    String formattedInput = newValue.trim();
                    String[] words = formattedInput.split("\\s+");
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String word : words) {
                        if (word.length() > 0) {
                            stringBuilder.append(Character.toUpperCase(word.charAt(0)));
                            if (word.length() > 1) {
                                if (containsInteger(word)) {
                                    stringBuilder.append(word.substring(1, word.length() - 1)).append(Character.toUpperCase(word.charAt(word.length() - 1)));


                                } else {
                                    stringBuilder.append(word.substring(1).toLowerCase());
                                }
                            }
                            stringBuilder.append(" ");
                        }
                    }
                    formattedInput = stringBuilder.toString().trim();
                    suggestedWordsTo = tst.suggest(formattedInput);
                    Platform.runLater(() -> addressSuggestionsListViewTo.setMinHeight(suggestedWordsTo.size() > 5 ? 5 * 30.0 : suggestedWordsTo.size() * 30.0));
                    Platform.runLater(() -> addressSuggestionsListViewTo.getItems().setAll(suggestedWordsTo));
                }
            }
        });

        //Update the TextField with the selected item
        addressSuggestionsListViewFrom.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            isSelectedFrom = true;
            searchFieldFrom.setText(newValue);
            String address = searchFieldFrom.getText();
            isSelectedFrom = false;
            if (mapView.getMapModel().getAddressMap().get(address) != null) {
                OsmNode osmNode = mapView.getMapModel().getAddressMap().get(address);
                fromX = osmNode.getMinX();
                fromY = osmNode.getMinY();
                System.out.println("*** STARTING ADDRESS COORDINATE ***"); //TEST
                System.out.println(address);
                System.out.println("LON: " + fromX + ", LAT: " + fromY); //TEST
            }
        });

        addressSuggestionsListViewTo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            isSelectedTo = true;
            searchFieldTo.setText(newValue);
            String address = searchFieldTo.getText();
            isSelectedTo = false;
            if (mapView.getMapModel().getAddressMap().get(address) != null) {
                OsmNode osmNode = mapView.getMapModel().getAddressMap().get(address);
                toY = osmNode.getMinY();
                toX = osmNode.getMinX();
                System.out.println("*** DESTINATION ADDRESS COORDINATE ***"); //TEST
                System.out.println(address);
                System.out.println("LON: " + toX + ", LAT: " + toY); //TEST
            }
        });

        getChildren().addAll(searchFieldFrom, addressSuggestionsListViewFrom, searchFieldTo, addressSuggestionsListViewTo);
    }


    public void storeAddressesInAlgorithm() {
      for (String address : mapView.getMapModel().getAddressMap().keySet()) {
          tst.insert(address);
      }

    }
    public void setTo(double x, double y){
        setToX((float) x);
        setToY((float) y);
        String toX = "" + x;
        String toY = "" + y;
        toX = toX.substring(0, 10) + "..";
        toY = toY.substring(0, 10) + "..";
        setToText(toX + ", " + toY);
        mapView.redrawNavigation();
    }
    public void setFrom(double x, double y){
        setFromX((float) x);
        setFromY((float) y);
        String fromX = "" + x;
        String fromY = "" + y;
        fromX = fromX.substring(0, 10) + "..";
        fromY = fromY.substring(0, 10) + "..";
        setFromText(fromX + ", " + fromY);
        mapView.redrawNavigation();
    }



    public float getFromX() {
        return fromX;
    }

    public float getFromY() {
        return fromY;
    }

    public float getToX() {
        return toX;
    }

    public float getToY() {
        return toY;
    }

    public void setFromX(float x) {
        fromX = x;
    }

    public void setFromY(float y) {
        fromY = y;
    }

    public void setToX(float x) {
        toX = x;
    }

    public void setToY(float y) {
        toY = y;
    }

    public void setFromText(String from) {
        searchFieldFrom.setText(from);
    }

    public void setToText(String from) {
        searchFieldTo.setText(from);
    }
    public static boolean containsInteger(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        boolean containsInteger = false;
        boolean endsWithCharacter = false;

        int length = input.length();
        int startIndex = 0;

        // Check each character from startIndex onwards
        for (int i = startIndex; i < length; i++) {
            char c = input.charAt(i);

            if (Character.isDigit(c)) {
                containsInteger = true;
            } else if (i == length - 1 && !Character.isDigit(c)) {
                endsWithCharacter = true;
            }
        }

        return containsInteger && endsWithCharacter;
    }



}
