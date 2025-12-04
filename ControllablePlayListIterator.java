package studiplayer.audio;

import java.util.*;

public class ControllablePlayListIterator implements Iterator<AudioFile> {

    private final List<AudioFile> playList;
    private int currentIndex = 0;

    public ControllablePlayListIterator(List<AudioFile> playList) {
        this(playList, null, SortCriterion.DEFAULT);
    }

    public ControllablePlayListIterator(List<AudioFile> playList, String search, SortCriterion sortCriterion) {
        this.playList = new ArrayList<>();

        // Apply filtering
        for (AudioFile audioFile : playList) {
            if (matchesSearch(audioFile, search)) {
                this.playList.add(audioFile);
            }
        }

        // Apply sorting
        if (sortCriterion != null && sortCriterion != SortCriterion.DEFAULT) {
            Comparator<AudioFile> comparator = getComparator(sortCriterion);
            if (comparator != null) {
                this.playList.sort(comparator);
            }
        }
    }

    private boolean matchesSearch(AudioFile audioFile, String search) {
        if (search == null || search.isEmpty()) {
            return true;
        }
        search = search.toLowerCase();
        boolean matches = false;

        if (audioFile.getAuthor() != null && audioFile.getAuthor().toLowerCase().contains(search)) {
            matches = true;
        } else if (audioFile.getTitle() != null && audioFile.getTitle().toLowerCase().contains(search)) {
            matches = true;
        } else if (audioFile instanceof TaggedFile) {
            TaggedFile taggedFile = (TaggedFile) audioFile;
            if (taggedFile.getAlbum() != null && taggedFile.getAlbum().toLowerCase().contains(search)) {
                matches = true;
            }
        }
        return matches;
    }

    private Comparator<AudioFile> getComparator(SortCriterion sortCriterion) {
        switch (sortCriterion) {
            case AUTHOR:
                return new AuthorComparator();
            case TITLE:
                return new TitleComparator();
            case ALBUM:
                return new AlbumComparator();
            case DURATION:
                return new DurationComparator();
            default:
                return null;
        }
    }

    public List<AudioFile> getPlayList() {
        return playList;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < playList.size();
    }

    @Override
    public AudioFile next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more elements in the iterator");
        }
        return playList.get(currentIndex++);
    }

    public AudioFile jumpToAudioFile(AudioFile file) {
        int index = playList.indexOf(file);
        if (index != -1) {
            currentIndex = index;
            return playList.get(currentIndex++);
        } else {
            return null;
        }
    }
}
