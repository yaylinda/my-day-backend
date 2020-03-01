package yay.linda.mydaybackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yay.linda.mydaybackend.entity.CatalogEvent;
import yay.linda.mydaybackend.service.CatalogEventService;
import yay.linda.mydaybackend.web.error.ErrorDTO;

import java.util.List;
import java.util.Map;

import static yay.linda.mydaybackend.web.error.ErrorMessages.UNAUTHORIZED;
import static yay.linda.mydaybackend.web.error.ErrorMessages.UNEXPECTED_ERROR;

@Api(tags = "Day Event Catalog Controller")
@RestController
@RequestMapping("/catalog/events")
@CrossOrigin
public class CatalogEventController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogEventController.class);

    @Autowired
    private CatalogEventService catalogEventService;

    @GetMapping(
            value = "",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Retrieve Day Event Catalogs, given a valid Session-Token")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved Day Event Catalogs"),
            @ApiResponse(code = 403, message = UNAUTHORIZED, response = ErrorDTO.class),
            @ApiResponse(code = 500, message = UNEXPECTED_ERROR, response = ErrorDTO.class)
    })
    public ResponseEntity<Map<String, List<CatalogEvent>>> getCatalogEvents(
            @ApiParam(value = "Session-Token", required = true)
            @RequestHeader("Session-Token") String sessionToken) {
        LOGGER.info("GET Day Event Catalog request: sessionToken={}", sessionToken);
        return ResponseEntity.ok(catalogEventService.getCatalogEvents(sessionToken));
    }

    @PostMapping(
            value = "/{eventType}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add a Day Event to the Catalog, given a valid Session-Token")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully added a Day Event to the Catalog"),
            @ApiResponse(code = 403, message = UNAUTHORIZED, response = ErrorDTO.class),
            @ApiResponse(code = 500, message = UNEXPECTED_ERROR, response = ErrorDTO.class)
    })
    public ResponseEntity<List<CatalogEvent>> addCatalog(
            @ApiParam(value = "Session-Token", required = true)
            @RequestHeader("Session-Token") String sessionToken,
            @ApiParam(value = "eventType", required = true)
            @PathVariable(value = "eventType") String eventType,
            @ApiParam(value = "catalogEvent", required = true)
            @RequestBody CatalogEvent catalogEvent) {
        LOGGER.info("POST Day Event Catalog request: eventType={}, catalogEvent={}, sessionToken={}",
                eventType, catalogEvent, sessionToken);
        return ResponseEntity.ok(catalogEventService.addCatalogEvent(eventType, catalogEvent, sessionToken));
    }

    @PutMapping(
            value = "/{eventType}/catalogEventId/{catalogEventId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add a Day Event to the Catalog, given a valid Session-Token")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated a Day Event in the Catalog"),
            @ApiResponse(code = 403, message = UNAUTHORIZED, response = ErrorDTO.class),
            @ApiResponse(code = 500, message = UNEXPECTED_ERROR, response = ErrorDTO.class)
    })
    public ResponseEntity<List<CatalogEvent>> updateCatalog(
            @ApiParam(value = "Session-Token", required = true)
            @RequestHeader("Session-Token") String sessionToken,
            @ApiParam(value = "eventType", required = true)
            @PathVariable(value = "eventType") String eventType,
            @ApiParam(value = "catalogEventId", required = true)
            @PathVariable(value = "catalogEventId") String catalogEventId,
            @ApiParam(value = "catalogEvent", required = true)
            @RequestBody CatalogEvent catalogEvent) {
        LOGGER.info("POST Day Event Catalog request: eventType={}, catalogEventId={}, catalogEvent={}, sessionToken={}",
                eventType, catalogEventId, catalogEvent, sessionToken);
        return ResponseEntity.ok(catalogEventService.updateCatalogEvent(eventType, catalogEventId, catalogEvent, sessionToken));
    }

    @DeleteMapping(
            value = "/{eventType}/catalogEventId/{catalogEventId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a Day Event from the Catalog, given a valid Session-Token")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully deleted a Day Event from the Catalog"),
            @ApiResponse(code = 403, message = UNAUTHORIZED, response = ErrorDTO.class),
            @ApiResponse(code = 500, message = UNEXPECTED_ERROR, response = ErrorDTO.class)
    })
    public ResponseEntity<List<CatalogEvent>> deleteCatalog(
            @ApiParam(value = "Session-Token", required = true)
            @RequestHeader("Session-Token") String sessionToken,
            @ApiParam(value = "eventType", required = true)
            @PathVariable(value = "eventType") String eventType,
            @ApiParam(value = "catalogEventId", required = true)
            @PathVariable(value = "catalogEventId") String catalogEventId) {
        LOGGER.info("POST Day Event Catalog request: eventType={}, catalogEventId={}, sessionToken={}",
                eventType, catalogEventId, sessionToken);
        return ResponseEntity.ok(catalogEventService.deleteCatalogEvent(eventType, catalogEventId, sessionToken));
    }
}
