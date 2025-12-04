package studiplayer.audio;

import java.util.Comparator;

public class AlbumComparator implements Comparator<AudioFile> {
    @Override
    public int compare(AudioFile a1, AudioFile a2) {
        if (a1 == null || a2 == null) {
            throw new IllegalArgumentException("Cannot compare null AudioFile objects.");
        }

        boolean isTagged1 = a1 instanceof TaggedFile;
        boolean isTagged2 = a2 instanceof TaggedFile;

        if (!isTagged1 && !isTagged2) {
            return 0;
        }

        if (!isTagged1) {
            return -1;
        }
        if (!isTagged2) {
            return 1;
        }

        String album1 = ((TaggedFile) a1).getAlbum();
        String album2 = ((TaggedFile) a2).getAlbum();

        album1 = album1 != null ? album1 : "";
        album2 = album2 != null ? album2 : "";

        boolean isEmptyAlbum1 = album1.isEmpty();
        boolean isEmptyAlbum2 = album2.isEmpty();

        if (isEmptyAlbum1 && isEmptyAlbum2) {
            return 0;
        } else if (isEmptyAlbum1) {
            return -1;
        } else if (isEmptyAlbum2) {
            return 1;
        } else {
            int albumComparison = album1.compareTo(album2);
            if (albumComparison != 0) {
                return albumComparison;
            }
            String title1 = a1.getTitle() != null ? a1.getTitle() : "";
            String title2 = a2.getTitle() != null ? a2.getTitle() : "";
            return title1.compareTo(title2);
        }
    }
}
