package yay.linda.mydaybackend.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Day Event Catalog Controller")
@RestController
@RequestMapping("/day-event-catalog")
@CrossOrigin
public class DayEventCatalogController {
}
