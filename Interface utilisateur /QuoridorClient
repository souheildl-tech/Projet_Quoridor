package com.quoridor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class QuoridorClient {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    // 1. Se connecter au serveur Python
    public boolean startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            return true; 
        } catch (IOException e) {
            System.err.println("Erreur de connexion ?");
            return false;
        }
    }

    // 2. Envoyer un message et attendre la réponse
    public String sendMessage(String msg) {
        if (out == null || in == null) {
            return "Erreur : Non connecté au serveur.";
        }
        try {
            out.println(msg);
            return in.readLine();
        } catch (IOException e) {
            return "Erreur de communication avec le serveur.";
        }
    }

    // 3. Fermer proprement les flux réseau
    public void stopConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
            System.out.println("Connexion stopé.");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'arrêt de la connexion.");
        }
    }


    public static void main(String[] args) {
        QuoridorClient client = new QuoridorClient();
        
        System.out.println("Tentative de connexion au serveur Python...");
        if (client.startConnection("127.0.0.1", 65432)) {
            System.out.println("Connecté avec succès !");

           
            String response1 = client.sendMessage("Ping");
            System.out.println("Envoi : Ping | Reçu : " + response1);

         
            String response2 = client.sendMessage("Message envoyé à Python !");
            System.out.println("Envoi : Message envoyé à Python ! | Reçu : " + response2);

            client.stopConnection();
        }
    }
}
