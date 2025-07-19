package chat_video;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioReceiver implements Runnable {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private InputStream in;
    private SourceDataLine speakers;
    private int port;
    private boolean running = true;

    public AudioReceiver(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Audio Receiver listening on port " + port);
            clientSocket = serverSocket.accept();
            System.out.println("Audio Client connected to Receiver!");
            in = clientSocket.getInputStream();

            // Audio format
            AudioFormat format = new AudioFormat(16000, 8, 2, true, true);
            
            // Setup speakers
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            speakers.open(format);
            speakers.start();

            byte[] buffer = new byte[1024];
            int bytesRead;

            while (running && (bytesRead = in.read(buffer)) > 0) {
                speakers.write(buffer, 0, bytesRead);
            }
        } catch (IOException | LineUnavailableException ex) {
            Logger.getLogger(AudioReceiver.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Audio Receiver stopped.");
        } finally {
            stopReceiver();
        }
    }

    public void stopReceiver() {
        running = false;
        try {
            if (speakers != null) {
                speakers.stop();
                speakers.close();
            }
            if (in != null) in.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(AudioReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

