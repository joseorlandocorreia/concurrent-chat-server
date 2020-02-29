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

                if (messageFromServer == null) {
                    break;
                }

                System.out.println("Message Received: " + messageFromServer);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Server disconnected");
        closeConnections();
    }

    //Constructor
    public Client(String hostName, int portNumber) {

        try {
            serverSocket = new Socket(InetAddress.getByName(hostName), portNumber);
            openStreams();

            System.out.println("What is your desired alias?:");
            String clientNickName = keyboardIn.readLine();

            if (clientNickName != null) {
                clientOut.write(clientNickName);
                clientOut.newLine();
                clientOut.flush();
                System.out.println(clientIn.readLine());
            }

        } catch (IOException e) {
            System.out.println("Server is down. Closing program");
            System.exit(200);
            //e.printStackTrace();
        }

    }

    private void closeConnections() {
        try {

            clientIn.close();
            System.out.println("clientIn Stream closed");

            clientOut.close();
            System.out.println("ClientOut Stream closed");

            serverSocket.close();
            System.out.println("Server Socket closed");

            System.out.println("Closing Client");
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
                    System.out.println("Message to send?: ");
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
