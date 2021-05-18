/* Question/TODO:
 * 1. Certains ISO_A3 valent "-99" je sais pas trop pourquoi.
 * 2. J'ai lu qu'il faut mettre xmlns="http://www.opengis.net/kml/2.2" dans la balise <kml> mais
 * j'arrive pas et ça marche sans.
 * 3. Certains pays s'affichent pas bien je sais pas si c'est normal
 * 4. Je n'arrive pas a faire un beau formattage des coordonnées.
 *  */

/* REFERENCES
*
* https://www.sigterritoires.fr/index.php/kml-pour-bien-commencer/
* https://gis.stackexchange.com/questions/260211/what-are-the-minimum-kml-file-requirement-to-draw-multiple-polygons
* http://learningzone.rspsoc.org.uk/index.php/Learning-Materials/Introduction-to-OGC-Standards/9.2-Example-of-KML-file-types#9.2.4
* https://www.i-programmer.info/projects/131-mapping-a-gis/1276-inside-the-kml-placemark.html?start=1
*
*  */

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
        List<Country> country_list = parsing_country_list(jsonParser, filename);

        // AFFICHAGE CLASSIQUE
        for(int i = 0; i < country_list.size(); i++){
            System.out.println(country_list.get(i));
        }

        try{
            creating_KML_file("countries.kml", country_list);
        } catch (IOException e){
            e.getMessage();
        }

    }

    /**
     * Cette fonction lit un fichier geojson et en retourne une liste de pays
     * @param jsonParser : le parser
     * @param filename : le nom du fichier geojson à lire
     * @exception IOException : si on n'arrive pas à écrire ou fermer le nouveau fichier (FileReader)
     * @exception ParseException : si il y a un problème avec le parser
     * @return : une liste de tous les pays lu dans le fichier geojson
     */
    private static List<Country> parsing_country_list(JSONParser jsonParser, String filename){

        List<Country> country_list = new ArrayList<>();

        try (FileReader reader = new FileReader(filename)){

            //Lecture du fichier
            JSONObject obj = (JSONObject) jsonParser.parse(reader);

            //Récupération de features qui correspond au tableau de pays
            JSONArray features = (JSONArray) obj.get("features");

            // Loop sur tous les pays dans "features" et on les met tous dans un tableau
            for (int i_features = 0; i_features < features.size(); i_features++){

                // On prépare le pays, la liste de liste de coordonnées (en cas de multipolygone)
                // et la liste de coordonnées de base
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
                if (coordinates_type.equals("Polygon")){

                    // On recupère le seul tableau de coordonnées et on regarde sa taille
                    JSONArray coordinates_arr = (JSONArray) coordinates.get(0);

                    //On crée la liste de coordinates reformulées.
                    coordinates_list = reformulate_coordinates(coordinates_arr);
                    list_of_coordinates_list.add(coordinates_list);

                }else{ //multipolygone

                    // On loop sur tous les tableaux de coordonnées pour en faire un
                    //tableau du nombres de coordonnées par tableau
                    int nbr_arr = coordinates.size();
                    for(int i_coordinates = 0; i_coordinates < nbr_arr; i_coordinates++){
                        JSONArray arr = (JSONArray) coordinates.get(i_coordinates);
                        JSONArray arr2 = (JSONArray) arr.get(0);
                        coordinates_list = reformulate_coordinates(arr2);
                        list_of_coordinates_list.add(coordinates_list);
                    }

                }
                country = new Country(country_name, country_abrv, coordinates_type, list_of_coordinates_list);
                country_list.add(country);
            }

        } catch (IOException | ParseException e){
            e.printStackTrace();
        }

        return country_list;
    }

    /**
     * Cette methode enlève les ][ dans les coordonnées d'un tableau pour le fichier kml puisse les lire
     * @param arr : le tableau contenant les coordonnées
     * @return : une liste de toutes les coordonnées reformulée
     */
    private static ArrayList<String> reformulate_coordinates(JSONArray arr){

        ArrayList<String> coordinates_list = new ArrayList<>();

        for (Object o : arr) {
            String coordinate = o.toString();
            coordinate = coordinate.replace("[", "").replace("]", "");
            coordinates_list.add(coordinate);
        }
        return coordinates_list;
    }

    /**
     * Cette methode permet la création d'un fichier kml
     * @param filename : le nom du fichier kml
     * @param country_list : la lise des pays qu'il faut mettre dans le fichier kml
     * @exception IOException : pour FileWriter
     */
    private static void creating_KML_file(String filename, List<Country> country_list) throws IOException {

        Element kml = new Element("kml");
        Document document = new Document(kml);
        Element doc = new Element("Document"); // KML doit avoir qu'un seul element en dessous de la racine

        Element coordinates = null;
        Element placemark = null;
        Element parent_element = null;

        for(Country country : country_list){

            // Elements de base (Placemark, nom_pays)
            placemark = new Element("Placemark");
            doc.addContent(placemark);
            placemark.addContent(new Element("name").setText(country.getName()));

            // Dans le cas d'un pays multipolygon il faut rajouter la balise
            //<MultiGeometry> pour que le fichier sache qu'il y a plusieurs polygons.
            if(country.getType().equals("MultiPolygon")){
                parent_element = new Element("MultiGeometry");
                placemark.addContent(parent_element);
            }else{
                parent_element = placemark;
            }

            // On y met toutes les balises nécessaires et les coordonnées.
            for(int i = 0; i < country.getList_of_coordinates_list().size(); i++){
                // L'element parent correspond à <Placemark> si polygon et <MultiGeomerty> si multipolygon
                coordinates = genereate_kml_linearRing(parent_element);
                StringBuilder all_coordinates = generate_all_coordinates(country.getList_of_coordinates_list().get(i));
                coordinates.setText(all_coordinates.toString());
            }

        }

        document.getRootElement().addContent(doc);

        XMLOutputter xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(Format.getPrettyFormat());
        xmlOutputter.output(document, new FileWriter(filename));

    }

    /**
     * Cette methode créer un grand string de toutes les coordonnées.
     * @param list : la liste des coordonnées
     * @return : un stringbuilder de toutes les coordonnées prête à être écrite dans le fichier kml
     */
    private static StringBuilder generate_all_coordinates(List<String> list){
        StringBuilder str = new StringBuilder();
        for (String coordinates : list) {
            str.append(coordinates);
            str.append(" ");
        }
        return str;
    }

    /**
     * Cette methode génère les balises kml <linearRing></linearRing> et <coordinates></coordinates>.
     * @param parent_elem : l'élément parent sur lequel il faut coller l'élément linearRing
     * @return : l'élément JSON coordinates dans lequel on va y mettre les coordonnées.
     */
    private static Element genereate_kml_linearRing(Element parent_elem){
        Element linearRing = new Element("LinearRing");
        parent_elem.addContent(linearRing);
        Element coordinates = new Element("coordinates");
        linearRing.addContent(coordinates);

        return coordinates;
    }

}
