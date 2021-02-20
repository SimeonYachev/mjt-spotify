package bg.sofia.uni.fmi.mjt.spotify.command;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CommandExecutorTest {

    private CommandExecutor cmdExec;
    private int dummyHashCode;

    @Before
    public void initialize() {
        cmdExec = new CommandExecutor();
        dummyHashCode = 42069;
    }

    @After
    public void deleteTestData() {
        File playlists = new File("playlists.txt");
        File tempPlaylists = new File("tempPlaylist.txt");
        if (Files.exists(playlists.toPath())) {
            try (BufferedReader br = new BufferedReader(new FileReader(playlists));
                 PrintWriter writer = new PrintWriter(new FileWriter(tempPlaylists), true)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("test")) {
                        continue;
                    }
                    writer.println(line);
                }
            } catch (IOException e) {
                throw new RuntimeException("There is a problem with the playlists file", e);
            }
        }

        File users = new File("users.txt");
        File tempUsers = new File("tempUsers.txt");
        if (Files.exists(users.toPath())) {
            try (BufferedReader br = new BufferedReader(new FileReader(users));
                    PrintWriter writer = new PrintWriter(new FileWriter(tempUsers), true)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("test")) {
                        continue;
                    }
                    writer.println(line);
                }
            } catch (IOException e) {
                throw new RuntimeException("There is a problem with the users file", e);
            }
        }

        try {
            Files.deleteIfExists(playlists.toPath());
            boolean successful = tempPlaylists.renameTo(playlists);
            Files.deleteIfExists(users.toPath());
            successful = tempUsers.renameTo(users);
        } catch (IOException e) {
            throw new RuntimeException("There is a problem with deleting the files", e);
        }
    }

    @Test
    public void testRegisterUnknownCommand() {
        String message = "register simo@gmail.com";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Unknown command ]" + System.lineSeparator();

        assertEquals("Wrong server response for register.", expectedReply, actualReply);
    }

    @Test
    public void testRegisterInvalidEmail() {
        String message = "register simo@gmail/.com parola";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Email simo@gmail/.com is invalid, select a valid one ]" + System.lineSeparator();

        assertEquals("Wrong server response for register.", expectedReply, actualReply);
    }

    @Test
    public void testRegisterEmailAlreadyTaken() {
        String message = "register simo@gmail.com parola";

        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Email simo@gmail.com is already taken, select another one ]" + System.lineSeparator();

        assertEquals("Wrong server response for register.", expectedReply, actualReply);
    }

    @Test
    public void testRegisterSuccessful() {
        String message = "register simotest@gmail.com parola";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ User with email simotest@gmail.com successfully registered ]" + System.lineSeparator();

        assertEquals("Wrong server response for register.", expectedReply, actualReply);
    }

    @Test
    public void testLoginUnknownCommand() {
        String message = "login simo@gmail.com";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Unknown command ]" + System.lineSeparator();

        assertEquals("Wrong server response for login.", expectedReply, actualReply);
    }

    @Test
    public void testLoginInvalidCombination() {
        String message = "login simoinvalid@gmail.com parola";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Invalid email/password combination ]" + System.lineSeparator();

        assertEquals("Wrong server response for login.", expectedReply, actualReply);
    }

    @Test
    public void testLoginSuccessful() {
        String message = "login simo@gmail.com parola";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ User with email simo@gmail.com successfully logged in ]" + System.lineSeparator();

        assertEquals("Wrong server response for login.", expectedReply, actualReply);
    }

    @Test
    public void testCreatePlaylistNotLoggedIn() {
        String message = "create-playlist testlist";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ You are not logged in ]" + System.lineSeparator();

        assertEquals("Wrong server response for create-playlist.", expectedReply, actualReply);
    }

    @Test
    public void testCreatePlaylistUnknownCommand() {
        String message = "create-playlist";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Unknown command ]" + System.lineSeparator();

        assertEquals("Wrong server response for create-playlist.", expectedReply, actualReply);
    }

    @Test
    public void testCreatePlaylistNameAlreadyTaken() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "create-playlist list1";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Playlist name list1 is already taken, select another one ]" + System.lineSeparator();

        assertEquals("Wrong server response for create-playlist.", expectedReply, actualReply);
    }

    @Test
    public void testCreatePlaylistSuccessful() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "create-playlist testlist";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Playlist testlist successfully created ]" + System.lineSeparator();

        assertEquals("Wrong server response for create-playlist.", expectedReply, actualReply);
    }

    @Test
    public void testAddSongUnknownCommand() {
        String message = "add-song-to testlist";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Unknown command ]" + System.lineSeparator();

        assertEquals("Wrong server response for add-song-to.", expectedReply, actualReply);
    }

    @Test
    public void testAddSongNotLoggedIn() {
        String message = "add-song-to testlist adele-hello";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ You are not logged in ]" + System.lineSeparator();

        assertEquals("Wrong server response for add-song-to.", expectedReply, actualReply);
    }

    @Test
    public void testAddSongNoSuchSong() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "add-song-to testlist adele-goodbye";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ There is no such song ]" + System.lineSeparator();

        assertEquals("Wrong server response for add-song-to.", expectedReply, actualReply);
    }

    @Test
    public void testAddSongNoSuchPlaylist() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "add-song-to invalidlist adele-hello";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Playlist with name invalidlist doesn't exist ]" + System.lineSeparator();

        assertEquals("Wrong server response for add-song-to.", expectedReply, actualReply);
    }

    @Test
    public void testAddSongAlreadyInPlaylist() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "add-song-to list1 adele-hello";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Song adele-hello is already in playlist list1 ]" + System.lineSeparator();

        assertEquals("Wrong server response for add-song-to.", expectedReply, actualReply);
    }

    @Test
    public void testAddSongSuccessful() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "create-playlist testlist";
        cmdExec.execute(dummyHashCode, message);
        message = "add-song-to testlist adele-hello";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Song adele-hello successfully added to playlist testlist ]" + System.lineSeparator();

        assertEquals("Wrong server response for add-song-to.", expectedReply, actualReply);
    }

    @Test
    public void testShowPlaylistNotLoggedIn() {
        String message = "show-playlist testlist";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ You are not logged in ]" + System.lineSeparator();

        assertEquals("Wrong server response for show-playlist.", expectedReply, actualReply);
    }

    @Test
    public void testShowPlaylistUnknownCommand() {
        String message = "show-playlist";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Unknown command ]" + System.lineSeparator();

        assertEquals("Wrong server response for show-playlist.", expectedReply, actualReply);
    }

    @Test
    public void testShowPlaylistNoSuchPlaylist() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "show-playlist invalidlist";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ There isn't a playlist with name invalidlist ]" + System.lineSeparator();

        assertEquals("Wrong server response for show-playlist.", expectedReply, actualReply);
    }

    @Test
    public void testShowPlaylistEmpty() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "create-playlist testlist";
        cmdExec.execute(dummyHashCode, message);
        message = "show-playlist testlist";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Playlist testlist is empty ]" + System.lineSeparator();

        assertEquals("Wrong server response for show-playlist.", expectedReply, actualReply);
    }

    @Test
    public void testShowPlaylistSuccessful() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "show-playlist list1";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ list1: [adele-hello, fletcher-bitter] ]" + System.lineSeparator();

        assertEquals("Wrong server response for show-playlist.", expectedReply, actualReply);
    }

    @Test
    public void testSearchSongsUnknownCommand() {
        String message = "search";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Unknown command ]" + System.lineSeparator();

        assertEquals("Wrong server response for search.", expectedReply, actualReply);
    }

    @Test
    public void testSearchSongsNotLoggedIn() {
        String message = "search le";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ You are not logged in ]" + System.lineSeparator();

        assertEquals("Wrong server response for search.", expectedReply, actualReply);
    }

    @Test
    public void testSearchSongsNoResults() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "search azis";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ No results found ]" + System.lineSeparator();

        assertEquals("Wrong server response for search.", expectedReply, actualReply);
    }

    @Test
    public void testSearchSongsSuccessful() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "search le ll";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ [fletcher-bitter, adele-hello] ]" + System.lineSeparator();

        assertEquals("Wrong server response for search.", expectedReply, actualReply);
    }

    @Test
    public void testTopSongsUnknownCommand() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "top";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Unknown command ]" + System.lineSeparator();

        assertEquals("Wrong server response for top songs.", expectedReply, actualReply);
    }

    @Test
    public void testTopSongsNotLoggedIn() {
        String message = "top 5";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ You are not logged in ]" + System.lineSeparator();

        assertEquals("Wrong server response for top songs.", expectedReply, actualReply);
    }

    @Test
    public void testTopSongsNoSongsPlaying() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "top 5";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ No songs currently playing ]" + System.lineSeparator();

        assertEquals("Wrong server response for top songs.", expectedReply, actualReply);
    }

    @Test
    public void testTopSongsSuccessful() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "play adele-hello";
        cmdExec.execute(dummyHashCode, message);
        message = "top 5";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        List<String> result = new ArrayList<>();
        result.add("adele-hello");

        String expectedReply = "[ " + result + " ]" + System.lineSeparator();

        assertEquals("Wrong server response for top songs.", expectedReply, actualReply);
    }

    @Test
    public void testPlayUnknownCommand() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "play";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Unknown command ]" + System.lineSeparator();

        assertEquals("Wrong server response for play.", expectedReply, actualReply);
    }

    @Test
    public void testPlayNotLoggedIn() {
        String message = "play adele-hello";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ You are not logged in ]" + System.lineSeparator();

        assertEquals("Wrong server response for play.", expectedReply, actualReply);
    }

    @Test
    public void testPlayNoSuchSong() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "play adele-goodbye";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ There is no such song ]" + System.lineSeparator();

        assertEquals("Wrong server response for play.", expectedReply, actualReply);
    }

    @Test
    public void testPlayAnotherSongCurrentlyPlaying() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "play adele-hello";
        cmdExec.execute(dummyHashCode, message);
        message = "play fletcher-bitter";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Another song is currently playing ]" + System.lineSeparator();

        assertEquals("Wrong server response for play.", expectedReply, actualReply);
    }

    @Test
    public void testPlaySuccessful() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "play adele-hello";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply;
        try {
            AudioFormat audioFormat = AudioSystem.getAudioInputStream(new File("adele-hello.wav")).getFormat();
            expectedReply = "Encoding:" + audioFormat.getEncoding() + " " +
                    "SampleRate:" + audioFormat.getSampleRate() + " " +
                    "SampleSizeInBits:" + audioFormat.getSampleSizeInBits() + " " +
                    "Channels:" + audioFormat.getChannels() + " " +
                    "FrameSize:" + audioFormat.getFrameSize() + " " +
                    "FrameRate:" + audioFormat.getFrameRate() + " " +
                    "BigEndian:" + audioFormat.isBigEndian() + " adele-hello";
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new RuntimeException("There is a problem with getting audio format", e);
        }

        expectedReply += System.lineSeparator();

        assertEquals("Wrong server response for play.", expectedReply, actualReply);
    }

    @Test
    public void testStopNotLoggedIn() {
        String message = "stop";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ You are not logged in ]" + System.lineSeparator();

        assertEquals("Wrong server response for stop.", expectedReply, actualReply);
    }

    @Test
    public void testStopSuccessful() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "play adele-hello";
        cmdExec.execute(dummyHashCode, message);
        message = "stop";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Song stopped successfully ]" + System.lineSeparator();

        assertEquals("Wrong server response for stop.", expectedReply, actualReply);
    }

    @Test
    public void testLogoutNotLoggedIn() {
        String message = "logout";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ You are not logged in ]" + System.lineSeparator();

        assertEquals("Wrong server response for logout.", expectedReply, actualReply);
    }

    @Test
    public void testLogoutSuccessful() {
        String message = "login simo@gmail.com parola";
        cmdExec.execute(dummyHashCode, message);
        message = "logout";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Successfully logged out ]" + System.lineSeparator();

        assertEquals("Wrong server response for logout.", expectedReply, actualReply);
    }

    @Test
    public void testDisconnect() {
        String message = "disconnect";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Disconnected from server ]" + System.lineSeparator();

        assertEquals("Wrong server response for disconnect.", expectedReply, actualReply);
    }

    @Test
    public void testUnknownCommand() {
        String message = "bla";
        String actualReply = cmdExec.execute(dummyHashCode, message);

        String expectedReply = "[ Unknown command ]" + System.lineSeparator();

        assertEquals("Wrong server response for an unknown command.", expectedReply, actualReply);
    }

}
