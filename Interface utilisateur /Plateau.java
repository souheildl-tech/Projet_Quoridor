package com.quoridor;

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

// Classe responsable de l'affichage graphique du plateau de Quoridor via la bibliothèque JavaFX
public class Plateau extends BorderPane {
    
    // Constantes définissant la géométrie et les proportions de la grille de jeu
    public final int TAILLE_CASE = 40;
    public final int ESPACE_MUR = 10;
    public final int NB_CASES = 9;

    // Conteneurs visuels pour le rendu spatial et le suivi des ressources
    private Pane zoneJeu;
    private List<Circle> indicateurs = new ArrayList<>();
    private HBox conteneurMursJoueur;
    private HBox conteneurMursIA;
    private Label labelIA;
    private Label labelJoueur;
    
    // Constructeur initialisant l'arrière-plan et l'agencement visuel de l'interface
    public Plateau() {
        this.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #4f6fa8, #000000);");
        initialiserInterface();
    }

    // Construit la disposition globale incluant la grille centrale et les panneaux latéraux
    private void initialiserInterface() {
        labelIA = new Label(" Joueur 2");
        labelJoueur = new Label(" Joueur 1 ");

        HBox panneauIA = creerPanneauJoueur(labelIA, Color.web("#cd7070"));
        conteneurMursIA = new HBox(3);
        mettreAJourMurs(conteneurMursIA, 10);
        panneauIA.getChildren().add(conteneurMursIA);
        this.setTop(panneauIA);

        zoneJeu = new Pane();
        dessinerGrille();
        this.setCenter(zoneJeu);
        BorderPane.setMargin(zoneJeu, new Insets(10));

        HBox panneauJoueur = creerPanneauJoueur(labelJoueur, Color.web("#88a0c0"));
        conteneurMursJoueur = new HBox(3);
        mettreAJourMurs(conteneurMursJoueur, 10);
        panneauJoueur.getChildren().add(conteneurMursJoueur);
        this.setBottom(panneauJoueur);
    }
    
    // Génère l'interface d'identification d'un participant avec son icône et son pseudonyme
    private HBox creerPanneauJoueur(Label etiquetteNom, Color couleurPion) {
        HBox panneau = new HBox(15);
        panneau.setAlignment(Pos.CENTER_LEFT);
        panneau.setPadding(new Insets(10, 20, 10, 20));
        panneau.setStyle("-fx-background-color: rgba(27, 31, 46, 0.8);"); 
        
        etiquetteNom.setFont(Font.font("Century Gothic", FontWeight.BOLD, 16));
        etiquetteNom.setTextFill(Color.web("#e2e8f0"));

        Circle iconePion = new Circle(8, couleurPion);
        iconePion.setStroke(Color.WHITE);
        iconePion.setStrokeWidth(1.5);

        panneau.getChildren().addAll(iconePion, etiquetteNom);
        return panneau;
    }

    // Actualise la représentation graphique du stock de murs disponibles pour un joueur
    public void mettreAJourMurs(HBox conteneur, int nombreMurs) {
        conteneur.getChildren().clear();
        for (int index = 0; index < nombreMurs; index++) {
            Rectangle murVisuel = new Rectangle(6, 18);
            murVisuel.setFill(Color.web("#b89c72")); 
            murVisuel.setArcWidth(6);
            murVisuel.setArcHeight(6);
            conteneur.getChildren().add(murVisuel);
        }
    }

