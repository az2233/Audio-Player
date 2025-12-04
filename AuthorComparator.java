package studiplayer.audio;

import java.util.Comparator;

public class AuthorComparator implements Comparator<AudioFile> {
    @Override
    public int compare(AudioFile a, AudioFile b) {
        if (a == null || b == null)
            throw new NullPointerException("Arguments are null");

        if (a.getAuthor() == null || b.getAuthor() == null)
            throw new NullPointerException("Author must not be null");
        return a.getAuthor().compareTo(b.getAuthor());
    }
}
