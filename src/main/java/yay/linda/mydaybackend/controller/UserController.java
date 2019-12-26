package yay.linda.mydaybackend.controller;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yay.linda.mydaybackend.model.LoginRequest;
import yay.linda.mydaybackend.model.RegisterRequest;
import yay.linda.mydaybackend.model.UserDTO;
import yay.linda.mydaybackend.service.UserService;

@Api(tags = "User Controller")
@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/{sessionToken}")
    public ResponseEntity<UserDTO> getUserFromSessionToken(@PathVariable("sessionToken") String sessionToken) {
        LOGGER.info("GET USER from sessionToken request: sessionToken={}", sessionToken);
        return ResponseEntity.ok(userService.getUserFromSessionToken(sessionToken));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(
            @RequestBody RegisterRequest registerRequest,
            @RequestParam(value = "isGuest", defaultValue = "false") Boolean isGuest) {
        LOGGER.info("REGISTER request: {}, isGuest: {}", registerRequest, isGuest);
        return new ResponseEntity(userService.register(registerRequest, isGuest), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequest loginRequest) {
        LOGGER.info("LOGIN request: {}", loginRequest);
        return ResponseEntity.ok(userService.login(loginRequest));
    }

    @GetMapping("/logout/{sessionToken}")
    public ResponseEntity<UserDTO> logout(@PathVariable("sessionToken") String sessionToken) {
        LOGGER.info("LOGOUT request: sessionToken={}", sessionToken);
        return ResponseEntity.ok(userService.logout(sessionToken));
    }

}
