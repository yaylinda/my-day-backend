package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.DayEventCatalog;
import yay.linda.mydaybackend.model.EventType;
import yay.linda.mydaybackend.repository.DayEventCatalogRepository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DayEventCatalogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DayEventCatalogService.class);

    @Autowired
    private SessionService sessionService;

    @Autowired
    private DayEventCatalogRepository dayEventCatalogRepository;

    public Map<String, List<DayEventCatalog>> getCatalogs(String sessionToken) {

        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        Map<String, List<DayEventCatalog>> catalogs = new HashMap<>();

        Arrays.stream(EventType.values()).forEach(t -> {
            List<DayEventCatalog> list = dayEventCatalogRepository.findByBelongsToAndType(username, t);
            LOGGER.info("Found {} DayEvents for {} of type {}", list.size(), username, t);
            catalogs.put(t.name(), list);
        });

        return catalogs;
    }

}
