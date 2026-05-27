import time
import math
import random
from scipy.stats import binomtest

# Importation des composants fondamentaux du moteur Quoridor et des heuristiques décisionnelles
from moteur import EtatQuoridor, simuler_coup, copier_etat
from mcts import mcts
from minimax import minimax


# Configuration du volume itératif déterminant l'intervalle de confiance de la distribution statistique
N_PARTIES = 100

# Mécanisme de troncature temporelle prévenant la stagnation infinie des arbres de recherche
LIMITE_TOURS = 200   


# Instanciation d'un graphe topologique vierge pour l'initialisation d'un affrontement
def creer_etat_initial():
    return EtatQuoridor()

# Évaluation des conditions matricielles de terminaison pour l'une ou l'autre des entités
def partie_terminee(etat):
    return etat.position_ia[0] == 8 or etat.position_joueur[0] == 0

# Validation stricte du franchissement de la ligne d'arrivée par l'Intelligence Artificielle
def ia_a_gagne(etat):
    return etat.position_ia[0] == 8


def _flip_row(r):
    """Transformation affine opérant une symétrie axiale sur l'axe des ordonnées de la matrice Quoridor"""
    return 8 - r

def _flip_wall_h(murs_h):
    """Transposition spatiale des obstacles horizontaux préservant la topologie du graphe inversé"""
    return {(7 - r, c) for r, c in murs_h}

def _flip_wall_v(murs_v):
    """Transposition spatiale des obstacles verticaux via projection sur la matrice miroir"""
    return {(7 - r, c) for r, c in murs_v}

def _flip_etat(etat):
    """Instanciation d'un état de jeu en miroir pour standardiser l'heuristique d'évaluation de l'IA"""
    from moteur import EtatQuoridor
    e = EtatQuoridor()
    
    # Permutation et inversion matricielle des coordonnées spatiales des deux entités
    e.position_ia     = (_flip_row(etat.position_joueur[0]), etat.position_joueur[1])
    e.position_joueur = (_flip_row(etat.position_ia[0]),     etat.position_ia[1])
    
    # Inversion des variables d'inventaire matériel limitant le déploiement d'obstacles
    e.murs_ia     = etat.murs_joueur
    e.murs_joueur = etat.murs_ia
    
    # Application de la transformation géométrique sur l'ensemble des structures physiques existantes
    e.murs_horizontaux = _flip_wall_h(etat.murs_horizontaux)
    e.murs_verticaux   = _flip_wall_v(etat.murs_verticaux)
    return e

def _unflip_coup(coup):
    """Rétro-projection du vecteur d'action généré par l'espace en miroir vers le référentiel cartésien d'origine"""
    if coup is None:
        return None
        
    # Rétablissement de la coordonnée cartésienne pour une action de translation
    if coup[0] == "MOVE":
        return ("MOVE", _flip_row(coup[1]), coup[2])
        
    # Rétablissement des coordonnées spatiales pour l'insertion d'une barrière physique
    elif coup[0] == "WALL":
        orient, r, c = coup[1], coup[2], coup[3]
        if orient == "H":
            return ("WALL", "H", 7 - r, c)   
        else:
            return ("WALL", "V", 7 - r, c)
    return coup

def ia_mcts(etat, est_ia, temps=1.0, ucb=1.5):
    """Encapsulation de l'algorithme Monte-Carlo adaptant dynamiquement l'objectif topologique selon le rôle attribué"""
    if est_ia:
        return mcts(etat, temps_alloue_secondes=temps, constante_ucb=ucb)
    else:
        etat_flip = _flip_etat(etat)
        coup_flip = mcts(etat_flip, temps_alloue_secondes=temps, constante_ucb=ucb)
        return _unflip_coup(coup_flip)


def ia_minimax(etat, est_ia, profondeur=3):
    """Interface d'appel pour l'arbre de décision Minimax optimisé par la méthode d'élagage Alpha-Bêta"""
    _, coup = minimax(
        etat, profondeur,
        float('-inf'), float('inf'),
        est_joueur_maximisant=est_ia
    )
    return coup


def jouer_match(nom_a, ia_func_a, nom_b, ia_func_b, n=N_PARTIES):
    """Moteur d'exécution asynchrone orchestrant les affrontements avec alternance stricte des rôles initiaux"""
    victoires_a = 0
    victoires_b = 0
    nuls = 0
    temps_a = []
    temps_b = []

    for i in range(n):
        etat = creer_etat_initial()
        
        # Alternance déterministe de la priorité d'engagement pour annuler le biais du premier joueur
        a_joue_ia = (i % 2 == 0)
        tour_ia = True
        tours = 0

        # Boucle de résolution temporelle simulant l'affrontement jusqu'à satisfaction des critères d'arrêt
        while not partie_terminee(etat) and tours < LIMITE_TOURS:
            
            # Aiguillage dynamique du flux de contrôle vers l'heuristique décisionnelle active
            if tour_ia:
                func = ia_func_a if a_joue_ia else ia_func_b
                temps_list = temps_a if a_joue_ia else temps_b
            else:
                func = ia_func_b if a_joue_ia else ia_func_a
                temps_list = temps_b if a_joue_ia else temps_a

            t0 = time.time()
            coup = func(etat, est_ia=tour_ia)
            temps_list.append((time.time() - t0) * 1000)

            if coup is None:
                break

            simuler_coup(etat, coup, est_ia=tour_ia)
            tour_ia = not tour_ia
            tours += 1

        # Évaluation des conditions de terminaison et actualisation de la distribution statistique
        if tours >= LIMITE_TOURS:
            nuls += 1
        elif ia_a_gagne(etat):
            
            # Enregistrement d'une occurrence de victoire pour l'algorithme assigné au rôle d'Intelligence Artificielle
            if a_joue_ia:
                victoires_a += 1
            else:
                victoires_b += 1
        else:
            
            # Enregistrement d'une occurrence de victoire pour l'algorithme assigné au rôle du joueur humain
            if a_joue_ia:
                victoires_b += 1
            else:
                victoires_a += 1

        # Génération du flux de journalisation détaillant la résolution de la branche explorée
        gagnant = "A" if (
            (ia_a_gagne(etat) and a_joue_ia) or
            (not ia_a_gagne(etat) and not a_joue_ia)
        ) else ("Nul" if tours >= LIMITE_TOURS else "B")
        print(f"    Partie {i+1:3d}/{n}  →  Gagnant: {gagnant}  (tours: {tours})")

    t_moy_a = sum(temps_a) / len(temps_a) if temps_a else 0
    t_moy_b = sum(temps_b) / len(temps_b) if temps_b else 0
    return victoires_a, victoires_b, nuls, t_moy_a, t_moy_b


