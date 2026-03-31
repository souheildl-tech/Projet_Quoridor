package com.quoridor;

// Importe les outils JavaFX pour l'interface graphique et les listes
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;
import java.util.List;

// Gère l'affichage visuel du plateau de jeu
public class Plateau extends BorderPane {
    
    // Paramètres de taille pour la grille et les murs
    public final int TAILLE_CASE = 40;
    public final int ESPACE_MUR = 10;
    public final int NB_CASES = 9;

    // Éléments visuels de l'interface (plateau, murs, joueurs, indicateurs)
    private Pane zoneJeu;
    private List<Circle> indicateurs = new ArrayList<>();
    private HBox conteneurMursJoueur;
    private HBox conteneurMursIA;
    private Label labelIA;
    private Label labelJoueur;
    
    // Prépare la fenêtre avec un fond sombre et lance la création visuelle
    public Plateau() {
        this.setStyle("-fx-background-color: #2a1714;");
        initialiserInterface();
    }

    // Assemble tous les panneaux (grille au centre, joueurs en haut et en bas)
    private void initialiserInterface() {
        labelIA = new Label(" Joueur 2");
        labelJoueur = new Label(" Joueur 1 ");

        // Prépare et place la barre du joueur 2 (IA) tout en haut
        HBox panneauIA = creerPanneauJoueur(labelIA, Color.web("#FF69B4"));
        conteneurMursIA = new HBox(3);
        mettreAJourMurs(conteneurMursIA, 10);
        panneauIA.getChildren().add(conteneurMursIA);
        this.setTop(panneauIA);

        // Prépare et place la grille de jeu au centre avec une marge
        zoneJeu = new Pane();
        dessinerGrille();
        this.setCenter(zoneJeu);
        BorderPane.setMargin(zoneJeu, new Insets(10));

        // Prépare et place la barre du joueur 1 tout en bas
        HBox panneauJoueur = creerPanneauJoueur(labelJoueur, Color.web("#eec273"));
        conteneurMursJoueur = new HBox(3);
        mettreAJourMurs(conteneurMursJoueur, 10);
        panneauJoueur.getChildren().add(conteneurMursJoueur);
        this.setBottom(panneauJoueur);
    }
    
    // Fabrique la boîte d'information d'un joueur avec son nom et sa couleur
    private HBox creerPanneauJoueur(Label etiquetteNom, Color couleurPion) {
        // Configure l'espacement, l'alignement et la couleur de fond
        HBox panneau = new HBox(15);
        panneau.setAlignment(Pos.CENTER_LEFT);
        panneau.setPadding(new Insets(10, 20, 10, 20));
        panneau.setStyle("-fx-background-color: #1a0f0d;"); 
        
        // Formate le texte en gras et en blanc
        etiquetteNom.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        etiquetteNom.setTextFill(Color.WHITE);

        // Crée le petit rond coloré servant d'icône
        Circle iconePion = new Circle(8, couleurPion);
        iconePion.setStroke(Color.BLACK);

        panneau.getChildren().addAll(iconePion, etiquetteNom);
        return panneau;
    }

    // Redessine visuellement le stock de murs restants
    public void mettreAJourMurs(HBox conteneur, int nombreMurs) {
        conteneur.getChildren().clear();
        
        // Ajoute un petit rectangle beige pour chaque mur disponible
        for (int index = 0; index < nombreMurs; index++) {
            Rectangle murVisuel = new Rectangle(6, 18);
            murVisuel.setFill(Color.web("#eec273")); 
            murVisuel.setStroke(Color.web("#a37d36"));
            murVisuel.setArcWidth(3);
            murVisuel.setArcHeight(3);
            conteneur.getChildren().add(murVisuel);
        }
    }

