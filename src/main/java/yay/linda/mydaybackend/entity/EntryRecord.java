package yay.linda.mydaybackend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import yay.linda.mydaybackend.model.EntryRecordType;

@Data
public class EntryRecord {
    @Id
    private String id;
    private EntryRecordType entryRecordType;
    private String name;
    private String color;
    private String icon;
    private String creator; // username of person who created it
}
