package studiplayer.audio;

import studiplayer.basic.BasicPlayer;

public abstract class SampledFile extends AudioFile {

    private long duration = 0;

    public SampledFile() {
        super();
    }

    public SampledFile(String path) throws NotPlayableException {
        super(path);
    }

    public static String timeFormatter(long timeInMicroSeconds) {
        if (timeInMicroSeconds < 0)
            throw new RuntimeException("Time should be a non-negative value");

        if (timeInMicroSeconds >= 6000000000L)
            throw new RuntimeException("Time value overflows format");

        long timeInSeconds = timeInMicroSeconds / 1_000_000;

        long mins = timeInSeconds / 60;
        long secs = timeInSeconds % 60;

        String minutes = (mins < 10) ? "0" + mins : String.valueOf(mins);
        String seconds = (secs < 10) ? "0" + secs : String.valueOf(secs);

        return String.format("%s:%s", minutes, seconds);
    }

    @Override
    public void play() throws NotPlayableException {
        try {
            BasicPlayer.play(getPathname());
        } catch (Exception e) {
            throw new NotPlayableException(getPathname(), "Audio file cannot play");
        }
    }

    @Override
    public void togglePause() {
        BasicPlayer.togglePause();
    }

    @Override
    public void stop() {
        BasicPlayer.stop();
    }

    @Override
    public String formatDuration() {
        return SampledFile.timeFormatter(getDuration());
    }

    @Override
    public String formatPosition() {
        return SampledFile.timeFormatter(BasicPlayer.getPosition());
    }


    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

}
