package dk.itu.parsers;

import dk.itu.datastructure.phtree.PhTree;

import dk.itu.model.*;
import dk.itu.utils.ThemesFactory;
import org.codehaus.stax2.XMLInputFactory2;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipInputStream;

import static dk.itu.utils.FibonacciSearchOsm.fibonaccianSearch;

public class OsmParser implements Parser {
    private final List<OsmElement> allNodes = new ArrayList<>();
    private final List<OsmElement> allWays = new ArrayList<>();
    private final List<OsmElement> allRelations = new ArrayList<>();
    private final HashMap<String, OsmInfoNode> addressMap = new HashMap<>();
    private final Set<String> usedTags = new HashSet<>(List.of("addr:street", "addr:housenumber", "addr:postcode", "addr:city", "name", "type", "role", "oneway", "junction", "foot", "bicycle"));
    private final Set<Long> blackListRel = new HashSet<>(List.of(11734020L, 5176048L, 13524030L, 13517925L, 13517699L, 9342805L, 4052654L, 9375298L, 5687318L, 13524499L, 13536156L, 5523740L, 5712032L, 10631323L, 13536194L, 14695176L, 5468624L, 3058453L, 13524233L, 5259758L, 5421328L, 14683490L, 9348827L, 13525593L, 14682319L, 5678127L, 14682974L, 14682822L, 10149145L, 8688373L, 15466139L, 1137998077L, 13525532L, 13525551L, 15535742L, 5607674L, 13524545L, 13538259L, 13524509L, 5326788L));

    private List<PhTree> allNewPhTrees = new ArrayList<>();
    private final PhTree newPhTreeNN = new PhTree();
    private final PhTree newPhTreeIcons = new PhTree();
    private double minY, maxY, minX, maxX;

    public OsmParser(String filePath, ParserFactory.OsmFileType fileType) {
            usedTags.addAll(ThemesFactory.getTheme().theme.keySet());
            long startTimeParse = System.currentTimeMillis();

            switch (fileType) {
                case OSM -> {
                    // custom osm files
                    try (InputStream is = Files.newInputStream(Path.of(filePath))) {
                        parse(is);
                    } catch (IOException | XMLStreamException e) {
                        throw new UnsupportedOperationException("Failed to parse custom file");
                    }
                }
                case ZIP -> {
                    // osm zip files
                    try (ZipInputStream input = new ZipInputStream(Files.newInputStream(Path.of(filePath)))) {
                        input.getNextEntry();
                        parse(input);
                    } catch (IOException | XMLStreamException e) {
                        throw new UnsupportedOperationException("Failed to parse Custom Zip");
                    }
                }
            }
            System.out.println("Finished parsing of " + filePath + " in " + (System.currentTimeMillis() - startTimeParse) + "ms");
    }

