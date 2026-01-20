package com.quoridor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Menu extends Application {

  
    public void start(Stage stage) {
        
        // 1. Création du Plateau
        Plateau monPlateau = new Plateau();
        
        // Joueur 1 (Blanc) en bas au milieu (Ligne 8, Colonne 4)
        monPlateau.placerPion(8, 4, Color.WHITE);
        
        // Joueur 2 (Noir) en haut au milieu (Ligne 0, Colonne 4)
        monPlateau.placerPion(0, 4, Color.BLACK);

        // 2. Mise en page
        StackPane root = new StackPane(monPlateau);
        root.setStyle("-fx-padding: 20; -fx-background-color: #222222;");

        // 3. Création de la scène et configuration de la fenêtre
        Scene scene = new Scene(root);
        
        stage.setTitle("Quoridor ");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
