package yay.linda.mydaybackend.web.error;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException sessionTokenNotFound(String sessionToken) {
        return new NotFoundException(String.format("Session Token='%s' does not exist.", sessionToken));
    }

    public static NotFoundException usernameNotFound(String username) {
        return new NotFoundException(String.format("Username='%s' does not exist.", username));
    }

    public static NotFoundException gameNotFound(String gameId) {
        return new NotFoundException(String.format("Game with id='%s' does not exist.", gameId));
    }

    public static NotFoundException catalogEventNotFound(String eventType, String catalogEventId) {
        return new NotFoundException(String.format(
                "Catalog of eventType='%s' with catalogEventId='%s' does not exist.",
                eventType, catalogEventId));
    }
}
