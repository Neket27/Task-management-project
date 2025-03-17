package app.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(Class clazz, Long id) {
        super(clazz.getSimpleName() + " with id " + id + " not found");
    }
}
