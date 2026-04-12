import math
import time
import random

from moteur import obtenir_coups_legaux, simuler_coup, copier_etat, jouer_partie_aleatoire

# Structure représentant une situation de jeu dans l'arbre de réflexion
class NoeudMCTS:
    def __init__(self, etat, parent=None, coup_menant_ici=None, est_ia=True):
        # État du plateau à cet instant précis
        self.etat = etat
        # Nœud précédent dans l'arbre
        self.parent = parent
        # L'action qui a permis d'arriver à ce plateau
        self.coup_menant_ici = coup_menant_ici
        # Indique à qui c'est le tour de jouer
        self.est_ia = est_ia
        # Liste des coups qui découlent de cette situation
        self.enfants = []
        # Nombre de fois que l'IA a exploré cette piste
        self.visites = 0
        # Nombre de fois que cette piste a mené à une victoire
        self.victoires = 0
        # Liste des coups qu'on n'a pas encore essayés depuis cette position
        self.coups_non_explores = obtenir_coups_legaux(etat, est_ia)

    # Formule mathématique UCB1 pour équilibrer curiosité et rentabilité
    def ucb1(self, constante=1.41):
        if self.visites == 0:
            return float('inf')
        exploitation = self.victoires / self.visites
        exploration = constante * math.sqrt(math.log(self.parent.visites) / self.visites)
        return exploitation + exploration

    # Sélectionne l'enfant avec le meilleur potentiel selon la formule
    def meilleur_enfant(self):
        return max(self.enfants, key=lambda enfant: enfant.ucb1())

# Algorithme jouant des milliers de parties aléatoires pour trouver le coup parfait
def mcts(etat, temps_alloue_secondes):
    # Initialise la racine de l'arbre de réflexion avec le plateau actuel
    racine = NoeudMCTS(etat, est_ia=True)
    temps_debut = time.time()

    while time.time() - temps_debut < temps_alloue_secondes:
        noeud_actuel = racine

        # ÉTAPE 1 SÉLECTION : On descend dans l'arbre en choisissant les meilleures pistes
        while not noeud_actuel.coups_non_explores and noeud_actuel.enfants:
            noeud_actuel = noeud_actuel.meilleur_enfant()

        # ÉTAPE 2 EXPANSION : On ajoute une nouvelle branche à notre réflexion
        if noeud_actuel.coups_non_explores:
            coup_choisi = random.choice(noeud_actuel.coups_non_explores)
            noeud_actuel.coups_non_explores.remove(coup_choisi)

            nouvel_etat = copier_etat(noeud_actuel.etat)
            simuler_coup(nouvel_etat, coup_choisi, noeud_actuel.est_ia)
          
            nouvel_enfant = NoeudMCTS(nouvel_etat, parent=noeud_actuel, coup_menant_ici=coup_choisi, est_ia=not noeud_actuel.est_ia)
            noeud_actuel.enfants.append(nouvel_enfant)
            noeud_actuel = nouvel_enfant

        # ÉTAPE 3 SIMULATION : On termine la partie complètement au hasard à vitesse grand V
        resultat_victoire_ia = jouer_partie_aleatoire(noeud_actuel.etat, noeud_actuel.est_ia)

        # ÉTAPE 4 RÉTROPROPAGATION : On remonte jusqu'à la racine pour noter le résultat
        while noeud_actuel is not None:
            noeud_actuel.visites += 1
            if resultat_victoire_ia == 1:
                noeud_actuel.victoires += 1
            noeud_actuel = noeud_actuel.parent

    # Arrêt de sécurité au cas où l'IA serait totalement bloquée
    if not racine.enfants:
        return None
        
    # Choix final sélectionnant le coup qui a été le plus exploré car c'est le plus robuste
    meilleur_coup_final = max(racine.enfants, key=lambda enfant: enfant.visites)
    # Renvoie la commande correspondante au serveur
    return meilleur_coup_final.coup_menant_ici
