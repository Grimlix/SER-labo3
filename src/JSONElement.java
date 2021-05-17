import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONElement {
    private final JSONObject feature;

    public JSONElement(JSONObject _feature) {
        if (_feature == null)
            throw new IllegalArgumentException("Musst have a JSONObject element!");

        feature = _feature;
    }

    public String getName() {
        return (String) ((JSONObject) feature.get("properties")).get("ADMIN");
    }

    public String getAbbreviation() {
        return (String) ((JSONObject) feature.get("properties")).get("ISO_A3");
    }

    public boolean isMultiPolygon() {
        JSONObject geometry = (JSONObject) feature.get("geometry");
        String formType = (String) geometry.get("type");
        return formType.equals("MultiPolygon");
    }

    public boolean isPolygon() {
        JSONObject geometry = (JSONObject) feature.get("geometry");
        String formType = (String) geometry.get("type");
        return formType.equals("Polygon");
    }

    public JSONArray getCoordinates() {
        if (isPolygon()) {
            JSONObject geometry = (JSONObject) feature.get("geometry");
            return (JSONArray) ((JSONArray) geometry.get("coordinates")).get(0);
        }

        return null;
    }

    public JSONArray getPolygons() {
        if (isMultiPolygon()) {
            JSONObject geometry = (JSONObject) feature.get("geometry");
            return (JSONArray) geometry.get("coordinates");
        }
        return null;
    }
}
