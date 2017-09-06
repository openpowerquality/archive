package json;

import java.util.ArrayList;

/**
 * Created by anthony on 10/23/14.
 */
public class Trends extends JsonData {
    public ArrayList<Trend> trends;

    public Trends() {
        super("trends");
        this.trends = new ArrayList<>();
    }

    public void add(Trend trend) {
        this.trends.add(trend);
    }
}
