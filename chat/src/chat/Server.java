package chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final int CHAT_PORT = 1201;
    private static final int VIDEO_PORT = 1202;
    private static final int AUDIO_PORT = 1203;

    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket chatServerSocket = new ServerSocket(CHAT_PORT);
        System.out.println("Chat Server started on port " + CHAT_PORT);
        System.out.println("Waiting for chat clients...");

        // Start Video/Audio Server Handler
        VideoAudioServerHandler vaHandler = new VideoAudioServerHandler(VIDEO_PORT, AUDIO_PORT);
        new Thread(vaHandler).start();

        // Accept two chat clients
        for (int i = 0; i < 2; i++) {
            Socket clientSocket = chatServerSocket.accept();
            System.out.println("Chat Client " + (i + 1) + " connected: " + clientSocket);
            ClientHandler clientHandler = new ClientHandler(clientSocket, "Client " + (i + 1));
            clients.add(clientHandler);
            new Thread(clientHandler).start();
        }
        System.out.println("Two chat clients connected. Chat can begin.");
    }

    // Method to broadcast messages to all clients except the sender
    public static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // Inner class to handle each client connection
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        private String clientName;

        public ClientHandler(Socket socket, String clientName) {
            this.socket = socket;
            this.clientName = clientName;
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String message) {
            try {
                out.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String message;
            try {
                while (true) {
                    message = in.readUTF();
                    System.out.println(clientName + ": " + message);
                    broadcastMessage(clientName + ": " + message, this);
                }
            } catch (IOException e) {
                System.out.println(clientName + " disconnected.");
                clients.remove(this);
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}


