package com.csan.chat.client;

import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

class ClientSomething {
    private static final String SERVER_HOST = "localhost";
    private static final int PORT = 4004;

    private Socket clientSocket;
    private BufferedReader inSocket; // поток чтения из сокета
    private BufferedWriter outSocket; // поток чтения в сокет
    private BufferedReader inputUser; // поток чтения с консоли
    private String nickname;

    public ClientSomething() {
        try {
            this.clientSocket = new Socket(SERVER_HOST, PORT);
        } catch (IOException e) {
            System.err.println("Socket failed");
        }
        try {
            inputUser = new BufferedReader(new InputStreamReader(System.in));
            inSocket = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outSocket = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.enterNickname();
            new ReadMsg().start(); // нить читающая сообщения из сокета в бесконечном цикле
            new WriteMsg().start(); // нить пишущая сообщения в сокет приходящие с консоли в бесконечном цикле
        } catch (IOException e) {
            ClientSomething.this.downService();
        }
    }

    private void enterNickname() {
        try {
            nickname = inputUser.readLine();
            outSocket.write("Hello " + nickname + "\n");
            outSocket.flush();
        } catch (IOException ignored) {
        }

    }

    private void downService() {
        try {
            if (!clientSocket.isClosed()) {
                clientSocket.close();
                inSocket.close();
                outSocket.close();
            }
        } catch (IOException ignored) {}
    }

    private class ReadMsg extends Thread {

        @Override
        public void run() {

            String serverMessage;
            try {
                while (true) {
                    serverMessage = inSocket.readLine();
                    if (serverMessage.equals("\\stop")) {
                        ClientSomething.this.downService();
                        break;
                    }
                    System.out.println(serverMessage);
                }
            } catch (IOException e) {
                ClientSomething.this.downService();
            }
        }
    }

    public class WriteMsg extends Thread {

        @Override
        public void run() {
            while (true) {
                String userMessage;
                try {
                    Date time = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    String timeString = dateFormat.format(time);
                    userMessage = inputUser.readLine();
                    if (userMessage.equals("\\stop")) {
                        outSocket.write("\\stop" + "\n");
                        ClientSomething.this.downService();
                        break;
                    } else {
                        outSocket.write("[" + timeString + "] " + nickname + ": " + userMessage + "\n"); // отправляем на сервер
                    }
                    outSocket.flush();
                } catch (IOException e) {
                    ClientSomething.this.downService();
                }
            }
        }
    }
}

public class Client {
    public static void main(String[] args) {
        new ClientSomething();
    }
}