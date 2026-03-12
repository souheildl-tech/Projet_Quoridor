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
    """Algorithme BFS : Retourne la distance minimum, ou float('inf') si bloqué."""
    file_a_explorer = deque([(position_depart, 0)]) 
    cases_visitees = set([position_depart])
    
    while len(file_a_explorer) > 0:
        case_actuelle, distance = file_a_explorer.popleft()
        ligne_actuelle = case_actuelle[0]
        
        if ligne_actuelle == ligne_arrivee:
            return distance
            
        voisins = get_voisins_valides(state, case_actuelle, ignore_joueurs=True)
        for voisin in voisins:
            if voisin not in cases_visitees:
                cases_visitees.add(voisin)
                file_a_explorer.append((voisin, distance + 1))
                
    return float('inf')

def mur_est_valide(state, l, c, orientation):
    if l < 0 or l > 7 or c < 0 or c > 7:
        return False
        
    if orientation == "H":
        if (l, c) in state.horizontal_walls or (l, c-1) in state.horizontal_walls or (l, c+1) in state.horizontal_walls: return False
        if (l, c) in state.vertical_walls: return False
    else: 
        if (l, c) in state.vertical_walls or (l-1, c) in state.vertical_walls or (l+1, c) in state.vertical_walls: return False
        if (l, c) in state.horizontal_walls: return False
            
    # Simulation BFS pour vérifier l'enfermement
    if orientation == "H": state.horizontal_walls.add((l, c))
    else: state.vertical_walls.add((l, c))
    
    ia_dist = get_shortest_path_length(state, state.ia_pos, 8)
    joueur_dist = get_shortest_path_length(state, state.joueur_pos, 0)
    
    if orientation == "H": state.horizontal_walls.remove((l, c))
    else: state.vertical_walls.remove((l, c))
    
    return (ia_dist != float('inf') and joueur_dist != float('inf'))