def test_binomial(victoires_a, victoires_b, alpha=0.05):
    """Évaluation de l'hypothèse nulle modélisant une stricte équivalence stochastique entre les deux algorithmes"""
    n_decisifs = victoires_a + victoires_b
    if n_decisifs == 0:
        return 1.0, False, "Aucune partie décisive — impossible de conclure"

    res = binomtest(victoires_a, n=n_decisifs, p=0.5, alternative='two-sided')
    p = res.pvalue
    sig = p < alpha

    if sig:
        meilleur = "A" if victoires_a > victoires_b else "B"
        texte = f" SIGNIFICATIF — {meilleur} est meilleur  (p = {p:.4f} < {alpha})"
    else:
        texte = f"—  Non significatif — équivalence probable  (p = {p:.4f} ≥ {alpha})"

    return p, sig, texte


# Structure de données volatile accumulant les métriques pour la synthèse analytique finale
resultats = []   

def afficher_entete(titre):
    print()
    print("═" * 74)
    print(f"  {titre}")
    print("═" * 74)

def run_duel(nom_a, func_a, nom_b, func_b, n=N_PARTIES):
    print(f"\n  ▶ {nom_a}  vs  {nom_b}  ({n} parties)")
    print("  " + "─" * 62)
    va, vb, nuls, ta, tb = jouer_match(nom_a, func_a, nom_b, func_b, n)
    p, sig, texte = test_binomial(va, vb)
    n_tot = va + vb + nuls

    barre_a = "█" * int(100 * va / n_tot / 5) if n_tot else ""
    barre_b = "█" * int(100 * vb / n_tot / 5) if n_tot else ""

    print(f"  {'─'*62}")
    print(f"  Victoires A ({nom_a:<20}) : {va:3d} ({100*va/n_tot:5.1f}%)  {barre_a}")
    print(f"  Victoires B ({nom_b:<20}) : {vb:3d} ({100*vb/n_tot:5.1f}%)  {barre_b}")
    print(f"  Nuls / Timeout              : {nuls:3d} ({100*nuls/n_tot:5.1f}%)")
    print(f"  Temps moy. par coup         : A = {ta:.0f} ms   B = {tb:.0f} ms")
    print(f"  Test binomial               : {texte}")

    resultats.append(dict(A=nom_a, B=nom_b, VA=va, VB=vb, Nuls=nuls,
                          TA=ta, TB=tb, p=p, sig=sig))


# Définition des scénarios d'évaluation expérimentale


# Protocole d'évaluation des performances algorithmiques selon la profondeur d'exploration de l'arbre
# run_duel("Minimax p=3",
#          lambda e, est_ia: ia_minimax(e, est_ia, profondeur=3),
#          "Minimax p=4",
#          lambda e, est_ia: ia_minimax(e, est_ia, profondeur=4))


# Évaluation comparative directe opposant l'approche stochastique Monte-Carlo à l'approche déterministe Minimax
afficher_entete("4 / MCTS vs Minimax — Confrontations croisées")


run_duel("MCTS 1.5s  UCB=1.5",
         lambda e, est_ia: ia_mcts(e, est_ia, temps=1.5, ucb=1.5),
         "Minimax p=3",
         lambda e, est_ia: ia_minimax(e, est_ia, profondeur=3))


# Formatage console du tableau récapitulatif synthétisant les métriques d'évaluation


# Génération du rapport analytique formatant les probabilités d'erreur issues des tests binomiaux
print()
print()
print("═" * 74)
print("  RÉCAPITULATIF GLOBAL")
print("═" * 74)
print(f"  {'A':<22} {'B':<22} {'VA':>4} {'VB':>4} {'Nuls':>5} {'p-val':>7}  Résultat")
print(f"  {'─'*22} {'─'*22} {'─'*4} {'─'*4} {'─'*5} {'─'*7}  {'─'*24}")

sig_count = 0
for r in resultats:
    marker = "" if r["sig"] else "—"
    gagnant = ""
    if r["sig"]:
        gagnant = f"→ {'A' if r['VA'] > r['VB'] else 'B'} meilleur"
        sig_count += 1
    print(
        f"  {r['A']:<22} {r['B']:<22} "
        f"{r['VA']:>4} {r['VB']:>4} {r['Nuls']:>5} "
        f"{r['p']:>7.4f}  {marker} {gagnant}"
    )

print()
print(f"  {sig_count}/{len(resultats)} duels ont une différence statistiquement significative (α=0.05)")
print()
print("  Légende :  p < 0.05 → différence significative")
print("            —  p ≥ 0.05 → pas de différence détectée")
print()
print("═" * 74)
