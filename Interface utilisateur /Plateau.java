package com.quoridor;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class Plateau extends Pane {
    
    public static final int TAILLE_CASE = 40;
    public static final int ESPACE_MUR = 10;
    public static final int NB_CASES = 9;
    
    private Circle pionBlanc;
    private Circle pionNoirVisuel;


    public Plateau() {
        dessinerGrille();
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

    public void placerPionVisuel(int ligne, int colonne, Color couleur) {
        double rayon = TAILLE_CASE / 2.5;
        Circle pion = new Circle(calculerCentre(colonne), calculerCentre(ligne), rayon, couleur);
        pion.setStroke(Color.BLACK);
        if (couleur == Color.WHITE) this.pionBlanc = pion;
        this.getChildren().add(pion);
    }

    public void deplacerPionBlancVisuel(int ligne, int colonne) {
        pionBlanc.setCenterX(calculerCentre(colonne));
        pionBlanc.setCenterY(calculerCentre(ligne));
    }

    public void placerPionNoirVisuel(int ligne, int colonne) {
        double rayon = TAILLE_CASE / 2.5;
        pionNoirVisuel = new Circle(calculerCentre(colonne), calculerCentre(ligne), rayon, Color.BLACK);
        pionNoirVisuel.setStroke(Color.LIGHTGRAY); 
        pionNoirVisuel.setStrokeWidth(2);
        this.getChildren().add(pionNoirVisuel);
    }

    public void deplacerPionNoirVisuel(int ligne, int colonne) {
        pionNoirVisuel.setCenterX(calculerCentre(colonne));
        pionNoirVisuel.setCenterY(calculerCentre(ligne));
    }

    public void placerMurVisuel(int ligne, int colonne, boolean horizontal) {
        double x, y, largeurMur, hauteurMur;
        if (horizontal) {
            largeurMur = (TAILLE_CASE * 2) + ESPACE_MUR; hauteurMur = ESPACE_MUR;
            x = calculerCoord(colonne); y = calculerCoord(ligne + 1) - ESPACE_MUR;
        } else {
            largeurMur = ESPACE_MUR; hauteurMur = (TAILLE_CASE * 2) + ESPACE_MUR;
            x = calculerCoord(colonne + 1) - ESPACE_MUR; y = calculerCoord(ligne);
        }
        Rectangle mur = new Rectangle(x, y, largeurMur, hauteurMur);
        mur.setFill(Color.CHOCOLATE);
        mur.setStroke(Color.BLACK);
        this.getChildren().add(mur);
    }

    private double calculerCoord(int index) { return index * (TAILLE_CASE + ESPACE_MUR); }
    private double calculerCentre(int index) { return calculerCoord(index) + (TAILLE_CASE / 2.0); }
}
