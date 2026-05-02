package com.quoridor;

// Importe les outils de gestion de la souris et de l'interface graphique
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.application.Platform;
import javafx.geometry.Insets;

// Fait le lien entre les règles mathématiques (Moteur) et l'affichage (Plateau)
public class ControleurJeu {
    
    // Éléments principaux de l'architecture du jeu
    private Moteur moteur;
    private Plateau vue;
    private QuoridorClient clientPython;
    private Process processusPython;
    
    // Bloque les actions du joueur quand c'est le tour de l'IA
    private boolean tourIA = false;
    
    // Représentations visuelles des deux joueurs
    private Circle pionBlancVisuel;
    private Circle pionNoirVisuel;

    // Prépare le contrôleur et lance le serveur IA au démarrage
    public ControleurJeu(Moteur moteur, Plateau vue, String modeIA) {
        this.moteur = moteur;
        this.vue = vue;
        
        // Place les pions de départ sur le plateau avec leurs couleurs respectives
        pionBlancVisuel = vue.creerPionVisuel(moteur.pionBlancLigne, moteur.pionBlancCol, Color.web("#eec273")); 
        pionNoirVisuel = vue.creerPionVisuel(moteur.pionNoirLigne, moteur.pionNoirCol, Color.web("#FF69B4"));   

        // Démarre automatiquement le script Python en arrière-plan
        try {
            System.out.println("Lancement automatique du serveur Python...");
            ProcessBuilder constructeurProcessus = new ProcessBuilder("python3", "serveur.py");
            constructeurProcessus.redirectErrorStream(true);
            processusPython = constructeurProcessus.start();
            Thread.sleep(1500); 
        } catch (Exception erreur) {
            erreur.printStackTrace();
        }

        // Connecte le jeu Java au cerveau Python via le réseau local
        try {
            clientPython = new QuoridorClient();
            clientPython.demarrerConnexion("127.0.0.1", 65432);
            
            // Envoie immédiatement le choix de l'adversaire au serveur
            clientPython.envoyerMessage("MODE:" + modeIA);
            
        } catch (Exception erreur) {
            erreur.printStackTrace();
        }
        
        // Active les clics de souris et affiche les premiers coups possibles
        ajouterEcouteurSouris();
        montrerIndicateurs();
    }

    // Définit ce qu'il se passe quand le joueur clique sur le plateau
    private void ajouterEcouteurSouris() {
        vue.getZoneJeu().setOnMouseClicked(evenement -> {
            
            // Ignore le clic si la partie est finie ou si l'IA réfléchit
            if (moteur.partieTerminee || tourIA) return; 
            
            vue.cacherIndicateurs();

            // Calcule sur quelle ligne et colonne le clic a eu lieu
            double positionX = evenement.getX();
            double positionY = evenement.getY();
            int tailleBloc = vue.TAILLE_CASE + vue.ESPACE_MUR; 
            int colonneClic = (int) (positionX / tailleBloc);
            int ligneClic = (int) (positionY / tailleBloc);
            
            // Calcule où le clic a eu lieu précisément dans la case (pour les murs)
            double resteX = positionX % tailleBloc;
            double resteY = positionY % tailleBloc;
            
            String commandeAEnvoyer = null; 

            // Vérifie que le clic est bien dans la grille avec le clic gauche
            if (colonneClic >= 0 && colonneClic < moteur.NB_CASES && ligneClic >= 0 && ligneClic < moteur.NB_CASES) {
                if (evenement.getButton() == MouseButton.PRIMARY) {
                    
                    int limiteMur = vue.TAILLE_CASE - 8; 

                    // Cas 1 : Le joueur clique au centre d'une case pour se déplacer
                    if (resteX < limiteMur && resteY < limiteMur) {
                        if (moteur.estDeplacementValide(moteur.pionBlancLigne, moteur.pionBlancCol, ligneClic, colonneClic)) {
                            // Met à jour la position mathématique et visuelle
                            moteur.pionBlancLigne = ligneClic;
                            moteur.pionBlancCol = colonneClic;
                            pionBlancVisuel.setCenterX((colonneClic * tailleBloc) + (vue.TAILLE_CASE / 2.0));
                            pionBlancVisuel.setCenterY((ligneClic * tailleBloc) + (vue.TAILLE_CASE / 2.0));
                            commandeAEnvoyer = "MOVE:" + ligneClic + "," + colonneClic;
                        }
                    }
                    // Cas 2 : Le joueur clique sur la fente de droite pour poser un mur vertical
                    else if (resteX >= limiteMur && resteY < limiteMur) {
                        if (colonneClic < moteur.NB_CASES - 1 && ligneClic < moteur.NB_CASES - 1) {
                            if (moteur.mursJoueur > 0 && moteur.murEstValide(ligneClic, colonneClic, false)) {
                                // Ajoute le mur dans la logique, l'affiche, et réduit le stock
                                moteur.mursPlaques.add(moteur.new MurLogique(ligneClic, colonneClic, false));
                                vue.placerMurVisuel(ligneClic, colonneClic, false, Color.web("#eec273"));
                                moteur.mursJoueur--;
                                vue.mettreAJourMurs(vue.getConteneurMursJoueur(), moteur.mursJoueur);
                                commandeAEnvoyer = "MUR:" + ligneClic + "," + colonneClic + ",V";
                            }
                        }
                    }
                    // Cas 3 : Le joueur clique sur la fente du bas pour poser un mur horizontal
                    else if (resteX < limiteMur && resteY >= limiteMur) {
                        if (colonneClic < moteur.NB_CASES - 1 && ligneClic < moteur.NB_CASES - 1) {
                            if (moteur.mursJoueur > 0 && moteur.murEstValide(ligneClic, colonneClic, true)) {
                                // Ajoute le mur dans la logique, l'affiche, et réduit le stock
                                moteur.mursPlaques.add(moteur.new MurLogique(ligneClic, colonneClic, true));
                                vue.placerMurVisuel(ligneClic, colonneClic, true, Color.web("#eec273"));
                                moteur.mursJoueur--;
                                vue.mettreAJourMurs(vue.getConteneurMursJoueur(), moteur.mursJoueur);
                                commandeAEnvoyer = "MUR:" + ligneClic + "," + colonneClic + ",H";
                            }
                        }
                    }
                }
            }

            // Si le coup était valide, on l'envoie à l'IA après avoir vérifié la victoire
            if (commandeAEnvoyer != null) {
                verifierVictoire();
                if (!moteur.partieTerminee) {
                    envoyerAction(commandeAEnvoyer);
                } else {
                    montrerIndicateurs();
                }
            } else {
                // Si le coup était invalide on remet les points verts
                montrerIndicateurs();
            }
        });
    }

