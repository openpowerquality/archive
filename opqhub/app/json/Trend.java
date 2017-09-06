package json;


import java.util.ArrayList;

public class Trend extends JsonData {
    public Long id;
    public String description;
    public ArrayList<Double> points;

    public Trend() {
        super("trend");
        this.id = -1L;
        this.description = "";
        this.points = new ArrayList<>();
    }

    public Trend(Long id, String description, ArrayList<Double> points) {
        super("trend");
        this.id = id;
        this.description = description;
        this.points = points;
    }
}
