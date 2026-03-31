package com.quoridor;

// Outils nécessaires pour la connexion réseau et la lecture/écriture de texte
import java.io.BufferedReader; 
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Gère la communication réseau entre le jeu Java et le cerveau Python
public class QuoridorClient {

    // Éléments gérant la connexion et l'échange de texte avec le serveur
    private Socket socketClient;
    private PrintWriter fluxSortant;
    private BufferedReader fluxEntrant;
    
    // Garde en mémoire le processus Python pour pouvoir le fermer à la fin
    private static Process processusServeurPython;

    // Ouvre la connexion réseau et prépare les outils d'envoi et de réception
    public void demarrerConnexion(String adresseIp, int port) throws IOException {
        socketClient = new Socket(adresseIp, port);
        fluxSortant = new PrintWriter(socketClient.getOutputStream(), true);
        fluxEntrant = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
    }

    // Envoie une commande au serveur et attend sa réponse en retour
    public String envoyerMessage(String message) throws IOException {
        fluxSortant.println(message);
        return fluxEntrant.readLine();
    }

    // Ferme proprement tous les flux et coupe la connexion réseau
    public void arreterConnexion() throws IOException {
        fluxEntrant.close();
        fluxSortant.close();
        socketClient.close();
    }

    // Programme de test indépendant pour vérifier que la communication fonctionne
    public static void main(String[] arguments) {
        
        // Tente d'allumer le serveur Python automatiquement en arrière-plan
        try {
            System.out.println("Démarrage du serveur Python...");
            
            ProcessBuilder constructeurProcessus = new ProcessBuilder("python3", "serveur.py");
            constructeurProcessus.redirectErrorStream(true); 
            
            processusServeurPython = constructeurProcessus.start();
            Thread.sleep(1000); 
            
        // Attrape et affiche les erreurs si Python n'arrive pas à se lancer
        } catch (IOException | InterruptedException erreur) {
            System.err.println("Impossible de lancer le serveur Python !");
            erreur.printStackTrace();
            return;
        }

        QuoridorClient clientTest = new QuoridorClient();
        
        // Tente de se connecter au serveur et d'échanger des messages de test
        try {
            System.out.println("Tentative de connexion...");
            clientTest.demarrerConnexion("127.0.0.1", 65432);
            System.out.println("Connecté !");

            String reponse = clientTest.envoyerMessage("Ping");
            System.out.println("Envoi: Ping | Reçu: " + reponse);

            reponse = clientTest.envoyerMessage("Salut Python");
            System.out.println("Envoi: Salut Python | Reçu: " + reponse);
            
            clientTest.arreterConnexion();
            
        // Gère les erreurs de réseau pendant les tests
        } catch (IOException erreurReseau) {
            erreurReseau.printStackTrace();
            System.err.println("Erreur : Impossible de se connecter au serveur Python.");
            
        // Coupe systématiquement le serveur Python à la fin des tests
        } finally {
            if (processusServeurPython != null) {
                System.out.println("Fermeture du serveur Python...");
                processusServeurPython.destroy();
            }
        }
    }
}
