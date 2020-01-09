package yay.linda.mydaybackend.model;

import lombok.Data;

@Data
public class StatsDTO {
    private ChartData day;
    private ChartData week;
    private ChartData month;
    private ChartData year;
}
