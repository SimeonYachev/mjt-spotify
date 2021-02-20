package bg.sofia.uni.fmi.mjt.spotify.command;

import bg.sofia.uni.fmi.mjt.spotify.exceptions.ExceptionLogger;
import bg.sofia.uni.fmi.mjt.spotify.storage.UserStorage;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * This class is responsible for managing client commands and server replies.
 * The songs used should be in .wav format and named [artist]-[song] in all lower case.
 * They should also be added manually in the "songs.txt" file( [artist]--[song] in all lower case ).
 * Once created accounts and playlists could only be deleted manually from the respective file.
 */
public class CommandExecutor {
    private static final String USERS_FILE = "users.txt";
    private static final String SONGS_FILE = "songs.txt";
    private static final String PLAYLISTS_FILE = "playlists.txt";

    private UserStorage users;
    private Map<String, Set<String>> songs;
    private Map<Integer, String> nowPlaying;
    private Map<String, Integer> playingCount;
    private ExceptionLogger logger;

    public CommandExecutor() {
        logger = new ExceptionLogger();
        nowPlaying = new HashMap<>();
        playingCount = new HashMap<>();
        Map<String, String> registeredUsers = new HashMap<>();
        Map<Integer, String> loggedUsers = new HashMap<>();

        Path usersFilePath = Path.of(USERS_FILE);
        if (Files.exists(usersFilePath)) {
            try (BufferedReader br = Files.newBufferedReader(usersFilePath)) {
                String line;
                while ((line = br.readLine()) != null) {
                    int whitespaceIndex = line.indexOf(" ");
                    String email = line.substring(0, whitespaceIndex);
                    String password = line.substring(whitespaceIndex + 1);
                    registeredUsers.put(email, password);
                }
            } catch (IOException e) {
                logger.logException(e);
                throw new RuntimeException("There is a problem with the users file", e);
            }
        }
        this.users = new UserStorage(registeredUsers, loggedUsers);

        this.songs = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(SONGS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                int doubleDashIndex = line.indexOf("--");
                String artist = line.substring(0, doubleDashIndex);
                String songName = line.substring(doubleDashIndex + 2);

                if (!songs.containsKey(artist)) {
                    Set<String> repertoire = new HashSet<>();
                    repertoire.add(songName);
                    songs.put(artist, repertoire);
                } else {
                    songs.get(artist).add(songName);
                }
            }
        } catch (IOException e) {
            logger.logException(e);
            throw new RuntimeException("There is a problem with the songs file", e);
        }
    }

    public String execute(Integer scHash, String message) {
        String reply;
        if (message.startsWith("register")) {
            reply = register(scHash, message);
        } else if (message.startsWith("login")) {
            reply = login(scHash, message);
        } else if (message.startsWith("search")) {
            reply = searchSongs(scHash, message);
        } else if (message.startsWith("top")) {
            reply = topSongs(scHash, message);
        } else if (message.startsWith("create-playlist")) {
            reply = createPlaylist(scHash, message);
        } else if (message.startsWith("add-song-to")) {
            reply = addSong(scHash, message);
        } else if (message.startsWith("show-playlist")) {
            reply = showPlaylist(scHash, message);
        } else if (message.startsWith("play")) {
            reply = play(scHash, message);
        } else if (message.equals("stop")) {
            reply = stop(scHash);
        } else if (message.equals("logout")) {
            reply = logout(scHash);
        } else if (message.equals("disconnect")) {
            reply = disconnect(scHash);
        } else {
            reply = "[ Unknown command ]";
        }

        return reply + System.lineSeparator();
    }

    private String register(Integer scHash, String message) {
        if (invalidThreePartCommandFormat(message)) {
            return "[ Unknown command ]";
        }

        EmailPasswordCommand cmd = getEmailPassword(message);
        String email = cmd.email();
        String password = cmd.password();

        if (invalidEmail(email)) {
            return "[ Email " + email + " is invalid, select a valid one ]";
        }

        String reply;
        if (users.registeredUsers().containsKey(email)) {
            reply = "[ Email " + email + " is already taken, select another one ]";
        } else {
            try (FileWriter fileWriter = new FileWriter(USERS_FILE, true);
                    PrintWriter writer = new PrintWriter(fileWriter, true)) {
                writer.println(email + " " + password);
            } catch (IOException e) {
                logger.logException(e);
                throw new IllegalStateException("A problem occurred while writing to the users file", e);
            }

            users.registeredUsers().put(email, password);
            users.loggedUsers().put(scHash, email);
            reply = "[ User with email " + email + " successfully registered ]";
        }

        return reply;
    }

    private String login(Integer scHash, String message) {
        if (invalidThreePartCommandFormat(message)) {
            return "[ Unknown command ]";
        }

        EmailPasswordCommand cmd = getEmailPassword(message);
        String email = cmd.email();
        String password = cmd.password();

        String reply;
        if (users.registeredUsers().containsKey(email) && password.equals(users.registeredUsers().get(email))) {
            reply = "[ User with email " + email + " successfully logged in ]";
            users.loggedUsers().put(scHash, email);
        } else {
            reply = "[ Invalid email/password combination ]";
        }

        return reply;
    }

    private String searchSongs(Integer scHash, String message) {
        String check = validateTwoPartCommand(scHash, message);
        if (!check.equals("")) {
            return check;
        }

        Set<String> searchWords = new HashSet<>();
        String searchInput = message.substring(message.indexOf(" ") + 1).toLowerCase().strip();
        while (searchInput.contains(" ")) {
            int whitespaceIndex = searchInput.indexOf(" ");
            searchWords.add(searchInput.substring(0, whitespaceIndex));
            searchInput = searchInput.substring(whitespaceIndex + 1);
        }
        searchWords.add(searchInput);

        Set<String> searchResult = new HashSet<>();
        String result;
        for (String word : searchWords) {
            for (String artist : songs.keySet()) {
                if (artist.contains(word)) {
                    for (String songName : songs.get(artist)) {
                        result = artist + "-" + songName;
                        searchResult.add(result);
                    }
                } else {
                    for (String songName : songs.get(artist)) {
                        if (songName.contains(word)) {
                            result = artist + "-" + songName;
                            searchResult.add(result);
                        }
                    }
                }
            }
        }

        return searchResult.isEmpty() ? "[ No results found ]" : "[ " + searchResult + " ]";
    }

    private String topSongs(Integer scHash, String message) {
        String check = validateTwoPartCommand(scHash, message);
        if (!check.equals("")) {
            return check;
        }

        if (playingCount.isEmpty()) {
            return "[ No songs currently playing ]";
        }

        int n = Integer.parseInt(message.substring(message.indexOf(" ") + 1));

        List<String> sortedSongs =
                playingCount.entrySet().stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                        .limit(n)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toUnmodifiableList());

        return "[ " + sortedSongs + " ]";
    }

    private String createPlaylist(Integer scHash, String message) {
        String check = validateTwoPartCommand(scHash, message);
        if (!check.equals("")) {
            return check;
        }

        String playlistName = message.substring(message.indexOf(" ") + 1);

        Path playlistsFilePath = Path.of(PLAYLISTS_FILE);
        if (Files.exists(playlistsFilePath)) {
            try (BufferedReader br = Files.newBufferedReader(playlistsFilePath)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(playlistName)) {
                        return "[ Playlist name " + playlistName + " is already taken, select another one ]";
                    }
                }
            } catch (IOException e) {
                logger.logException(e);
                throw new RuntimeException("There is a problem with the playlists file", e);
            }
        }

        try (FileWriter fileWriter = new FileWriter(PLAYLISTS_FILE, true);
                PrintWriter writer = new PrintWriter(fileWriter, true)) {
            writer.println(playlistName);
        } catch (IOException e) {
            logger.logException(e);
            throw new IllegalStateException("A problem occurred while writing to a file", e);
        }

        return "[ Playlist " + playlistName + " successfully created ]";
    }

    private String addSong(Integer scHash, String message) {
        if (invalidThreePartCommandFormat(message)) {
            return "[ Unknown command ]";
        }
        if (notLoggedIn(scHash)) {
            return "[ You are not logged in ]";
        }

        int wsIndex = message.indexOf(" ");
        int secondWsIndex = message.substring(wsIndex + 1).indexOf(" ")
                + message.substring(0, wsIndex).length() + 1;
        String playlistName = message.substring(wsIndex + 1, secondWsIndex);
        String songToAdd = message.substring(secondWsIndex + 1).toLowerCase();

        if (unavailableSong(songToAdd)) {
            return "[ There is no such song ]";
        }

        String playlist = getPlaylist(playlistName);
        if (playlist.equals("wrong name")) {
            return "[ Playlist with name " + playlistName + " doesn't exist ]";
        }

        if (playlist.contains(songToAdd)) {
            return "[ Song " + songToAdd + " is already in playlist " + playlistName + " ]";
        }

        String updatedPlaylist
                = playlist.contains("::") ? playlist + songToAdd + ";" : playlist + "::" + songToAdd + ";";

        int playlistLineIndex = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(PLAYLISTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(playlistName)) {
                    break;
                }
                playlistLineIndex++;
            }
        } catch (IOException e) {
            logger.logException(e);
            throw new RuntimeException("There is a problem with the playlists file", e);
        }

        Path playlistFilePath = Path.of(PLAYLISTS_FILE);
        try {
            List<String> lines = Files.readAllLines(playlistFilePath, StandardCharsets.UTF_8);
            lines.set(playlistLineIndex, updatedPlaylist);
            Files.write(playlistFilePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.logException(e);
            throw new IllegalStateException("A problem occurred while writing to a file", e);
        }

        return "[ Song " + songToAdd + " successfully added to playlist " + playlistName + " ]";
    }

    private String showPlaylist(Integer scHash, String message) {
        String check = validateTwoPartCommand(scHash, message);
        if (!check.equals("")) {
            return check;
        }

        String playlistName = message.substring(message.indexOf(" ") + 1);
        String playlist = getPlaylist(playlistName);
        if (playlist.equals("wrong name")) {
            return "[ There isn't a playlist with name " + playlistName + " ]";
        } else if (!playlist.contains("::")) {
            return "[ Playlist " + playlistName + " is empty ]";
        } else {
            return "[ " + playlistName + ": "
                    + Arrays.toString(playlist.substring(playlist.indexOf("::") + 2).split(";")) + " ]";
        }
    }

    private String play(Integer scHash, String message) {
        String check = validateTwoPartCommand(scHash, message);
        if (!check.equals("")) {
            return check;
        }

        if (nowPlaying.containsKey(scHash)) {
            return "[ Another song is currently playing ]";
        }

        String song = message.substring(message.indexOf(" ") + 1).toLowerCase();
        if (unavailableSong(song)) {
            return "[ There is no such song ]";
        }

        String reply;
        try {
            AudioFormat audioFormat = AudioSystem.getAudioInputStream(new File(song + ".wav")).getFormat();
            reply = "Encoding:" + audioFormat.getEncoding() + " "
                    + "SampleRate:" + audioFormat.getSampleRate() + " "
                    + "SampleSizeInBits:" + audioFormat.getSampleSizeInBits() + " "
                    + "Channels:" + audioFormat.getChannels() + " "
                    + "FrameSize:" + audioFormat.getFrameSize() + " "
                    + "FrameRate:" + audioFormat.getFrameRate() + " "
                    + "BigEndian:" + audioFormat.isBigEndian() + " " + song;
        } catch (IOException | UnsupportedAudioFileException e) {
            logger.logException(e);
            throw new RuntimeException("There is a problem with getting audio format", e);
        }

        nowPlaying.put(scHash, song);
        if (playingCount.containsKey(song)) {
            playingCount.put(song, playingCount.get(song) + 1);
        } else {
            playingCount.put(song, 1);
        }
        return reply;
    }

    private String stop(Integer scHash) {
        if (notLoggedIn(scHash)) {
            return "[ You are not logged in ]";
        } else {
            String song = nowPlaying.get(scHash);
            nowPlaying.remove(scHash);
            if (playingCount.get(song) == 1) {
                playingCount.remove(song);
            } else {
                playingCount.put(song, playingCount.get(song) - 1);
            }
            return "[ Song stopped successfully ]";
        }
    }

    private String logout(Integer scHash) {
        if (notLoggedIn(scHash)) {
            return "[ You are not logged in ]";
        } else {
            users.loggedUsers().remove(scHash);
            return "[ Successfully logged out ]";
        }
    }

    private String disconnect(Integer scHash) {
        users.loggedUsers().remove(scHash);

        return "[ Disconnected from server ]";
    }

    private boolean invalidEmail(String email) {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        return !email.matches(regex);
    }

    private boolean invalidThreePartCommandFormat(String message) {
        return !message.contains(" ") || !message.substring(message.indexOf(" ") + 1).contains(" ");
    }

    private EmailPasswordCommand getEmailPassword(String s) {
        int wsIndex = s.indexOf(" ");
        int secondWsIndex = s.substring(wsIndex + 1).indexOf(" ")
                + s.substring(0, wsIndex).length() + 1;
        String email = s.substring(wsIndex + 1, secondWsIndex);
        String password = s.substring(secondWsIndex + 1).strip();

        return new EmailPasswordCommand(email, password);
    }

    private boolean notLoggedIn(Integer scHash) {
        return !users.loggedUsers().containsKey(scHash);
    }

    private String getPlaylist(String playlistName) {
        Path playlistsFilePath = Path.of(PLAYLISTS_FILE);
        if (Files.exists(playlistsFilePath)) {
            try (BufferedReader br = Files.newBufferedReader(playlistsFilePath)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(playlistName)) {
                        return line;
                    }
                }
                return "wrong name";
            } catch (IOException e) {
                logger.logException(e);
                throw new RuntimeException("There is a problem with the playlists file", e);
            }
        }

        return "";
    }

    private String validateTwoPartCommand(int scHash, String message) {
        if (!message.contains(" ")) {
            return "[ Unknown command ]";
        }
        if (notLoggedIn(scHash)) {
            return "[ You are not logged in ]";
        }
        return "";
    }

    private boolean unavailableSong(String song) {
        int dashIndex = song.indexOf("-");
        String artistToAdd = song.substring(0, dashIndex);
        String songNameToAdd = song.substring(dashIndex + 1);

        return !this.songs.containsKey(artistToAdd) || !this.songs.get(artistToAdd).contains(songNameToAdd);
    }
}

