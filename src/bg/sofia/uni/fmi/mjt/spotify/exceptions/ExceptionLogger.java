package bg.sofia.uni.fmi.mjt.spotify.exceptions;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is responsible for logging exceptions in a text file.
 */
public class ExceptionLogger {

    public void logException(Exception e) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("logger.txt", true))) {
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd/HH:mm:ss").format(new Date());
            writer.println("------------------------");
            writer.println(timeStamp);
            writer.println(" ");
            writer.println("Exception stacktrace: ");
            e.printStackTrace(writer);
            writer.println(" ");
        } catch (IOException ioException) {
            throw new RuntimeException("There is a problem with logging an exception", ioException);
        }
    }
}
