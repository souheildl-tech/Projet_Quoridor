import random
from moteur import get_voisins_valides, get_shortest_path_length, mur_est_valide

def choisir_meilleur_coup_pion(state):
    mouvements_possibles = get_voisins_valides(state, state.ia_pos)
    meilleur_coup = None
    meilleure_distance = float('inf') 
    
    for coup in mouvements_possibles:
        pos_initiale = state.ia_pos
        state.ia_pos = coup
        
        distance = get_shortest_path_length(state, state.ia_pos, 8)
        state.ia_pos = pos_initiale
        
        if distance < meilleure_distance:
            meilleure_distance = distance
            meilleur_coup = ("MOVE", coup[0], coup[1])
            
    return meilleur_coup

def generer_coup_mur_aleatoire(state):
    tentatives = 0
    while tentatives < 50:
        l, c = random.randint(0, 7), random.randint(0, 7)
        orientation = random.choice(["H", "V"])
        if mur_est_valide(state, l, c, orientation):
            return ("WALL", orientation, l, c)
        tentatives += 1
    return None

def calculer_meilleur_coup(state):
    """Fonction principale appelée par le serveur pour obtenir l'action de l'IA."""
    coup_choisi = None
    
    dist_joueur = get_shortest_path_length(state, state.joueur_pos, 0)
    chance_mur = 0.4 if dist_joueur < 4 else 0.1 

    if state.ia_walls > 0 and random.random() < chance_mur:
        coup_choisi = generer_coup_mur_aleatoire(state)
    
    if coup_choisi is None:
        coup_choisi = choisir_meilleur_coup_pion(state)
        
    # cas où l'IA serait bloquée
    if coup_choisi is None:
        voisins = get_voisins_valides(state, state.ia_pos)
        if voisins:
            coup_choisi = ("MOVE", voisins[0][0], voisins[0][1])

    return coup_choisi
