package com.quoridor;

// Importe les outils pour gérer les listes et les files d'attente
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

// Gère la logique et les règles strictes du jeu
public class Moteur {
    
    // Taille de la grille
    public final int NB_CASES = 9;
    
    // Position de départ et stock de murs du joueur blanc (humain)
    public int pionBlancLigne = 8;
    public int pionBlancCol = 4;
    public int mursJoueur = 10;
    
    // Position de départ et stock de murs du joueur noir (IA)
    public int pionNoirLigne = 0; 
    public int pionNoirCol = 4;   
    public int mursIA = 10; 

    // État de la partie
    public boolean partieTerminee = false;
    // Garde en mémoire tous les murs déjà posés
    public List<MurLogique> mursPlaques = new ArrayList<>();

    // Représente un mur virtuel sur le plateau
    public class MurLogique {
        public int ligne;
        public int col;
        public boolean horizontal;
        
        // Crée un mur avec ses coordonnées et son sens
        public MurLogique(int l, int c, boolean h) { this.ligne = l; this.col = c; this.horizontal = h; }
    }

    // Vérifie si le passage entre deux cases est libre de tout mur
    public boolean cheminLibreDeMurs(int ligne1, int col1, int ligne2, int col2) {
        // Teste chaque mur posé pour voir s'il coupe la trajectoire
        for (MurLogique mur : mursPlaques) {
            // Blocage vers le haut
            if (ligne2 < ligne1 && mur.horizontal && mur.ligne == ligne2 && (mur.col == col1 || mur.col == col1 - 1)) return false;
            // Blocage vers le bas
            if (ligne2 > ligne1 && mur.horizontal && mur.ligne == ligne1 && (mur.col == col1 || mur.col == col1 - 1)) return false;
            // Blocage vers la droite
            if (col2 > col1 && !mur.horizontal && mur.col == col1 && (mur.ligne == ligne1 || mur.ligne == ligne1 - 1)) return false;
            // Blocage vers la gauche
            if (col2 < col1 && !mur.horizontal && mur.col == col2 && (mur.ligne == ligne1 || mur.ligne == ligne1 - 1)) return false;
        }
        return true;
    }

    
    // Algorithme BFS vérifiant si un joueur n'est pas totalement bloqué
    public boolean aUnChemin(int ligneDepart, int colDepart, int ligneCible) {
        // Grille pour retenir les cases déjà visitées par l'algorithme
        boolean[][] visite = new boolean[NB_CASES][NB_CASES];
        // File d'attente pour explorer les cases progressivement
        Queue<int[]> fileAttente = new LinkedList<>();
        
        fileAttente.add(new int[]{ligneDepart, colDepart});
        visite[ligneDepart][colDepart] = true;
        
        // Directions de base (haut, bas, gauche, droite)
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        // Cherche tant qu'il y a des cases accessibles
        while (!fileAttente.isEmpty()) {
            int[] caseActuelle = fileAttente.poll();
            int ligne = caseActuelle[0];
            int col = caseActuelle[1];

            // Stoppe la recherche si la ligne d'arrivée est trouvée
            if (ligne == ligneCible) return true;

            // Regarde les cases voisines accessibles
            for (int[] direction : directions) {
                int prochaineLigne = ligne + direction[0];
                int prochaineCol = col + direction[1];

                // Ajoute la case à la file si elle est sur le plateau, non visitée et sans mur
                if (prochaineLigne >= 0 && prochaineLigne < NB_CASES && prochaineCol >= 0 && prochaineCol < NB_CASES) {
                    if (!visite[prochaineLigne][prochaineCol] && cheminLibreDeMurs(ligne, col, prochaineLigne, prochaineCol)) {
                        visite[prochaineLigne][prochaineCol] = true;
                        fileAttente.add(new int[]{prochaineLigne, prochaineCol});
                    }
                }
            }
        }
        // Aucun chemin n'a été trouvé
        return false; 
    }

