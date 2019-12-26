package yay.linda.mydaybackend.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Days Controller")
@RestController
@RequestMapping("/days")
@CrossOrigin
public class DayController {
}
