import java.util.ArrayList;
import java.util.List;

public class Country {

    private String name;
    private String abrv;
    private List<ArrayList<String>> list_of_coordinates_list;
    private String type;

    public Country(String name, String abrv, String type, List<ArrayList<String>> list_of_coordinates_list) {
        this.name = name;
        this.abrv = abrv;
        this.type = type;
        this.list_of_coordinates_list = list_of_coordinates_list;
    }

    public Country (){}

    public String getName() {
        return name;
    }
    public String getType(){
        return type;
    }

    public List<ArrayList<String>> getList_of_coordinates_list() {
        return list_of_coordinates_list;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("(").append(abrv).append(") ").append(name);
        for (ArrayList<String> strings : list_of_coordinates_list) {
            str.append("\n\t  - " + strings.size() + " coordinates");
        }
        return str.toString();
    }
}
