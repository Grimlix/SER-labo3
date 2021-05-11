



public class Country_Polygon extends Country {

    private int nb_coordinates;

    public Country_Polygon(String name, String abrv, int nb_coordinates) {
        super(name, abrv);
        this.nb_coordinates = nb_coordinates;
    }

    @Override
    public String toString(){
        return super.toString() + "\n\t  - " + nb_coordinates + " coordinates";
    }
}
