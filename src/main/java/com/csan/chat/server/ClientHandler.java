package com.csan.chat.server;

import java.io.*;
import java.net.*;

class ClientHandler extends Thread {

    private Socket clientSocket;
    private BufferedReader inSocket; // поток чтения из сокета
    private BufferedWriter outSocket; // поток завписи в сокет


    public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        inSocket = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        outSocket = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        start();
    }

    @Override
    public void run() {
        String word;
        try {
            // первое сообщение отправленное сюда - никнейм
            word = inSocket.readLine();
            try {
                outSocket.write(word + "\n");
                outSocket.flush();
            } catch (IOException ignored) {
            }
            try {
                while (true) {
                    word = inSocket.readLine();
                    if (word.equals("\\stop")) {
                        this.downService();
                        break;
                    }
                    System.out.println("Echoing: " + word);
                    for (ClientHandler vr : Server.serverList) {
                        vr.send(word); // отослать принятое сообщение с привязанного клиента всем остальным влючая его
                    }
                }
            } catch (NullPointerException ignored) {
            }
        } catch (IOException e) {
            this.downService();
        }
    }

    private void send(String message) {
        try {
            outSocket.write(message + "\n");
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
                for (ClientHandler vr : Server.serverList) {
                    if (vr.equals(this)) {
                        vr.interrupt();
                    }
                    Server.serverList.remove(this);
                }
            }
        } catch (IOException ignored) {
        }
    }
}