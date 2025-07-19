package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VideoAudioServerHandler implements Runnable {
    private int videoPort;
    private int audioPort;
    private ServerSocket videoServerSocket;
    private ServerSocket audioServerSocket;
    private List<Socket> videoClients;
    private List<Socket> audioClients;

    public VideoAudioServerHandler(int videoPort, int audioPort) {
        this.videoPort = videoPort;
        this.audioPort = audioPort;
        this.videoClients = new ArrayList<>();
        this.audioClients = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            videoServerSocket = new ServerSocket(videoPort);
            audioServerSocket = new ServerSocket(audioPort);
            System.out.println("Video/Audio Server Handler started on video port " + videoPort + " and audio port " + audioPort);

            // Accept video clients
            new Thread(() -> {
                while (!videoServerSocket.isClosed()) {
                    try {
                        Socket clientSocket = videoServerSocket.accept();
                        videoClients.add(clientSocket);
                        System.out.println("Video client connected: " + clientSocket.getInetAddress());
                        new Thread(new VideoAudioRelay(clientSocket, videoClients, true)).start();
                    } catch (IOException ex) {
                        Logger.getLogger(VideoAudioServerHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();

            // Accept audio clients
            new Thread(() -> {
                while (!audioServerSocket.isClosed()) {
                    try {
                        Socket clientSocket = audioServerSocket.accept();
                        audioClients.add(clientSocket);
                        System.out.println("Audio client connected: " + clientSocket.getInetAddress());
                        new Thread(new VideoAudioRelay(clientSocket, audioClients, false)).start();
                    } catch (IOException ex) {
                        Logger.getLogger(VideoAudioServerHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();

        } catch (IOException ex) {
            Logger.getLogger(VideoAudioServerHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stop() {
        try {
            if (videoServerSocket != null) videoServerSocket.close();
            if (audioServerSocket != null) audioServerSocket.close();
            for (Socket s : videoClients) s.close();
            for (Socket s : audioClients) s.close();
        } catch (IOException ex) {
            Logger.getLogger(VideoAudioServerHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class VideoAudioRelay implements Runnable {
    private Socket clientSocket;
    private List<Socket> connectedClients;
    private boolean isVideo;

    public VideoAudioRelay(Socket clientSocket, List<Socket> connectedClients, boolean isVideo) {
        this.clientSocket = clientSocket;
        this.connectedClients = connectedClients;
        this.isVideo = isVideo;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[4096]; // Increased buffer size for video/audio
            int bytesRead;
            while ((bytesRead = clientSocket.getInputStream().read(buffer)) != -1) {
                for (Socket otherClient : connectedClients) {
                    if (otherClient != clientSocket && !otherClient.isClosed()) {
                        try {
                            otherClient.getOutputStream().write(buffer, 0, bytesRead);
                            otherClient.getOutputStream().flush();
                        } catch (IOException e) {
                            // Handle client disconnection during relay
                            System.out.println((isVideo ? "Video" : "Audio") + " client disconnected during relay: " + otherClient.getInetAddress());
                            connectedClients.remove(otherClient);
                            otherClient.close();
                        }
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println((isVideo ? "Video" : "Audio") + " client disconnected: " + clientSocket.getInetAddress());
        } finally {
            try {
                connectedClients.remove(clientSocket);
                clientSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(VideoAudioRelay.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}


