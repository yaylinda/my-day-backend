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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yay.linda.mydaybackend.model.DayDTO;
import yay.linda.mydaybackend.model.StatsDTO;
import yay.linda.mydaybackend.service.DayService;
import yay.linda.mydaybackend.service.StatsService;
import yay.linda.mydaybackend.web.error.ErrorDTO;

import java.util.List;

import static yay.linda.mydaybackend.web.error.ErrorMessages.UNAUTHORIZED;
import static yay.linda.mydaybackend.web.error.ErrorMessages.UNEXPECTED_ERROR;

@Api(tags = "Stats Controller")
@RestController
@RequestMapping("/stats")
@CrossOrigin
public class StatsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsController.class);

    @Autowired
    private StatsService statsService;

    @GetMapping(value = "/{statsType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Computes Stats for a User, given a valid Session-Token")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully computed Stats"),
            @ApiResponse(code = 403, message = UNAUTHORIZED, response = ErrorDTO.class),
            @ApiResponse(code = 500, message = UNEXPECTED_ERROR, response = ErrorDTO.class)
    })
    public ResponseEntity<StatsDTO> getStats(
            @ApiParam(value = "statsType", required = true)
            @PathVariable(value = "statsType") String statsType,
            @ApiParam(value = "Session-Token", required = true)
            @RequestHeader("Session-Token") String sessionToken) {
        LOGGER.info("GET days request: statsType={}, sessionToken={}", statsType, sessionToken);
        return ResponseEntity.ok(statsService.getStats(statsType, sessionToken));
    }
}
