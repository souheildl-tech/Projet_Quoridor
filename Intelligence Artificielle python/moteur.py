# Importation de la structure de données à double extrémité pour l'optimisation des parcours de graphes
from collections import deque

# Importation du module stochastique pour l'échantillonnage aléatoire des actions
import random

# Structure de données encapsulant l'état topologique et matériel de la grille de Quoridor
class EtatQuoridor:
    
    def __init__(self):
        # Instanciation des coordonnées cartésiennes initiales pour l'Intelligence Artificielle et le joueur humain
        self.position_ia = (0, 4)      
        self.position_joueur = (8, 4)  
        
        # Allocation de l'inventaire matériel limitant les capacités d'obstruction
        self.murs_ia = 10
        self.murs_joueur = 10
        
        # Ensembles de hachage mémorisant les coordonnées spatiales des obstacles déployés
        self.murs_horizontaux = set()
        self.murs_verticaux = set()

# Évaluation géométrique certifiant l'absence d'interférence physique entre deux nœuds adjacents
def est_chemin_libre(etat, ligne1, col1, ligne2, col2):
    # Détection des collisions potentielles sur l'axe des ordonnées induites par des structures horizontales
    if ligne2 < ligne1: return not ((ligne2, col1) in etat.murs_horizontaux or (ligne2, col1 - 1) in etat.murs_horizontaux) 
    if ligne2 > ligne1: return not ((ligne1, col1) in etat.murs_horizontaux or (ligne1, col1 - 1) in etat.murs_horizontaux) 
    
    # Détection des collisions potentielles sur l'axe des abscisses induites par des structures verticales
    if col2 > col1: return not ((ligne1, col1) in etat.murs_verticaux or (ligne1 - 1, col1) in etat.murs_verticaux)     
    if col2 < col1: return not ((ligne1, col2) in etat.murs_verticaux or (ligne1 - 1, col2) in etat.murs_verticaux)     
    
    return True

# Identification matricielle de tous les nœuds accessibles depuis une coordonnée source spécifique
def obtenir_voisins_valides(etat, position, est_ia=True, ignorer_joueurs=False):
    ligne, col = position
    position_adversaire = etat.position_joueur if est_ia else etat.position_ia
    voisins = []
    
    # Vecteurs de translation modélisant les mouvements orthogonaux stricts
    directions = [(-1, 0), (1, 0), (0, -1), (0, 1)] 
    
    # Itération sur l'espace vectoriel pour valider l'intégrité topologique des déplacements
    for delta_ligne, delta_col in directions:
        prochaine_ligne, prochaine_col = ligne + delta_ligne, col + delta_col
        
        # Vérification stricte de l'appartenance à la matrice spatiale et de l'absence d'obstruction
        if 0 <= prochaine_ligne <= 8 and 0 <= prochaine_col <= 8 and est_chemin_libre(etat, ligne, col, prochaine_ligne, prochaine_col):
            
            # Traitement du cas standard caractérisé par une cellule cible inoccupée
            if ignorer_joueurs or (prochaine_ligne, prochaine_col) != position_adversaire:
                voisins.append((prochaine_ligne, prochaine_col))
                
            # Application de la résolution mathématique de collision lors de la rencontre avec l'entité antagoniste
            elif not ignorer_joueurs and (prochaine_ligne, prochaine_col) == position_adversaire:
                
                saut_ligne, saut_col = prochaine_ligne + delta_ligne, prochaine_col + delta_col
                
                # Projection du saut acrobatique rectiligne par-dessus l'obstacle dynamique
                if 0 <= saut_ligne <= 8 and 0 <= saut_col <= 8 and est_chemin_libre(etat, prochaine_ligne, prochaine_col, saut_ligne, saut_col):
                    voisins.append((saut_ligne, saut_col))
                
                # Dérivation des trajectoires diagonales secondaires si le vecteur principal est géométriquement bloqué
                else:
                    if delta_ligne != 0: 
                        if col > 0 and est_chemin_libre(etat, prochaine_ligne, prochaine_col, prochaine_ligne, prochaine_col - 1): voisins.append((prochaine_ligne, prochaine_col - 1))
                        if col < 8 and est_chemin_libre(etat, prochaine_ligne, prochaine_col, prochaine_ligne, prochaine_col + 1): voisins.append((prochaine_ligne, prochaine_col + 1))
                    else: 
                        if ligne > 0 and est_chemin_libre(etat, prochaine_ligne, prochaine_col, prochaine_ligne - 1, prochaine_col): voisins.append((prochaine_ligne - 1, prochaine_col))
                        if ligne < 8 and est_chemin_libre(etat, prochaine_ligne, prochaine_col, prochaine_ligne + 1, prochaine_col): voisins.append((prochaine_ligne + 1, prochaine_col))
                        
    return voisins

