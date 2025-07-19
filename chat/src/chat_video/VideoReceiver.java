package chat_video;

import java.awt.Image;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class VideoReceiver implements Runnable {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectInputStream in;
    private JLabel displayLabel;
    private int port;

    public VideoReceiver(JLabel displayLabel, int port) {
        this.displayLabel = displayLabel;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Video Receiver listening on port " + port);
            clientSocket = serverSocket.accept();
            System.out.println("Video Client connected to Receiver!");
            in = new ObjectInputStream(clientSocket.getInputStream());

            ImageIcon ic;
            while (true) {
                ic = (ImageIcon) in.readObject();
                Image image = ic.getImage();
                Image newImg = image.getScaledInstance(displayLabel.getWidth(), displayLabel.getHeight(), java.awt.Image.SCALE_SMOOTH);
                ic = new ImageIcon(newImg);
                displayLabel.setIcon(ic);
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(VideoReceiver.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Video Receiver stopped.");
        } finally {
            try {
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
                if (serverSocket != null) serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(VideoReceiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void stopReceiver() {
        try {
            if (in != null) in.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(VideoReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}


