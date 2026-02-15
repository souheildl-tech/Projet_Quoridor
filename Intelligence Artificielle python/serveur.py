import socket
import time

HOST = '127.0.0.1'
PORT = 65432

ia_ligne = 0
ia_col = 4

def start_server():
    global ia_ligne, ia_col
    print(f"--- CERVEAU PYTHON DÉMARRÉ sur {HOST}:{PORT} ---")
    
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((HOST, PORT))
        s.listen()
        
        print("En attente de l'interface Java...")
        
        conn, addr = s.accept()
        with conn:
            print(f"Connecté au jeu ! ({addr})")
            
            while True:
                data = conn.recv(1024)
                if not data:
                    print("Déconnexion du client Java.")
                    break
                
                msg_recu = data.decode('utf-8').strip()
                print(f"\n[Reçu] Action du Joueur : {msg_recu}")
                
            
                try:
                    if msg_recu.startswith("MOVE:"):
                        _, pos = msg_recu.split(":")
                        l, c = map(int, pos.split(","))
                        print(f"-> Le joueur Blanc s'est déplacé en [{l}, {c}]")
                        
                    elif msg_recu.startswith("WALL:"):
                        _, params = msg_recu.split(":")
                        l, c, orient = params.split(",")
                        print(f"-> Le joueur Blanc a posé un mur en [{l}, {c}] (Orientation: {orient})")
                        
                except ValueError as e:
                    print(f"[ERREUR] Message mal formaté reçu du Java : {msg_recu} -> {e}")

                time.sleep(0.2)
                
                # L'IA avance toujours bêtement vers le bas
                ia_ligne += 1
                if ia_ligne > 8: 
                    ia_ligne = 8 
                

                reponse = f"MOVE:{ia_ligne},{ia_col}\n"
                print(f"[Envoi] L'IA joue : {reponse.strip()}")
                conn.sendall(reponse.encode('utf-8'))

if __name__ == "__main__":
    start_server()
