import socket
from moteur import QuoridorState
from minimax import minimax

HOST = '127.0.0.1'
PORT = 65432
PROFONDEUR_MINIMAX = 2

def start_server():
    print(f"--- CERVEAU MINIMAX DÉMARRÉ ({HOST}:{PORT}) ---")
    jeu = QuoridorState()
    
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((HOST, PORT))  
        s.listen()  
         
        conn, addr = s.accept()  
        with conn:
            print(f"Joueur connecté ! ({addr})")
            
            while True:
                data = conn.recv(1024)  
                if not data: break
                
                msg_recu = data.decode('utf-8').strip()
                
                # Mise à jour
                if msg_recu.startswith("MOVE:"):
                    coords = msg_recu.split(":")[1].split(",")
                    jeu.joueur_pos = (int(coords[0]), int(coords[1]))
                elif msg_recu.startswith("MUR:"):
                    infos = msg_recu.split(":")[1].split(",")
                    l_mur, c_mur, orientation = int(infos[0]), int(infos[1]), infos[2]
                    if orientation == "H": jeu.horizontal_walls.add((l_mur, c_mur))
                    else: jeu.vertical_walls.add((l_mur, c_mur))
                    jeu.joueur_walls -= 1
                
                if jeu.joueur_pos[0] == 0:
                    print(" LE JOUEUR A GAGNÉ !")
                    break 
                
                print("L'IA réfléchit...")
                # L'appel au cerveau
                score, coup_choisi = minimax(jeu, PROFONDEUR_MINIMAX, float('-inf'), float('inf'), True)
                
                if coup_choisi is None:
                    coup_choisi = ("MOVE", jeu.ia_pos[0] + 1, jeu.ia_pos[1])

                # Jouer et répondre
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
                
                print(f"L'IA joue : {reponse.strip()}")
                conn.sendall(reponse.encode('utf-8'))

                if jeu.ia_pos[0] == 8:
                    print(" L'IA A GAGNÉ !")
                    break

if __name__ == "__main__":
    start_server()
