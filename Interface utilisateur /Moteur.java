package com.quoridor;

import java.util.ArrayList;
import java.util.List;

public class Moteur {
    private int pionBlancLigne = 8;
    private int pionBlancCol = 4;
    
    private boolean tourIA = false;
    private boolean partieTerminee = false;
    private List<MurLogique> mursPlaques = new ArrayList<>();

    public class MurLogique {
        public int ligne, col;
        public boolean horizontal;
        public MurLogique(int l, int c, boolean h) { 
            this.ligne = l; this.col = c; this.horizontal = h; 
        }
    }

    public boolean estDeplacementValide(int ligClic, int colClic) {
        if (Math.abs(pionBlancLigne - ligClic) + Math.abs(pionBlancCol - colClic) != 1) return false;

        // Vérification des collisions avec les murs posés
        for (MurLogique mur : mursPlaques) {
            if (ligClic < pionBlancLigne && mur.horizontal && mur.ligne == ligClic && (mur.col == pionBlancCol || mur.col == pionBlancCol - 1)) return false;
            if (ligClic > pionBlancLigne && mur.horizontal && mur.ligne == pionBlancLigne && (mur.col == pionBlancCol || mur.col == pionBlancCol - 1)) return false;
            if (colClic > pionBlancCol && !mur.horizontal && mur.col == pionBlancCol && (mur.ligne == pionBlancLigne || mur.ligne == pionBlancLigne - 1)) return false;
            if (colClic < pionBlancCol && !mur.horizontal && mur.col == colClic && (mur.ligne == pionBlancLigne || mur.ligne == pionBlancLigne - 1)) return false;
        }
        return true;
    }

    public boolean emplacementMurLibre(int l, int c) {
        for (MurLogique m : mursPlaques) {
            if (m.ligne == l && m.col == c) return false; 
        }
        return true;
    }

    public void majPositionBlanc(int ligne, int col) {
        this.pionBlancLigne = ligne;
        this.pionBlancCol = col;
    }

    public void ajouterMur(int l, int c, boolean h) {
        mursPlaques.add(new MurLogique(l, c, h));
    }

    public boolean verifierVictoire() {
        if (pionBlancLigne == 0) {
            partieTerminee = true;
            return true;
        }
        return false;
    }

    public boolean isTourIA() { return tourIA; }
    public void setTourIA(boolean tourIA) { this.tourIA = tourIA; }
    public boolean isPartieTerminee() { return partieTerminee; }
}
