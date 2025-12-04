package studiplayer.audio;

public abstract class AudioFile {
    private String pathname;
    private String filename;
    private String author;
    private String title;

    public AudioFile() {
        this.pathname = "";
        this.filename = "";
        this.author = "";
        this.title = "";
    }

    public AudioFile(String path) throws NotPlayableException {
        try {
            parsePathname(path);
            parseFilename(getFilename());
        } catch (Exception e) {
            throw new NotPlayableException(path, "File not found!");
        }
    }

    public void parsePathname(String path) {
        path = path.trim();  // Trim whitespace
        if (path.isEmpty()) {
            this.pathname = "";
            this.filename = "";
            return;
        }

        // Normalize path separators and handle multiple separators
        String separator = System.getProperty("file.separator");
        path = path.replace("\\", separator).replace("/", separator);  // Replace slashes
        path = normalizePath(path, separator.charAt(0));  // Normalize multiple separators

        // Handle drive letter for non-Windows systems without using regex
        if (!isWindows() && path.length() > 1 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':') {
            // Extract the drive letter
            String driveLetter = String.valueOf(path.charAt(0));
            // Remove the drive letter and colon from the path
            path = path.substring(2);
            // Prepend the drive letter as a directory
            path = separator + driveLetter + path;
        }

        this.pathname = path;  // Store the normalized path as pathname

        // Extract filename by finding the last separator
        int lastSeparatorIndex = path.lastIndexOf(separator);
        if (lastSeparatorIndex != -1) {
            this.filename = path.substring(lastSeparatorIndex + 1).trim();  // Trim the filename
        } else {
            this.filename = path.trim();  // If no separator, the entire path is the filename
        }
    }

    public String normalizePath(String path, char separator) {
        StringBuilder builder = new StringBuilder();
        boolean lastWasSeparator = false;

        for (int i = 0; i < path.length(); i++) {
            char currentChar = path.charAt(i);
            if (currentChar == separator) {
                if (!lastWasSeparator) {
                    builder.append(separator);
                    lastWasSeparator = true;
                }
            } else {
                builder.append(currentChar);
                lastWasSeparator = false;
            }
        }

        return builder.toString();
    }

    public void parseFilename(String filename) {

        if (filename.equals(" - ")) {
            this.author = "";
            this.title = "";
            return;
        }

        // Special case handling for "-"
        if (filename.equals("-")) {
            this.author = "";
            this.title = "-";
            return;
        }

        // Handle completely empty filenames after trimming
        if (filename.isEmpty()) {
            this.author = "";
            this.title = "";
            return;
        }

        // Remove the file extension if any
        int dotIndex = filename.lastIndexOf('.');
        String baseName = (dotIndex == -1) ? filename : filename.substring(0, dotIndex);
        baseName = baseName.trim();

        // Special case handling for trimmed base name "-"
        if (baseName.equals("-")) {
            this.author = "";
            this.title = ""; // For " - ", set title to ""
            return;
        }

        // Split the base name into author and title using a dash if it exists
        if (!baseName.contains("-")) {
            this.author = "";
            this.title = baseName;
            return;
        }

        // Use the trimmed base name without extension for splitting
        String[] parts = splitAtDash(baseName);

        // Assign author and title based on the number of parts
        if (parts.length == 2) {
            this.author = parts[0].trim();
            this.title = parts[1].trim();
        } else {
            this.author = "";
            this.title = "";
        }
    }

    private String[] splitAtDash(String baseName) {
        int index = baseName.indexOf(" - ");
        if (index == -1) {
            return new String[] { "" }; // No delimiter found
        }
        String author = baseName.substring(0, index);
        String title = baseName.substring(index + 3); // Skip " - "
        return new String[] { author, title };
    }


    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public String getPathname() {
        return pathname;
    }

    public String getFilename() {
        return filename;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        if (this.author.isEmpty()) {
            return this.title;
        }
        return this.author + " - " + this.title;
    }

    public abstract void play() throws NotPlayableException;
    public abstract void togglePause();
    public abstract void stop();
    public abstract String formatDuration();
    public abstract String formatPosition();

}
