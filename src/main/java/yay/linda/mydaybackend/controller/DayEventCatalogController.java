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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yay.linda.mydaybackend.entity.DayEventCatalog;
import yay.linda.mydaybackend.model.DayDTO;
import yay.linda.mydaybackend.service.DayEventCatalogService;
import yay.linda.mydaybackend.service.DayService;
import yay.linda.mydaybackend.web.error.ErrorDTO;

import java.util.List;
import java.util.Map;

import static yay.linda.mydaybackend.web.error.ErrorMessages.UNAUTHORIZED;
import static yay.linda.mydaybackend.web.error.ErrorMessages.UNEXPECTED_ERROR;

@Api(tags = "Day Event Catalog Controller")
@RestController
@RequestMapping("/catalog/events")
@CrossOrigin
public class DayEventCatalogController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DayEventCatalogController.class);

    @Autowired
    private DayEventCatalogService dayEventCatalogService;

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Retrieve Day Event Catalogs, given a valid Session-Token")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved Day Event Catalogs"),
            @ApiResponse(code = 403, message = UNAUTHORIZED, response = ErrorDTO.class),
            @ApiResponse(code = 500, message = UNEXPECTED_ERROR, response = ErrorDTO.class)
    })
    public ResponseEntity<Map<String, List<DayEventCatalog>>> getCatalogs(
            @ApiParam(value = "Session-Token", required = true)
            @RequestHeader("Session-Token") String sessionToken) {
        LOGGER.info("GET Day Event Catalog request: sessionToken={}", sessionToken);
        return ResponseEntity.ok(dayEventCatalogService.getCatalogs(sessionToken));
    }

}