# Résolution de la distance minimale absolue via l'algorithme BFS appliqué à la matrice de jeu
def obtenir_longueur_chemin_court(etat, position_depart, ligne_arrivee):
    # Initialisation de la file FIFO et du registre d'exploration pour prévenir la redondance cyclique
    file_a_explorer = deque([(position_depart, 0)]) 
    cases_visitees = set([position_depart])
    
    # Propagation itérative de la recherche topologique jusqu'à l'intersection avec l'axe d'arrivée
    while file_a_explorer:
        case_actuelle, distance = file_a_explorer.popleft()
        
        if case_actuelle[0] == ligne_arrivee:
            return distance 
            
        for voisin in obtenir_voisins_valides(etat, case_actuelle, ignorer_joueurs=True):
            if voisin not in cases_visitees:
                cases_visitees.add(voisin)
                file_a_explorer.append((voisin, distance + 1))
                
    # Retourne l'infini mathématique attestant d'un isolement complet de l'entité
    return float('inf') 

# Analyse structurelle validant l'insertion spatiale d'une barrière sans conflit topologique
def est_mur_valide(etat, ligne, col, orientation):
    if ligne < 0 or ligne > 7 or col < 0 or col > 7: return False
    
    # Prévention des chevauchements stricts et partiels pour une configuration de type horizontale
    if orientation == "H":
        if (ligne, col) in etat.murs_horizontaux or (ligne, col-1) in etat.murs_horizontaux or (ligne, col+1) in etat.murs_horizontaux or (ligne, col) in etat.murs_verticaux: return False
    # Prévention des chevauchements stricts et partiels pour une configuration de type verticale
    else:
        if (ligne, col) in etat.murs_verticaux or (ligne-1, col) in etat.murs_verticaux or (ligne+1, col) in etat.murs_verticaux or (ligne, col) in etat.murs_horizontaux: return False
        
    return True

# Génération exhaustive de l'espace des actions légales combinant translations et modifications structurelles
def obtenir_coups_legaux(etat, est_ia):
    coups = []
    position_actuelle = etat.position_ia if est_ia else etat.position_joueur
    murs_restants = etat.murs_ia if est_ia else etat.murs_joueur
    
    # Intégration systématique des vecteurs de mouvement mathématiquement autorisés
    for voisin in obtenir_voisins_valides(etat, position_actuelle, est_ia):
        coups.append(("MOVE", voisin[0], voisin[1]))
        
    # Évaluation conditionnelle de l'arsenal d'obstruction si le stock de l'entité le permet
    if murs_restants > 0:
        position_adversaire = etat.position_joueur if est_ia else etat.position_ia
        direction = -1 if est_ia else 1 
        
        # Réduction heuristique de l'espace de recherche ciblant le voisinage immédiat de l'entité adverse
        ligne_cible = position_adversaire[0] + direction
        if 0 <= ligne_cible <= 7:
            for colonne in range(max(0, position_adversaire[1] - 1), min(8, position_adversaire[1] + 1)):
                for orientation in ["H", "V"]:
                    
                    if est_mur_valide(etat, ligne_cible, colonne, orientation):
                        # Altération temporaire de l'espace de hachage pour anticiper les répercussions du placement
                        if orientation == "H": etat.murs_horizontaux.add((ligne_cible, colonne))
                        else: etat.murs_verticaux.add((ligne_cible, colonne))
                            
                        # Exécution de l'algorithme BFS garantissant l'absence d'enfermement mathématique définitif
                        if obtenir_longueur_chemin_court(etat, etat.position_ia, 8) != float('inf') and \
                           obtenir_longueur_chemin_court(etat, etat.position_joueur, 0) != float('inf'):
                            coups.append(("WALL", orientation, ligne_cible, colonne))
                            
                        # Restauration de l'intégrité de la mémoire spatiale après l'analyse de viabilité
                        if orientation == "H": etat.murs_horizontaux.remove((ligne_cible, colonne))
                        else: etat.murs_verticaux.remove((ligne_cible, colonne))
                        
    return coups