    // Trace le fond du plateau et toutes les cases noires
    private void dessinerGrille() {
        // Calcule la taille totale et applique un fond rouge brique pour les fentes
        int tailleTotale = (NB_CASES * TAILLE_CASE) + ((NB_CASES - 1) * ESPACE_MUR);
        zoneJeu.setPrefSize(tailleTotale, tailleTotale);
        zoneJeu.setMaxSize(tailleTotale, tailleTotale);
        zoneJeu.setStyle("-fx-background-color: #702d26;"); 

        // Place chaque case en sautant les espaces prévus pour les murs
        for (int ligne = 0; ligne < NB_CASES; ligne++) {
            for (int colonne = 0; colonne < NB_CASES; colonne++) {
                int positionX = colonne * (TAILLE_CASE + ESPACE_MUR);
                int positionY = ligne * (TAILLE_CASE + ESPACE_MUR);
                
                // Dessine la case noire avec des bords légèrement arrondis
                Rectangle caseJeu = new Rectangle(positionX, positionY, TAILLE_CASE, TAILLE_CASE);
                caseJeu.setFill(Color.web("#1c1c1c"));
                caseJeu.setStroke(Color.web("#111111"));
                caseJeu.setArcWidth(5); 
                caseJeu.setArcHeight(5);
                
                zoneJeu.getChildren().add(caseJeu);
            }
        }
    }

    // Pose le pion rond d'un joueur sur sa case de départ
    public Circle creerPionVisuel(int ligne, int colonne, Color couleur) {
        // Calcule le centre exact de la case ciblée
        double positionX = (colonne * (TAILLE_CASE + ESPACE_MUR)) + (TAILLE_CASE / 2.0);
        double positionY = (ligne * (TAILLE_CASE + ESPACE_MUR)) + (TAILLE_CASE / 2.0);
        
        // Dessine et place le pion sur le plateau
        Circle pion = new Circle(positionX, positionY, TAILLE_CASE / 2.5);
        pion.setFill(couleur);
        pion.setStroke(Color.BLACK);
        
        zoneJeu.getChildren().add(pion);
        return pion;
    }

    // Affiche un mur sur le plateau entre les cases
    public void placerMurVisuel(int ligne, int colonne, boolean estHorizontal, Color couleurMur) {
        double largeurMur, hauteurMur, positionX, positionY;
        
        // Calcule les dimensions et la position selon le sens du mur
        if (estHorizontal) {
            largeurMur = (TAILLE_CASE * 2) + ESPACE_MUR;
            hauteurMur = ESPACE_MUR;
            positionX = (colonne * (TAILLE_CASE + ESPACE_MUR));
            positionY = ((ligne + 1) * (TAILLE_CASE + ESPACE_MUR)) - ESPACE_MUR;
        } else {
            largeurMur = ESPACE_MUR;
            hauteurMur = (TAILLE_CASE * 2) + ESPACE_MUR;
            positionX = ((colonne + 1) * (TAILLE_CASE + ESPACE_MUR)) - ESPACE_MUR;
            positionY = (ligne * (TAILLE_CASE + ESPACE_MUR));
        }

        // Crée le rectangle du mur et l'ajoute au jeu
        Rectangle mur = new Rectangle(positionX, positionY, largeurMur, hauteurMur);
        mur.setFill(couleurMur); 
        mur.setStroke(Color.web("#222222")); 
        mur.setArcWidth(4);
        mur.setArcHeight(4);
        
        zoneJeu.getChildren().add(mur);
    }

    // Permet d'accéder aux éléments du plateau depuis l'extérieur
    public Pane getZoneJeu() { return zoneJeu; }
    public HBox getConteneurMursJoueur() { return conteneurMursJoueur; }
    public HBox getConteneurMursIA() { return conteneurMursIA; }

    // Efface tous les points verts des déplacements possibles
    public void cacherIndicateurs() {
        zoneJeu.getChildren().removeAll(indicateurs);
        indicateurs.clear();
    }

    // Dessine un point vert transparent pour montrer une case accessible
    public void ajouterIndicateur(int ligne, int colonne) {
        double positionX = (colonne * (TAILLE_CASE + ESPACE_MUR)) + (TAILLE_CASE / 2.0);
        double positionY = (ligne * (TAILLE_CASE + ESPACE_MUR)) + (TAILLE_CASE / 2.0);
        
        Circle point = new Circle(positionX, positionY, 8, Color.LIGHTGREEN);
        point.setOpacity(0.6);
        point.setMouseTransparent(true);
        
        zoneJeu.getChildren().add(point);
        indicateurs.add(point);
    }
    
    // Affiche un message de victoire coloré à côté du nom du gagnant
    public void afficherVictoireIA() {
        labelIA.setText(labelIA.getText() + " a gagné.");
        labelIA.setTextFill(Color.web("#ff4444")); 
    }

    public void afficherVictoireJoueur() {
        labelJoueur.setText(labelJoueur.getText() + " a gagné.");
        labelJoueur.setTextFill(Color.web("#44ff44")); 
    }
}
