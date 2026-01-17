package com.quoridor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Menu extends Application {

  
    public void start(Stage stage) {
        
        // 1. Création du Plateau
        Plateau monPlateau = new Plateau();
        
        // 2. On le centre dans la fenêtre via un StackPane
        StackPane root = new StackPane(monPlateau);
        
        
        root.setStyle("-fx-padding: 20; -fx-background-color: #222222;");

        // 3. Création de la scène
        Scene scene = new Scene(root);
        
        // 4. Configuration de la fenêtre
        stage.setTitle("Quoridor - Menu Principal");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
