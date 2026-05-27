package com.quoridor;

import java.io.BufferedReader; 
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Interface de communication réseau gérant l'instanciation du moteur Python et la transmission des trames TCP
public class QuoridorClient {

    // Composants de l'architecture socket pour l'établissement du tunnel de communication bidirectionnel
    private Socket socketClient;
    private PrintWriter fluxSortant;
    private BufferedReader fluxEntrant;
    
    // Instance statique du processus système encapsulant l'exécution de l'Intelligence Artificielle
    private static Process processusServeurPython;

    // Initialise l'environnement d'exécution externe pour le script Python via une invocation système asynchrone
    public void lancerServeurPythonLocal() {
        try {
            System.out.println("Démarrage du serveur Python en arrière-plan...");
            
            // Construction de la commande système pointant vers l'exécutable Python et le script cible
            ProcessBuilder constructeurProcessus = new ProcessBuilder("python3", "serveur.py");
            
            // Fusion des flux d'erreur et de sortie standard pour unifier la capture de la journalisation
            constructeurProcessus.redirectErrorStream(true); 
            processusServeurPython = constructeurProcessus.start();
            
            // Délégation de la lecture des logs à un fil d'exécution secondaire pour éviter le blocage de l'interface JavaFX
            new Thread(() -> {
                try (BufferedReader lecteur = new BufferedReader(new InputStreamReader(processusServeurPython.getInputStream()))) {
                    String ligneConsole;
                    while ((ligneConsole = lecteur.readLine()) != null) {
                        System.out.println("[Message de Python] : " + ligneConsole);
                    }
                } catch (IOException erreurLecture) {
                    erreurLecture.printStackTrace();
                }
            }).start();
            
            // Suspension temporelle du fil principal garantissant l'ouverture et l'écoute effective du port réseau par le script Python
            Thread.sleep(1500); 
            
        } catch (IOException | InterruptedException erreur) {
            System.err.println("Erreur critique : Impossible de lancer le cerveau Python");
            erreur.printStackTrace();
        }
    }

    // Établissement de la connexion TCP orientée connexion avec l'adresse IP et le port de destination spécifiés
    public void demarrerConnexion(String adresseIp, int port) throws IOException {
        socketClient = new Socket(adresseIp, port);
        fluxSortant = new PrintWriter(socketClient.getOutputStream(), true);
        fluxEntrant = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
    }

    // Sérialisation et expédition d'une requête textuelle suivie d'une attente synchrone de la réponse du serveur
    public String envoyerMessage(String message) throws IOException {
        fluxSortant.println(message);
        return fluxEntrant.readLine();
    }

    // Procédure de nettoyage libérant les ressources réseau et interrompant l'exécution du processus enfant Python
    public void arreterConnexion() throws IOException {
        if (fluxEntrant != null) fluxEntrant.close();
        if (fluxSortant != null) fluxSortant.close();
        if (socketClient != null) socketClient.close();
        
        // Transmission d'un signal de terminaison système (SIGTERM) pour détruire proprement le serveur d'Intelligence Artificielle
        if (processusServeurPython != null) {
            processusServeurPython.destroy();
        }
    }
}
