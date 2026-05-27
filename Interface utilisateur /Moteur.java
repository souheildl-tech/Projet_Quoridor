package com.quoridor;

// Importation des structures de données dynamiques pour la gestion des collections et des files d'attente
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

// Moteur central gérant la logique mathématique et les règles strictes du jeu Quoridor
public class Moteur {
    
    // Constante définissant la dimension standard de la matrice de jeu
    public final int NB_CASES = 9;
    
    // Coordonnées cartésiennes initiales et réserve d'obstacles pour le joueur humain
    public int pionBlancLigne = 8;
    public int pionBlancCol = 4;
    public int mursJoueur = 10;
    
    // Coordonnées cartésiennes initiales et réserve d'obstacles pour l'Intelligence Artificielle
    public int pionNoirLigne = 0; 
    public int pionNoirCol = 4;   
    public int mursIA = 10; 

    // Indicateur booléen signalant la conclusion mathématique de l'affrontement
    public boolean partieTerminee = false;
    
    // Structure de données mémorisant l'historique des obstacles physiquement déployés sur le plateau
    public List<MurLogique> mursPlaques = new ArrayList<>();

    // Classe interne modélisant la structure logique et spatiale d'un mur
    public class MurLogique {
        public int ligne;
        public int col;
        public boolean horizontal;
        
        // Constructeur initialisant l'obstacle avec ses coordonnées et son orientation spatiale
        public MurLogique(int l, int c, boolean h) { this.ligne = l; this.col = c; this.horizontal = h; }
    }

    // Évalue géométriquement si la transition entre deux cellules adjacentes est obstruée par un mur
    public boolean cheminLibreDeMurs(int ligne1, int col1, int ligne2, int col2) {
        
        // Itération sur l'ensemble des obstacles actifs pour détecter une éventuelle collision spatiale
        for (MurLogique mur : mursPlaques) {
            
            // Détection d'une obstruction géométrique lors d'un déplacement vers le nord
            if (ligne2 < ligne1 && mur.horizontal && mur.ligne == ligne2 && (mur.col == col1 || mur.col == col1 - 1)) return false;
            
            // Détection d'une obstruction géométrique lors d'un déplacement vers le sud
            if (ligne2 > ligne1 && mur.horizontal && mur.ligne == ligne1 && (mur.col == col1 || mur.col == col1 - 1)) return false;
            
            // Détection d'une obstruction géométrique lors d'un déplacement vers l'est
            if (col2 > col1 && !mur.horizontal && mur.col == col1 && (mur.ligne == ligne1 || mur.ligne == ligne1 - 1)) return false;
            
            // Détection d'une obstruction géométrique lors d'un déplacement vers l'ouest
            if (col2 < col1 && !mur.horizontal && mur.col == col2 && (mur.ligne == ligne1 || mur.ligne == ligne1 - 1)) return false;
        }
        return true;
    }

    // Implémentation de l'algorithme BFS pour certifier l'existence d'un graphe de cheminement valide
    public boolean aUnChemin(int ligneDepart, int colDepart, int ligneCible) {
        
        // Matrice booléenne de mémorisation spatiale pour éviter le traitement redondant de l'algorithme BFS
        boolean[][] visite = new boolean[NB_CASES][NB_CASES];
        
        // Structure de type FIFO régulant l'ordre d'exploration géographique des nœuds
        Queue<int[]> fileAttente = new LinkedList<>();
        
        fileAttente.add(new int[]{ligneDepart, colDepart});
        visite[ligneDepart][colDepart] = true;
        
        // Vecteurs de déplacement standardisés modélisant les mouvements orthogonaux
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        // Boucle de propagation explorant la grille tant qu'il reste des cellules accessibles
        while (!fileAttente.isEmpty()) {
            int[] caseActuelle = fileAttente.poll();
            int ligne = caseActuelle[0];
            int col = caseActuelle[1];

            // Interruption de la recherche asynchrone si les coordonnées atteignent la cible stratégique
            if (ligne == ligneCible) return true;

            // Évaluation systématique des cellules adjacentes selon les vecteurs directionnels
            for (int[] direction : directions) {
                int prochaineLigne = ligne + direction[0];
                int prochaineCol = col + direction[1];

                // Intégration du nœud voisin dans la file d'attente s'il respecte les contraintes physiques
                if (prochaineLigne >= 0 && prochaineLigne < NB_CASES && prochaineCol >= 0 && prochaineCol < NB_CASES) {
                    if (!visite[prochaineLigne][prochaineCol] && cheminLibreDeMurs(ligne, col, prochaineLigne, prochaineCol)) {
                        visite[prochaineLigne][prochaineCol] = true;
                        fileAttente.add(new int[]{prochaineLigne, prochaineCol});
                    }
                }
            }
        }
        
        // Retourne un statut d'échec si l'algorithme BFS a épuisé la totalité des possibilités d'exploration
        return false; 
    }

