package studiplayer.audio;

public class NotPlayableException extends Exception {
    private String pathName;

    public NotPlayableException(String pathName, String message) {
        super(message);
        this.pathName = pathName;
    }

    public NotPlayableException(String pathName, Throwable t) {
        super(t);
        this.pathName = pathName;
    }

    public NotPlayableException(String pathName, String message, Throwable t) {
        super(message, t);
        this.pathName = pathName;
    }

    public String getPathName() {
        return pathName;
    }

    @Override
    public String toString() {
        return super.toString() + "; PathName: " + pathName;
    }
}
