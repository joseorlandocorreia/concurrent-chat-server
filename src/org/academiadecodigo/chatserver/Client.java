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

        //Client client = new Client("192.168.250.205", 6666);

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

                if (messageFromServer != null) {
                    System.out.println("Message Received: " + messageFromServer);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    //Constructor
    public Client(String hostName, int portNumber) {

        try {
            serverSocket = new Socket(InetAddress.getByName(hostName), portNumber);
            openStreams();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void closeConnections() {
        try {

            clientIn.close();
            System.out.println("client In Stream closed");

            clientOut.close();
            System.out.println("Client Out Stream closed");

            serverSocket.close();
            System.out.println("Server Socket closed");

            System.exit(100);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientRunnable implements Runnable {

        String clientAlias;

        public ClientRunnable() {
            try {
                System.out.println("What is your nickname?: ");
                clientAlias = keyboardIn.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            while (!serverSocket.isClosed()) {

                try {
                    System.out.println("Message to send?: ");
                    String clientMessage = keyboardIn.readLine();

                    clientOut.write("["+ clientAlias + "]: " + clientMessage);
                    clientOut.newLine();
                    clientOut.flush();

                    if (clientMessage.equals("/quit")) {
                        closeConnections();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
