import math
import time
import random

# Importation des dépendances externes issues du moteur logique de Quoridor
from moteur import obtenir_coups_legaux, simuler_coup, copier_etat, obtenir_voisins_valides, obtenir_longueur_chemin_court

# Structure arborescente fondamentale représentant un état de jeu pour l'algorithme MCTS
class NoeudMCTS:
    def __init__(self, parent=None, coup_menant_ici=None, est_ia=True):
        # Initialisation du nœud avec ses métadonnées statistiques et son historique de parenté
        self.parent = parent
        self.coup_menant_ici = coup_menant_ici
        self.est_ia = est_ia
        self.enfants = []
        self.visites = 0
        self.victoires = 0
        self.coups_non_explores = None

    def ucb1(self, constante_c):
        # Priorité absolue à l'exploration d'un nœud n'ayant jamais été visité
        if self.visites == 0:
            return float('inf')
            
        # Évaluation mathématique basée sur la formule Upper Confidence Bound pour équilibrer l'exploration et l'exploitation
        exploitation = self.victoires / self.visites
        exploration = constante_c * math.sqrt(math.log(self.parent.visites) / self.visites)
        return exploitation + exploration

    def meilleur_enfant(self, constante_c):
        # Identification du nœud descendant maximisant la valeur du compromis UCB1
        return max(self.enfants, key=lambda enfant: enfant.ucb1(constante_c))


def obtenir_coups_intelligents(etat, est_ia):
    # Filtre heuristique réduisant l'espace de recherche en priorisant les actions stratégiquement viables
    tous_les_coups = obtenir_coups_legaux(etat, est_ia)
    
    dist_ia = obtenir_longueur_chemin_court(etat, etat.position_ia, 8)
    dist_hu = obtenir_longueur_chemin_court(etat, etat.position_joueur, 0)
    
    coups_deplacements = [c for c in tous_les_coups if c[0] == "MOVE"]
    coups_murs = [c for c in tous_les_coups if c[0] == "WALL"]
    
    # Stratégie agressive privilégiant la course pure si l'entité possède une avance mathématique
    if (est_ia and dist_ia <= dist_hu) or (not est_ia and dist_hu <= dist_ia):
        return coups_deplacements if coups_deplacements else tous_les_coups
    else:
        # Échantillonnage restreint des murs pour entraver l'adversaire sans saturer l'arbre de calcul
        if coups_murs:
            echantillon_murs = random.sample(coups_murs, min(2, len(coups_murs)))
            return coups_deplacements + echantillon_murs
        return coups_deplacements



def jouer_partie_heuristique(etat_simu, est_ia_depart):
    # Phase de simulation rapide (rollout) exécutant une fin de partie semi-aléatoire pour évaluer la branche
    tour_actuel = est_ia_depart
    
    # Dispositif de sécurité empêchant les boucles infinies lors des simulations algorithmiques
    limite_tours = 60 
    
    while etat_simu.position_ia[0] != 8 and etat_simu.position_joueur[0] != 0 and limite_tours > 0:
        coups_possibles = []
        position_actuelle = etat_simu.position_ia if tour_actuel else etat_simu.position_joueur
        
        # Extraction exclusive des vecteurs de déplacement pour maximiser la vitesse de calcul
        for voisin in obtenir_voisins_valides(etat_simu, position_actuelle, est_ia=tour_actuel, ignorer_joueurs=False):
            coups_possibles.append(("MOVE", voisin[0], voisin[1]))
            
        if not coups_possibles: break
            
        coups_qui_avancent = []
        for coup in coups_possibles:
            _, ligne_destination, _ = coup
            if tour_actuel and ligne_destination > position_actuelle[0]:
                coups_qui_avancent.append(coup)
            elif not tour_actuel and ligne_destination < position_actuelle[0]:
                coups_qui_avancent.append(coup)
                
        # Biais probabiliste orientant le pion de manière agressive vers son objectif final
        if coups_qui_avancent and random.random() < 0.80:
            coup_choisi = random.choice(coups_qui_avancent)
        else:
            coup_choisi = random.choice(coups_possibles)
            
        simuler_coup(etat_simu, coup_choisi, tour_actuel)
        tour_actuel = not tour_actuel
        limite_tours -= 1
        
    return 1 if etat_simu.position_ia[0] == 8 else 0



