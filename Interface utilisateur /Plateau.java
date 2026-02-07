package com.quoridor;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class Plateau extends Pane {
    
    public static final int TAILLE_CASE = 40;
    public static final int ESPACE_MUR = 10;
    public static final int NB_CASES = 9;

    private Circle pionBlancVisuel;

    public Plateau() {
        dessinerGrille();
    }

    private void dessinerGrille() {
        int tailleTotale = (NB_CASES * TAILLE_CASE) + ((NB_CASES - 1) * ESPACE_MUR);
        this.setPrefSize(tailleTotale, tailleTotale);
        this.setStyle("-fx-background-color: #4a3b2a;"); 

        for (int i = 0; i < NB_CASES; i++) {
            for (int j = 0; j < NB_CASES; j++) {
                int x = j * (TAILLE_CASE + ESPACE_MUR);
                int y = i * (TAILLE_CASE + ESPACE_MUR);
                Rectangle caseJeu = new Rectangle(x, y, TAILLE_CASE, TAILLE_CASE);
                caseJeu.setFill(Color.BURLYWOOD);
                caseJeu.setStroke(Color.BLACK);
                this.getChildren().add(caseJeu);
            }
        }
    }

    public void initialiserPionBlancVisuel(int ligne, int colonne) {
        double x = (colonne * (TAILLE_CASE + ESPACE_MUR)) + (TAILLE_CASE / 2.0);
        double y = (ligne * (TAILLE_CASE + ESPACE_MUR)) + (TAILLE_CASE / 2.0);
        pionBlancVisuel = new Circle(x, y, TAILLE_CASE / 2.5, Color.WHITE);
        pionBlancVisuel.setStroke(Color.BLACK);
        pionBlancVisuel.setStrokeWidth(2);
        this.getChildren().add(pionBlancVisuel);
    }

    public void deplacerPionBlancVisuel(int nouvelleLigne, int nouvelleCol) {
        double nouveauX = (nouvelleCol * (TAILLE_CASE + ESPACE_MUR)) + (TAILLE_CASE / 2.0);
        double nouveauY = (nouvelleLigne * (TAILLE_CASE + ESPACE_MUR)) + (TAILLE_CASE / 2.0);
        pionBlancVisuel.setCenterX(nouveauX);
        pionBlancVisuel.setCenterY(nouveauY);
    }
    
    public void placerMurVisuel(int ligne, int colonne, boolean horizontal) {
        double largeurMur, hauteurMur;
        double x, y;

        if (horizontal) {
            // MUR HORIZONTAL
            largeurMur = (TAILLE_CASE * 2) + ESPACE_MUR; 
            hauteurMur = ESPACE_MUR;                     
            
            x = (colonne * (TAILLE_CASE + ESPACE_MUR));
            y = ((ligne + 1) * (TAILLE_CASE + ESPACE_MUR)) - ESPACE_MUR;
            
        } else {
            // MUR VERTICAL
            largeurMur = ESPACE_MUR;
            hauteurMur = (TAILLE_CASE * 2) + ESPACE_MUR;
            
            x = ((colonne + 1) * (TAILLE_CASE + ESPACE_MUR)) - ESPACE_MUR;
            y = (ligne * (TAILLE_CASE + ESPACE_MUR));
        }

        // Création visuelle du mur
        Rectangle mur = new Rectangle(x, y, largeurMur, hauteurMur);
        mur.setFill(Color.CHOCOLATE); // Couleur bois distincte
        mur.setStroke(Color.BLACK);
        
        // Ajout au rendu graphique
        this.getChildren().add(mur);
    }
}
