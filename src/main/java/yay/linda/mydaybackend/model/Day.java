package yay.linda.mydaybackend.model;

import lombok.Data;

import java.util.List;

@Data
public class Day {
    private String date;
    private List<DayEntryRecord> entries;
}
