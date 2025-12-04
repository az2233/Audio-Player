package studiplayer.audio;

public class AudioFileFactory {

    public static AudioFile createAudioFile(String path) throws NotPlayableException {
        String tempPath = path.toLowerCase();
        if (tempPath.endsWith("wav")) {
            return new WavFile(path);
        } else if (tempPath.endsWith("ogg") || tempPath.endsWith("mp3")) {
            return new TaggedFile(path);
        } else {
            throw new NotPlayableException(path, "Unknown suffix for studiplayer.audio.AudioFile \"" + path + "\"");
        }

    }

}
