package studiplayer.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import studiplayer.audio.AudioFile;
import studiplayer.audio.NotPlayableException;
import studiplayer.audio.PlayList;
import studiplayer.audio.SortCriterion;

import java.io.File;
import java.net.URL;

public class Player extends Application {

    public static final String DEFAULT_PLAYLIST = "playlists/DefaultPlayList.m3u";
    private static final String PLAYLIST_DIRECTORY = "playlists/";
    private static final String INITIAL_PLAY_TIME_LABEL = "00:00";
    private static final String NO_CURRENT_SONG = "No Song";

    private PlayList playList = new PlayList();
    private boolean useCertPlayList = false;
    private Button playButton;
    private Button pauseButton;
    private Button stopButton;
    private Button nextButton;
    private Label playListLabel = new Label(PLAYLIST_DIRECTORY);
    private Label playTimeLabel = new Label(INITIAL_PLAY_TIME_LABEL);
    private Label currentSongLabel = new Label(NO_CURRENT_SONG);
    private ChoiceBox<SortCriterion> sortChoiceBox;
    private TextField searchTextField;
    private Button filterButton;

    private PlayerThread playerThread;
    private TimerThread timerThread;
    private SongTable songTable;

    @Override
    public void start(Stage stage) throws Exception {
        BorderPane root = new BorderPane();

        FileChooser playListChooser = new FileChooser();
        playListChooser.setTitle("Open Playlist");
        playListChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("M3U Playlist Files", "*.m3u")
        );

        playListChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedPlayList = playListChooser.showOpenDialog(stage);

        if (selectedPlayList != null) {
            playList = new PlayList(selectedPlayList.getAbsolutePath());
        } else {
            playList = new PlayList(DEFAULT_PLAYLIST);
        }
        songTable = new SongTable(playList);

        if (playList.size() > 0) {
            songTable.selectSong(playList.iterator().next());
            playListLabel.setText(selectedPlayList.getAbsolutePath());
            currentSongLabel.setText(playList.currentAudioFile().toString());
            playTimeLabel.setText(playList.currentAudioFile().formatPosition());
        }

        Accordion filterPane = setupFilter();

        VBox detailPane = setupDetails();

        VBox.setVgrow(filterPane, Priority.ALWAYS);
        VBox.setVgrow(songTable, Priority.ALWAYS);
        VBox.setVgrow(detailPane, Priority.ALWAYS);

        root.setTop(filterPane);
        root.setCenter(songTable);
        root.setBottom(detailPane);

