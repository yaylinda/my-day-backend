package yay.linda.mydaybackend.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ChartData {

    private List<String> labels;
    private Map<String, Object> labelsDataMap;
    private List<String> legend; // used to label the colors in a stacked bar chart (for activities)

    public static ChartData dayChartData() {
        ChartData chartData = new ChartData();
        chartData.setLabels(Arrays.asList(
                "12 AM", "01 AM", "02 AM", "03 AM", "04 AM", "05 AM", "06 AM", "07 AM", "08 AM", "09 AM", "10 AM", "11 AM",
                "12 PM", "01 PM", "02 PM", "03 PM", "04 PM", "05 PM", "06 PM", "07 PM", "08 PM", "09 PM", "10 PM", "11 PM"));
        chartData.setLabelsDataMap(new HashMap<>());
        chartData.setLegend(new ArrayList<>());
        return chartData;
    }
}
