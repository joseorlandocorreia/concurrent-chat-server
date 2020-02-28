package org.academiadecodigo.chatserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private int portNumber = 6666;
    private ServerSocket serverSocket;
    private List<ServerWorker> workersList;

    private ExecutorService workerThreads = Executors.newFixedThreadPool(100);

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    private Server() {
        try {

            workersList = Collections.synchronizedList(new LinkedList<>());
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server started: " + serverSocket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        while (true) {
            try {

                System.out.println("waiting for connections ...");
                Socket workerSocket = serverSocket.accept();

                System.out.println("New client connected: " + workerSocket);

                ServerWorker serverWorker = new ServerWorker(workerSocket);
                workersList.add(serverWorker);
                workerThreads.submit(serverWorker);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void broadcast(String messageToBroadcast) {
        for (ServerWorker worker : workersList) {
            worker.sendMessage(messageToBroadcast);
        }
    }


    public class ServerWorker implements Runnable {

        private Socket workerSocket;
        private BufferedReader workerIn;
        private BufferedWriter workerOut;

        public ServerWorker(Socket workerSocket) {
            this.workerSocket = workerSocket;
            openStreams();
        }

        @Override
        public void run() {

            String receivedMessage;

            while (!workerSocket.isClosed()) {
                try {
                    if ((receivedMessage = workerIn.readLine()) != null) {

                        System.out.println("Message from: " + workerSocket + " Message: " + receivedMessage);
                        broadcast("Message from: " + workerSocket + " Message: " + receivedMessage);

                        if (receivedMessage.equals("/quit")) {
                            System.out.println("Closing connection from: " + workerSocket);
                            closeStreams();
                            workersList.remove(this);
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void openStreams() {
            try {
                workerIn = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));
                workerOut = new BufferedWriter(new OutputStreamWriter(workerSocket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void closeStreams() {
            try {
                workerOut.close();
                workerIn.close();
                workerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String messageToSend) {
            try {
                workerOut.write(messageToSend);
                workerOut.newLine();
                workerOut.flush();
            } catch (IOException e) {
                workersList.remove(this);
                //e.printStackTrace();
            }

        }

    }

}
