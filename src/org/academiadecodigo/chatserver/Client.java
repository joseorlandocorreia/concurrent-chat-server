package org.academiadecodigo.chatserver;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket serverSocket;

    private BufferedReader keyboardIn;
    private BufferedReader clientIn;
    private BufferedWriter clientOut;

    public static void main(String[] args) {

        Client client = new Client("127.0.0.1", 6666);

        Thread clientThread = new Thread(client.new ClientRunnable());
        clientThread.start();

        client.start();

    }

    private void openStreams() {

        try {
            keyboardIn = new BufferedReader(new InputStreamReader(System.in));
            clientIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            clientOut = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void start() {

        while (!serverSocket.isClosed()) {

            try {

                String messageFromServer = clientIn.readLine();

                if (messageFromServer == null) {
                    break;
                }

                System.out.println(StringUtils.MESSAGE_RECEIVED + messageFromServer);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(StringUtils.SERVER_DISCONNECTED);
        closeConnections();
    }

    //Constructor
    public Client(String hostName, int portNumber) {

        try {
            serverSocket = new Socket(InetAddress.getByName(hostName), portNumber);
            openStreams();

            System.out.println(StringUtils.ASK_ALIAS);
            String clientNickName = keyboardIn.readLine();

            if (clientNickName != null) {
                clientOut.write(clientNickName);
                clientOut.newLine();
                clientOut.flush();
                System.out.println(clientIn.readLine());
            }

        } catch (IOException e) {
            System.out.println(StringUtils.SERVER_DOWN);
            System.exit(200);
        }

    }

    private void closeConnections() {
        try {

            clientIn.close();
            System.out.println(StringUtils.IN_STREAM_CLOSING);

            clientOut.close();
            System.out.println(StringUtils.OUT_STREAM_CLOSING);

            serverSocket.close();
            System.out.println(StringUtils.SERVER_SOCKET_CLOSING);

            System.out.println(StringUtils.PROGRAM_CLOSING);
            System.exit(100);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientRunnable implements Runnable {

        @Override
        public void run() {

            while (!serverSocket.isClosed()) {

                try {
                    System.out.println(StringUtils.ASK_MESSAGE);
                    String clientMessage = keyboardIn.readLine();

                    clientOut.write(clientMessage);
                    clientOut.newLine();
                    clientOut.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
