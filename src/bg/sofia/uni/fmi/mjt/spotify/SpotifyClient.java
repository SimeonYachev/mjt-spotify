package bg.sofia.uni.fmi.mjt.spotify;

import bg.sofia.uni.fmi.mjt.spotify.exceptions.ExceptionLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SpotifyClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7777;

    private SongPlayer player;
    private ExceptionLogger logger;

    public SpotifyClient() {
        logger = new ExceptionLogger();
    }

    private void startClient() {
        try (SocketChannel socketChannel = SocketChannel.open();
                BufferedReader reader = new BufferedReader(Channels.newReader(socketChannel, StandardCharsets.UTF_8));
                PrintWriter writer = new PrintWriter(Channels.newWriter(socketChannel, StandardCharsets.UTF_8), true);
                Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println("Connected to the server.");

            while (true) {
                System.out.print("Enter message: ");
                String message = scanner.nextLine();

                writer.println(message);

                String reply = reader.readLine();
                if (reply.replace(System.lineSeparator(), "").startsWith("Encoding:")) {
                    player = new SongPlayer(reply);
                    Thread playerThread = new Thread(player);
                    playerThread.setDaemon(true);
                    playerThread.start();
                    System.out.println("[ Your song is now playing... ]");
                    continue;
                }
                System.out.println(reply);
                if (reply.replace(System.lineSeparator(), "").equals("[ Song stopped successfully ]")) {
                    player.stop();
                }
                if (reply.replace(System.lineSeparator(), "").equals("[ Disconnected from server ]")) {
                    break;
                }
            }
        } catch (IOException e) {
            logger.logException(e);
            throw new RuntimeException("There is a problem with the network communication.", e);
        }
    }

    public static void main(String[] args) {
        SpotifyClient client = new SpotifyClient();
        client.startClient();
    }
}
