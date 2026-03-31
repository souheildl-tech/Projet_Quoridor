# Importe la structure de file pour optimiser le calcul des chemins
from collections import deque

# Classe contenant l'état actuel du plateau
class EtatQuoridor:
    
    def __init__(self):
        # Initialisation des positions de départ de l'IA et du joueur
        self.position_ia = (0, 4)      
        self.position_joueur = (8, 4)  
        
        # Initialisation du stock de murs
        self.murs_ia = 10
        self.murs_joueur = 10
        
        # Ensembles stockant les coordonnées des murs posés sur le plateau
        self.murs_horizontaux = set()
        self.murs_verticaux = set()

# Vérifie si aucun mur ne bloque le passage entre deux cases adjacentes
def est_chemin_libre(etat, ligne1, col1, ligne2, col2):
    # Teste les blocages vers le haut et le bas avec les murs horizontaux
    if ligne2 < ligne1: return not ((ligne2, col1) in etat.murs_horizontaux or (ligne2, col1 - 1) in etat.murs_horizontaux) 
    if ligne2 > ligne1: return not ((ligne1, col1) in etat.murs_horizontaux or (ligne1, col1 - 1) in etat.murs_horizontaux) 
    
    # Teste les blocages vers la droite et la gauche avec les murs verticaux
    if col2 > col1: return not ((ligne1, col1) in etat.murs_verticaux or (ligne1 - 1, col1) in etat.murs_verticaux)     
    if col2 < col1: return not ((ligne1, col2) in etat.murs_verticaux or (ligne1 - 1, col2) in etat.murs_verticaux)     
    
    return True

# Renvoie toutes les cases accessibles depuis une position donnée
def obtenir_voisins_valides(etat, position, est_ia=True, ignorer_joueurs=False):
    ligne, col = position
    position_adversaire = etat.position_joueur if est_ia else etat.position_ia
    voisins = []
    
    # Directions de base correspondant au haut bas gauche et droite
    directions = [(-1, 0), (1, 0), (0, -1), (0, 1)] 
    
    # Teste chaque direction pour trouver les cases libres
    for delta_ligne, delta_col in directions:
        prochaine_ligne, prochaine_col = ligne + delta_ligne, col + delta_col
        
        # Vérifie que la case ciblée est dans les limites et sans mur
        if 0 <= prochaine_ligne <= 8 and 0 <= prochaine_col <= 8 and est_chemin_libre(etat, ligne, col, prochaine_ligne, prochaine_col):
            
            # Gère le cas normal où la case visée est vide
            if ignorer_joueurs or (prochaine_ligne, prochaine_col) != position_adversaire:
                voisins.append((prochaine_ligne, prochaine_col))
                
            # Gère la règle du saut si l'adversaire occupe la case
            elif not ignorer_joueurs and (prochaine_ligne, prochaine_col) == position_adversaire:
                
                saut_ligne, saut_col = prochaine_ligne + delta_ligne, prochaine_col + delta_col
                
                # Tente de sauter tout droit par-dessus l'adversaire
                if 0 <= saut_ligne <= 8 and 0 <= saut_col <= 8 and est_chemin_libre(etat, prochaine_ligne, prochaine_col, saut_ligne, saut_col):
                    voisins.append((saut_ligne, saut_col))
                
                # Tente les sauts en diagonale si le saut rectiligne est bloqué
                else:
                    if delta_ligne != 0: 
                        if col > 0 and est_chemin_libre(etat, prochaine_ligne, prochaine_col, prochaine_ligne, prochaine_col - 1): voisins.append((prochaine_ligne, prochaine_col - 1))
                        if col < 8 and est_chemin_libre(etat, prochaine_ligne, prochaine_col, prochaine_ligne, prochaine_col + 1): voisins.append((prochaine_ligne, prochaine_col + 1))
                    else: 
                        if ligne > 0 and est_chemin_libre(etat, prochaine_ligne, prochaine_col, prochaine_ligne - 1, prochaine_col): voisins.append((prochaine_ligne - 1, prochaine_col))
                        if ligne < 8 and est_chemin_libre(etat, prochaine_ligne, prochaine_col, prochaine_ligne + 1, prochaine_col): voisins.append((prochaine_ligne + 1, prochaine_col))
                        
    return voisins

