package com.quoridor;

public class Moteur {

    private int pionBlancLigne = 8;
    private int pionBlancCol = 4;

    public Moteur() {

    }

    // Vérification des règles mathématiques
    public boolean estDeplacementValide(int arriveeLigne, int arriveeCol) {
        int diffLigne = Math.abs(this.pionBlancLigne - arriveeLigne);
        int diffCol = Math.abs(this.pionBlancCol - arriveeCol);
        return (diffLigne + diffCol) == 1;
    }

    // Mise à jour des positions
    public void majPositionBlanc(int ligne, int col) {
        this.pionBlancLigne = ligne;
        this.pionBlancCol = col;
    }

    // Getters pour que le contrôleur puisse lire les positions
    public int getPionBlancLigne() { return pionBlancLigne; }
    public int getPionBlancCol() { return pionBlancCol; }
}
