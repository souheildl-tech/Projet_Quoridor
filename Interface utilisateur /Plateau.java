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

public class Plateau extends BorderPane {
    
    public static final int TAILLE_CASE = 40;
    public static final int ESPACE_MUR = 10;
    public static final int NB_CASES = 9;
    
    private Pane zoneJeu;
    private Circle pionBlanc;
    private Circle pionNoir; 
    private HBox conteneurMursJoueur;
    private HBox conteneurMursIA;
    private List<Circle> indicateurs = new ArrayList<>();

    public Plateau() {
        this.setStyle("-fx-background-color: #312e2b;");
        initialiserInterface();
    }

    public Pane getZoneJeu() { return zoneJeu; }

    private void initialiserInterface() {
        // Panneau IA
        HBox panneauIA = creerPanneauJoueur(" IA ", Color.BLACK);
        conteneurMursIA = new HBox(3);
        panneauIA.getChildren().add(conteneurMursIA);
        this.setTop(panneauIA);

        // Zone de Jeu
        zoneJeu = new Pane();
        dessinerGrille();
        this.setCenter(zoneJeu);
        BorderPane.setMargin(zoneJeu, new Insets(10));

        // Panneau Joueur
        HBox panneauJoueur = creerPanneauJoueur(" Joueur 1 ", Color.WHITE);
        conteneurMursJoueur = new HBox(3);
        panneauJoueur.getChildren().add(conteneurMursJoueur);
        this.setBottom(panneauJoueur);
    }

    private void dessinerGrille() {
        int tailleTotale = (NB_CASES * TAILLE_CASE) + ((NB_CASES - 1) * ESPACE_MUR);
        this.setPrefSize(tailleTotale, tailleTotale);
        this.setStyle("-fx-background-color: #4a3b2a;"); 

        for (int i = 0; i < NB_CASES; i++) {
            for (int j = 0; j < NB_CASES; j++) {
                Rectangle caseJeu = new Rectangle(calculerCoord(j), calculerCoord(i), TAILLE_CASE, TAILLE_CASE);
                caseJeu.setFill(Color.BURLYWOOD);
                caseJeu.setStroke(Color.BLACK);
                this.getChildren().add(caseJeu);
            }
        }
    }

   public void mettreAJourMurs(boolean isJoueur, int nbMurs) {
        HBox conteneur = isJoueur ? conteneurMursJoueur : conteneurMursIA;
        conteneur.getChildren().clear();
        for (int i = 0; i < nbMurs; i++) {
            Rectangle murUI = new Rectangle(6, 18);
            murUI.setFill(Color.CHOCOLATE);
            murUI.setStroke(Color.web("#5c3a21"));
            murUI.setArcWidth(3); murUI.setArcHeight(3);
            conteneur.getChildren().add(murUI);
        }
    }

    public void placerPionVisuel(int ligne, int colonne, Color couleur) {
        Circle pion = new Circle(calculerX(colonne), calculerY(ligne), TAILLE_CASE / 2.5);
        pion.setFill(couleur);
        pion.setStroke(couleur == Color.BLACK ? Color.GRAY : Color.BLACK);
        
        if (couleur == Color.WHITE) this.pionBlanc = pion;
        else this.pionNoir = pion;
        zoneJeu.getChildren().add(pion);
    }

    public void deplacerPionVisuel(boolean isBlanc, int ligne, int colonne) {
        Circle pion = isBlanc ? pionBlanc : pionNoir;
        pion.setCenterX(calculerX(colonne));
        pion.setCenterY(calculerY(ligne));
    }

    public void placerMurVisuel(int ligne, int colonne, boolean horizontal) {
        double x, y, largeurMur, hauteurMur;
        if (horizontal) {
            largeurMur = (TAILLE_CASE * 2) + ESPACE_MUR; hauteurMur = ESPACE_MUR;
            x = (colonne * (TAILLE_CASE + ESPACE_MUR));
            y = ((ligne + 1) * (TAILLE_CASE + ESPACE_MUR)) - ESPACE_MUR;
        } else {
            largeurMur = ESPACE_MUR; hauteurMur = (TAILLE_CASE * 2) + ESPACE_MUR;
            x = ((colonne + 1) * (TAILLE_CASE + ESPACE_MUR)) - ESPACE_MUR;
            y = (ligne * (TAILLE_CASE + ESPACE_MUR));
        }
        Rectangle mur = new Rectangle(x, y, largeurMur, hauteurMur);
        mur.setFill(Color.CHOCOLATE);
        mur.setStroke(Color.BLACK);
        zoneJeu.getChildren().add(mur);
    }

    public void afficherIndicateur(int ligne, int colonne) {
        Circle point = new Circle(calculerX(colonne), calculerY(ligne), 8);
        point.setFill(Color.LIGHTGREEN);
        point.setOpacity(0.6);
        point.setMouseTransparent(true);
        zoneJeu.getChildren().add(point);
        indicateurs.add(point);
    }

    public void cacherIndicateurs() {
        zoneJeu.getChildren().removeAll(indicateurs);
        indicateurs.clear();
    }

    private double calculerX(int colonne) { return (colonne * (TAILLE_CASE + ESPACE_MUR)) + (TAILLE_CASE / 2.0); }
    private double calculerY(int ligne) { return (ligne * (TAILLE_CASE + ESPACE_MUR)) + (TAILLE_CASE / 2.0); }
}
