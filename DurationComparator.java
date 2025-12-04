package studiplayer.audio;

import java.util.Comparator;

public class DurationComparator implements Comparator<AudioFile> {
    @Override
    public int compare(AudioFile a, AudioFile b) {
        if (a == null || b == null)
            throw new NullPointerException("Arguments are null");

        long durationA = a instanceof SampledFile ? ((SampledFile) a).getDuration() : 0;
        long durationB = b instanceof SampledFile ? ((SampledFile) b).getDuration() : 0;

        return Long.compare(durationA, durationB);
    }
}
