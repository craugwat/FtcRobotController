package org.firstinspires.ftc.teamcode;
import java.io.*;
import java.net.*;

public class portForward {
    private static final String DESTINATION_HOST = "limelight.local";
    private static final int DESTINATION_PORT = 5800;
    private static final int LOCAL_PORT = 5800;

    public static void portForward() throws IOException {
        ServerSocket serverSocket = new ServerSocket(LOCAL_PORT);
        System.out.println("Server started on port " + LOCAL_PORT);

//        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Accepted connection from " + clientSocket.getInetAddress());
            ClientThread clientThread = new ClientThread(clientSocket);
            clientThread.start();
//        }
    }

    private static class ClientThread extends Thread {
        private Socket clientSocket;

        public ClientThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    Socket serverSocket = new Socket(DESTINATION_HOST, DESTINATION_PORT);
                    InputStream clientIn = clientSocket.getInputStream();
                    OutputStream clientOut = clientSocket.getOutputStream();
                    InputStream serverIn = serverSocket.getInputStream();
                    OutputStream serverOut = serverSocket.getOutputStream()
            ) {
                // Start threads to forward data in both directions
                Thread clientToServer = new ForwardThread(clientIn, serverOut);
                Thread serverToClient = new ForwardThread(serverIn, clientOut);

                clientToServer.start();
                serverToClient.start();

                // Wait for either direction to complete
                try {
                    clientToServer.join();
                    serverToClient.join();
                } catch (InterruptedException e) {
                    // Handle the interruption
                }

            } catch (IOException e) {
                // Handle exception
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    // Handle exception
                }
            }
        }
    }

    private static class ForwardThread extends Thread {
        private InputStream input;
        private OutputStream output;

        public ForwardThread(InputStream input, OutputStream output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public void run() {
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    output.flush();
                }
            } catch (IOException e) {
                // Handle exception
            } finally {
                try {
                    output.close();
                    input.close();
                } catch (IOException e) {
                    // Handle exception
                }
            }
        }
    }

}
