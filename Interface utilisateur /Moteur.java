package com.quoridor;

import java.util.ArrayList;
import java.util.List;

public class Moteur {
    private int pionBlancLigne = 8;
    private int pionBlancCol = 4;
    private int pionNoirLigne = 0;
    private int pionNoirCol = 4;
    
    private int mursJoueur = 10;
    private int mursIA = 10;
    
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
    
    public boolean estDeplacementValide(int l1, int c1, int l2, int c2) {
        if (Math.abs(l1 - l2) + Math.abs(c1 - c2) != 1) return false;
        for (MurLogique mur : mursPlaques) {
            if (l2 < l1 && mur.horizontal && mur.ligne == l2 && (mur.col == c1 || mur.col == c1 - 1)) return false;
            if (l2 > l1 && mur.horizontal && mur.ligne == l1 && (mur.col == c1 || mur.col == c1 - 1)) return false;
            if (c2 > c1 && !mur.horizontal && mur.col == c1 && (mur.ligne == l1 || mur.ligne == l1 - 1)) return false;
            if (c2 < c1 && !mur.horizontal && mur.col == c2 && (mur.ligne == l1 || mur.ligne == l1 - 1)) return false;
        }
        return true;
    }

    public boolean emplacementMurLibre(int l, int c) {
        for (MurLogique m : mursPlaques) {
            if (m.ligne == l && m.col == c) return false;
        }
        return true;
    }

    public boolean verifierVictoireBlanc() {
        if (pionBlancLigne == 0) partieTerminee = true;
        return partieTerminee;
    }

    public void majPositionBlanc(int l, int c) { pionBlancLigne = l; pionBlancCol = c; }
    public void majPositionNoir(int l, int c) { pionNoirLigne = l; pionNoirCol = c; }
    public void utiliserMurJoueur(int l, int c, boolean h) { mursPlaques.add(new MurLogique(l, c, h)); mursJoueur--; }
    public void utiliserMurIA(int l, int c, boolean h) { mursPlaques.add(new MurLogique(l, c, h)); mursIA--; }

    public int getPionBlancLigne() { return pionBlancLigne; }
    public int getPionBlancCol() { return pionBlancCol; }
    public int getMursJoueur() { return mursJoueur; }
    public int getMursIA() { return mursIA; }
    public boolean isTourIA() { return tourIA; }
    public void setTourIA(boolean tourIA) { this.tourIA = tourIA; }
    public boolean isPartieTerminee() { return partieTerminee; }
    public void setPartieTerminee(boolean b) { this.partieTerminee = b; }
}
