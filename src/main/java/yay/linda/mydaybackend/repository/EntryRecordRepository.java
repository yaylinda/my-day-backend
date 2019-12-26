package yay.linda.mydaybackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import yay.linda.mydaybackend.entity.EntryRecord;

public interface EntryRecordRepository extends MongoRepository<EntryRecord, String> {
}