    // Calcule mathématiquement la position des cases et génère la structure du labyrinthe
    private void dessinerGrille() {
        int tailleTotale = (NB_CASES * TAILLE_CASE) + ((NB_CASES - 1) * ESPACE_MUR);
        zoneJeu.setPrefSize(tailleTotale, tailleTotale);
        zoneJeu.setMaxSize(tailleTotale, tailleTotale);
        zoneJeu.setStyle("-fx-background-color: #0d0f17; -fx-background-radius: 8;"); 

        for (int ligne = 0; ligne < NB_CASES; ligne++) {
            for (int colonne = 0; colonne < NB_CASES; colonne++) {
                int positionX = colonne * (TAILLE_CASE + ESPACE_MUR);
                int positionY = ligne * (TAILLE_CASE + ESPACE_MUR);
                
                Rectangle caseJeu = new Rectangle(positionX, positionY, TAILLE_CASE, TAILLE_CASE);
                caseJeu.setFill(Color.web("#232838"));
                caseJeu.setArcWidth(8); 
                caseJeu.setArcHeight(8);
                
                zoneJeu.getChildren().add(caseJeu);
            }
        }
    }

    // Instancie un objet graphique circulaire représentant un pion sur le plateau
    public Circle creerPionVisuel(int ligne, int colonne, Color couleur) {
        double positionX = (colonne * (TAILLE_CASE + ESPACE_MUR)) + (TAILLE_CASE / 2.0);
        double positionY = (ligne * (TAILLE_CASE + ESPACE_MUR)) + (TAILLE_CASE / 2.0);
        
        Circle pion = new Circle(positionX, positionY, TAILLE_CASE / 2.5);
        pion.setFill(couleur);
        pion.setStroke(Color.WHITE);
        pion.setStrokeWidth(2.5);
        
        zoneJeu.getChildren().add(pion);
        return pion;
    }

    // Calcule l'alignement géométrique et dessine un obstacle physique entre les cases
    public void placerMurVisuel(int ligne, int colonne, boolean estHorizontal, Color couleurMur) {
        double largeurMur, hauteurMur, positionX, positionY;
        
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

        Rectangle mur = new Rectangle(positionX, positionY, largeurMur, hauteurMur);
        mur.setFill(couleurMur); 
        mur.setArcWidth(10);
        mur.setArcHeight(10);
        mur.setStroke(Color.web("#0d0f17")); 
        mur.setStrokeWidth(1.5);
        
        zoneJeu.getChildren().add(mur);
    }

    // Fournit l'accès externe aux composants visuels pour le contrôleur
    public Pane getZoneJeu() { return zoneJeu; }
    public HBox getConteneurMursJoueur() { return conteneurMursJoueur; }
    public HBox getConteneurMursIA() { return conteneurMursIA; }

    // Efface les marqueurs de déplacement pour nettoyer la grille visuelle
    public void cacherIndicateurs() {
        zoneJeu.getChildren().removeAll(indicateurs);
        indicateurs.clear();
    }

    // Dessine un repère visuel translucide pour indiquer une position accessible au joueur
    public void ajouterIndicateur(int ligne, int colonne) {
        double positionX = (colonne * (TAILLE_CASE + ESPACE_MUR)) + (TAILLE_CASE / 2.0);
        double positionY = (ligne * (TAILLE_CASE + ESPACE_MUR)) + (TAILLE_CASE / 2.0);
        
        Circle point = new Circle(positionX, positionY, TAILLE_CASE / 6.0, Color.web("#88a0c0"));
        point.setOpacity(0.5);
        point.setMouseTransparent(true);
        
        zoneJeu.getChildren().add(point);
        indicateurs.add(point);
    }
    
    // Permet de mettre à jour dynamiquement les textes d'identification selon le mode d'IA sélectionné
    public void setNomsJoueurs(String nomHaut, String nomBas) {
        labelIA.setText(" " + nomHaut);
        labelJoueur.setText(" " + nomBas + " ");
    }

    // Modifie la couleur et le texte de l'interface pour annoncer la victoire de l'IA
    public void afficherVictoireIA() {
        labelIA.setText(labelIA.getText() + " a gagné.");
        labelIA.setTextFill(Color.web("#cd7070")); 
    }

    // Modifie la couleur et le texte de l'interface pour annoncer la victoire du joueur
    public void afficherVictoireJoueur() {
        labelJoueur.setText(labelJoueur.getText() + " a gagné.");
        labelJoueur.setTextFill(Color.web("#88a0c0")); 
    }
}
