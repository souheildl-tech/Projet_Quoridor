# Importation des dépendances externes issues du moteur logique pour la simulation des états
from moteur import obtenir_longueur_chemin_court, obtenir_coups_legaux, simuler_coup, annuler_coup

# Fonction d'évaluation heuristique quantifiant l'avantage stratégique de l'Intelligence Artificielle sur la configuration spatiale actuelle
def evaluer_etat(etat):
    
    # Détermination algorithmique de la distance minimale séparant chaque entité de son objectif de victoire
    distance_ia = obtenir_longueur_chemin_court(etat, etat.position_ia, 8)
    distance_joueur = obtenir_longueur_chemin_court(etat, etat.position_joueur, 0)
    
    # Attribution déterministe de valeurs terminales absolues en cas de condition de victoire ou de défaite immédiate
    if distance_ia == 0: return 1000
    if distance_joueur == 0: return -1000
    
    # Pondération mathématique favorisant la vélocité de l'IA tout en valorisant la conservation de son inventaire d'obstacles
    score = distance_joueur - distance_ia
    score += (etat.murs_ia - etat.murs_joueur) * 0.5
    
    return score

# Implémentation récursive de l'algorithme Minimax optimisé par la technique d'élagage Alpha-Bêta pour la réduction de l'arbre de recherche
def minimax(etat, profondeur, alpha, beta, est_joueur_maximisant):
    
    # Condition d'arrêt interrompant l'exploration lors de l'atteinte de la profondeur maximale ou d'un état terminal du jeu
    if profondeur == 0 or etat.position_ia[0] == 8 or etat.position_joueur[0] == 0:
        return evaluer_etat(etat), None

    meilleur_coup = None

    # Branche de maximisation modélisant le comportement optimal de l'Intelligence Artificielle
    if est_joueur_maximisant: 
        evaluation_maximale = float('-inf')
        
        # Itération exhaustive sur le domaine des actions légales pour simuler les ramifications d'états virtuels
        for coup in obtenir_coups_legaux(etat, est_ia=True):
            information_annulation = simuler_coup(etat, coup, est_ia=True)
            score_evalue, _ = minimax(etat, profondeur - 1, alpha, beta, False)
            annuler_coup(etat, coup, information_annulation, est_ia=True)
            
            # Actualisation de la valeur maximale locale et mémorisation du vecteur d'action correspondant
            if score_evalue > evaluation_maximale:
                evaluation_maximale = score_evalue
                meilleur_coup = coup
            
            # Application de la coupe Alpha-Bêta pour écarter mathématiquement les sous-arbres sous-optimaux
            alpha = max(alpha, score_evalue)
            if beta <= alpha:
                break
                
        return evaluation_maximale, meilleur_coup

    # Branche de minimisation modélisant la réponse antagoniste rationnelle du joueur humain
    else: 
        evaluation_minimale = float('inf')
        
        # Projection des actions adverses légales pour identifier la séquence la plus pénalisante pour l'algorithme
        for coup in obtenir_coups_legaux(etat, est_ia=False):
            information_annulation = simuler_coup(etat, coup, est_ia=False)
            score_evalue, _ = minimax(etat, profondeur - 1, alpha, beta, True)
            annuler_coup(etat, coup, information_annulation, est_ia=False)
            
            # Actualisation de la valeur minimale locale et mémorisation de l'action adverse optimale
            if score_evalue < evaluation_minimale:
                evaluation_minimale = score_evalue
                meilleur_coup = coup
                
            # Ajustement de la borne Bêta et interruption prématurée de la boucle pour élaguer les branches stériles
            beta = min(beta, score_evalue)
            if beta <= alpha:
                break
                
        return evaluation_minimale, meilleur_coup
