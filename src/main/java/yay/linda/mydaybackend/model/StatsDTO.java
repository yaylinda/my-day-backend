package yay.linda.mydaybackend.model;

import lombok.Data;

import java.util.Map;

@Data
public class StatsDTO {
    private ChartData day;
    private ChartData week;
    private ChartData month;
    private ChartData year;
    private Map<String, ChartData> summary;
}
