package studiplayer.audio;

import studiplayer.basic.TagReader;

import java.util.Map;

public class TaggedFile extends SampledFile {

    private String album;

    public TaggedFile() {
        super();
    }

    public TaggedFile(String path) throws NotPlayableException {
        super(path);
        readAndStoreTags();
    }

    public String getAlbum() {
        return album;
    }

    public void readAndStoreTags() throws NotPlayableException {
        try {
            Map<String, Object> tagMap = TagReader.readTags(getPathname());
            for (String tag : tagMap.keySet()) {
                Object value = tagMap.get(tag);
                System.out.printf("key: %-25s value: %-30s (type: %s)\n",
                        tag, value, value.getClass().getSimpleName());
            }
            String fileName = getFilename();
            String title = (String) tagMap.getOrDefault("title", fileName.substring(0, fileName.lastIndexOf(".")));
            String author = (String) tagMap.getOrDefault("author", "");
            String album = (String) tagMap.getOrDefault("album", "");
            long duration = (long) tagMap.getOrDefault("duration", 0);

            this.album = album.trim();
            setTitle(title.trim());
            setAuthor(author.trim());
            setDuration(duration);
        } catch (Exception e) {
            throw new NotPlayableException(getPathname(), "Cannot read and store tags");
        }
    }

    @Override
    public String toString() {
        if (album.isEmpty())
            return super.toString() + " - " + formatDuration();
        return super.toString() + " - " + album + " - " + formatDuration();
    }
}
