# Importation des modules réseau pour l'architecture client-serveur et intégration de la logique d'Intelligence Artificielle
import socket
import time 
from moteur import EtatQuoridor
from minimax import minimax
from mcts import mcts

# Définition des constantes de configuration réseau TCP et des paramètres heuristiques par défaut pour les algorithmes
HOTE = '127.0.0.1'
PORT = 65432
PROFONDEUR_MINIMAX = 3
ALGORITHME_CHOISI = "MCTS"
TEMPS_MCTS_SECONDES = 2.0

# Initialisation de l'infrastructure socket TCP et mise en écoute passive pour l'établissement de la communication avec l'interface JavaFX
def demarrer_serveur():
    # Injection des variables globales pour autoriser la modification dynamique des paramètres de calcul durant l'exécution
    global ALGORITHME_CHOISI, PROFONDEUR_MINIMAX, TEMPS_MCTS_SECONDES
    
    print(f"Demarrage de l'intelligence artificielle sur {HOTE}:{PORT}")
    jeu = EtatQuoridor()
    
    # Instanciation du socket réseau selon le protocole IPv4 et le standard de transmission TCP
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as service_reseau:
        
        # Désactivation de la latence de blocage du port au niveau du système d'exploitation pour garantir une réaffectation immédiate
        service_reseau.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        
        service_reseau.bind((HOTE, PORT))  
        service_reseau.listen()            
        connexion, adresse = service_reseau.accept()  
        
        with connexion:
            print(f"Interface distante liee depuis {adresse}")
            
            tour_mcts_arena = True 
            
            # Boucle d'écoute asynchrone capturant le flux d'octets en provenance du client graphique
            while True:
                donnees = connexion.recv(1024)  
                if not donnees: 
                    break
                
                message_recu = donnees.decode('utf-8').strip()
                
                # Interception du protocole d'initialisation pour configurer l'environnement algorithmique selon les sélections de l'utilisateur
                if message_recu.startswith("MODE:"):
                    elements = message_recu.split(":")
                    ALGORITHME_CHOISI = elements[1]
                    
                    difficulte = 2 
                    if len(elements) > 2: difficulte = int(elements[2])
                    
                    # Ajustement quantitatif de l'arbre de recherche Minimax et du budget temporel alloué au MCTS selon le niveau de complexité requis
                    if difficulte == 1:
                        PROFONDEUR_MINIMAX = 1
                        TEMPS_MCTS_SECONDES = 0.05  
                    elif difficulte == 2:
                        PROFONDEUR_MINIMAX = 3
                        TEMPS_MCTS_SECONDES = 0.5  
                    elif difficulte == 3:
                        PROFONDEUR_MINIMAX = 3
                        TEMPS_MCTS_SECONDES = 1.5  
                        
                    # Détermination de l'alternance d'exécution initiale pour le mode Arène basé sur la trame réseau d'initialisation
                    if len(elements) > 3:
                        premier_joueur = elements[3]
                        if ALGORITHME_CHOISI == "ARENA":
                            tour_mcts_arena = (premier_joueur == "MCTS")
                    
                    print(f"Mode de jeu : {ALGORITHME_CHOISI} | Niveau : {difficulte}")
                    connexion.sendall(b"OK\n")
                    continue
                
                # Isolation logique du bloc d'instructions dédié à la résolution des affrontements automatisés entre les deux entités algorithmiques
                if message_recu == "DEMANDER_COUP" and ALGORITHME_CHOISI == "ARENA":
                    
                    # Bifurcation du flux d'exécution confiant la prise de décision au MCTS probabiliste ou au Minimax déterministe
                    if tour_mcts_arena:
                        print(f"\n[ARENA] Le MCTS (Blancs/Haut) réfléchit ({TEMPS_MCTS_SECONDES}s)...")
                        debut_chrono = time.time()
                        coup_choisi = mcts(jeu, TEMPS_MCTS_SECONDES)
                        print(f"-> Temps MCTS : {time.time() - debut_chrono:.2f} secondes")
                    else:
                        print(f"\n[ARENA] Le Minimax (Noirs/Bas) réfléchit (Profondeur {PROFONDEUR_MINIMAX})...")
                        debut_chrono = time.time()
                        _, coup_choisi = minimax(jeu, PROFONDEUR_MINIMAX, float('-inf'), float('inf'), False)
                        print(f"-> Temps Minimax : {time.time() - debut_chrono:.2f} secondes")
                    
                    # Sécurité algorithmique injectant un mouvement de translation par défaut en cas de saturation complète de l'espace de recherche
                    if coup_choisi is None:
                        if tour_mcts_arena: coup_choisi = ("MOVE", jeu.position_ia[0] + 1, jeu.position_ia[1]) 
                        else: coup_choisi = ("MOVE", jeu.position_joueur[0] - 1, jeu.position_joueur[1])

                    reponse = ""
                    # Formatage des coordonnées cartésiennes de translation selon le protocole de communication textuel attendu
                    if coup_choisi[0] == "MOVE":
                        if tour_mcts_arena: jeu.position_ia = (coup_choisi[1], coup_choisi[2])
                        else: jeu.position_joueur = (coup_choisi[1], coup_choisi[2])
                        reponse = f"MOVE:{coup_choisi[1]},{coup_choisi[2]}\n"
                        
                    # Mémorisation des barrières physiques générées et décrémentation des inventaires respectifs
                    elif coup_choisi[0] == "WALL":
                        orientation, ligne_mur, colonne_mur = coup_choisi[1], coup_choisi[2], coup_choisi[3]
                        if orientation == "H": jeu.murs_horizontaux.add((ligne_mur, colonne_mur))
                        else: jeu.murs_verticaux.add((ligne_mur, colonne_mur))
                        
                        if tour_mcts_arena: jeu.murs_ia -= 1
                        else: jeu.murs_joueur -= 1
                        reponse = f"MUR:{ligne_mur},{colonne_mur},{orientation}\n"

                    # Inversion du booléen de contrôle pour transférer l'autorité décisionnelle à l'entité algorithmique antagoniste
                    tour_mcts_arena = not tour_mcts_arena
                    connexion.sendall(reponse.encode('utf-8'))
                    
                    # Évaluation des conditions de terminaison topologique pour déclarer le vainqueur algorithmique de l'affrontement
                    if jeu.position_ia[0] == 8:
                        print("\n le mcts gagne !")
                        break
                    if jeu.position_joueur[0] == 0:
                        print("\n le minimax gagne !")
                        break
                        
                    continue 

                # Isolement de l'architecture logique traitant les requêtes asymétriques entre l'utilisateur humain et l'Intelligence Artificielle
                
                # Déclenchement de la première évaluation heuristique si la priorité d'engagement a été octroyée à l'algorithme
                if message_recu == "START_IA":
                    print("L'IA prend le premier tour de la partie !")
                    
                # Désérialisation des coordonnées reçues pour synchroniser la matrice d'état locale avec la translation du pion adverse
                elif message_recu.startswith("MOVE:"):
                    coordonnees = message_recu.split(":")[1].split(",")
                    jeu.position_joueur = (int(coordonnees[0]), int(coordonnees[1]))
                    
                # Extraction des métadonnées d'obstruction et intégration de la nouvelle barrière physique dans les registres d'état
                elif message_recu.startswith("MUR:"):
                    informations = message_recu.split(":")[1].split(",")
                    ligne_mur, colonne_mur, orientation = int(informations[0]), int(informations[1]), informations[2]
                    
                    if orientation == "H":
                        jeu.murs_horizontaux.add((ligne_mur, colonne_mur))
                    else:
                        jeu.murs_verticaux.add((ligne_mur, colonne_mur))
                    jeu.murs_joueur -= 1
                
                if jeu.position_joueur[0] == 0:
                    print("Le Joueur humain remporte la partie.")
                    break 
                
                print(f"L'algorithme {ALGORITHME_CHOISI} lance une sequence d'evaluation...")

                # Orientation dynamique du calcul vers le graphe d'exploration Minimax ou l'approche stochastique MCTS
                if ALGORITHME_CHOISI == "MINIMAX":
                    score, coup_choisi = minimax(jeu, PROFONDEUR_MINIMAX, float('-inf'), float('inf'), True)
                else:
                    coup_choisi = mcts(jeu, TEMPS_MCTS_SECONDES)
                
                if coup_choisi is None:
                    coup_choisi = ("MOVE", jeu.position_ia[0] + 1, jeu.position_ia[1]) 

                reponse = ""
                if coup_choisi[0] == "MOVE":
                    jeu.position_ia = (coup_choisi[1], coup_choisi[2])
                    reponse = f"MOVE:{jeu.position_ia[0]},{jeu.position_ia[1]}\n"
                    
                elif coup_choisi[0] == "WALL":
                    orientation, ligne_mur, colonne_mur = coup_choisi[1], coup_choisi[2], coup_choisi[3]
                    
                    if orientation == "H": 
                        jeu.murs_horizontaux.add((ligne_mur, colonne_mur))
                    else: 
                        jeu.murs_verticaux.add((ligne_mur, colonne_mur))
                    jeu.murs_ia -= 1
                    reponse = f"MUR:{ligne_mur},{colonne_mur},{orientation}\n"
                
                print(f"Action emise: {reponse.strip()}")
                connexion.sendall(reponse.encode('utf-8'))

                if jeu.position_ia[0] == 8:
                    print("L'IA a franchi la ligne d'arrivée")
                    break

# Point d'entrée principal amorçant la boucle d'écoute du serveur réseau à l'exécution du script Python
if __name__ == "__main__":
    demarrer_serveur()
