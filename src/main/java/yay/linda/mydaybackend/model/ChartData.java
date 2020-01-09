package yay.linda.mydaybackend.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static yay.linda.mydaybackend.Constants.HOUR_ORDER;
import static yay.linda.mydaybackend.Constants.MONTHS_ORDER;
import static yay.linda.mydaybackend.Constants.WEEKDAYS_ORDER;
import static yay.linda.mydaybackend.Constants.WEEK_NUM_ORDER;

@Data
public class ChartData {

    private List<String> labels;
    private Map<String, Object> labelsDataMap;
    private List<String> legend; // used to label the colors in a stacked bar chart (for activities)

    public ChartData() {
        this.setLabelsDataMap(new HashMap<>());
        this.setLegend(new ArrayList<>());
    }


    public static ChartData dayChartData() {
        ChartData chartData = new ChartData();
        chartData.setLabels(HOUR_ORDER);
        return chartData;
    }

    public static ChartData weekChartData() {
        ChartData chartData = new ChartData();
        chartData.setLabels(WEEKDAYS_ORDER);
        return chartData;
    }

    public static ChartData monthChartData() {
        ChartData chartData = new ChartData();
        chartData.setLabels(WEEK_NUM_ORDER);
        return chartData;
    }

    public static ChartData yearChartData() {
        ChartData chartData = new ChartData();
        chartData.setLabels(MONTHS_ORDER);
        return chartData;

    }
}
