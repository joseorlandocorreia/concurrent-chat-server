package org.academiadecodigo.chatserver;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket serverSocket;

    BufferedReader keyboardIn;
    BufferedReader clientIn;
    BufferedWriter clientOut;

    public static void main(String[] args) {

        Client client = new Client("127.0.0.1", 55555);

        Thread clientInThread = new Thread(client.new ClientRunnable());
        clientInThread.start();

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
        String messageFromServer;

        while (true) {
            try {

                messageFromServer = clientIn.readLine();
                System.out.println("Message Received: " + messageFromServer);

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

    private class ClientRunnable implements Runnable {
        String clientMessage;

        @Override
        public void run() {

            while (true) {

                try {
                    System.out.println("Message to send?: ");
                    clientMessage = keyboardIn.readLine();

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
