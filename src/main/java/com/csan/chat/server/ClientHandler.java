package com.csan.chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private static final int PORT = 4004;

    private static final String HOST = "localhost";

    private Server server;

    private PrintWriter outMessage;

    private Scanner inMessage;

    private int clientsCount;

    public ClientHandler(Socket clientSocket, Server server) {
        try {
            clientsCount++;
            this.server = server;
            this.outMessage = new PrintWriter(clientSocket.getOutputStream());
            this.inMessage = new Scanner(clientSocket.getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                server.sendMessageToAllClients("New member joined the chat!");
                server.sendMessageToAllClients(clientsCount + "members.");
                break;
            }

            while (true) {
                if (inMessage.hasNext()) {
                    String clientMessage = inMessage.nextLine();
                    if (clientMessage.equalsIgnoreCase("**stop")) {
                        break;
                    }
                    System.out.println(clientMessage);
                    server.sendMessageToAllClients(clientMessage);
                }
                // останавливаем выполнение потока на 100 мс
                Thread.sleep(100);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            this.leave();
        }
    }

    // отправляем сообщение
    public void sendMsg(String msg) {
        try {
            outMessage.println(msg);
            outMessage.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // клиент выходит из чата
    public void leave() {
        server.removeClient(this);
        clientsCount--;
        server.sendMessageToAllClients(clientsCount + "members");
    }

}