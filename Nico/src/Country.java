


public class Country {

    private String name;
    private String abrv;

    public Country(String name, String abrv) {
        this.name = name;
        this.abrv = abrv;
    }

    public Country (){}

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("(").append(abrv).append(") ").append(name);
        return ("(" + abrv + ") " + name);
    }
}
