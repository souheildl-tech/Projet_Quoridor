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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

// Classe principale gérant l'interface graphique du menu pour le jeu Quoridor
public class Menu extends Application {

    // Fenêtre et scène principales contenant les différents écrans de l'application
    private Stage fenetrePrincipale;
    private Scene sceneMenu;

    // Point d'entrée initial de l'application JavaFX
    public void start(Stage fenetre) {
        this.fenetrePrincipale = fenetre;
        sceneMenu = new Scene(creerEcranAccueil(), 1100, 700);
        fenetre.setTitle("Quoridor - Accueil");
        fenetre.setScene(sceneMenu);
        fenetre.setMaximized(true); 
        fenetre.show();
    }

    // Construit et retourne l'architecture visuelle de l'écran de démarrage
    private StackPane creerEcranAccueil() {
        VBox contenuCentral = new VBox(35);
        contenuCentral.setAlignment(Pos.CENTER);
        
        // Configuration de l'arrière-plan avec un dégradé radial
        StackPane racineAccueil = new StackPane();
        racineAccueil.setStyle("-fx-background-color: radial-gradient(center 10% 10%, radius 100%, #3b4d75, #000000);");

        Label titre = new Label("QUORIDOR");
        titre.setFont(Font.font("Century Gothic", FontWeight.BOLD, 85));
        titre.setTextFill(Color.web("#e0af68")); 
        
        // Application d'une ombre portée pour accentuer la profondeur du titre
        DropShadow ombreSimple = new DropShadow();
        ombreSimple.setColor(Color.web("#000000", 0.6));
        ombreSimple.setOffsetY(4);
        titre.setEffect(ombreSimple);

        Button boutonJouer = creerBoutonSimple("JOUER UNE PARTIE");
        Button boutonQuitter = creerBoutonSimple("QUITTER");

        // Attribution des actions déclenchées lors du clic sur les boutons
        boutonJouer.setOnAction(e -> sceneMenu.setRoot(creerEcranSelectionMode()));
        boutonQuitter.setOnAction(e -> Platform.exit());

        contenuCentral.getChildren().addAll(titre, boutonJouer, boutonQuitter);

        // Intégration du bouton permettant d'afficher les règles du jeu
        Button boutonRegles = new Button("⚙️");
        boutonRegles.setStyle("-fx-background-color: transparent; -fx-text-fill: #e0af68; -fx-font-size: 45px; -fx-cursor: hand;");
        boutonRegles.setOnAction(e -> afficherRegles());
        
        racineAccueil.getChildren().addAll(contenuCentral, boutonRegles);
        StackPane.setAlignment(boutonRegles, Pos.TOP_RIGHT);
        StackPane.setMargin(boutonRegles, new Insets(20));

        return racineAccueil;
    }

    // Construit l'interface de sélection des adversaires virtuels
    private HBox creerEcranSelectionMode() {
        HBox racineSelection = new HBox();
        racineSelection.setStyle("-fx-background-color: linear-gradient(to bottom right, #3b4d75, #000000);"); 

        // Ajout d'un plateau statique en arrière-plan pour l'esthétique
        Plateau plateauDeco = new Plateau();
        plateauDeco.setDisable(true); 
        StackPane zonePlateau = new StackPane(plateauDeco);
        zonePlateau.setAlignment(Pos.CENTER);
        zonePlateau.setStyle("-fx-background-color: transparent;");
        HBox.setHgrow(zonePlateau, Priority.ALWAYS); 

        // Panneau latéral contenant les différentes options de jeu
        VBox panneauMenu = new VBox(15);
        panneauMenu.setPrefWidth(400);
        panneauMenu.setMinWidth(400);
        panneauMenu.setStyle("-fx-background-color: #1b1f2e; -fx-padding: 30;"); 
        panneauMenu.setAlignment(Pos.TOP_LEFT);

        Label titreMenu = new Label("♟ Choisissez le mode");
        titreMenu.setStyle("-fx-text-fill: #e0af68; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Century Gothic';");
        VBox.setMargin(titreMenu, new Insets(20, 0, 30, 0));

        // Création des cartes interactives pour l'algorithme Minimax et le MCTS
        HBox btnMinimax = creerCarteMenu("♣", "Jouer vs Minimax", "Défiez le Minimax", () -> afficherChoixDifficulte("MINIMAX"));
        HBox btnMCTS = creerCarteMenu("◉", "Jouer vs MCTS", "Défiez le MCTS", () -> afficherChoixDifficulte("MCTS"));
        HBox btnArena = creerCarteMenu("⚔️", "Mode Arène", "Observez les IA s'affronter", () -> afficherChoixDifficulte("ARENA"));
        HBox btnRetour = creerCarteMenu("⬅️", "Retour", "Revenir à l'écran titre", () -> sceneMenu.setRoot(creerEcranAccueil()));

        panneauMenu.getChildren().addAll(titreMenu, btnMinimax, btnMCTS, btnArena, btnRetour);
        racineSelection.getChildren().addAll(zonePlateau, panneauMenu);

        return racineSelection;
    }

