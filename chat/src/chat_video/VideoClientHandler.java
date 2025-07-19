package chat_video;

import com.github.sarxos.webcam.Webcam;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class VideoClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Webcam cam;
    private JLabel displayLabel;
    private boolean running = true;

    public VideoClientHandler(Socket socket, JLabel displayLabel) {
        this.socket = socket;
        this.displayLabel = displayLabel;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(VideoClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        cam = Webcam.getDefault();
        if (cam != null) {
            cam.open();
        } else {
            System.err.println("No webcam found.");
            running = false;
        }

        while (running) {
            try {
                // Send video
                if (cam != null && cam.isOpen()) {
                    BufferedImage br = cam.getImage();
                    if (br != null) {
                        ImageIcon ic = new ImageIcon(br);
                        out.writeObject(ic);
                        out.flush();
                    }
                }

                // Receive video
                ImageIcon receivedIc = (ImageIcon) in.readObject();
                if (receivedIc != null) {
                    displayLabel.setIcon(new ImageIcon(receivedIc.getImage().getScaledInstance(displayLabel.getWidth(), displayLabel.getHeight(), java.awt.Image.SCALE_SMOOTH)));
                }
                Thread.sleep(50); // Control frame rate
            } catch (IOException | ClassNotFoundException | InterruptedException ex) {
                Logger.getLogger(VideoClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                running = false;
            }
        }
        stop();
    }

    public void stop() {
        running = false;
        try {
            if (cam != null && cam.isOpen()) {
                cam.close();
            }
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException ex) {
            Logger.getLogger(VideoClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}


