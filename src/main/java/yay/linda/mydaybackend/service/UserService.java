package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.Session;
import yay.linda.mydaybackend.entity.User;
import yay.linda.mydaybackend.model.LoginRequest;
import yay.linda.mydaybackend.model.RegisterRequest;
import yay.linda.mydaybackend.model.UserDTO;
import yay.linda.mydaybackend.repository.UserRepository;
import yay.linda.mydaybackend.web.error.NotFoundException;
import yay.linda.mydaybackend.web.error.RegisterException;
import yay.linda.mydaybackend.web.error.UsernamePasswordMismatchException;

import java.util.Optional;

import static yay.linda.mydaybackend.Constants.BCRYPT_LOG_ROUNDS;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionService sessionService;

    /**
     *
     * @param sessionToken
     * @return
     */
    public UserDTO getUserFromSessionToken(String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (!optionalUser.isPresent()) {
            throw NotFoundException.usernameNotFound(username);
        }

        return new UserDTO(optionalUser.get().getUsername(), sessionToken);
    }

    /**
     *
     * @param registerRequest
     * @param isGuest
     * @return
     */
    public UserDTO register(RegisterRequest registerRequest, Boolean isGuest) {
        if (usernameExists(registerRequest.getUsername())) {
            throw RegisterException.usernameTaken(registerRequest.getUsername());
        }

        createUser(registerRequest, isGuest);

        Session session = sessionService.createSession(registerRequest.getUsername());

        return new UserDTO(registerRequest.getUsername(), session.getSessionToken());
    }

    /**
     *
     * @param loginRequest
     * @return
     */
    public UserDTO login(LoginRequest loginRequest) {

        if (!usernameExists(loginRequest.getUsername())) {
            throw NotFoundException.usernameNotFound(loginRequest.getUsername());
        }

        if (!verifyPassword(loginRequest.getUsername(), loginRequest.getPassword())) {
            throw new UsernamePasswordMismatchException(loginRequest.getUsername());
        }

        Session session = sessionService.createSession(loginRequest.getUsername());

        return new UserDTO(loginRequest.getUsername(), session.getSessionToken());
    }

    /**
     *
     * @param sessionToken
     * @return
     */
    public UserDTO logout(String sessionToken) {
        String username = sessionService.deactivateSession(sessionToken);
        return new UserDTO(username, sessionToken);
    }

    /*-------------------------------------------------------------------------
        PRIVATE HELPER METHODS
     -------------------------------------------------------------------------*/

    private boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    private boolean verifyPassword(String username, String password) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> NotFoundException.usernameNotFound(username));
        return BCrypt.checkpw(password, user.getPassword());
    }

    private void createUser(RegisterRequest registerRequest, Boolean isGuest) {
        // TODO - do something with isGuest later
        LOGGER.info("Creating new User: {}", registerRequest);
        User user = new User(
                registerRequest.getUsername(),
                BCrypt.hashpw(registerRequest.getPassword(), BCrypt.gensalt(BCRYPT_LOG_ROUNDS)));
        userRepository.save(user);
    }
}
