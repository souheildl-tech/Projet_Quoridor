package com.quoridor;

// Importation des bibliothèques JavaFX nécessaires pour l'interface graphique.
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

// Classe principale gérant le menu de lancement du jeu Quoridor.
public class Menu extends Application {

    // Garde le conteneur en mémoire pour pouvoir le désactiver pendant le chargement
    private VBox racineMenu;

    // Méthode de démarrage appelée automatiquement par JavaFX.
    public void start(Stage fenetre) {

        // Initialisation de l'étiquette contenant le titre du jeu.
        Label titre = new Label("Quoridor");
        titre.setFont(Font.font("Century Gothic", javafx.scene.text.FontPosture.ITALIC, 55));
        titre.setTextFill(Color.WHITE);

        // Ajout d'une ombre portée pour donner du relief au titre.
        DropShadow eclat = new DropShadow();
        eclat.setColor(Color.web("#000000")); 
        titre.setEffect(eclat); 

        // Création des boutons pour choisir son adversaire
        Button boutonMinimax = creerBouton("JOUER VS MINIMAX");
        Button boutonMCTS = creerBouton("JOUER VS MCTS");
        Button boutonQuitter = creerBouton("QUITTER");

        // Conteneur vertical pour centrer le titre et les boutons de menu
        racineMenu = new VBox(30); 
        racineMenu.setAlignment(Pos.CENTER);
        racineMenu.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 80%, #a71919, #a71919);");
        racineMenu.getChildren().addAll(titre, boutonMinimax, boutonMCTS, boutonQuitter);

        // Configuration du bouton d'affichage des règles
        Button boutonRegles = new Button("⚙️");
        boutonRegles.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 40px; -fx-cursor: hand;");
        
        // Agrandissement du bouton des règles lors du survol de la souris
        boutonRegles.setOnMouseEntered(e -> { boutonRegles.setScaleX(1.2); boutonRegles.setScaleY(1.2); });
        boutonRegles.setOnMouseExited(e -> { boutonRegles.setScaleX(1.0); boutonRegles.setScaleY(1.0); });
        boutonRegles.setOnAction(e -> afficherRegles(fenetre));

        // Superposition des éléments avec le bouton d'options placé en haut à droite
        StackPane calquePrincipal = new StackPane();
        calquePrincipal.getChildren().addAll(racineMenu, boutonRegles);
        StackPane.setAlignment(boutonRegles, Pos.TOP_RIGHT);
        StackPane.setMargin(boutonRegles, new Insets(10));

        // Définition de la scène principale du menu
        Scene sceneMenu = new Scene(calquePrincipal, 600, 450);

        // Action déclenchée lors du clic sur les boutons de jeu
        boutonMinimax.setOnAction(event -> lancerJeu(fenetre, boutonMinimax, "MINIMAX"));
        boutonMCTS.setOnAction(event -> lancerJeu(fenetre, boutonMCTS, "MCTS"));

        // Fermeture de l'application lors du clic sur le bouton quitter
        boutonQuitter.setOnAction(event -> Platform.exit());

        // Configuration finale de la fenêtre principale
        fenetre.setTitle("Quoridor");
        fenetre.setResizable(false);
        fenetre.setScene(sceneMenu);
        fenetre.setMaximized(true);
        fenetre.show();
    }

    // Lance le jeu en transmettant le choix de l'IA au contrôleur
    private void lancerJeu(Stage fenetre, Button boutonClique, String modeIA) {
        boutonClique.setText("CHARGEMENT...");
        racineMenu.setDisable(true);

        // Mise en place d'une courte pause pour laisser l'interface se mettre à jour
        PauseTransition pause = new PauseTransition(Duration.seconds(0.1));
        pause.setOnFinished(e -> {
            
            // Initialisation de l'architecture du jeu
            Moteur moteur = new Moteur();
            Plateau vuePlateau = new Plateau();
            
            // Transmet le choix de l'algorithme au contrôleur principal
            ControleurJeu controleur = new ControleurJeu(moteur, vuePlateau, modeIA);

            // Préparation de la scène de jeu et application à la fenêtre
            StackPane racineJeu = new StackPane(vuePlateau);
            racineJeu.setStyle("-fx-padding: 20; -fx-background-color: #222;");
            Scene sceneJeu = new Scene(racineJeu);

            // Remplace le menu par le plateau de jeu en ajoutant le mode dans le titre
            fenetre.setScene(sceneJeu);
            fenetre.setTitle("Quoridor - En jeu (" + modeIA + ")");
            fenetre.centerOnScreen(); 
        });
        pause.play(); 
    }

