import org.json.simple.JSONArray;

import java.util.List;

public class Country_Polygon extends Country {

    private List<String> coordinates_list;

    public Country_Polygon(String name, String abrv, List<String> coordinates_list) {
        super(name, abrv);
        this.coordinates_list = coordinates_list;
    }

    public List<String> getCoordinates_list() {
        return coordinates_list;
    }

    @Override
    public String toString(){
        return super.toString() + "\n\t  - " + coordinates_list.size() + " coordinates";
    }
}
