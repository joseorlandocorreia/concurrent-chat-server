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

    private List<ServerWorker> serverWorkersList;

    private ExecutorService workerThreads = Executors.newFixedThreadPool(10);

    Socket clientSocket;

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    public Server() {
        try {
            serverWorkersList = Collections.synchronizedList(new LinkedList<>());
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server started: " + serverSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        while (true) {
            try {

                ServerWorker serverWorker = new ServerWorker(serverSocket.accept());
                serverWorkersList.add(serverWorker);
                workerThreads.submit(serverWorker);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(String messageToBroadcast) {
        for (ServerWorker worker : serverWorkersList) {
            worker.sendMessage(messageToBroadcast);
        }
    }


    public class ServerWorker implements Runnable {

        private Socket serverWorkerSocket;
        private BufferedReader serverWorkerIn;
        private BufferedWriter serverWorkerOut;

        public ServerWorker(Socket serverWorkerSocket) {
            this.serverWorkerSocket = serverWorkerSocket;
            openStreams();
        }

        @Override
        public void run() {

            String receivedMessage;

            while (true) {
                try {
                    receivedMessage = serverWorkerIn.readLine();
                    //System.out.println("Message received from: " + serverWorkerSocket + " Message: " + receivedMessage);
                    broadcast("Message from: " + serverWorkerSocket + " Message: " + receivedMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        private void openStreams() {
            try {
                serverWorkerIn = new BufferedReader(new InputStreamReader(serverWorkerSocket.getInputStream()));
                serverWorkerOut = new BufferedWriter(new OutputStreamWriter(serverWorkerSocket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String messageToSend) {
            try {
                serverWorkerOut.write(messageToSend);
                serverWorkerOut.newLine();
                serverWorkerOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}
