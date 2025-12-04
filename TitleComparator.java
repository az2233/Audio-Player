package studiplayer.audio;

import java.util.Comparator;

public class TitleComparator implements Comparator<AudioFile> {
    @Override
    public int compare(AudioFile a, AudioFile b) {
        if (a == null || b == null)
            throw new NullPointerException("Arguments are null");

        if (a.getTitle() == null || b.getTitle() == null)
            throw new NullPointerException("Album must not be null");
        return a.getTitle().compareTo(b.getTitle());
    }
}