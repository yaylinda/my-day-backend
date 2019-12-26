package yay.linda.mydaybackend.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Default Controller")
@RestController
@RequestMapping("")
@CrossOrigin
public class DayEventCatalogController {
}
