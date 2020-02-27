package org.academiadecodigo.chatserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket serverSocket;
    private Scanner keyboardIn;
    private OutputStream clientOut;
    private InputStream clientIn;

    public static void main(String[] args) {

        Client client = new Client("127.0.0.1", 55555);

        Thread clientInThread = new Thread(client.new ClientRunnable());
        clientInThread.start();

        client.start();

    }

    private void start() {
        String messageFromServer;
        byte[] readBuffer = new byte[1024];
        int numberOfCharsRead;

        while (true) {
            try {

                numberOfCharsRead = clientIn.read(readBuffer);
                messageFromServer = new String(readBuffer, 0, numberOfCharsRead);
                System.out.print("Message Received: " + messageFromServer);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Constructor
    public Client(String hostName, int portNumber) {

        try {

            keyboardIn = new Scanner(System.in);
            serverSocket = new Socket(InetAddress.getByName(hostName), portNumber);
            clientOut = serverSocket.getOutputStream();
            clientIn = serverSocket.getInputStream();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientRunnable implements Runnable {
        String clientMessage;

        @Override
        public void run() {

            while (true) {
                System.out.println("Message to send?: ");
                clientMessage = keyboardIn.nextLine();
                sendMessage(clientMessage);
            }
        }

        private void sendMessage(String messageToSend) {
            try {
                clientOut.write(messageToSend.getBytes(), 0, messageToSend.length());
                clientOut.write("\n".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
