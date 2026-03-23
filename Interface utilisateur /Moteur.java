package com.quoridor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
        
        //  1. MOUVEMENT CLASSIQUE (1 case)
        if (Math.abs(l1 - l2) + Math.abs(c1 - c2) == 1) {
            if (l2 == pionNoirLigne && c2 == pionNoirCol) return false; // Interdit d'aller sur l'adversaire
            return cheminLibreDeMurs(l1, c1, l2, c2);
        }

        // 2. SAUT TOUT DROIT (Distance de 2, même axe)
        if (Math.abs(l1 - l2) == 2 && c1 == c2) {
            int midL = (l1 + l2) / 2;
            if (midL == pionNoirLigne && c1 == pionNoirCol) {
                // Vérifier qu'il n'y a ni mur avant lui, ni mur après lui
                return cheminLibreDeMurs(l1, c1, midL, c1) && cheminLibreDeMurs(midL, c1, l2, c2);
            }
        }
        if (Math.abs(c1 - c2) == 2 && l1 == l2) {
            int midC = (c1 + c2) / 2;
            if (l1 == pionNoirLigne && midC == pionNoirCol) {
                return cheminLibreDeMurs(l1, c1, l1, midC) && cheminLibreDeMurs(l1, midC, l2, c2);
            }
        }

        // 3. SAUT EN DIAGONALE (Si le saut droit est bloqué par un mur/bord)
        if (Math.abs(l1 - l2) == 1 && Math.abs(c1 - c2) == 1) {
            
            // Option A : Contournement sur l'axe vertical
            if (pionNoirLigne == l2 && pionNoirCol == c1) {
                if (cheminLibreDeMurs(l1, c1, l2, c1)) {
                    int caseDerriere = l2 + (l2 - l1);
                    boolean bloqueDerriere = (caseDerriere < 0 || caseDerriere >= 9 || !cheminLibreDeMurs(l2, c1, caseDerriere, c1));
                    if (bloqueDerriere) {
                        return cheminLibreDeMurs(l2, c1, l2, c2);
                    }
                }
            }
            
            // Option B : Contournement sur l'axe horizontal
            if (pionNoirLigne == l1 && pionNoirCol == c2) {
                if (cheminLibreDeMurs(l1, c1, l1, c2)) {
                    int caseDerriere = c2 + (c2 - c1);
                    boolean bloqueDerriere = (caseDerriere < 0 || caseDerriere >= 9 || !cheminLibreDeMurs(l1, c2, l1, caseDerriere));
                    if (bloqueDerriere) {
                        return cheminLibreDeMurs(l1, c2, l2, c2);
                    }
                }
            }
        }

        return false;
    }

    private boolean cheminLibreDeMurs(int l1, int c1, int l2, int c2) {
        for (MurLogique mur : mursPlaques) {
            if (l2 < l1 && mur.horizontal && mur.ligne == l2 && (mur.col == c1 || mur.col == c1 - 1)) return false;
            if (l2 > l1 && mur.horizontal && mur.ligne == l1 && (mur.col == c1 || mur.col == c1 - 1)) return false;
            if (c2 > c1 && !mur.horizontal && mur.col == c1 && (mur.ligne == l1 || mur.ligne == l1 - 1)) return false;
            if (c2 < c1 && !mur.horizontal && mur.col == c2 && (mur.ligne == l1 || mur.ligne == l1 - 1)) return false;
        }
        return true;
    }
    private boolean aUnChemin(int startLigne, int startCol, int targetLigne) {
        boolean[][] visite = new boolean[9][9];
        Queue<int[]> file = new LinkedList<>();
        
        file.add(new int[]{startLigne, startCol});
        visite[startLigne][startCol] = true;

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!file.isEmpty()) {
            int[] actuel = file.poll();
            int l = actuel[0];
            int c = actuel[1];

            if (l == targetLigne) return true;

            for (int[] dir : directions) {
                int nextL = l + dir[0];
                int nextC = c + dir[1];

                if (nextL >= 0 && nextL < 9 && nextC >= 0 && nextC < 9) {
                    if (!visite[nextL][nextC] && estDeplacementValide(l, c, nextL, nextC)) {
                        visite[nextL][nextC] = true;
                        file.add(new int[]{nextL, nextC});
                    }
                }
            }
        }
        return false; 
    }

   
    public boolean murEstValide(int l, int c, boolean h) {
        // Vérifier les collisions physiques (superposition et croisement)
        for (MurLogique m : mursPlaques) {
            if (m.ligne == l && m.col == c) return false;
            // Un mur horizontal ne peut pas couper un mur vertical
            if (m.ligne == l && m.col == c && m.horizontal != h) return false; 
        }

    
        MurLogique murTest = new MurLogique(l, c, h);
        mursPlaques.add(murTest);

        // Lancement du BFS pour les deux joueurs
        boolean blancPeutFinir = aUnChemin(pionBlancLigne, pionBlancCol, 0); 
        boolean noirPeutFinir = aUnChemin(pionNoirLigne, pionNoirCol, 8);    

        mursPlaques.remove(murTest);

        // Le mur n'est valide que si AUCUN joueur n'est enfermé
        return blancPeutFinir && noirPeutFinir;
    }

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
