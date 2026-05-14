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