    // Affiche une fenêtre modale superposée pour configurer les paramètres de la partie
    private void afficherChoixDifficulte(String modeIA) {
        Stage fenetreDifficulte = new Stage();
        fenetreDifficulte.initModality(Modality.APPLICATION_MODAL); 
        fenetreDifficulte.initOwner(fenetrePrincipale);
        fenetreDifficulte.setTitle("Configuration de la partie");

        VBox miseEnPage = new VBox(15);
        miseEnPage.setPadding(new Insets(25));
        miseEnPage.setStyle("-fx-background-color: #1b1f2e; -fx-border-color: #e0af68; -fx-border-width: 3;"); 
        miseEnPage.setAlignment(Pos.CENTER);

        // Utilisation d'un tableau pour modifier la variable depuis une expression lambda
        final String[] choixPremierJoueur = {""};

        Label titreOrdre = new Label("1. QUI COMMENCE ?");
        titreOrdre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #88a0c0; -fx-font-family: 'Century Gothic';");
        
        HBox conteneurBoutons = new HBox(10);
        conteneurBoutons.setAlignment(Pos.CENTER);
        
        Button btn1, btn2, btn3;
        
        // Modification dynamique des boutons d'ordre de jeu selon le mode sélectionné
        if (modeIA.equals("ARENA")) {
            btn1 = creerBoutonToggle("MCTS", true);
            btn2 = creerBoutonToggle("Minimax", false);
            btn3 = creerBoutonToggle("Aléatoire", false);
            choixPremierJoueur[0] = "MCTS"; 

            btn1.setOnAction(e -> { gererSelection(btn1, btn2, btn3); choixPremierJoueur[0] = "MCTS"; });
            btn2.setOnAction(e -> { gererSelection(btn2, btn1, btn3); choixPremierJoueur[0] = "MINIMAX"; });
            btn3.setOnAction(e -> { gererSelection(btn3, btn1, btn2); choixPremierJoueur[0] = "ALEATOIRE"; });
        } else {
            btn1 = creerBoutonToggle("Moi", true);
            btn2 = creerBoutonToggle("L'IA", false);
            btn3 = creerBoutonToggle("Aléatoire", false);
            choixPremierJoueur[0] = "HUMAIN"; 

            btn1.setOnAction(e -> { gererSelection(btn1, btn2, btn3); choixPremierJoueur[0] = "HUMAIN"; });
            btn2.setOnAction(e -> { gererSelection(btn2, btn1, btn3); choixPremierJoueur[0] = "IA"; });
            btn3.setOnAction(e -> { gererSelection(btn3, btn1, btn2); choixPremierJoueur[0] = "ALEATOIRE"; });
        }

        conteneurBoutons.getChildren().addAll(btn1, btn2, btn3);
        miseEnPage.getChildren().addAll(titreOrdre, conteneurBoutons);
        
        Label titreNiveau = new Label("2. NIVEAU DE L'IA");
        titreNiveau.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #88a0c0; -fx-font-family: 'Century Gothic';");
        VBox.setMargin(titreNiveau, new Insets(10, 0, 0, 0));
        miseEnPage.getChildren().add(titreNiveau);

        // Définition des étiquettes de difficulté adaptées au comportement de l'algorithme choisi
        String descFacile = modeIA.equals("MINIMAX") ? "Profondeur 1" : (modeIA.equals("MCTS") ? "Réflexion : 0.5s" : "Match Rapide");
        String descMoyen = modeIA.equals("MINIMAX") ? "Profondeur 2" : (modeIA.equals("MCTS") ? "Réflexion : 1.5s" : "Match Standard");
        String descDifficile = modeIA.equals("MINIMAX") ? "Profondeur 3" : (modeIA.equals("MCTS") ? "Réflexion : 3.0s" : "Match Épique");

        HBox btnFacile = creerCarteDifficulte("△", "Facile", descFacile, () -> { fenetreDifficulte.close(); lancerJeu(modeIA, 1, choixPremierJoueur[0]); });
        HBox btnMoyen = creerCarteDifficulte("⟁", "Moyen", descMoyen, () -> { fenetreDifficulte.close(); lancerJeu(modeIA, 2, choixPremierJoueur[0]); });
        HBox btnDifficile = creerCarteDifficulte("▲", "Difficile", descDifficile, () -> { fenetreDifficulte.close(); lancerJeu(modeIA, 3, choixPremierJoueur[0]); });

        Button btnRetour = new Button("ANNULER");
        btnRetour.setStyle("-fx-background-color: transparent; -fx-text-fill: #88a0c0; -fx-cursor: hand; -fx-font-weight: bold;");
        btnRetour.setOnAction(e -> fenetreDifficulte.close());

        miseEnPage.getChildren().addAll(btnFacile, btnMoyen, btnDifficile, btnRetour);

        Scene scene = new Scene(miseEnPage, 380, 550);
        fenetreDifficulte.setScene(scene);
        fenetreDifficulte.setResizable(false);
        fenetreDifficulte.showAndWait();
    }