# Projection d'un état futur par l'application d'une action tout en mémorisant le point de retour
def simuler_coup(etat, coup, est_ia):
    position_annulation = etat.position_ia if est_ia else etat.position_joueur
    
    # Actualisation des coordonnées cartésiennes lors de l'exécution d'une translation
    if coup[0] == "MOVE":
        if est_ia: etat.position_ia = (coup[1], coup[2])
        else: etat.position_joueur = (coup[1], coup[2])
        
    # Décrémentation de l'inventaire matériel et enregistrement de la nouvelle coordonnée d'obstruction
    elif coup[0] == "WALL":
        if est_ia: etat.murs_ia -= 1
        else: etat.murs_joueur -= 1
        
        if coup[1] == "H": etat.murs_horizontaux.add((coup[2], coup[3]))
        else: etat.murs_verticaux.add((coup[2], coup[3]))
        
    return position_annulation

# Rétrogradation temporelle de l'état du graphe par réversion explicite de l'action simulée
def annuler_coup(etat, coup, position_annulation, est_ia):
    # Réassignation de la coordonnée spatiale originelle d'avant la translation
    if coup[0] == "MOVE":
        if est_ia: etat.position_ia = position_annulation
        else: etat.position_joueur = position_annulation
        
    # Restitution de l'actif matériel et extraction de la signature spatiale du système d'obstruction
    elif coup[0] == "WALL":
        if est_ia: etat.murs_ia += 1
        else: etat.murs_joueur += 1
        
        if coup[1] == "H": etat.murs_horizontaux.remove((coup[2], coup[3]))
        else: etat.murs_verticaux.remove((coup[2], coup[3])) 


# Clonage profond de l'objet pour préserver la structure lors de l'exploration de l'arbre des possibles
def copier_etat(etat):
    copie = EtatQuoridor()
    copie.position_ia = etat.position_ia
    copie.position_joueur = etat.position_joueur
    copie.murs_ia = etat.murs_ia
    copie.murs_joueur = etat.murs_joueur
    copie.murs_horizontaux = set(etat.murs_horizontaux)
    copie.murs_verticaux = set(etat.murs_verticaux)
    return copie

# Exécution d'une simulation de Monte-Carlo stochastique jusqu'à la détermination d'un état terminal
def jouer_partie_aleatoire(etat, tour_ia):
    # Instanciation d'un clone topologique pour isoler la simulation du flux temporel principal
    etat_simule = copier_etat(etat)
    est_ia = tour_ia
    
    # Itération de la boucle de résolution jusqu'à la validation mathématique d'un critère de victoire
    while etat_simule.position_ia[0] != 8 and etat_simule.position_joueur[0] != 0:
        # Extraction dynamique de l'arborescence des actions instantanées disponibles
        coups = obtenir_coups_legaux(etat_simule, est_ia)
        # Rupture de la boucle d'exécution si l'espace des solutions s'avère totalement vide
        if not coups:
            break
            
        # Sélection stochastique uniforme d'un vecteur d'action dans l'espace des possibles
        coup_choisi = random.choice(coups)
        # Application directe de la perturbation topologique issue de l'échantillonnage
        simuler_coup(etat_simule, coup_choisi, est_ia)
        # Inversion du flux de contrôle pour respecter l'alternance d'exécution asymétrique
        est_ia = not est_ia
        
    # Restitution d'un score de récompense binaire favorisant les occurrences de victoire de l'IA
    if etat_simule.position_ia[0] == 8:
        return 1
    return 0
