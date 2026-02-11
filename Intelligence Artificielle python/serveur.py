import socket
import time

HOST = '127.0.0.1'
PORT = 65432

# Position de départ de l'IA (Pion Noir)
ia_ligne = 0
ia_col = 4

def start_server():
    global ia_ligne, ia_col
    print(f"--- CERVEAU PYTHON DÉMARRÉ sur {HOST}:{PORT} ---")
    
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:

        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((HOST, PORT))
        s.listen()
        
        print("En attente du corps Java (ControleurJeu)...")
        
        conn, addr = s.accept()
        with conn:
            print(f"Connecté au jeu ! ({addr})")
            
            while True:
                # 1. Attendre le coup du Joueur 
                data = conn.recv(1024)
                if not data:
                    print("Déconnexion du client Java.")
                    break
                
                msg_recu = data.decode('utf-8').strip()
                print(f"Le joueur a joué : {msg_recu}")
                
                # 2. Réfléchir (IA très bête : on avance)
                time.sleep(0.5)
                
                
                ia_ligne += 1
                
                # Si l'IA arrive au bout (victoire), elle s'arrête
                if ia_ligne > 8:
                    ia_ligne = 8

                # 3. Formater et Envoyer la réponse
                reponse = f"MOVE:{ia_ligne},{ia_col}\n"
                print(f"L'IA joue : {reponse.strip()}")
                
                conn.sendall(reponse.encode('utf-8'))

if __name__ == "__main__":
    start_server()