        Scene playerScene = new Scene(root, 600, 400);
        stage.setScene(playerScene);
        stage.setTitle("APA Player");
        stage.show();

    }

    public Accordion setupFilter() {
        // Filter
        Accordion filterPane = new Accordion();

        VBox filterData = new VBox();
        filterData.setSpacing(10);

        HBox searchBox = new HBox();
        Label searchLabel = new Label("Search text");
        searchTextField = new TextField();

        searchBox.setSpacing(20);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        searchBox.setPrefHeight(Region.USE_COMPUTED_SIZE);

        searchBox.getChildren().addAll(searchLabel, searchTextField);

        HBox sortBox = new HBox();

        sortBox.setSpacing(20);
        sortBox.setAlignment(Pos.CENTER_LEFT);
        sortBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        sortBox.setPrefHeight(Region.USE_COMPUTED_SIZE);

        Label sortLabel = new Label("Sort by");
        sortChoiceBox = new ChoiceBox<>();
        sortChoiceBox.setPrefWidth(140);

        sortChoiceBox.getItems().addAll(SortCriterion.AUTHOR, SortCriterion.TITLE, SortCriterion.ALBUM, SortCriterion.DURATION);
        sortChoiceBox.getSelectionModel().selectFirst();
        sortChoiceBox.setOnAction(e -> {
            SortCriterion selectedCriterion = sortChoiceBox.getSelectionModel().getSelectedItem();
            if (selectedCriterion != null) {
                playList.setSortCriterion(selectedCriterion);
            }
        });

        filterButton = new Button("display");
        filterButton.setOnAction(e -> {
            String searchText = searchTextField.getText();
            SortCriterion selectedCriterion = sortChoiceBox.getSelectionModel().getSelectedItem();
            playList.setSearch(searchText);
            playList.setSortCriterion(selectedCriterion);
        });

        sortBox.getChildren().addAll(sortLabel, sortChoiceBox, filterButton);

        filterData.getChildren().addAll(searchBox, sortBox);

        TitledPane filterTitledPane = new TitledPane("Filter", filterData);
        filterPane.getPanes().add(filterTitledPane);
        filterPane.setExpandedPane(filterTitledPane);

        return filterPane;
    }

    public VBox setupDetails() {
        // Detail Pane Setup
        VBox detailPane = new VBox();
        detailPane.setPadding(new Insets(5));
        GridPane details = new GridPane();
        details.setHgap(2);
        details.setVgap(2);

        ColumnConstraints keyCol = new ColumnConstraints();
        keyCol.setPrefWidth(100);

        ColumnConstraints valueCol = new ColumnConstraints();
        valueCol.setPrefWidth(Region.USE_COMPUTED_SIZE);

        details.getColumnConstraints().addAll(keyCol, valueCol);

        RowConstraints playListRow = new RowConstraints();
        RowConstraints songRow = new RowConstraints();
        RowConstraints timeRow = new RowConstraints();

        details.getRowConstraints().addAll(playListRow, songRow, timeRow);

        details.add(new Label("Playlist"), 0, 0);
        details.add(playListLabel, 1, 0);
        details.add(new Label("Current Song"), 0, 1);
        details.add(currentSongLabel, 1, 1);
        details.add(new Label("Playtime"), 0, 2);
        details.add(playTimeLabel, 1, 2);

        HBox controlsPane = new HBox();
        controlsPane.setAlignment(Pos.CENTER);

        playButton = createButton("play");
        pauseButton = createButton("pause");
        stopButton = createButton("stop");
        nextButton = createButton("next");

        setButtonStates(false, true, true, false);

        playButton.setOnAction(e -> playCurrentSong());
        pauseButton.setOnAction(e -> pauseCurrentSong());
        stopButton.setOnAction(e -> stopCurrentSong());
        nextButton.setOnAction(e -> playNextSong());

        controlsPane.getChildren().addAll(playButton, pauseButton, stopButton, nextButton);

        detailPane.setSpacing(10);
        detailPane.getChildren().addAll(details, controlsPane);

        return detailPane;

    }

    private void playCurrentSong() {
        if (playList.currentAudioFile() == null) {
            return;
        }
        setButtonStates(true, false, false, false);

        if (playerThread == null || !playerThread.isAlive()) {
            terminateThreads(false);
            playerThread = new PlayerThread();
            playerThread.start();

            timerThread = new TimerThread();
            timerThread.start();
        } else if (playerThread.paused) {
            playerThread.resumePlayback();
        }

        System.out.println("Playing " + playList.currentAudioFile());
        System.out.println("Filename is " + playList.currentAudioFile().getFilename());
    }

    private void pauseCurrentSong() {
        if (playerThread != null && playerThread.isAlive() && !playerThread.paused) {
            playerThread.pausePlayback();
            setButtonStates(true, false, false, false);
            System.out.println("Pausing " + playList.currentAudioFile());
            System.out.println("Filename is " + playList.currentAudioFile().getFilename());
        }
    }

    private void stopCurrentSong() {
        if (playerThread != null) {
            playerThread.terminate();
            playerThread = null;
        }
        if (timerThread != null) {
            timerThread.terminate();
            timerThread = null;
        }
        if (playList.currentAudioFile() != null) {
            playList.currentAudioFile().stop();
        }
        setButtonStates(false, true, true, false);
        updateSongInfo(null);
        System.out.println("Stopping " + playList.currentAudioFile());
        System.out.println("Filename is " + playList.currentAudioFile().getFilename());
    }

    private void playNextSong() {
        stopCurrentSong();
        playList.nextSong();
        songTable.selectSong(playList.currentAudioFile());
        updateSongInfo(playList.currentAudioFile());
        playCurrentSong();
        setButtonStates(true, false, false, false);
        System.out.println("Switching to next audio file");
        System.out.println("Stopped = false, paused = " + (playerThread != null && playerThread.paused));
        System.out.println("Playing " + playList.currentAudioFile());
        System.out.println("Filename is " + playList.currentAudioFile().getFilename());
    }

    private void setButtonStates(boolean playButtonState, boolean pauseButtonState, boolean stopButtonState, boolean nextButtonState) {
        playButton.setDisable(playButtonState);
        pauseButton.setDisable(pauseButtonState);
        stopButton.setDisable(stopButtonState);
        nextButton.setDisable(nextButtonState);
    }

    private void updateSongInfo(AudioFile af) {
        if (af == null) {
            currentSongLabel.setText(NO_CURRENT_SONG);
            playTimeLabel.setText(INITIAL_PLAY_TIME_LABEL);
        } else {
            currentSongLabel.setText(af.toString());
            playTimeLabel.setText(af.formatPosition());
        }
    }

    private Button createButton(String iconfile) {
        Button button = null;
        try {
            URL url = getClass().getResource("/icons/" + iconfile + ".jpg");
            Image icon = new Image(url.toString());
            ImageView imageView = new ImageView(icon);
            imageView.setFitHeight(20);
            imageView.setFitWidth(20);
            button = new Button("", imageView);
            button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            button.setStyle("-fx-background-color: #fff;");
        } catch (Exception e) {
            System.out.println("Image " + "icons/"
                    + iconfile + " not found!");
            System.exit(-1);
        }
        return button;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void loadPlayList(String pathName) {
        try {
            setPlayList(pathName);
            songTable.refreshSongs();
        } catch (Exception e) {
            showErrorDialog("Failed to load playlist: " + e.getMessage());
        }
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setPlayList(String pathname) {
        if (pathname == null) {
            this.playList = new PlayList();
            return;
        }
        this.playList = new PlayList(pathname);
    }

    public void setUseCertPlayList(boolean value) {
        this.useCertPlayList = value;
    }

    private void startThreads(boolean onlyTimer) {
        if (!onlyTimer) {
            playerThread = new PlayerThread();
            playerThread.start();
        }
        timerThread = new TimerThread();
        timerThread.start();
    }

    private void terminateThreads(boolean onlyTimer) {
        if (!onlyTimer && playerThread != null) {
            playerThread.terminate();
            playerThread = null;
        }
        if (timerThread != null) {
            timerThread.terminate();
            timerThread = null;
        }
    }

    private class PlayerThread extends Thread {
        private volatile boolean stopped = false;
        private volatile boolean paused = false;

        public void terminate() {
            stopped = true;
            if (playList.currentAudioFile() != null) {
                playList.currentAudioFile().stop();
            }
            interrupt();
        }

        public void pausePlayback() {
            paused = true;
            if (playList.currentAudioFile() != null) {
                playList.currentAudioFile().togglePause();
            }
        }

        public void resumePlayback() {
            paused = false;
            synchronized (this) {
                notify();
            }
            if (playList.currentAudioFile() != null) {
                playList.currentAudioFile().togglePause();
            }
        }

        @Override
        public void run() {
            while (!stopped) {
                AudioFile currentSong = playList.currentAudioFile();
                if (currentSong == null) {
                    break;
                }

                Platform.runLater(() -> {
                    songTable.selectSong(currentSong);
                    updateSongInfo(currentSong);
                });

                try {
                    currentSong.play();

                    synchronized (this) {
                        while (paused && !stopped) {
                            wait();
                        }
                    }

                } catch (NotPlayableException e) {
                    System.out.println("NotPlayableException: " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (stopped) {
                    break;
                }

                playList.nextSong();
            }
        }
    }

    private class TimerThread extends Thread {
        private volatile boolean running = true;

        public void terminate() {
            running = false;
            interrupt();
        }

        @Override
        public void run() {
            try {
                while (running) {
                    Thread.sleep(1000);
                    Platform.runLater(() -> {
                        AudioFile audioFile = playList.currentAudioFile();
                        if (audioFile != null) {
                            playTimeLabel.setText(audioFile.formatPosition());
                        }
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}