    // Valide mathématiquement et topologiquement l'autorisation de déployer un nouvel obstacle
    public boolean murEstValide(int ligne, int col, boolean estHorizontal) {
        
        // Analyse préventive des superpositions spatiales avec les structures déjà implémentées
        for (MurLogique mur : mursPlaques) {
            
            // Identification d'un conflit de coordonnées strict
            if (mur.ligne == ligne && mur.col == col) return false;
            
            // Identification d'une collision géométrique sur l'axe horizontal
            if (estHorizontal && mur.horizontal && mur.ligne == ligne && (mur.col == col - 1 || mur.col == col + 1)) return false;
            
            // Identification d'une collision géométrique sur l'axe vertical
            if (!estHorizontal && !mur.horizontal && mur.col == col && (mur.ligne == ligne - 1 || mur.ligne == ligne + 1)) return false;
        }

        // Déploiement temporaire de la structure en mémoire pour simuler ses conséquences sur le graphe de jeu
        MurLogique murTest = new MurLogique(ligne, col, estHorizontal);
        mursPlaques.add(murTest);

        // Invocation du contrôle BFS pour garantir le respect strict de la règle d'anti-enfermement
        boolean blancPeutFinir = aUnChemin(pionBlancLigne, pionBlancCol, 0);
        boolean noirPeutFinir = aUnChemin(pionNoirLigne, pionNoirCol, 8);

        // Suppression de l'obstacle simulé après la validation algorithmique
        mursPlaques.remove(mursPlaques.size() - 1);
        
        // Autorise la modification de l'environnement si les deux entités conservent un chemin vers la victoire
        return blancPeutFinir && noirPeutFinir;
    }

    // Analyse géométrique et logique déterminant la validité d'une translation de pion
    public boolean estDeplacementValide(int ligneDepart, int colDepart, int ligneArrivee, int colArrivee) {
        
        // Extraction des coordonnées cartésiennes de l'entité adverse pour anticiper les collisions
        int ligneAdversaire = (ligneDepart == pionBlancLigne && colDepart == pionBlancCol) ? pionNoirLigne : pionBlancLigne;
        int colAdversaire = (ligneDepart == pionBlancLigne && colDepart == pionBlancCol) ? pionNoirCol : pionBlancCol;

        // Modélisation du déplacement orthogonal standard d'une unité de distance
        if (Math.abs(ligneDepart - ligneArrivee) + Math.abs(colDepart - colArrivee) == 1) {
            
            // Rejet de la translation si la cellule cible est occupée par l'adversaire
            if (ligneArrivee == ligneAdversaire && colArrivee == colAdversaire) return false; 
            
            // Autorisation finale sous réserve d'absence de barrière architecturale sur le segment
            return cheminLibreDeMurs(ligneDepart, colDepart, ligneArrivee, colArrivee);
        }

        // Évaluation de la trajectoire pour un saut acrobatique sur l'axe vertical
        if (Math.abs(ligneDepart - ligneArrivee) == 2 && colDepart == colArrivee) {
            int ligneMilieu = (ligneDepart + ligneArrivee) / 2;
            if (ligneMilieu == ligneAdversaire && colDepart == colAdversaire) { 
                return cheminLibreDeMurs(ligneDepart, colDepart, ligneMilieu, colDepart) && cheminLibreDeMurs(ligneMilieu, colDepart, ligneArrivee, colArrivee);
            }
        }
        
        // Évaluation de la trajectoire pour un saut acrobatique sur l'axe horizontal
        if (Math.abs(colDepart - colArrivee) == 2 && ligneDepart == ligneArrivee) {
            int colMilieu = (colDepart + colArrivee) / 2;
            if (ligneDepart == ligneAdversaire && colMilieu == colAdversaire) {
                return cheminLibreDeMurs(ligneDepart, colDepart, ligneDepart, colMilieu) && cheminLibreDeMurs(ligneDepart, colMilieu, ligneArrivee, colArrivee);
            }
        }

        // Traitement d'exception mathématique autorisant un déplacement diagonal sous contraintes
        if (Math.abs(ligneDepart - ligneArrivee) == 1 && Math.abs(colDepart - colArrivee) == 1) {
            
            // Analyse du scénario géométrique où l'entité bloquante est alignée sur l'axe horizontal
            if (ligneAdversaire == ligneDepart && Math.abs(colAdversaire - colDepart) == 1) {
                int colArriere = colAdversaire + (colAdversaire - colDepart); 
                
                // Détection de l'impossibilité physique d'effectuer un saut standard due à la bordure ou à un mur
                boolean estBloque = (colArriere < 0 || colArriere >= NB_CASES || !cheminLibreDeMurs(ligneAdversaire, colAdversaire, ligneAdversaire, colArriere));
                
                // Validation du chemin de contournement diagonal si l'axe principal s'avère totalement obstrué
                if (estBloque && colArrivee == colAdversaire) {
                    return cheminLibreDeMurs(ligneDepart, colDepart, ligneAdversaire, colAdversaire) && cheminLibreDeMurs(ligneAdversaire, colAdversaire, ligneArrivee, colArrivee);
                }
            }
            // Analyse du scénario géométrique où l'entité bloquante est alignée sur l'axe vertical
            else if (colAdversaire == colDepart && Math.abs(ligneAdversaire - ligneDepart) == 1) {
                int ligneArriere = ligneAdversaire + (ligneAdversaire - ligneDepart); 
                
                // Détection de l'impossibilité physique d'effectuer un saut standard due à la bordure ou à un mur
                boolean estBloque = (ligneArriere < 0 || ligneArriere >= NB_CASES || !cheminLibreDeMurs(ligneAdversaire, colAdversaire, ligneArriere, colAdversaire));
                
                // Validation du chemin de contournement diagonal si l'axe principal s'avère totalement obstrué
                if (estBloque && ligneArrivee == ligneAdversaire) {
                    return cheminLibreDeMurs(ligneDepart, colDepart, ligneAdversaire, colAdversaire) && cheminLibreDeMurs(ligneAdversaire, colAdversaire, ligneArrivee, colArrivee);
                }
            }
        }
        
        // Rejet définitif de la requête si la signature géométrique ne correspond à aucun modèle légal du système
        return false;
    }
}
