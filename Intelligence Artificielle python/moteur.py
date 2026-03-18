from collections import deque

class QuoridorState:
    def __init__(self):
        self.ia_pos = (0, 4)      
        self.joueur_pos = (8, 4)  
        self.ia_walls = 10
        self.joueur_walls = 10
        self.horizontal_walls = set()
        self.vertical_walls = set()

def get_voisins_valides(state, position, ignore_joueurs=False):
    ligne, col = position
    voisins = []
    if ligne < 8 and not ((ligne, col) in state.horizontal_walls or (ligne, col - 1) in state.horizontal_walls):
        voisins.append((ligne + 1, col))
    if ligne > 0 and not ((ligne - 1, col) in state.horizontal_walls or (ligne - 1, col - 1) in state.horizontal_walls):
        voisins.append((ligne - 1, col))
    if col < 8 and not ((ligne, col) in state.vertical_walls or (ligne - 1, col) in state.vertical_walls):
        voisins.append((ligne, col + 1))
    if col > 0 and not ((ligne, col - 1) in state.vertical_walls or (ligne - 1, col - 1) in state.vertical_walls):
        voisins.append((ligne, col - 1))
            
    if not ignore_joueurs:
        adversaire = state.joueur_pos if position == state.ia_pos else state.ia_pos
        if adversaire in voisins:
            voisins.remove(adversaire)
    return voisins

def get_shortest_path_length(state, position_depart, ligne_arrivee):
    file_a_explorer = deque([(position_depart, 0)]) 
    cases_visitees = set([position_depart])
    
    while file_a_explorer:
        case_actuelle, distance = file_a_explorer.popleft()
        if case_actuelle[0] == ligne_arrivee:
            return distance 
            
        for voisin in get_voisins_valides(state, case_actuelle, ignore_joueurs=True):
            if voisin not in cases_visitees:
                cases_visitees.add(voisin)
                file_a_explorer.append((voisin, distance + 1))
    return float('inf') 

def mur_est_valide(state, l, c, orientation):
    if l < 0 or l > 7 or c < 0 or c > 7: 
        return False
    if orientation == "H":
        if (l, c) in state.horizontal_walls or (l, c-1) in state.horizontal_walls or (l, c+1) in state.horizontal_walls or (l, c) in state.vertical_walls:
            return False
    else:
        if (l, c) in state.vertical_walls or (l-1, c) in state.vertical_walls or (l+1, c) in state.vertical_walls or (l, c) in state.horizontal_walls: 
            return False
    return True

def get_legal_moves(state, is_ia):
    coups = []
    pos_actuelle = state.ia_pos if is_ia else state.joueur_pos
    murs_restants = state.ia_walls if is_ia else state.joueur_walls
    
    # 1. Les mouvements
    for v in get_voisins_valides(state, pos_actuelle):
        coups.append(("MOVE", v[0], v[1]))
        
    # 2. Les murs (Optimisation de la zone de recherche)
    if murs_restants > 0:
        adv_pos = state.joueur_pos if is_ia else state.ia_pos
        min_l, max_l = max(0, adv_pos[0] - 2), min(7, adv_pos[0] + 2)
        min_c, max_c = max(0, adv_pos[1] - 2), min(7, adv_pos[1] + 2)
        
        for l in range(min_l, max_l + 1):
            for c in range(min_c, max_c + 1):
                for orientation in ["H", "V"]:
                    if mur_est_valide(state, l, c, orientation):
                        if orientation == "H": state.horizontal_walls.add((l, c))
                        else: state.vertical_walls.add((l, c))
                        
                        if get_shortest_path_length(state, state.ia_pos, 8) != float('inf') and \
                           get_shortest_path_length(state, state.joueur_pos, 0) != float('inf'):
                            coups.append(("WALL", orientation, l, c))
                        
                        if orientation == "H": state.horizontal_walls.remove((l, c))
                        else: state.vertical_walls.remove((l, c))
    return coups
