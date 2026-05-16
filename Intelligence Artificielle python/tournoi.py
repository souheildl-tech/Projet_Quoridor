import time
# Importation de la logique brute
from moteur import EtatQuoridor, simuler_coup, obtenir_coups_legaux
# Importation des deux cerveaux
from minimax import minimax
from mcts import mcts

# Paramètres du tournoi
NOMBRE_PARTIES = 20
PROFONDEUR_MINIMAX = 3 
TEMPS_MCTS = 1.0 

# Variables pour les statistiques
victoires_minimax = 0
victoires_mcts = 0
temps_total_minimax = 0.0
temps_total_mcts = 0.0
coups_joues_minimax = 0
coups_joues_mcts = 0

# Scores Elo de départ
elo_minimax = 1200
elo_mcts = 1200
CONSTANTE_K = 32 

# Fonction mathématique pour mettre à jour le score Elo après chaque partie
def calculer_nouveau_elo(elo_gagnant, elo_perdant):
    esperance_gagnant = 1 / (1 + 10 ** ((elo_perdant - elo_gagnant) / 400))
    esperance_perdant = 1 / (1 + 10 ** ((elo_gagnant - elo_perdant) / 400))
    
    nouveau_elo_gagnant = elo_gagnant + CONSTANTE_K * (1 - esperance_gagnant)
    nouveau_elo_perdant = elo_perdant + CONSTANTE_K * (0 - esperance_perdant)
    
    return nouveau_elo_gagnant, nouveau_elo_perdant

print(f"--- DEBUT DU TOURNOI : {NOMBRE_PARTIES} PARTIES ---")

# Boucle principale du tournoi
for partie in range(1, NOMBRE_PARTIES + 1):
    jeu = EtatQuoridor()
    
    # Alterne qui commence (très important pour l'équité)
    tour_minimax = (partie % 2 == 1) 
    
    print(f"\nLancement de la partie {partie}/{NOMBRE_PARTIES}...")
    
    # Fait tourner la partie jusqu'à ce que quelqu'un gagne
    while jeu.position_ia[0] != 8 and jeu.position_joueur[0] != 0:
        
        # Vérifie si le joueur actuel n'est pas bloqué
        if not obtenir_coups_legaux(jeu, tour_minimax):
            break
            
        debut_chrono = time.time()
        
        if tour_minimax:
            # Le Minimax joue (on lui donne le rôle de l'IA)
            _, coup = minimax(jeu, PROFONDEUR_MINIMAX, float('-inf'), float('inf'), True)
            temps_reflexion = time.time() - debut_chrono
            temps_total_minimax += temps_reflexion
            coups_joues_minimax += 1
        else:
            # Le MCTS joue (on lui donne le rôle du Joueur)
            coup = mcts(jeu, TEMPS_MCTS)
            temps_reflexion = time.time() - debut_chrono
            temps_total_mcts += temps_reflexion
            coups_joues_mcts += 1
            
        # Sécurité anti-crash si l'IA renvoie un coup vide
        if coup is None:
            break
            
        # Applique le coup sur le plateau
        simuler_coup(jeu, coup, tour_minimax)
        
        # Passe le tour à l'autre
        tour_minimax = not tour_minimax

    # Fin de la partie, détermination du gagnant
    if jeu.position_ia[0] == 8:
        print("-> Victoire : MINIMAX")
        victoires_minimax += 1
        elo_minimax, elo_mcts = calculer_nouveau_elo(elo_minimax, elo_mcts)
    else:
        print("-> Victoire : MCTS")
        victoires_mcts += 1
        elo_mcts, elo_minimax = calculer_nouveau_elo(elo_mcts, elo_minimax)

# --- CALCUL DES METRIQUES FINALES ---
temps_moyen_minimax = temps_total_minimax / max(1, coups_joues_minimax)
temps_moyen_mcts = temps_total_mcts / max(1, coups_joues_mcts)
taux_victoire_minimax = (victoires_minimax / NOMBRE_PARTIES) * 100
taux_victoire_mcts = (victoires_mcts / NOMBRE_PARTIES) * 100

print("\n======================================")
print("          RÉSULTATS DU TOURNOI        ")
print("======================================")
print(f"MINIMAX (Profondeur {PROFONDEUR_MINIMAX}):")
print(f"- Victoires       : {victoires_minimax} ({taux_victoire_minimax}%)")
print(f"- Temps par coup  : {temps_moyen_minimax:.3f} secondes")
print(f"- Classement Elo  : {int(elo_minimax)}")
print("--------------------------------------")
print(f"MCTS (Temps alloué {TEMPS_MCTS}s):")
print(f"- Victoires       : {victoires_mcts} ({taux_victoire_mcts}%)")
print(f"- Temps par coup  : {temps_moyen_mcts:.3f} secondes")
print(f"- Classement Elo  : {int(elo_mcts)}")
print("======================================")