    public void parse(InputStream is) throws XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory2.newInstance();
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);

        long currentId = -1L; // ALL
        Map<String, String> currentTags = new HashMap<>(); // ALL
        Float currentX = null, currentY = null;
        List<OsmElement> currentNodesList = new ArrayList<>(); // WAYS
        List<OsmElement> currentMembersList = new ArrayList<>(); // RELATIONS

        while (reader.hasNext()) {
            reader.next();
            if (reader.isCharacters()) {
                continue;
            }

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "bounds" -> {
                        minY = Double.parseDouble(reader.getAttributeValue(null, "minlat"));
                        maxY = Double.parseDouble(reader.getAttributeValue(null, "maxlat"));
                        minX = Double.parseDouble(reader.getAttributeValue(null, "minlon"));
                        maxX = Double.parseDouble(reader.getAttributeValue(null, "maxlon"));
                        continue;
                    }
                    case "node" -> {
                        currentId = Long.parseUnsignedLong(reader.getAttributeValue(null, "id"));
                        currentX = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                        currentY = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                        continue;
                    }
                    case "way", "relation" -> {
                        currentId = Long.parseUnsignedLong(reader.getAttributeValue(null, "id"));
                        continue;
                    }
                    case "member" -> {
                        switch (reader.getAttributeValue(null, "type")) {
                            case "way" -> {
                                int index = fibonaccianSearch(allWays, Long.parseUnsignedLong(reader.getAttributeValue(null, "ref")));
                                OsmElement way = index < 0 ? null : allWays.get(index);
                                currentMembersList.add(way == null ? null : way.setMemberType(reader.getAttributeValue(null, "role").intern()));
                            }
                        }
                        continue;
                    }
                    case "nd" -> {
                        int target = fibonaccianSearch(allNodes, Long.parseUnsignedLong(reader.getAttributeValue(null, "ref")));
                        if(target != -1){
                            currentNodesList.add(allNodes.get(target));
                        }
                        continue;
                    }
                    case "tag" -> {
                        String tagKey = reader.getAttributeValue(null, "k").intern();
                        if (usedTags.contains(tagKey)) {
                            currentTags.put(tagKey, reader.getAttributeValue(null, "v").intern());
                        }
                        continue;
                    }
                }
            }
            if (reader.isEndElement()) {
                switch (reader.getLocalName()) {
                    case "node" -> {
                        OsmElement node = currentTags.isEmpty() ? new OsmNode(currentId, currentY, currentX) : new OsmInfoNode(currentId, currentY, currentX, currentTags);
                        //Creating address
                        if (currentTags.containsKey("addr:street")) {
                               try {
                                   addressMap.put((currentTags.get("addr:street") + " " +
                                                   currentTags.get("addr:housenumber") + " " +
                                                   currentTags.get("addr:postcode") + " " +
                                                   currentTags.get("addr:city")),
                                           (OsmInfoNode) node);
                               } catch (Exception ignore) {
                                   //Should never happen,
                                   // but if OSM file has errors with address the address is skipped
                               }
                        }
                        currentTags.clear();
                        allNodes.add(node);
                        if (node.shouldBeDrawn()) newPhTreeIcons.put(node);
                    }
                        case "way" -> {
                            if(currentNodesList.isEmpty()) continue;//DO not create a way if it has no children
                            OsmWay way = new OsmWay(currentId, currentNodesList, currentTags, this);

                            if(currentTags.containsKey("highway")){
                                createEdgesNew(currentNodesList, way);
                                switch(currentTags.get("highway")){
                                    case "planned":
                                        way.setWalking(false);
                                        way.setCycling(false);
                                        way.setDriving(false);
                                        break;
                                    case "cycleway":
                                        way.setWalking(false);
                                        way.setDriving(false);
                                        break;
                                    case "track":
                                    case "path":
                                        way.setDriving(false);
                                        break;
                                    case "footway":
                                    case "pedestrian":
                                    case "steps":
                                        way.setCycling(false);
                                        way.setDriving(false);
                                        break;
                                    case "motorway":
                                    case "motorway_link":
                                    case "trunk":
                                        way.setWalking(false);
                                        way.setCycling(false);
                                        break;
                                    case "default":
                                        way.setWalking(true);
                                        way.setCycling(true);
                                        way.setDriving(true);
                                        break;
                                }

                                //Final check if special rules for walking/biking is in place (implemented redundantly becuase "bicycle"/"foot" can contain other values than yes/no
                                if(currentTags.containsKey("bicycle")){
                                    if(currentTags.get("bicycle").equals("yes")) way.setCycling(true);
                                    else if(currentTags.get("bicycle").equals("no")) way.setCycling(false);
                                }

                                if(currentTags.containsKey("foot")){
                                    if(currentTags.get("foot").equals("yes")) way.setWalking(true);
                                    else if(currentTags.get("foot").equals("no")) way.setWalking(false);
                                }
                            }
                        allWays.add(way);
                        if (way.shouldBeDrawn()) {
                            addToNewPhTree(way);
                        }
                        currentNodesList.clear();
                        currentTags.clear();
                    }
                    case "relation" -> {
                        if(blackListRel.contains(currentId)){
                            for(OsmElement oe : currentMembersList){
                                oe.setIndependent(false);
                                if (oe.getLayer() < allNewPhTrees.size() && allNewPhTrees.get(oe.getLayer()) != null) allNewPhTrees.get(oe.getLayer()).delete(oe);
                            }
                            currentMembersList.clear();
                            currentTags.clear();
                            continue;
                        }
                        if (currentMembersList.stream().parallel().allMatch(Objects::nonNull)) {
                            if(currentTags.containsKey("type") && currentTags.get("type").equals("multipolygon")){
                                for(OsmElement oe : currentMembersList){
                                    oe.setIndependent(false);
                                    if (oe.getLayer() < allNewPhTrees.size() && allNewPhTrees.get(oe.getLayer()) != null && !currentTags.containsKey("highway")) allNewPhTrees.get(oe.getLayer()).delete(oe);
                                }

                                OsmRelation relation = new OsmRelation(currentId, new ArrayList<>(currentMembersList), currentTags);
                                allRelations.add(relation);
                                if (relation.getChains().isEmpty()) {
                                    currentMembersList.clear();
                                    currentTags.clear();
                                    continue;
                                } else {
                                    if (relation.shouldBeDrawn()) addToNewPhTree(relation);
                                }
                            }
                        }
                        currentMembersList.clear();
                        currentTags.clear();
                    }
                }
            }
        }

        reader.close();
        addPhTree(newPhTreeIcons, 21);
    }

    public void addIconToPhTree(OsmInfoNode node){
        newPhTreeIcons.put(node);
    }
    private void addToNewPhTree(OsmElement e) {
        if (e.getLayer() == 10) return;
        if (allNewPhTrees.size()-1 >= e.getLayer()) {
            if (allNewPhTrees.get(e.getLayer()) == null) {
                allNewPhTrees.set(e.getLayer(), new PhTree());
            }
            allNewPhTrees.get(e.getLayer()).put(e);
        } else {
            allNewPhTrees.add(null);
            addToNewPhTree(e);
        }
    }

    private void addPhTree(PhTree phTree, int layer) {
        if (allNewPhTrees.size()-1 >= layer) {
            if (allNewPhTrees.get(layer) == null) {
                allNewPhTrees.set(layer, new PhTree());
            }
            allNewPhTrees.set(layer, phTree);
        } else {
            allNewPhTrees.add(null);
            addPhTree(phTree, layer);
        }
    }

    @Override
    public List<PhTree> getAllNewPhTrees() {
        return allNewPhTrees;

    }
    private void createEdgesNew(List<OsmElement> currentNodes, OsmWay parentWay) {
        List<OsmPathNode> pathNodes = new ArrayList<>();
        for (OsmElement node : currentNodes) {
            if (node instanceof OsmPathNode) {
                pathNodes.add((OsmPathNode) node);
            } else {
                if (parentWay.getIsComplete() && pathNodes.size() > 0 &&  pathNodes.get(0).getId() == node.getId()) {
                    continue;
                }
                OsmPathNode pathNode = OsmPathNode.convertToPathNode((OsmNode) node);
                allNodes.set(fibonaccianSearch(allNodes, node.getId()), pathNode);
                pathNodes.add(pathNode);
                newPhTreeNN.put(pathNode);
            }
        }
        for (int i = 0 ; i < pathNodes.size()-1 ; i++) {
            pathNodes.get(i).addConnectionTo(pathNodes.get(i+1), parentWay);
        }
        if(currentNodes.get(0) == currentNodes.get(currentNodes.size()-1)){
            pathNodes.get(pathNodes.size()-1).addConnectionTo(pathNodes.get(0), parentWay);
        }

        parentWay.replaceWithPathNodes(pathNodes);
    }

    @Override
    public HashMap<String, OsmInfoNode> getAddressMap() {return addressMap;}
    @Override
    public PhTree getNewPhTreeNN() {
        return newPhTreeNN;
    }

    @Override
    public double getMinX() {
        return minX;
    }

    @Override
    public double getMinY() {
        return minY;
    }

    @Override
    public double getMaxX() {
        return maxX;
    }

    @Override
    public double getMaxY() {
        return maxY;
    }
}

