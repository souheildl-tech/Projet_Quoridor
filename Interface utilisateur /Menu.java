package com.quoridor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Menu extends Application {

    public void start(Stage stage) {
        
        Moteur monMoteur = new Moteur();
        Plateau monPlateau = new Plateau();
        

        ControleurJeu controleur = new ControleurJeu(monPlateau, monMoteur);
        
        //  Initialisation visuelle basée sur le modèle
        monPlateau.initialiserPionBlancVisuel(monMoteur.getPionBlancLigne(), monMoteur.getPionBlancCol());

        //  Affichage standard
        StackPane root = new StackPane(monPlateau);
        root.setStyle("-fx-padding: 20; -fx-background-color: #222222;");

        Scene scene = new Scene(root);
        stage.setTitle("Quoridor - Phase MVC (Refactoring)");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
