package yay.linda.mydaybackend.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class StatsDTO {
    private Map<String, ChartData> score;
    private Map<String, ChartData> activity;
    private Map<String, ChartData> prompt;
    private Map<String, ChartData> summary;
}
