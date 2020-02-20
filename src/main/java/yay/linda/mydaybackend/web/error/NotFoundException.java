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

    public static NotFoundException dayNotFound(String dayId, String username) {
        return new NotFoundException(String.format("Day with dayId=%s does not exist for user %s", dayId, username));
    }

    public static NotFoundException catalogEventNotFound(String eventType, String catalogEventId) {
        return new NotFoundException(String.format(
                "Catalog of eventType='%s' with catalogEventId='%s' does not exist.",
                eventType, catalogEventId));
    }

    public static NotFoundException dayEventNotFound(String dayId, String eventType, String dayEventId, String username) {
        return new NotFoundException(String.format(
                "Day with id='%s' does not have an event of type='%s' with dayEventId='%s', for user '%s'",
                dayId, eventType, dayEventId, username));
    }
}