    // Vérifie si on a le droit de poser un mur à cet endroit
    public boolean murEstValide(int ligne, int col, boolean estHorizontal) {
        // Vérifie qu'il ne rentre pas en collision avec un mur existant
        for (MurLogique mur : mursPlaques) {
            // Superposition exacte
            if (mur.ligne == ligne && mur.col == col) return false;
            // Chevauchement horizontal
            if (estHorizontal && mur.horizontal && mur.ligne == ligne && (mur.col == col - 1 || mur.col == col + 1)) return false;
            // Chevauchement vertical
            if (!estHorizontal && !mur.horizontal && mur.col == col && (mur.ligne == ligne - 1 || mur.ligne == ligne + 1)) return false;
        }

        // Pose le mur virtuellement pour tester s'il bloque complètement un joueur
        MurLogique murTest = new MurLogique(ligne, col, estHorizontal);
        mursPlaques.add(murTest);

        // L'algorithme vérifie que les deux joueurs peuvent encore finir
        boolean blancPeutFinir = aUnChemin(pionBlancLigne, pionBlancCol, 0);
        boolean noirPeutFinir = aUnChemin(pionNoirLigne, pionNoirCol, 8);

        // Retire le mur virtuel
        mursPlaques.remove(mursPlaques.size() - 1);
        
        // Valide le mur seulement si personne n'est enfermé
        return blancPeutFinir && noirPeutFinir;
    }

    
    // Vérifie toutes les règles de déplacement et de saut de pion
    public boolean estDeplacementValide(int ligneDepart, int colDepart, int ligneArrivee, int colArrivee) {
        // Trouve les coordonnées de l'adversaire
        int ligneAdversaire = (ligneDepart == pionBlancLigne && colDepart == pionBlancCol) ? pionNoirLigne : pionBlancLigne;
        int colAdversaire = (ligneDepart == pionBlancLigne && colDepart == pionBlancCol) ? pionNoirCol : pionBlancCol;

        // Règle 1 : Déplacement classique d'une seule case
        if (Math.abs(ligneDepart - ligneArrivee) + Math.abs(colDepart - colArrivee) == 1) {
            // Interdit d'aller sur la case de l'adversaire
            if (ligneArrivee == ligneAdversaire && colArrivee == colAdversaire) return false; 
            // Valide si aucun mur ne bloque
            return cheminLibreDeMurs(ligneDepart, colDepart, ligneArrivee, colArrivee);
        }

        // Règle 2 : Saut en ligne droite par-dessus l'adversaire (verticalement)
        if (Math.abs(ligneDepart - ligneArrivee) == 2 && colDepart == colArrivee) {
            int ligneMilieu = (ligneDepart + ligneArrivee) / 2;
            if (ligneMilieu == ligneAdversaire && colDepart == colAdversaire) { 
                return cheminLibreDeMurs(ligneDepart, colDepart, ligneMilieu, colDepart) && cheminLibreDeMurs(ligneMilieu, colDepart, ligneArrivee, colArrivee);
            }
        }
        
        // Règle 2bis : Saut en ligne droite par-dessus l'adversaire (horizontalement)
        if (Math.abs(colDepart - colArrivee) == 2 && ligneDepart == ligneArrivee) {
            int colMilieu = (colDepart + colArrivee) / 2;
            if (ligneDepart == ligneAdversaire && colMilieu == colAdversaire) {
                return cheminLibreDeMurs(ligneDepart, colDepart, ligneDepart, colMilieu) && cheminLibreDeMurs(ligneDepart, colMilieu, ligneArrivee, colArrivee);
            }
        }

        // Règle 3 : Saut en diagonale si un mur empêche le saut en ligne droite
        if (Math.abs(ligneDepart - ligneArrivee) == 1 && Math.abs(colDepart - colArrivee) == 1) {
            
            // Si l'adversaire est sur le côté
            if (ligneAdversaire == ligneDepart && Math.abs(colAdversaire - colDepart) == 1) {
                int colArriere = colAdversaire + (colAdversaire - colDepart); 
                // Vérifie si on ne peut pas sauter tout droit à cause du vide ou d'un mur
                boolean estBloque = (colArriere < 0 || colArriere >= NB_CASES || !cheminLibreDeMurs(ligneAdversaire, colAdversaire, ligneAdversaire, colArriere));
                
                // Si bloqué, on autorise la diagonale
                if (estBloque && colArrivee == colAdversaire) {
                    return cheminLibreDeMurs(ligneDepart, colDepart, ligneAdversaire, colAdversaire) && cheminLibreDeMurs(ligneAdversaire, colAdversaire, ligneArrivee, colArrivee);
                }
            }
            // Si l'adversaire est devant ou derrière
            else if (colAdversaire == colDepart && Math.abs(ligneAdversaire - ligneDepart) == 1) {
                int ligneArriere = ligneAdversaire + (ligneAdversaire - ligneDepart); 
                // Vérifie si on ne peut pas sauter tout droit
                boolean estBloque = (ligneArriere < 0 || ligneArriere >= NB_CASES || !cheminLibreDeMurs(ligneAdversaire, colAdversaire, ligneArriere, colAdversaire));
                
                // Si bloqué, on autorise la diagonale
                if (estBloque && ligneArrivee == ligneAdversaire) {
                    return cheminLibreDeMurs(ligneDepart, colDepart, ligneAdversaire, colAdversaire) && cheminLibreDeMurs(ligneAdversaire, colAdversaire, ligneArrivee, colArrivee);
                }
            }
        }
        
        // Le mouvement ne correspond à aucune règle valide
        return false;
    }
}
