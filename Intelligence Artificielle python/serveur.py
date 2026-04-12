# Importation des modules réseau et de la logique de jeu
import socket
from moteur import EtatQuoridor
from minimax import minimax
from mcts import mcts

# Configuration de la connexion réseau et de la profondeur de réflexion
HOTE = '127.0.0.1'
PORT = 65432
PROFONDEUR_MINIMAX = 4

# Initialisation du serveur et attente de la connexion client
def demarrer_serveur():
    
    print(f"Demarrage de l'intelligence artificielle sur {HOTE}:{PORT}")
    jeu = EtatQuoridor()
    
    # Configuration du socket pour écouter les requêtes Java
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as service_reseau:
        service_reseau.bind((HOTE, PORT))  
        service_reseau.listen()            
        connexion, adresse = service_reseau.accept()  
        
        with connexion:
            print(f"Interface distante liee depuis {adresse}")
            
            # Boucle de jeu traitant les messages en continu
            while True:
                donnees = connexion.recv(1024)  
                if not donnees: 
                    break
                
                message_recu = donnees.decode('utf-8').strip()
                
                # Mise à jour du plateau selon l'action du joueur (déplacement ou mur)
                if message_recu.startswith("MOVE:"):
                    coordonnees = message_recu.split(":")[1].split(",")
                    jeu.position_joueur = (int(coordonnees[0]), int(coordonnees[1]))
                    
                elif message_recu.startswith("MUR:"):
                    informations = message_recu.split(":")[1].split(",")
                    ligne_mur, colonne_mur, orientation = int(informations[0]), int(informations[1]), informations[2]
                    
                    if orientation == "H":
                        jeu.murs_horizontaux.add((ligne_mur, colonne_mur))
                    else:
                        jeu.murs_verticaux.add((ligne_mur, colonne_mur))
                    jeu.murs_joueur -= 1
                
                # Arrêt de la partie si le joueur atteint son objectif
                if jeu.position_joueur[0] == 0:
                    print("Le Joueur remporte la session de calcul.")
                    break 
                
                # Lancement de l'algorithme pour déterminer le meilleur coup de l'IA
                print("L'algorithme lance une sequence d'evaluation...")
                score, coup_choisi = minimax(jeu, PROFONDEUR_MINIMAX, float('-inf'), float('inf'), True)
                
                # Coup de sécurité si l'arbre de recherche ne renvoie aucune solution
                if coup_choisi is None:
                    coup_choisi = ("MOVE", jeu.position_ia[0] + 1, jeu.position_ia[1]) 

                # Formatage et exécution de l'action choisie par l'IA
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
                
                # Transmission de la décision au client Java
                print(f"Action emise: {reponse.strip()}")
                connexion.sendall(reponse.encode('utf-8'))

                # Arrêt de la partie si l'IA atteint son objectif
                if jeu.position_ia[0] == 8:
                    print("L'IA a franchi la ligne d'arrivée")
                    break

# Lancement automatique du script
if __name__ == "__main__":
    demarrer_serveur()
