package yay.linda.mydaybackend.web.error;

public class UsernamePasswordMismatchException extends RuntimeException {
    public UsernamePasswordMismatchException(String username) {
        super(String.format("Incorrect password for username='%s'.", username));
    }
}
