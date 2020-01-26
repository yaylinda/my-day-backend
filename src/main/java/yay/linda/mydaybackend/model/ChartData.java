package yay.linda.mydaybackend.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ChartData<T> {

    private List<String> labels;
    private Map<String, T> labelsDataMap;
    private List<String> legend; // used to label the colors in a stacked bar chart (for activities)

    public ChartData() {
        this.labels = new ArrayList<>();
        this.labelsDataMap = new HashMap<>();
        this.legend = new ArrayList<>();
    }

    public ChartData(List<String> labels) {
        this.labels = labels;
        this.labelsDataMap = new HashMap<>();
        this.legend = new ArrayList<>();
    }

    public void setLabelsFromDataMap() {
        this.labels = new ArrayList<>(this.labelsDataMap.keySet());
    }
}
