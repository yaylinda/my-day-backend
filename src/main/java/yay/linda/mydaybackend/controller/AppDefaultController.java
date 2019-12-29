package yay.linda.mydaybackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yay.linda.mydaybackend.web.error.ErrorDTO;

import java.util.Map;

import static yay.linda.mydaybackend.web.error.ErrorMessages.UNEXPECTED_ERROR;

@Api(tags = "Application Default Controller")
@RestController
@RequestMapping("")
@CrossOrigin
public class AppDefaultController {

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Health check endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful health check"),
            @ApiResponse(code = 500, message = UNEXPECTED_ERROR, response = ErrorDTO.class)
    })
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "OK"));
    }
}