    // Instancie un bouton servant d'interrupteur pour la sélection d'options
    private Button creerBoutonToggle(String texte, boolean selectionne) {
        Button btn = new Button(texte);
        mettreAJourStyleBoutonToggle(btn, selectionne);
        return btn;
    }

    // Assure l'exclusion visuelle en désactivant les autres boutons lors d'un clic
    private void gererSelection(Button selectionne, Button... autres) {
        mettreAJourStyleBoutonToggle(selectionne, true);
        for (Button btn : autres) mettreAJourStyleBoutonToggle(btn, false);
    }

    // Modifie les propriétés graphiques du bouton selon son état d'activation
    private void mettreAJourStyleBoutonToggle(Button btn, boolean selectionne) {
        if (selectionne) {
            btn.setStyle("-fx-background-color: #e0af68; -fx-text-fill: #1b1f2e; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: #232838; -fx-text-fill: #88a0c0; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand; -fx-border-color: #3b4d75; -fx-border-radius: 5;");
        }
    }

    // Génère un composant interactif combinant texte et icône pour les niveaux de difficulté
    private HBox creerCarteDifficulte(String emoji, String titreTexte, String sousTitreTexte, Runnable action) {
        HBox carte = new HBox(15);
        carte.setAlignment(Pos.CENTER_LEFT);
        String styleNormal = "-fx-background-color: #232838; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #3b4d75; -fx-border-radius: 8;";
        String styleSurvol = "-fx-background-color: #2d3447; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #e0af68; -fx-border-radius: 8;";
        carte.setStyle(styleNormal);

        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 25px; -fx-text-fill: #e0af68;");

        VBox textes = new VBox(2);
        Label titre = new Label(titreTexte);
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Century Gothic';");
        Label sousTitre = new Label(sousTitreTexte);
        sousTitre.setStyle("-fx-text-fill: #e0af68; -fx-font-size: 12px; -fx-font-family: 'Century Gothic';");
        
        textes.getChildren().addAll(titre, sousTitre);
        carte.getChildren().addAll(icon, textes);

        // Gestion des événements liés au survol de la souris
        carte.setOnMouseEntered(e -> carte.setStyle(styleSurvol));
        carte.setOnMouseExited(e -> carte.setStyle(styleNormal));
        carte.setOnMouseClicked(e -> action.run());

        return carte;
    }

    // Crée un bouton standard avec une transition visuelle intégrée
    private Button creerBoutonSimple(String texte) {
        Button bouton = new Button(texte);
        String styleNormal = "-fx-background-color: #1b1f2e; -fx-background-radius: 25; -fx-text-fill: #e0af68; -fx-font-family: 'Century Gothic'; -fx-font-weight: bold; -fx-font-size: 18px; -fx-padding: 15 50; -fx-border-color: #e0af68; -fx-border-radius: 25; -fx-border-width: 2; -fx-cursor: hand;";
        String styleSurvol = "-fx-background-color: #232838; -fx-background-radius: 25; -fx-text-fill: white; -fx-font-family: 'Century Gothic'; -fx-font-weight: bold; -fx-font-size: 18px; -fx-padding: 15 50; -fx-border-color: white; -fx-border-radius: 25; -fx-border-width: 2; -fx-cursor: hand;";
        bouton.setStyle(styleNormal);
        
        bouton.setOnMouseEntered(e -> bouton.setStyle(styleSurvol));
        bouton.setOnMouseExited(e -> bouton.setStyle(styleNormal));
        return bouton;
    }

