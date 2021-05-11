
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
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
                    nbr_coordinates= nbr_coordinates_in_array(coordinates);

                    //On crée le pays de type polygone
                    country = new Country_Polygon(country_name, country_abrv, nbr_coordinates);

                }else{ //multipolygone

                    // On loop sur tous les tableaux de coordonnées pour en faire un
                    //tableau du nombres de coordonnées par tableau
                    int nbr_arr = coordinates.size();
                    List<Integer> nbr_coordinates_arr = new ArrayList<>();
                    for(int i_coordinates = 0; i_coordinates < nbr_arr; i_coordinates++){
                        JSONArray arr = (JSONArray) coordinates.get(i_coordinates);
                        nbr_coordinates = nbr_coordinates_in_array(arr);
                        nbr_coordinates_arr.add(nbr_coordinates);
                    }

                    //Création du pays de type multipolygon
                    country = new Country_Multipolygon(country_name, country_abrv, nbr_coordinates_arr);
                }

                country_list.add(country);
            }

        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } catch (ParseException e){
            e.printStackTrace();
        }
    }

    private static int nbr_coordinates_in_array(JSONArray coordinates){
        JSONArray coordinates_arr = (JSONArray) coordinates.get(0);
        return coordinates_arr.size();
    }

}


/*
* Certains ISO_A3 valent "-99" je sais pas trop pourquoi.
*
*
*
*
*  */