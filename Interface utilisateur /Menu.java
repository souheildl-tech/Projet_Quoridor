package com.quoridor;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Menu extends Application {

    @Override
    public void start(Stage stage) {
        Label titre = new Label("Quoridor");
        titre.setFont(Font.font("Century Gothic", javafx.scene.text.FontPosture.ITALIC, 55));
        titre.setTextFill(Color.WHITE);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#000000")); 
        titre.setEffect(glow); 

        Button boutonJouer = creerBouton("NOUVELLE PARTIE");
        Button boutonQuitter = creerBouton("QUITTER");

        VBox menuRoot = new VBox(40); 
        menuRoot.setAlignment(Pos.CENTER);
        menuRoot.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 80%, #a71919, #a71919);");
        menuRoot.getChildren().addAll(titre, boutonJouer, boutonQuitter);

-
        Button boutonRegles = new Button("⚙️");
        boutonRegles.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 28px; -fx-cursor: hand;");
        
        boutonRegles.setOnMouseEntered(e -> { boutonRegles.setScaleX(1.2); boutonRegles.setScaleY(1.2); });
        boutonRegles.setOnMouseExited(e -> { boutonRegles.setScaleX(1.0); boutonRegles.setScaleY(1.0); });
        boutonRegles.setOnAction(e -> afficherRegles(stage));

        StackPane calquePrincipal = new StackPane();
        calquePrincipal.getChildren().addAll(menuRoot, boutonRegles);
        StackPane.setAlignment(boutonRegles, Pos.TOP_RIGHT);
        StackPane.setMargin(boutonRegles, new Insets(10));

        Scene sceneMenu = new Scene(calquePrincipal, 600, 450);


        boutonJouer.setOnAction(event -> {
            boutonJouer.setText("CHARGEMENT...");
            menuRoot.setDisable(true);

 
            PauseTransition pause = new PauseTransition(Duration.seconds(0.1));
            pause.setOnFinished(e -> {
    
                Moteur monMoteur = new Moteur();
                Plateau monPlateau = new Plateau();
                // Le contrôleur s'occupe de lancer Python, lier la vue et mémoriser le jeu
                ControleurJeu controleur = new ControleurJeu(monPlateau, monMoteur); 
                Scene sceneJeu = new Scene(monPlateau, 800, 700);
                stage.setScene(sceneJeu);
                stage.setTitle("Quoridor - Contre l'IA Minimax");
                stage.centerOnScreen(); 
            });
            
            pause.play(); 
        });

        boutonQuitter.setOnAction(event -> Platform.exit());


        stage.setTitle("Quoridor - Projet PRO3600");
        stage.setResizable(false);
        stage.setScene(sceneMenu);
        stage.show();
    }

    private void afficherRegles(Stage parentStage) {
        Stage fenetreRegles = new Stage();
        fenetreRegles.initModality(Modality.APPLICATION_MODAL); 
        fenetreRegles.initOwner(parentStage);
        fenetreRegles.setTitle("Règles du Quoridor");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: #1a1a1a;"); 
        layout.setAlignment(Pos.CENTER);

        Label titre = new Label(" Comment jouer ?");
        titre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label texte = new Label(
            " Déplacement : Clic gauche sur une case (ou point vert).\n" +
            " Mur Vertical : Clic gauche sur l'espace à DROITE d'une case.\n" +
            " Mur Horizontal : Clic gauche sur l'espace en DESSOUS d'une case.\n\n" +
            " Il est strictement interdit d'enfermer totalement l'adversaire !\n\n" +
            " Saut : Si l'adversaire est devant vous, cliquez sur la case derrière lui pour sauter."
        );
        texte.setStyle("-fx-font-size: 14px; -fx-text-fill: #cccccc; -fx-line-spacing: 0.5em;");
        texte.setWrapText(true);

        Button btnOk = new Button("FERMER");
        btnOk.setStyle("-fx-background-color: #a71919; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20; -fx-background-radius: 15;");
        btnOk.setOnAction(e -> fenetreRegles.close());

        layout.getChildren().addAll(titre, texte, btnOk);
        Scene scene = new Scene(layout, 420, 320);
        fenetreRegles.setScene(scene);
        fenetreRegles.setResizable(false);
        fenetreRegles.showAndWait();
    }

    private Button creerBouton(String texte) {
        Button btn = new Button(texte);
        String styleNormal = "-fx-background-color: linear-gradient(#000000, #000000); -fx-background-radius: 30; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 18px; -fx-padding: 12 40; -fx-cursor: hand;";
        String styleHover = "-fx-background-color: linear-gradient(#1f3b58, #0a1118); -fx-background-radius: 30; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 18px; -fx-padding: 12 40; -fx-cursor: hand;";

        btn.setStyle(styleNormal);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.grayRgb(0, 0.4));
        shadow.setOffsetY(4);
        btn.setEffect(shadow);

        btn.setOnMouseEntered(e -> {
            if (!btn.isDisabled()) {
                btn.setStyle(styleHover);
                btn.setScaleX(1.05); 
                btn.setScaleY(1.05);
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.isDisabled()) {
                btn.setStyle(styleNormal);
                btn.setScaleX(1.0); 
                btn.setScaleY(1.0);
            }
        });

        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
