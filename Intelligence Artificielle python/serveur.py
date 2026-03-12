import socket
import time
from moteur import QuoridorState
from minimax import calculer_meilleur_coup

HOST = '127.0.0.1'
PORT = 65432

def start_server():
    print(f"--- CERVEAU PYTHON DÉMARRÉ sur {HOST}:{PORT} ---")
    jeu = QuoridorState()
    
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((HOST, PORT))  
        s.listen()  
         
        print("En attente du client Java...")
        conn, addr = s.accept()  
        
        with conn:
            print(f"Connecté au jeu ! ({addr})")
            
            while True:
                data = conn.recv(1024)  
                if not data:            
                    break
                
                msg_recu = data.decode('utf-8').strip()
                print(f"Message Java reçu : {msg_recu}")
                
                try:
                    if msg_recu.startswith("MOVE:"):
                        coords = msg_recu.split(":")[1].split(",")
                        jeu.joueur_pos = (int(coords[0]), int(coords[1]))
                    elif msg_recu.startswith("MUR:"):
                        infos = msg_recu.split(":")[1].split(",")
                        l_mur, c_mur, orientation = int(infos[0]), int(infos[1]), infos[2]
                        if orientation == "H":
                            jeu.horizontal_walls.add((l_mur, c_mur))
                        elif orientation == "V":
                            jeu.vertical_walls.add((l_mur, c_mur))
                        jeu.joueur_walls -= 1
                except Exception as e:
                    print(f"Erreur de parsing : {e}")
                
                if jeu.joueur_pos[0] == 0:
                    print(" LE JOUEUR A GAGNÉ !")
                    break 
                
                time.sleep(0.3) 
                
                # Utilisation du minimax
                coup_choisi = calculer_meilleur_coup(jeu)

                
                reponse = ""
                if coup_choisi[0] == "MOVE":
                    jeu.ia_pos = (coup_choisi[1], coup_choisi[2])
                    reponse = f"MOVE:{jeu.ia_pos[0]},{jeu.ia_pos[1]}\n"
                    
                elif coup_choisi[0] == "WALL":
                    orientation, l_mur, c_mur = coup_choisi[1], coup_choisi[2], coup_choisi[3]
                    if orientation == "H": jeu.horizontal_walls.add((l_mur, c_mur))
                    else: jeu.vertical_walls.add((l_mur, c_mur))
                    jeu.ia_walls -= 1
                    reponse = f"MUR:{l_mur},{c_mur},{orientation}\n"
                
                if reponse:
                    print(f"L'IA joue : {reponse.strip()}")
                    conn.sendall(reponse.encode('utf-8'))

                if jeu.ia_pos[0] == 8:
                    print(" L'IA A GAGNÉ !")
                    break

if __name__ == "__main__":
    start_server()
