import java.util.ArrayList;
import java.util.List;

public class Country_Multipolygon extends Country {

    private List<Integer> nbr_coordinates_arr;

    public Country_Multipolygon(String name, String abrv, List<Integer> nbr_coordinates_arr) {
        super(name, abrv);
        this.nbr_coordinates_arr = nbr_coordinates_arr;
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(super.toString());
        for (int i = 0; i < nbr_coordinates_arr.size(); i++){
            str.append("\n\t  - " + nbr_coordinates_arr.get(i) + " coordinates");
        }
        return str.toString();
    }
}
