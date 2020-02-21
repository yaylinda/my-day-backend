package yay.linda.mydaybackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yay.linda.mydaybackend.model.DayDTO;
import yay.linda.mydaybackend.model.DayEventDTO;
import yay.linda.mydaybackend.service.DayService;
import yay.linda.mydaybackend.web.error.ErrorDTO;

import java.util.List;

import static yay.linda.mydaybackend.web.error.ErrorMessages.NOT_FOUND;
import static yay.linda.mydaybackend.web.error.ErrorMessages.UNAUTHORIZED;
import static yay.linda.mydaybackend.web.error.ErrorMessages.UNEXPECTED_ERROR;

@Api(tags = "Days Controller")
@RestController
@RequestMapping("/days")
@CrossOrigin
public class DayController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DayController.class);

    @Autowired
    private DayService dayService;

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Retrieve all Days for a User, given a valid Session-Token")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved Days"),
            @ApiResponse(code = 403, message = UNAUTHORIZED, response = ErrorDTO.class),
            @ApiResponse(code = 500, message = UNEXPECTED_ERROR, response = ErrorDTO.class)
    })
    public ResponseEntity<List<DayDTO>> getDays(
            @ApiParam(value = "Session-Token", required = true)
            @RequestHeader("Session-Token") String sessionToken,
            @ApiParam(value = "Timezone", required = true, defaultValue = "UTC")
            @RequestHeader(value = "Timezone", defaultValue = "UTC") String timezone) {
        LOGGER.info("GET days request: timezone={}, sessionToken={}", timezone, sessionToken);
        return ResponseEntity.ok(dayService.getDays(timezone, sessionToken));
    }


    @PutMapping(
            value = "/{dayId}/events/{eventType}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Successfully added a DayEvent to a Day given a valid dayId and Session-Token")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated Day"),
            @ApiResponse(code = 403, message = UNAUTHORIZED, response = ErrorDTO.class),
            @ApiResponse(code = 404, message = NOT_FOUND, response = ErrorDTO.class),
            @ApiResponse(code = 500, message = UNEXPECTED_ERROR, response = ErrorDTO.class)
    })
    public ResponseEntity<DayDTO> updateDayAddEvent(
            @ApiParam(value = "dayId", required = true)
            @PathVariable(value="dayId") String dayId,
            @ApiParam(value = "eventType", required = true)
            @PathVariable(value="eventType") String eventType,
            @ApiParam(value = "dayEvent", required = true)
            @RequestBody DayEventDTO dayEvent,
            @ApiParam(value = "Timezone", required = true, defaultValue = "UTC")
            @RequestHeader(value = "Timezone", defaultValue = "UTC") String timezone,
            @ApiParam(value = "Session-Token", required = true)
            @RequestHeader("Session-Token") String sessionToken) {
        LOGGER.info("PUT day request: dayId={}, eventType={}, dayEvent={}, timezone={}, sessionToken={}",
                dayId, eventType, dayEvent, timezone, sessionToken);
        return new ResponseEntity<>(dayService.updateDayAddEvent(dayId, eventType, dayEvent, timezone, sessionToken), HttpStatus.OK);
    }

    @PutMapping(
            value = "/{dayId}/events/{eventType}/dayEventId/{dayEventId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Successfully added a DayEvent to a Day given a valid dayId and Session-Token")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated Day"),
            @ApiResponse(code = 403, message = UNAUTHORIZED, response = ErrorDTO.class),
            @ApiResponse(code = 404, message = NOT_FOUND, response = ErrorDTO.class),
            @ApiResponse(code = 500, message = UNEXPECTED_ERROR, response = ErrorDTO.class)
    })
    public ResponseEntity<DayDTO> updateDayUpdateEvent(
            @ApiParam(value = "dayId", required = true)
            @PathVariable(value="dayId") String dayId,
            @ApiParam(value = "eventType", required = true)
            @PathVariable(value="eventType") String eventType,
            @ApiParam(value = "dayEventId", required = true)
            @PathVariable(value="dayEventId") String dayEventId,
            @ApiParam(value = "dayEvent", required = true)
            @RequestBody DayEventDTO dayEvent,
            @ApiParam(value = "Session-Token", required = true)
            @RequestHeader("Session-Token") String sessionToken) {
        LOGGER.info("PUT day request: dayId={}, eventType={}, dayEvent={}, sessionToken={}",
                dayId, eventType, dayEvent, sessionToken);
        return new ResponseEntity<>(dayService.updateDayUpdateEvent(dayId, eventType, dayEventId, dayEvent, sessionToken), HttpStatus.OK);
    }

    @DeleteMapping(
            value = "/{dayId}/events/{eventType}/dayEventId/{dayEventId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Successfully deleted a DayEvent from a Day given a valid dayId and Session-Token")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully deleted event from Day"),
            @ApiResponse(code = 403, message = UNAUTHORIZED, response = ErrorDTO.class),
            @ApiResponse(code = 404, message = NOT_FOUND, response = ErrorDTO.class),
            @ApiResponse(code = 500, message = UNEXPECTED_ERROR, response = ErrorDTO.class)
    })
    public ResponseEntity<DayDTO> updateDayDeleteEvent(
            @ApiParam(value = "dayId", required = true)
            @PathVariable(value="dayId") String dayId,
            @ApiParam(value = "eventType", required = true)
            @PathVariable(value="eventType") String eventType,
            @ApiParam(value = "dayEventId", required = true)
            @PathVariable(value="dayEventId") String dayEventId,
            @ApiParam(value = "Session-Token", required = true)
            @RequestHeader("Session-Token") String sessionToken) {
        LOGGER.info("PUT day request: dayId={}, eventType={}, dayEventId={}, sessionToken={}",
                dayId, eventType, dayEventId, sessionToken);
        return new ResponseEntity<>(dayService.updateDayDeleteEvent(dayId, eventType, dayEventId, sessionToken), HttpStatus.OK);
    }
}