    // Transmet l'action du joueur à l'IA et attend sa réponse
    private void envoyerAction(String messageComplet) {
        tourIA = true;
        System.out.println("Envoi au serveur : " + messageComplet);

        // Lance un fil d'exécution séparé pour ne pas geler l'écran pendant le calcul
        new Thread(() -> {
            try {
                String reponseIA = clientPython.envoyerMessage(messageComplet);
                
                // Met à jour l'interface graphique en toute sécurité une fois la réponse reçue
                Platform.runLater(() -> {
                    traiterReponseIA(reponseIA);
                    tourIA = false;
                    System.out.println("Au tour du joueur.");
                });
            } catch (Exception erreur) {
                erreur.printStackTrace();
            }
        }).start();
    }

    // Analyse le texte reçu de l'IA et l'applique sur le plateau
    private void traiterReponseIA(String reponse) {
        if (reponse == null) return;
        String action = reponse.trim();

        // L'IA a choisi de déplacer son pion
        if (action.startsWith("MOVE:")) {
            try {
                // Découpe le texte pour trouver les nouvelles coordonnées
                String[] elements = action.split(":")[1].split(",");
                int ligne = Integer.parseInt(elements[0]);
                int colonne = Integer.parseInt(elements[1]);
                
                // Met à jour le cerveau et l'affichage du pion adverse
                moteur.pionNoirLigne = ligne;
                moteur.pionNoirCol = colonne;
                pionNoirVisuel.setCenterX((colonne * (vue.TAILLE_CASE + vue.ESPACE_MUR)) + (vue.TAILLE_CASE / 2.0));
                pionNoirVisuel.setCenterY((ligne * (vue.TAILLE_CASE + vue.ESPACE_MUR)) + (vue.TAILLE_CASE / 2.0));

                // Si l'IA atteint la ligne du bas, elle gagne
                if (ligne == 8) {
                    System.out.println("Victoire de l'adversaire constatée.");
                    moteur.partieTerminee = true; 
                    vue.afficherVictoireIA(); 
                }
                
                if (!moteur.partieTerminee) montrerIndicateurs(); 
                
            } catch (Exception erreur) {}
        }
        // L'IA a choisi de bloquer avec un mur
        else if (action.startsWith("MUR:")) {
            try {
                // Découpe le texte pour trouver où placer le mur
                String[] elements = action.split(":")[1].split(",");
                int ligne = Integer.parseInt(elements[0]);
                int colonne = Integer.parseInt(elements[1]);
                boolean estHorizontal = elements[2].equals("H");
                
                // Enregistre et dessine le mur adverse en rose
                moteur.mursPlaques.add(moteur.new MurLogique(ligne, colonne, estHorizontal));
                vue.placerMurVisuel(ligne, colonne, estHorizontal, Color.web("#FF69B4"));
                
                // Réduit le stock de l'IA et met à jour son interface
                if (moteur.mursIA > 0) {
                    moteur.mursIA--;
                    vue.mettreAJourMurs(vue.getConteneurMursIA(), moteur.mursIA);
                }
                
                if (!moteur.partieTerminee) montrerIndicateurs(); 
                
            } catch (Exception erreur) {}
        }
    }

    // Vérifie si le joueur humain a atteint la ligne du haut
    private void verifierVictoire() {
        if (moteur.pionBlancLigne == 0) {
            System.out.println("Victoire du joueur constatée.");
            moteur.partieTerminee = true;
            vue.afficherVictoireJoueur();
        }
    }

    // Affiche les petits points verts autour du joueur pour l'aider
    private void montrerIndicateurs() {
        vue.cacherIndicateurs();
        
        // Regarde seulement les cases proches pour éviter des calculs inutiles
        for (int ligne = moteur.pionBlancLigne - 2; ligne <= moteur.pionBlancLigne + 2; ligne++) {
            for (int colonne = moteur.pionBlancCol - 2; colonne <= moteur.pionBlancCol + 2; colonne++) {
                
                // Si la case est sur le plateau et atteignable selon les règles
                if (ligne >= 0 && ligne < moteur.NB_CASES && colonne >= 0 && colonne < moteur.NB_CASES) {
                    if (moteur.estDeplacementValide(moteur.pionBlancLigne, moteur.pionBlancCol, ligne, colonne)) {
                        // Dessine l'indicateur
                        vue.ajouterIndicateur(ligne, colonne);
                    }
                }
            }
        }
    }
}
