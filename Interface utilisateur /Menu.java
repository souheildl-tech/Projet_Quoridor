package com.quoridor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Menu extends Application {

    @Override
    public void start(Stage stage) {
        // 1. Instanciation de l'architecture
        Moteur monMoteur = new Moteur();
        Plateau monPlateau = new Plateau();
        ControleurJeu controleur = new ControleurJeu(monPlateau, monMoteur);

        // 2. Création de la scène (on met le Plateau qui est un BorderPane)
        Scene scene = new Scene(monPlateau);
        
        // 3. Configuration de la fenêtre
        stage.setTitle("Quoridor - Projet PRO3600");
        stage.setResizable(false);
        stage.setScene(scene);
        
        stage.setOnCloseRequest(e -> {
            System.out.println("Fermeture du jeu...");
            System.exit(0); 
        });
        
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
