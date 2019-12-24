package yay.linda.mydaybackend.model;

import lombok.Data;
import yay.linda.mydaybackend.entity.EntryRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DayEntryRecord {
    // copied from EntryRecord.class
    private EntryRecordType entryRecordType;
    private String name;
    private String color;

    // other fields
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
