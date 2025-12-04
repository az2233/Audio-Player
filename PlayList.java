package studiplayer.audio;

import java.io.*;
import java.util.*;

public class PlayList implements Iterable<AudioFile> {

    private final List<AudioFile> playList = new LinkedList<>();
    private int current = 0;
    private String search;
    private SortCriterion sortCriterion = SortCriterion.DEFAULT;

    public PlayList() {
    }

    public PlayList(String m3uPathName) {
        try {
            loadFromM3U(m3uPathName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void add(AudioFile file) {
        playList.add(file);
    }

    public void remove(AudioFile file) {
        playList.remove(file);
    }

    public int size() {
        return playList.size();
    }

    public AudioFile currentAudioFile() {
        if (playList.isEmpty())
            return null;
        List<AudioFile> filteredSorted = new ControllablePlayListIterator(playList, search, sortCriterion).getPlayList();
        return filteredSorted.isEmpty() ? null : filteredSorted.get(current % filteredSorted.size());
    }

    public void nextSong() {
        if (playList.isEmpty()) {
            current = 0;
        } else {
            List<AudioFile> filteredSorted = new ControllablePlayListIterator(playList, search, sortCriterion).getPlayList();
            current = (current + 1) % filteredSorted.size();
        }
    }

    public void loadFromM3U(String m3uPathName) throws IOException {

        File file = new File(m3uPathName);
        if (!file.exists()) {
            throw new IOException("PlayList file does not exist: " + m3uPathName);
        }

        try (BufferedReader playListReader = new BufferedReader(new FileReader(file))) {
            playList.clear();
            current = 0;
            String line;
            while ((line = playListReader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty())
                    continue;
                try {
                    AudioFile newFile = AudioFileFactory.createAudioFile(line);
                    playList.add(newFile);
                } catch (NotPlayableException e) {
                    System.err.println("Cannot load audio file: " + line + " - " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
        } catch (IOException ex) {
            throw new IOException("Error reading M3U file: " + ex.getMessage(), ex);
        }
    }

    public void saveAsM3U(String pathName) {
        try (BufferedWriter playListWriter = new BufferedWriter(new FileWriter(pathName))) {
            for (AudioFile audioFile : playList) {
                playListWriter.write(audioFile.getPathname() + "\n");
            }
            playListWriter.flush();
        } catch (IOException e) {
            System.out.println("Unable to write file !");
        }
    }

    public List<AudioFile> getList() {
        return new ArrayList<>(playList); // Return a copy to prevent external modifications
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 0 && current < playList.size()) {
            this.current = current;
        }
    }

    public SortCriterion getSortCriterion() {
        return sortCriterion;
    }

    public void setSortCriterion(SortCriterion sort) {
        this.sortCriterion = sort != null ? sort : SortCriterion.DEFAULT;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search.trim();
        updateCurrentIndexForFilteredView();
    }

    private void updateCurrentIndexForFilteredView() {
        List<AudioFile> filteredList = new ControllablePlayListIterator(playList, search, sortCriterion).getPlayList();
        ;
        if (!filteredList.isEmpty() && !playList.isEmpty()) {
            AudioFile currentFile = playList.get(current % playList.size());
            current = filteredList.indexOf(currentFile);
            if (current == -1) {
                current = 0;
            }
        } else {
            current = 0;
        }
    }

    public Iterator<AudioFile> iterator() {
        return new ControllablePlayListIterator(playList, search, sortCriterion);
    }

    public void jumpToAudioFile(AudioFile file) {
        List<AudioFile> filteredSorted = new ControllablePlayListIterator(playList, search, sortCriterion).getPlayList();
        int index = filteredSorted.indexOf(file);
        if (index != -1) {
            current = index;
        }
    }

    @Override
    public String toString() {
        return playList.toString();
    }
}
