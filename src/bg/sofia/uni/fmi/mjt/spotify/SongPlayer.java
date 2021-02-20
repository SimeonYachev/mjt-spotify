package bg.sofia.uni.fmi.mjt.spotify;

import javax.sound.sampled.DataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.IOException;

/**
 * This class is responsible for the audio playback
 */
public class SongPlayer implements Runnable {

    private String reply;
    private boolean stopped;

    public SongPlayer(String reply) {
        this.reply = reply;
    }

    @Override
    public void run() {
        reply = reply.replace(System.lineSeparator(), "");
        int encodingIndex = reply.indexOf("Encoding:");
        final AudioFormat.Encoding encoding =
                new AudioFormat.Encoding(reply.substring(encodingIndex + 9, reply.indexOf(" ")));

        reply = reply.substring(reply.indexOf(" ") + 1);
        int sampleRateIndex = reply.indexOf("SampleRate:");
        final float sampleRate = Float.parseFloat(reply.substring(sampleRateIndex + 11, reply.indexOf(" ")));

        reply = reply.substring(reply.indexOf(" ") + 1);
        int sampleSizeInBitsIndex = reply.indexOf("SampleSizeInBits:");
        final int sampleSizeInBits = Integer.parseInt(reply.substring(sampleSizeInBitsIndex + 17, reply.indexOf(" ")));

        reply = reply.substring(reply.indexOf(" ") + 1);
        int channelsIndex = reply.indexOf("Channels:");
        final int channels = Integer.parseInt(reply.substring(channelsIndex + 9, reply.indexOf(" ")));

        reply = reply.substring(reply.indexOf(" ") + 1);
        int frameSizeIndex = reply.indexOf("FrameSize:");
        final int frameSize = Integer.parseInt(reply.substring(frameSizeIndex + 10, reply.indexOf(" ")));

        reply = reply.substring(reply.indexOf(" ") + 1);
        int frameRateIndex = reply.indexOf("FrameRate:");
        final float frameRate = Float.parseFloat(reply.substring(frameRateIndex + 10, reply.indexOf(" ")));

        reply = reply.substring(reply.indexOf(" ") + 1);
        int bigEndianIndex = reply.indexOf("BigEndian:");
        final boolean bigEndian = Boolean.parseBoolean(reply.substring(bigEndianIndex + 10, reply.indexOf(" ")));

        String song = reply.substring(reply.indexOf(" ") + 1);

        AudioFormat format =
                new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);

        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine(info);
            dataLine.open();
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(song + ".wav"));

            dataLine.start();
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            int allBytes = stream.available();
            while (bytesRead < allBytes && !stopped) {
                int numBytesToRead = 512;
                int numBytesRead = stream.read(buffer, 0, numBytesToRead);
                if (numBytesRead == -1) {
                    break;
                }
                bytesRead += numBytesRead;
                dataLine.write(buffer, 0, numBytesRead);
            }

            dataLine.stop();
            dataLine.close();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            throw new RuntimeException("There is a problem with playing your song", e);
        }
    }

    public void stop() {
        stopped = true;
    }
}
