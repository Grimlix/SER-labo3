/* Question/TODO:
 * 1. Certains ISO_A3 valent "-99" je sais pas trop pourquoi.
 * 2. https://gis.stackexchange.com/questions/260211/what-are-the-minimum-kml-file-requirement-to-draw-multiple-polygons
 * 3. J'ai lu qu'il faut mettre xmlns="http://www.opengis.net/kml/2.2" dans la balise <kml> mais
 * j'arrive pas et ça marche sans.
 * 4. Pour l'instant ça affiche tout le pays pas juste les frontières
 * 5. Pour l'instant il n'y a pas de pays multpoylgon
 *
 *
 *  */


import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;


import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args){

        //Creaton du json parser
        JSONParser jsonParser = new JSONParser();

        String filename = "countries.geojson";
        List<Country> country_list = new ArrayList<>();
        parsing_country_list(jsonParser, filename, country_list);

        // AFFICHAGE CLASSIQUE
        for(int i = 0; i < country_list.size(); i++){
            System.out.println(country_list.get(i));
        }

        try{
            creating_KML_file("test.kml", country_list);
        } catch (IOException e){
            e.getMessage();
        }


    }

    private static void parsing_country_list(JSONParser jsonParser, String filename, List<Country> country_list){

        try (FileReader reader = new FileReader(filename)){

            //Lecture du fichier
            JSONObject obj = (JSONObject) jsonParser.parse(reader);

            //Récupération de features qui correspond au tableau de pays
            JSONArray features = (JSONArray) obj.get("features");

            // Loop sur tous les pays dans "features" et on les met tous dans un tableau
            for (int i_features = 0; i_features < features.size(); i_features++){

                Country country =  new Country();
                List<ArrayList<String>> list_of_coordinates_list = new ArrayList<ArrayList<String>>();
                ArrayList<String> coordinates_list = new ArrayList<String>();

                // Chaque element du tableau de features est un pays
                JSONObject country_obj = (JSONObject) features.get(i_features);

                // On récupère la partie properties et des deux paramètres demandés.
                JSONObject properties = (JSONObject) country_obj.get("properties");
                String country_name = (String) properties.get("ADMIN");
                String country_abrv = (String) properties.get("ISO_A3");

                // On recupère les differentes coordonnées
                JSONObject geometry = (JSONObject) country_obj.get("geometry");
                JSONArray coordinates = (JSONArray) geometry.get("coordinates");
                String coordinates_type = (String) geometry.get("type");

                // On utilise le type de coordonnées pour pouvoir enregistrer 1 tableau de coordonnées
                //pour les type polygone et plusieurs tableaux de coordonnées pour les multipolygone
                int nbr_coordinates;
                if (coordinates_type.equals("Polygon")){

                    // On recupère le seul tableau de coordonnées et on regarde sa taille
                    JSONArray coordinates_arr = (JSONArray) coordinates.get(0);

                    //On crée la liste de coordinates reformulées.
                    reformulate_coordinates(coordinates_arr, coordinates_list);

                    //On crée le pays de type polygone
                    country = new Country_Polygon(country_name, country_abrv, coordinates_list);

                }else{ //multipolygone

                    // On loop sur tous les tableaux de coordonnées pour en faire un
                    //tableau du nombres de coordonnées par tableau
                    int nbr_arr = coordinates.size();
                    for(int i_coordinates = 0; i_coordinates < nbr_arr; i_coordinates++){
                        JSONArray arr = (JSONArray) coordinates.get(i_coordinates);
                        JSONArray arr2 = (JSONArray) arr.get(0);
                        coordinates_list = new ArrayList<String>();
                        reformulate_coordinates(arr2, coordinates_list);
                        list_of_coordinates_list.add(coordinates_list);
                    }

                    //Création du pays de type multipolygon
                    country = new Country_Multipolygon(country_name, country_abrv, list_of_coordinates_list);
                }

                country_list.add(country);
            }

        } catch (IOException | ParseException e){
            e.printStackTrace();
        }
    }

    private static void reformulate_coordinates(JSONArray arr, ArrayList<String> coordinates_list){
        for (int a = 0; a < arr.size(); a++){
            String coordinate = arr.get(a).toString();
            coordinate = coordinate.replace("[", "").replace("]", "");
            //coordinate = coordinate + ",10";
            coordinates_list.add(coordinate);
        }
    }

    private static void creating_KML_file(String filename, List<Country> country_list) throws IOException {

        Element kml = new Element("kml");
        Document document = new Document(kml);
        Element doc = new Element("Document");

        // xmlns="http://www.opengis.net/kml/2.2"
        //kml.setAttribute(new Attribute("xmlns", "http://www.opengis.net/kml/2.2"));

        for(Country country : country_list){

            Element placemark = new Element("Placemark");
            doc.addContent(placemark);
            placemark.addContent(new Element("name").setText(country.getName()));

            if(country.getClass() == Country_Polygon.class){
                Element polygon = new Element("Polygon");
                placemark.addContent(polygon);
                polygon.addContent(new Element("altitudeMode").setText("absolute"));
                Element outerBoundaryIs = new Element("outerBoundaryIs");
                polygon.addContent(outerBoundaryIs);
                Element linearRing = new Element("LinearRing");
                outerBoundaryIs.addContent(linearRing);
                Element coordinates = new Element("coordinates");
                linearRing.addContent(coordinates);

                StringBuilder str = new StringBuilder("\n\n");
                for(int i = 0; i < ((Country_Polygon) country).getCoordinates_list().size(); i++){
                    str.append(((Country_Polygon) country).getCoordinates_list().get(i));
                    str.append("\n\t\t\t\t");
                }
                str.append("\n");
                coordinates.setText(str.toString());

            }

        }

        document.getRootElement().addContent(doc);

        XMLOutputter xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(Format.getPrettyFormat());
        xmlOutputter.output(document, new FileWriter(filename));

    }

}
