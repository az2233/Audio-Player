package studiplayer.audio;

import studiplayer.basic.WavParamReader;

public class WavFile extends SampledFile {

    public WavFile() {
        super();
    }

    public WavFile(String path) throws NotPlayableException {
        super(path);
        readAndSetDurationFromFile();
    }

    public static long computeDuration(long numberOfFrames, float frameRate) {
        float durationInSeconds = numberOfFrames / frameRate;
        return (long) (durationInSeconds * 1_000_000);
    }

    public void readAndSetDurationFromFile() throws NotPlayableException {
        try {
            WavParamReader.readParams(getPathname());

            long numberOfFrames = WavParamReader.getNumberOfFrames();
            float frameRate = WavParamReader.getFrameRate();

            long duration = WavFile.computeDuration(numberOfFrames, frameRate);
            setDuration(duration);
        } catch (Exception e) {
            throw new NotPlayableException(getPathname(), "Cannot read and set duration");
        }
    }

    @Override
    public String toString() {
        return super.toString() + " - " + formatDuration();
    }

}
