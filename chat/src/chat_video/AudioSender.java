package chat_video;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class AudioSender implements Runnable {

    private Socket socket;
    private OutputStream out;
    private TargetDataLine microphone;
    private String host;
    private int port;
    private boolean running = true;

    public AudioSender(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(host, port);
            out = socket.getOutputStream();

            // Audio format
            AudioFormat format = new AudioFormat(16000, 8, 2, true, true);
            
            // Setup microphone
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            System.out.println("Audio Sender connected to " + host + ":" + port);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while (running && (bytesRead = microphone.read(buffer, 0, buffer.length)) > 0) {
                out.write(buffer, 0, bytesRead);
                out.flush();
            }
        } catch (IOException | LineUnavailableException ex) {
            Logger.getLogger(AudioSender.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            stopSender();
        }
    }

    public void stopSender() {
        running = false;
        try {
            if (microphone != null) {
                microphone.stop();
                microphone.close();
            }
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException ex) {
            Logger.getLogger(AudioSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

