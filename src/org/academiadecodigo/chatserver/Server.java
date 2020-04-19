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
                System.out.println(StringUtils.CLIENT_NICKNAME_STR + clientNickName);

                while (workersList.containsKey(clientNickName)) {
                    out.write(StringUtils.NICKNAME_TAKEN);
                    out.newLine();
                    out.flush();
                    clientNickName = in.readLine();
                    System.out.println(StringUtils.CLIENT_NICKNAME_STR + clientNickName);
                    workersList.put(clientNickName, serverWorker);
                }

                System.out.println(StringUtils.NEW_CONNECTION + serverSocket + StringUtils.CLIENT_NICKNAME_STR + clientNickName);

                out.write(StringUtils.NICKNAME_VALID);
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
            openStreams();
        }

        @Override
        public void run() {

            String receivedMessage;

            while (workerSocket.isConnected()) {
                try {
                    receivedMessage = workerIn.readLine();

                    if ((receivedMessage) == null) {
                        return;
                    }

                    if (receivedMessage.charAt(0) == '/') {
                        processCommand(receivedMessage);
                        continue;
                    }

                    System.out.println(StringUtils.MESSAGE_FROM + workerSocket + StringUtils.MESSAGE_CONTENTS + receivedMessage);
                    broadcast(StringUtils.MESSAGE_FROM + workerSocket + StringUtils.MESSAGE_FROM + receivedMessage);

                } catch (IOException e) {
                    closeStreams();
                }
            }
        }

        private void processCommand(String userCommand) {

            String command = userCommand.split("=")[0];
            String commandArgument = userCommand.split("=")[1];

            switch (command) {
                case "/quit":
                    System.out.println(StringUtils.CLOSING_CONNECTION + workerSocket);
                    closeStreams();
                    workersList.remove(this);
                    break;
                case "/alias":
                    System.out.println(StringUtils.CHANGE_ALIAS_MSG);
                    //todo, implement this functionality

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
            }

        }

    }

}
