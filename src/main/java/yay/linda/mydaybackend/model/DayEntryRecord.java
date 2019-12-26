package yay.linda.mydaybackend.model;

import lombok.Data;
import yay.linda.mydaybackend.entity.EntryRecord;

import java.time.LocalDateTime;

@Data
public class DayEntryRecord extends EntryRecord {
    // other fields
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
