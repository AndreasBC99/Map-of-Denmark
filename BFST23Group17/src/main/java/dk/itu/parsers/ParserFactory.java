package dk.itu.parsers;

public class ParserFactory {

    public static Parser getParser(String filePath) {
        // for osm files
        if (filePath.endsWith(".osm")) {
            return new OsmParser(filePath, OsmFileType.OSM);
        }
        // for osm.zip files
        else if (filePath.endsWith(".zip")) {
            return new OsmParser(filePath, OsmFileType.ZIP);
        } else {
            throw new UnsupportedOperationException("File type not supported");
        }
    }

    public enum OsmFileType {
        OSM,
        ZIP
    }
}
