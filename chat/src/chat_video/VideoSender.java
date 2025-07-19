package chat_video;

import com.github.sarxos.webcam.Webcam;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class VideoSender implements Runnable {

    private Socket socket;
    private ObjectOutputStream out;
    private Webcam cam;
    private JLabel displayLabel;
    private String host;
    private int port;
    private boolean running = true;

    public VideoSender(JLabel displayLabel, String host, int port) {
        this.displayLabel = displayLabel;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            cam = Webcam.getDefault();
            cam.open();

            ImageIcon ic;
            BufferedImage br;
            System.out.println("Video Sender connected to " + host + ":" + port);

            while (running) {
                br = cam.getImage();
                if (br != null) {
                    ic = new ImageIcon(br);

                    try {
                        out.writeObject(ic);
                        out.flush();
                    } catch (IOException ex) {
                        Logger.getLogger(VideoSender.class.getName()).log(Level.SEVERE, null, ex);
                        break;
                    }

                    // Display on local screen
                    Image image = ic.getImage();
                    Image newImg = image.getScaledInstance(displayLabel.getWidth(), displayLabel.getHeight(), java.awt.Image.SCALE_SMOOTH);
                    ic = new ImageIcon(newImg);
                    displayLabel.setIcon(ic);
                }

                Thread.sleep(50); // Control frame rate
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(VideoSender.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            stopSender();
        }
    }

    public void stopSender() {
        running = false;
        try {
            if (cam != null && cam.isOpen()) {
                cam.close();
            }
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException ex) {
            Logger.getLogger(VideoSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