    // Formate une carte cliquable utilisée pour le menu de sélection principal
    private HBox creerCarteMenu(String emoji, String titreTexte, String sousTitreTexte, Runnable action) {
        HBox carte = new HBox(20);
        carte.setAlignment(Pos.CENTER_LEFT);
        String styleNormal = "-fx-background-color: #232838; -fx-padding: 15 20; -fx-background-radius: 10; -fx-cursor: hand;";
        String styleSurvol = "-fx-background-color: #2d3447; -fx-padding: 15 20; -fx-background-radius: 10; -fx-cursor: hand;";
        carte.setStyle(styleNormal);

        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 35px; -fx-text-fill: #e0af68;"); 

        VBox textes = new VBox(5);
        Label titre = new Label(titreTexte);
        titre.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Century Gothic';");
        Label sousTitre = new Label(sousTitreTexte);
        sousTitre.setStyle("-fx-text-fill: #8f98a8; -fx-font-size: 13px; -fx-font-family: 'Century Gothic';");
        
        textes.getChildren().addAll(titre, sousTitre);
        carte.getChildren().addAll(icon, textes);

        carte.setOnMouseEntered(e -> carte.setStyle(styleSurvol));
        carte.setOnMouseExited(e -> carte.setStyle(styleNormal));
        carte.setOnMouseClicked(e -> action.run());

        return carte;
    }

    // Initialise les composants du jeu et exécute la transition vers le plateau
    private void lancerJeu(String modeIA, int difficulte, String premierJoueur) {
        sceneMenu.getRoot().setDisable(true); 

        // Délai artificiel permettant d'assurer la fluidité visuelle de l'interface
        PauseTransition pause = new PauseTransition(Duration.seconds(0.1));
        pause.setOnFinished(e -> {
            
            // Instanciation de l'architecture modèle-vue-contrôleur
            Moteur moteur = new Moteur();
            Plateau vuePlateau = new Plateau();
            ControleurJeu controleur = new ControleurJeu(moteur, vuePlateau, modeIA, difficulte, premierJoueur);

            StackPane racineJeu = new StackPane(vuePlateau);
            racineJeu.setStyle("-fx-padding: 20; -fx-background-color: linear-gradient(to bottom right, #3b4d75, #000000);"); 
            Scene sceneJeu = new Scene(racineJeu);

            fenetrePrincipale.setScene(sceneJeu);
            fenetrePrincipale.setTitle("Quoridor - En jeu (" + modeIA + " - Nv " + difficulte + ")");
            fenetrePrincipale.centerOnScreen(); 
        });
        pause.play(); 
    }

    // Construit et affiche une boîte de dialogue détaillant les instructions de contrôle
    private void afficherRegles() {
        Stage fenetreRegles = new Stage();
        fenetreRegles.initModality(Modality.APPLICATION_MODAL); 
        fenetreRegles.initOwner(fenetrePrincipale);
        fenetreRegles.setTitle("Règles du jeu");

        VBox miseEnPage = new VBox(20);
        miseEnPage.setPadding(new Insets(30));
        miseEnPage.setStyle("-fx-background-color: #1b1f2e; -fx-border-color: #88a0c0; -fx-border-width: 3;"); 
        miseEnPage.setAlignment(Pos.CENTER);

        Label titre = new Label("COMMENT JOUER ?");
        titre.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e0af68;");

        Label texte = new Label(
            "• Déplacement : Clic sur une case de la grille.\n\n" +
            "• Mur Vertical : Clic sur l'espace vertical entre les cases.\n\n" +
            "• Mur Horizontal : Clic sur l'espace horizontal entre les cases.\n\n"
        );
        texte.setStyle("-fx-font-size: 15px; -fx-text-fill: #e2e8f0; -fx-line-spacing: 0.5em;");
        
        // Autorise le retour à la ligne automatique et ajuste dynamiquement la hauteur requise
        texte.setWrapText(true);
        texte.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        Button boutonOk = new Button("FERMER");
        boutonOk.setStyle("-fx-background-color: #cd7070; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10 30; -fx-background-radius: 5;"); 
        boutonOk.setOnAction(e -> fenetreRegles.close());

        miseEnPage.getChildren().addAll(titre, texte, boutonOk);

        Scene scene = new Scene(miseEnPage, 450, 350);
        fenetreRegles.setScene(scene);
        fenetreRegles.setResizable(false);
        fenetreRegles.showAndWait();
    }

    // Méthode de lancement standard pour les applications JavaFX
    public static void main(String[] args) {
        launch(args);
    }
}
