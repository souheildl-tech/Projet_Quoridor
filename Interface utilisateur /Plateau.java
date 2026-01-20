package com.quoridor;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;


public class Plateau extends Pane {
    
    // Constantes de dimensions 
    public static final int TAILLE_CASE = 40;
    public static final int ESPACE_MUR = 10;
    public static final int NB_CASES = 9;

    // Tableau pour stocker les cases (utile pour le Jour 3 ou 4 quand on cliquera dessus)
    private Rectangle[][] grilleCases;

    public Plateau() {
        this.grilleCases = new Rectangle[NB_CASES][NB_CASES];
        dessinerGrille();
    }

    private void dessinerGrille() {
        // 1. Calcul de la taille totale du plateau
        int tailleTotale = (NB_CASES * TAILLE_CASE) + ((NB_CASES - 1) * ESPACE_MUR);
        this.setPrefSize(tailleTotale, tailleTotale);
        
        
        this.setStyle("-fx-background-color: #4a3b2a;"); 

        // 2. Création de la grille 9x9
        for (int i = 0; i < NB_CASES; i++) {       
            for (int j = 0; j < NB_CASES; j++) {   
                
                // Formule de décalage : Index * (Taille de la case + Épaisseur du mur)
                int x = j * (TAILLE_CASE + ESPACE_MUR);
                int y = i * (TAILLE_CASE + ESPACE_MUR);

                Rectangle caseJeu = new Rectangle(x, y, TAILLE_CASE, TAILLE_CASE);
                caseJeu.setFill(Color.BURLYWOOD); 
                caseJeu.setStroke(Color.BLACK);
                
              
                caseJeu.setOnMouseEntered(e -> caseJeu.setFill(Color.rgb(240, 210, 160)));
                caseJeu.setOnMouseExited(e -> caseJeu.setFill(Color.BURLYWOOD));

                
                grilleCases[i][j] = caseJeu;
                this.getChildren().add(caseJeu);
            }
        }        
    }

    public Rectangle getCase(int ligne, int colonne) {
        return grilleCases[ligne][colonne];
    }

    public void placerPion(int ligne, int colonne, Color couleur) {
        // 1. Calcul du centre exact de la case ciblée
        double x = (colonne * (TAILLE_CASE + ESPACE_MUR)) + (TAILLE_CASE / 2.0);
        double y = (ligne * (TAILLE_CASE + ESPACE_MUR)) + (TAILLE_CASE / 2.0);

        // 2. Création du pion (cercle)
        double rayon = TAILLE_CASE / 2.5;
        Circle pion = new Circle(x, y, rayon);
        
        pion.setFill(couleur);
        pion.setStroke(Color.BLACK);
        pion.setStrokeWidth(2);

        // 3. Ajout visuel sur le plateau
        this.getChildren().add(pion);
    }
}
}
