import java.util.ArrayList;
import java.util.List;

public class Country_Multipolygon extends Country {

    private List<ArrayList<String>> list_of_coordinates_list;

    public Country_Multipolygon(String name, String abrv, List<ArrayList<String>> list_of_coordinates_list) {
        super(name, abrv);
        this.list_of_coordinates_list = list_of_coordinates_list;
    }

    public List<ArrayList<String>> getList_of_coordinates_list() {
        return list_of_coordinates_list;
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(super.toString());
        for (int i = 0; i < list_of_coordinates_list.size(); i++){
            str.append("\n\t  - " + list_of_coordinates_list.get(i).size() + " coordinates");
        }
        return str.toString();
    }
}
