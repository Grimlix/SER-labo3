
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.jdom2.*;
import org.jdom2.output.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String... args) {
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("countries.geojson")) {
            // lecture en JSON
            Object obj = jsonParser.parse(reader);
            JSONObject racine = (JSONObject) obj;
            JSONArray features = (JSONArray) racine.get("features");

            // affichage
            features.forEach(f -> printFeature((JSONObject) f));

            //Ecriture avec JDOM2 (en kml)
            Document doc = createJDOMStruct(features);

            XMLOutputter xmlOutputer = new XMLOutputter();
            xmlOutputer.setFormat(Format.getPrettyFormat());
            xmlOutputer.output(doc, new FileWriter("countries.kml"));

            System.out.println("KML File was created successfully!");

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }


    }

    private static void printFeature(JSONObject f) {
        JSONElement e = new JSONElement(f);

        String name = e.getName();
        String abbreviation = e.getAbbreviation();

        System.out.println("(" + abbreviation + ") " + name);

        if (e.isPolygon()) {
            JSONArray coordinates = e.getCoordinates();
            System.out.println("     - " + coordinates.size() + " coordinates");

        } else if (e.isMultiPolygon()) {
            JSONArray polygons = e.getPolygons();
            for (Object polygon : polygons) {
                JSONArray coordinates = (JSONArray) ((JSONArray) polygon).get(0);
                System.out.println("     - " + coordinates.size() + " coordinates");
            }
        }

    }

    private static Document createJDOMStruct(JSONArray features) {
        Element racine = new Element("kml");

        Document doc = new Document(racine);

        Element document = new Element("Document");

        for (Object f: features) {
            JSONElement e = new JSONElement((JSONObject)f);
            Element placemark = new Element("Placemark");
            placemark.addContent(new Element("name").setText(e.getName()));

            if (e.isPolygon()) {
                placemark.addContent(createLineRing(e.getCoordinates()));
            } else if (e.isMultiPolygon()) {
                Element multi = new Element("MultiGeometry");

                for (Object polygon : e.getPolygons()) {
                    multi.addContent(createLineRing( (JSONArray) ((JSONArray) polygon).get(0) ));
                }
                placemark.addContent(multi);
            }
            document.addContent(placemark);
        }

        doc.getRootElement().addContent(document);
        return doc;
    }

    private static Element createLineRing(JSONArray coordinates) {
        Element line = new Element("LinearRing");
        line.addContent(new Element("coordinates").setText(coorinatesToString(coordinates)));
        return line;
    }

    private static String coorinatesToString(JSONArray coordinates) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;

        for (Object coodinate: coordinates) {
            if (first) {
                first = false;
            } else {
                stringBuilder.append(" ");
            }
            stringBuilder.append(((JSONArray) coodinate).get(0));
            stringBuilder.append(",");
            stringBuilder.append(((JSONArray) coodinate).get(1));
        }

        return stringBuilder.toString();
    }
}