    // Méthode gérant l'affichage de la fenêtre secondaire contenant les règles.
    private void afficherRegles(Stage fenetreParente) {
        Stage fenetreRegles = new Stage();
        fenetreRegles.initModality(Modality.APPLICATION_MODAL); 
        fenetreRegles.initOwner(fenetreParente);
        fenetreRegles.setTitle("Options");

        // Conteneur vertical pour le texte des règles.
        VBox miseEnPage = new VBox(15);
        miseEnPage.setPadding(new Insets(25));
        miseEnPage.setStyle("-fx-background-color: #1a1a1a;"); 
        miseEnPage.setAlignment(Pos.CENTER);

        // Titre de la fenêtre des règles.
        Label titre = new Label("Comment jouer ?");
        titre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Corps du texte expliquant les commandes du jeu.
        Label texte = new Label(
            "• Déplacement : Clic sur une case en bois.\n" +
            "• Mur Vertical : Clic sur l'espace vertical entre les cases.\n" +
            "• Mur Horizontal : Clic sur l'espace horizontal entre les cases."
        );
        texte.setStyle("-fx-font-size: 14px; -fx-text-fill: #cccccc; -fx-line-spacing: 0.5em;");
        texte.setWrapText(true);

        // Bouton de fermeture de la fenêtre des règles.
        Button boutonOk = new Button("FERMER");
        boutonOk.setStyle("-fx-background-color: #a71919; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20; -fx-background-radius: 15;");
        boutonOk.setOnAction(e -> fenetreRegles.close());

        // Ajout des éléments au conteneur puis affichage.
        miseEnPage.getChildren().addAll(titre, texte, boutonOk);

        Scene scene = new Scene(miseEnPage, 420, 320);
        fenetreRegles.setScene(scene);
        fenetreRegles.setResizable(false);
        fenetreRegles.showAndWait();
    }
    
    // Méthode utilitaire pour générer des boutons avec un style graphique unifié.
    private Button creerBouton(String texte) {
        Button bouton = new Button(texte);
        String styleNormal = "-fx-background-color: linear-gradient(#000000, #000000); -fx-background-radius: 30; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 18px; -fx-padding: 12 40; -fx-cursor: hand;";
        String styleSurvol = "-fx-background-color: linear-gradient(#1f3b58, #0a1118); -fx-background-radius: 30; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 18px; -fx-padding: 12 40; -fx-cursor: hand;";

        bouton.setStyle(styleNormal);

        // Création d'une ombre portée sous le bouton.
        DropShadow ombre = new DropShadow();
        ombre.setColor(Color.grayRgb(0, 0.4));
        ombre.setOffsetY(4);
        bouton.setEffect(ombre);

        // Animation d'agrandissement et changement de couleur au survol.
        bouton.setOnMouseEntered(e -> {
            if (!bouton.isDisabled()) {
                bouton.setStyle(styleSurvol);
                bouton.setScaleX(1.05); 
                bouton.setScaleY(1.05);
            }
        });

        // Retour à l'état normal lorsque la souris quitte le bouton.
        bouton.setOnMouseExited(e -> {
            if (!bouton.isDisabled()) {
                bouton.setStyle(styleNormal);
                bouton.setScaleX(1.0); 
                bouton.setScaleY(1.0);
            }
        });

        return bouton;
    }

    // Point d'entrée de l'application JavaFX.
    public static void main(String[] args) {
        launch(args);
    }
}