# Calcule le chemin le plus court avec l'algorithme BFS
def obtenir_longueur_chemin_court(etat, position_depart, ligne_arrivee):
    # Prépare la file de recherche et mémorise les cases visitées
    file_a_explorer = deque([(position_depart, 0)]) 
    cases_visitees = set([position_depart])
    
    # Explore les cases niveau par niveau jusqu'à la ligne d'arrivée
    while file_a_explorer:
        case_actuelle, distance = file_a_explorer.popleft()
        
        if case_actuelle[0] == ligne_arrivee:
            return distance 
            
        for voisin in obtenir_voisins_valides(etat, case_actuelle, ignorer_joueurs=True):
            if voisin not in cases_visitees:
                cases_visitees.add(voisin)
                file_a_explorer.append((voisin, distance + 1))
                
    # Renvoie l'infini si aucun chemin n'est possible
    return float('inf') 

# Vérifie si le placement d'un mur respecte les limites et les autres murs
def est_mur_valide(etat, ligne, col, orientation):
    if ligne < 0 or ligne > 7 or col < 0 or col > 7: return False
    
    # Vérifie les collisions pour un mur horizontal
    if orientation == "H":
        if (ligne, col) in etat.murs_horizontaux or (ligne, col-1) in etat.murs_horizontaux or (ligne, col+1) in etat.murs_horizontaux or (ligne, col) in etat.murs_verticaux: return False
    # Vérifie les collisions pour un mur vertical
    else:
        if (ligne, col) in etat.murs_verticaux or (ligne-1, col) in etat.murs_verticaux or (ligne+1, col) in etat.murs_verticaux or (ligne, col) in etat.murs_horizontaux: return False
        
    return True

# Génère la liste de toutes les actions possibles incluant déplacements et murs
def obtenir_coups_legaux(etat, est_ia):
    coups = []
    position_actuelle = etat.position_ia if est_ia else etat.position_joueur
    murs_restants = etat.murs_ia if est_ia else etat.murs_joueur
    
    # Ajoute tous les déplacements valides à la liste des coups
    for voisin in obtenir_voisins_valides(etat, position_actuelle, est_ia):
        coups.append(("MOVE", voisin[0], voisin[1]))
        
    # Teste le placement de murs si le joueur en a encore en stock
    if murs_restants > 0:
        position_adversaire = etat.position_joueur if est_ia else etat.position_ia
        direction = -1 if est_ia else 1 
        
        # Optimisation limitant la recherche aux cases juste devant l'adversaire
        ligne_cible = position_adversaire[0] + direction
        if 0 <= ligne_cible <= 7:
            for colonne in range(max(0, position_adversaire[1] - 1), min(8, position_adversaire[1] + 1)):
                for orientation in ["H", "V"]:
                    
                    if est_mur_valide(etat, ligne_cible, colonne, orientation):
                        # Simule la pose du mur pour vérifier la validité du chemin
                        if orientation == "H": etat.murs_horizontaux.add((ligne_cible, colonne))
                        else: etat.murs_verticaux.add((ligne_cible, colonne))
                            
                        # Valide le coup si le mur ne bloque pas totalement un joueur
                        if obtenir_longueur_chemin_court(etat, etat.position_ia, 8) != float('inf') and \
                           obtenir_longueur_chemin_court(etat, etat.position_joueur, 0) != float('inf'):
                            coups.append(("WALL", orientation, ligne_cible, colonne))
                            
                        # Retire le mur simulé
                        if orientation == "H": etat.murs_horizontaux.remove((ligne_cible, colonne))
                        else: etat.murs_verticaux.remove((ligne_cible, colonne))
                        
    return coups

# Applique temporairement un coup sur le plateau et sauvegarde l'état précédent
def simuler_coup(etat, coup, est_ia):
    position_annulation = etat.position_ia if est_ia else etat.position_joueur
    
    # Met à jour les coordonnées pour un déplacement
    if coup[0] == "MOVE":
        if est_ia: etat.position_ia = (coup[1], coup[2])
        else: etat.position_joueur = (coup[1], coup[2])
        
    # Met à jour les listes et le stock pour un mur
    elif coup[0] == "WALL":
        if est_ia: etat.murs_ia -= 1
        else: etat.murs_joueur -= 1
        
        if coup[1] == "H": etat.murs_horizontaux.add((coup[2], coup[3]))
        else: etat.murs_verticaux.add((coup[2], coup[3]))
        
    return position_annulation

# Annule un coup simulé pour remettre le plateau dans son état d'origine
def annuler_coup(etat, coup, position_annulation, est_ia):
    # Restaure l'ancienne position
    if coup[0] == "MOVE":
        if est_ia: etat.position_ia = position_annulation
        else: etat.position_joueur = position_annulation
        
    # Retire le mur et le rend au joueur
    elif coup[0] == "WALL":
        if est_ia: etat.murs_ia += 1
        else: etat.murs_joueur += 1
        
        if coup[1] == "H": etat.murs_horizontaux.remove((coup[2], coup[3]))
        else: etat.murs_verticaux.remove((coup[2], coup[3])) 
