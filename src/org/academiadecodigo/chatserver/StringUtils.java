package org.academiadecodigo.chatserver;

public class StringUtils {
    public static final String NICKNAME_TAKEN = "This alias is already taken, choose a different one\n";
    public static final String NICKNAME_VALID = "Nickname valid, welcome to the server\n";
    public static final String CLIENT_NICKNAME_STR = "Client nickname: ";
    public static final String NEW_CONNECTION = "New client connected from: ";
    public static final String MESSAGE_FROM= "New message receivde from";
    public static final String MESSAGE_CONTENTS = "Message content: ";
    public static final String CLOSING_CONNECTION = "Quit command received, closing connection from: ";
    public static final String CHANGE_ALIAS_MSG = "Change alias command";

    //Client Strings
    public static final String MESSAGE_RECEIVED = "Message received from server: ";
    public static final String SERVER_DISCONNECTED = "Server disconnected";
    public static final String ASK_ALIAS = "What is your desired alias?: ";
    public static final String SERVER_DOWN = "Server is down, exiting program";
    public static final String IN_STREAM_CLOSING = "Closing ClientInput Stream";
    public static final String OUT_STREAM_CLOSING = "Closing ClientOutput Stream";
    public static final String SERVER_SOCKET_CLOSING = "Closing Server Socket";
    public static final String PROGRAM_CLOSING = "Exiting program";
    public static final String ASK_MESSAGE = "Message to send?: ";
}
