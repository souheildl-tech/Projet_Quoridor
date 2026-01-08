import socket

HOST = '127.0.0.1'
PORT = 65432

def start_server():
    print(f"--- Serveur Quoridor démarré sur {HOST}:{PORT} ---")
    
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1) 
        s.bind((HOST, PORT))
        s.listen()
        
        print("En attente de la connexion du client Java...")
        
        while True:
            conn, addr = s.accept()
            with conn:
                print(f"\n[+] Nouveau client connecté : {addr}")
                try:
                    while True:
                        data = conn.recv(1024)
                        if not data:
                            print(f"[-] Client {addr} déconnecté.")
                            break 
                        
                        message = data.decode('utf-8').strip() 
                        print(f"Reçu du Java : {message}")
                        
                       
                        if message == "Ping":
                            reponse = "Pong\n"
                        else:
                            reponse = f"Echo: {message}\n"
                        
                        conn.sendall(reponse.encode('utf-8'))
                
                except ConnectionResetError:
                    print(f"[-] Connexion perdue brutalement avec {addr}.")

if __name__ == "__main__":
    start_server()
