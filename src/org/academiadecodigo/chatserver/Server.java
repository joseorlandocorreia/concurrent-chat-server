package org.academiadecodigo.chatserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private int portNumber = 6666;
    private ServerSocket serverSocket;
    private Map<String, ServerWorker> workersList;
    private final String NICKNAME_TAKEN = "This alias is already taken, choose a different one\n";
    private final String NICKNAME_VALID = "Nickname valid, welcome to the server\n";

    private ExecutorService workerThreads = Executors.newFixedThreadPool(100);

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    private Server() {
        try {

            workersList = Collections.synchronizedMap(new Hashtable<>());
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

                BufferedReader in = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(workerSocket.getOutputStream()));

                ServerWorker serverWorker = new ServerWorker(workerSocket);

                String clientNickName = in.readLine();
                System.out.println("clt nick " + clientNickName);

                workersList.put(clientNickName, serverWorker);

                while (workersList.keySet().contains(clientNickName)) {
                    out.write(NICKNAME_TAKEN);
                    out.newLine();
                    out.flush();
                    clientNickName = in.readLine();
                    System.out.println("nickname + " + clientNickName);
                    workersList.put(clientNickName, serverWorker);
                }

                System.out.println("New client connected from: " + serverSocket + " nickname: " + clientNickName);

                out.write(NICKNAME_VALID);
                out.newLine();
                out.flush();

                serverWorker.setWorkerIn(in);
                serverWorker.setWorkerOut(out);


                System.out.println(workersList.keySet());
                workerThreads.submit(serverWorker);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void broadcast(String messageToBroadcast) {
        for (ServerWorker worker : workersList.values()) {
            worker.sendMessage(messageToBroadcast);
        }
    }

    //**********************************************
    private class ServerWorker implements Runnable {

        private Socket workerSocket;
        private BufferedReader workerIn;
        private BufferedWriter workerOut;

        public void setWorkerIn(BufferedReader workerIn) {
            this.workerIn = workerIn;
        }

        public void setWorkerOut(BufferedWriter workerOut) {
            this.workerOut = workerOut;
        }

        public ServerWorker(Socket workerSocket) {
            this.workerSocket = workerSocket;
            //openStreams();
        }

        @Override
        public void run() {

            String receivedMessage;

            while (!workerSocket.isClosed()) {
                try {
                    receivedMessage = workerIn.readLine();

                    if ((receivedMessage) == null) {
                        return;
                    }

                    if (receivedMessage.charAt(0) == '/') {
                        processCommand(receivedMessage);
                        continue;
                    }

                    System.out.println("Message from " + workerSocket + " Message: " + receivedMessage);
                    broadcast("Message from " + workerSocket + " Message: " + receivedMessage);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void processCommand(String userCommand) {

            String command = userCommand.split("=")[0];
            String commandArgument = userCommand.split("=")[1];

            switch (command) {
                case "/quit":
                    System.out.println("Closing connection from: " + workerSocket);
                    closeStreams();
                    workersList.remove(this);
                    break;
                case "/alias":
                    System.out.println("Change alias command");

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
