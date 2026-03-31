# Importe les fonctions du moteur de jeu
from moteur import obtenir_longueur_chemin_court, obtenir_coups_legaux, simuler_coup, annuler_coup

# Évalue l'avantage de l'IA sur le plateau actuel
def evaluer_etat(etat):
    # Calcule la distance restante pour chaque joueur
    distance_ia = obtenir_longueur_chemin_court(etat, etat.position_ia, 8)
    distance_joueur = obtenir_longueur_chemin_court(etat, etat.position_joueur, 0)
    
    # Attribue un score extrême en cas de victoire ou défaite
    if distance_ia == 0: return 1000
    if distance_joueur == 0: return -1000
    
    # Le score favorise l'IA si elle avance vite tout en gardant ses murs en réserve
    score = distance_joueur - distance_ia
    score += (etat.murs_ia - etat.murs_joueur) * 0.5
    
    return score

# Algorithme Minimax anticipant les coups avec élagage Alpha-Beta
def minimax(etat, profondeur, alpha, beta, est_joueur_maximisant):
    # Stoppe la recherche si la profondeur max est atteinte ou la partie terminée
    if profondeur == 0 or etat.position_ia[0] == 8 or etat.position_joueur[0] == 0:
        return evaluer_etat(etat), None

    meilleur_coup = None

    # Tour de l'IA cherchant à maximiser son propre score
    if est_joueur_maximisant: 
        evaluation_maximale = float('-inf')
        
        # Simule chaque coup possible et évalue la réponse adverse
        for coup in obtenir_coups_legaux(etat, est_ia=True):
            information_annulation = simuler_coup(etat, coup, est_ia=True)
            score_evalue, _ = minimax(etat, profondeur - 1, alpha, beta, False)
            annuler_coup(etat, coup, information_annulation, est_ia=True)
            
            # Sauvegarde la meilleure option trouvée
            if score_evalue > evaluation_maximale:
                evaluation_maximale = score_evalue
                meilleur_coup = coup
            
            # Optimisation Alpha-Beta pour ignorer les branches inutiles
            alpha = max(alpha, score_evalue)
            if beta <= alpha:
                break
                
        return evaluation_maximale, meilleur_coup

    # Tour du joueur cherchant à minimiser le score de l'IA
    else: 
        evaluation_minimale = float('inf')
        
        # Simule chaque coup adverse et anticipe le pire scénario pour l'IA
        for coup in obtenir_coups_legaux(etat, est_ia=False):
            information_annulation = simuler_coup(etat, coup, est_ia=False)
            score_evalue, _ = minimax(etat, profondeur - 1, alpha, beta, True)
            annuler_coup(etat, coup, information_annulation, est_ia=False)
            
            # Sauvegarde le coup adverse le plus pénalisant
            if score_evalue < evaluation_minimale:
                evaluation_minimale = score_evalue
                meilleur_coup = coup
                
            # Optimisation Alpha-Beta pour couper court aux chemins désavantageux
            beta = min(beta, score_evalue)
            if beta <= alpha:
                break
                
        return evaluation_minimale, meilleur_coup