def mcts(etat_initial, temps_alloue_secondes, constante_ucb=1.5):
    # Orchestration de l'algorithme Monte Carlo Tree Search avec gestion dynamique du budget temporel
    racine = NoeudMCTS(est_ia=True)
    temps_debut = time.time()
    simulations_jouees = 0

    # Boucle itérative principale contrainte par le temps alloué en secondes
    while time.time() - temps_debut < temps_alloue_secondes:
        noeud_actuel = racine
        etat_temporaire = copier_etat(etat_initial)

        # Génération dynamique des coups possibles lors de la première visite exploratoire du nœud
        if noeud_actuel.coups_non_explores is None:
            noeud_actuel.coups_non_explores = obtenir_coups_intelligents(etat_temporaire, noeud_actuel.est_ia)

        # Phase de Sélection (descente dans l'arbre) tant que le nœud est totalement développé
        while not noeud_actuel.coups_non_explores and noeud_actuel.enfants:
            noeud_actuel = noeud_actuel.meilleur_enfant(constante_ucb)
            simuler_coup(etat_temporaire, noeud_actuel.coup_menant_ici, not noeud_actuel.est_ia)
            
            if noeud_actuel.coups_non_explores is None:
                noeud_actuel.coups_non_explores = obtenir_coups_intelligents(etat_temporaire, noeud_actuel.est_ia)

        # Validation de l'état du graphe pour détecter une condition de victoire prématurée
        partie_terminee = (etat_temporaire.position_ia[0] == 8 or etat_temporaire.position_joueur[0] == 0)

        # Phase d'Expansion créant une nouvelle branche temporelle dans l'arbre d'exploration
        if not partie_terminee and noeud_actuel.coups_non_explores:
            coup_choisi = random.choice(noeud_actuel.coups_non_explores)
            noeud_actuel.coups_non_explores.remove(coup_choisi)

            simuler_coup(etat_temporaire, coup_choisi, noeud_actuel.est_ia)
            nouvel_enfant = NoeudMCTS(parent=noeud_actuel, coup_menant_ici=coup_choisi, est_ia=not noeud_actuel.est_ia)
            noeud_actuel.enfants.append(nouvel_enfant)
            noeud_actuel = nouvel_enfant

        if partie_terminee:
            # Extraction déterministe du résultat sans nécessiter d'approximation stochastique
            resultat_victoire_ia = 1 if etat_temporaire.position_ia[0] == 8 else 0
        else:
            # Phase de Simulation évaluant la viabilité de ce nouvel état de jeu par approche probabiliste
            resultat_victoire_ia = jouer_partie_heuristique(etat_temporaire, noeud_actuel.est_ia)
        
        simulations_jouees += 1

        # Phase de Rétropropagation remontant les statistiques de victoire jusqu'à la racine
        while noeud_actuel is not None:
            noeud_actuel.visites += 1
            
            # Identification de l'entité décisionnelle responsable de cette branche exploratoire
            if noeud_actuel.parent is not None:
                
                if noeud_actuel.parent.est_ia == True:
                    # Renforcement positif du score si l'issue de la simulation favorise l'IA
                    noeud_actuel.victoires += resultat_victoire_ia
                else:
                    # Inversion mathématique du gain pour modéliser le comportement antagoniste de l'humain
                    noeud_actuel.victoires += (1 - resultat_victoire_ia)
                    
            noeud_actuel = noeud_actuel.parent


    temps_ecoule = time.time() - temps_debut
    vitesse = int(simulations_jouees / temps_ecoule) if temps_ecoule > 0 else 0
    print(f"📊 [MCTS Stats] c={constante_ucb} | Temps: {temps_ecoule:.2f}s | Simulations: {simulations_jouees} | Vitesse: {vitesse} sim/sec")

    if not racine.enfants:
        return None
        
    # Sélection finale de l'action la plus robuste basée sur la densité statistique des visites
    meilleur_coup_final = max(racine.enfants, key=lambda enfant: enfant.visites)
    return meilleur_coup_final.coup_menant_ici
