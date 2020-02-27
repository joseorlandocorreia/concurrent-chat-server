package org.academiadecodigo.chatserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        private InputStream serverWorkerIn;
        private OutputStream serverWorkerOut;

        public ServerWorker(Socket serverWorkerSocket) {
            this.serverWorkerSocket = serverWorkerSocket;
            initStreams();
        }

        @Override
        public void run() {

            String receivedMessage;
            byte[] recvBuffer = new byte[1024];
            int numberOfCharsRead;

            while (true) {
                try {
                    numberOfCharsRead = serverWorkerIn.read(recvBuffer, 0, recvBuffer.length);
                    receivedMessage = new String(recvBuffer, 0, numberOfCharsRead);
                    System.out.print("Message received from: " + serverWorkerSocket + " Message: " + receivedMessage);
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        private void initStreams() {
            try {

                serverWorkerIn = serverWorkerSocket.getInputStream();
                serverWorkerOut = serverWorkerSocket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String messageToSend) {
            try {
                serverWorkerOut.write(messageToSend.getBytes(),0, messageToSend.length());
                serverWorkerOut.write("\n".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
