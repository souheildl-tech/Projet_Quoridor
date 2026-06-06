package com.quoridor;

import javafx.scene.input.MouseButton;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.geometry.Pos;

// Contrôleur principal orchestrant la logique de l'interface graphique et les communications avec le serveur Python
public class ControleurJeu {
    
    // Instances de l'architecture modèle-vue gérant la logique locale et le rendu visuel
    private Moteur moteur;
    private Plateau vue;
    
    // Client réseau TCP assurant la liaison avec le moteur d'Intelligence Artificielle
    private QuoridorClient clientPython;
    
    // Verrou de sécurité empêchant les interactions humaines pendant le temps de calcul de l'IA
    private boolean tourIA = false;
    
    // Représentations graphiques des pions sur le plateau JavaFX
    private Circle pionBlancVisuel;
    private Circle pionNoirVisuel;

    // Paramètres de configuration de la session de jeu actuelle
    private String modeActuel;
    private int difficulteActuelle; 
    private String premierJoueurActuel; 
    
    // Variables dédiées à la gestion de la boucle d'événements pour le mode Arène
    private boolean tourMCTSArena; 
    private PauseTransition boucleArena;

    // Initialise les composants du jeu et établit la connexion réseau au démarrage
    public ControleurJeu(Moteur moteur, Plateau vue, String modeIA, int difficulte, String premierJoueur) {
        this.moteur = moteur;
        this.vue = vue;
        this.modeActuel = modeIA;
        this.difficulteActuelle = difficulte; 
        
        // Configuration dynamique de l'affichage des noms selon le mode de jeu choisi
        String nomHaut = modeIA.equals("ARENA") ? "MCTS" : modeIA;
        String nomBas = modeIA.equals("ARENA") ? "MINIMAX" : "Joueur 1";
        vue.setNomsJoueurs(nomHaut, nomBas);

        // Résolution probabiliste du premier joueur si l'option aléatoire a été sélectionnée
        if (premierJoueur.equals("ALEATOIRE")) {
            if (modeIA.equals("ARENA")) {
                premierJoueur = Math.random() < 0.5 ? "MCTS" : "MINIMAX";
            } else {
                premierJoueur = Math.random() < 0.5 ? "HUMAIN" : "IA";
            }
        }
        this.premierJoueurActuel = premierJoueur;
        
        // Définition de l'état initial d'alternance pour les affrontements entre machines
        if (modeIA.equals("ARENA")) {
            this.tourMCTSArena = this.premierJoueurActuel.equals("MCTS");
        }
        
        // Instanciation graphique des pions à leurs coordonnées de départ respectives
        pionBlancVisuel = vue.creerPionVisuel(moteur.pionBlancLigne, moteur.pionBlancCol, Color.web("#88a0c0")); 
        pionNoirVisuel = vue.creerPionVisuel(moteur.pionNoirLigne, moteur.pionNoirCol, Color.web("#cd7070"));   

        // Démarrage asynchrone du script Python et établissement des canaux de communication
        clientPython = new QuoridorClient();
        clientPython.lancerServeurPythonLocal();

        try {
            System.out.println("Tentative de connexion au serveur Python...");
            clientPython.demarrerConnexion("127.0.0.1", 65432);
            
            // Transmission du protocole d'initialisation au serveur pour configurer l'algorithme
            clientPython.envoyerMessage("MODE:" + modeIA + ":" + difficulte + ":" + this.premierJoueurActuel);
        } catch (Exception erreur) {
            System.err.println("CRITIQUE : La connexion au serveur Python a échoué !");
            erreur.printStackTrace();
        }
        
        // Aiguillage logique selon que le mode nécessite une intervention humaine ou non
        if (modeIA.equals("ARENA")) {
            vue.cacherIndicateurs();
            vue.getZoneJeu().setOnMouseClicked(null); 
            lancerMatchArena(); 
        } else {
            ajouterEcouteurSouris();
            
            // Déclenchement automatique du premier tour si l'Intelligence Artificielle a la priorité
            if (this.premierJoueurActuel.equals("IA")) {
                vue.cacherIndicateurs();
                System.out.println("L'IA prend le premier tour.");
                envoyerAction("START_IA");
            } else {
                montrerIndicateurs();
            }
        }
    }

    // Capture les événements de la souris et interprète géométriquement l'intention du joueur
    private void ajouterEcouteurSouris() {
        vue.getZoneJeu().setOnMouseClicked(evenement -> {
            
            // Interruption du processus si la partie est achevée ou si la machine calcule
            if (moteur.partieTerminee || tourIA) return; 
            
            vue.cacherIndicateurs();

            // Extraction des coordonnées exactes du pointeur en pixels
            double positionX = evenement.getX();
            double positionY = evenement.getY();
            
            // Calcul matriciel de la case ciblée selon la taille standard des éléments visuels
            int tailleBloc = vue.TAILLE_CASE + vue.ESPACE_MUR; 
            int colonneClic = (int) (positionX / tailleBloc);
            int ligneClic = (int) (positionY / tailleBloc);
            
            // Utilisation de l'opérateur modulo pour déterminer la position interne dans la case
            double resteX = positionX % tailleBloc;
            double resteY = positionY % tailleBloc;
            
            String commandeAEnvoyer = null; 

            // Validation des limites spatiales pour éviter les erreurs de dépassement
            if (colonneClic >= 0 && colonneClic < moteur.NB_CASES && ligneClic >= 0 && ligneClic < moteur.NB_CASES) {
                if (evenement.getButton() == MouseButton.PRIMARY) {
                    
                    // Définition de la marge d'erreur géométrique pour différencier un pion d'un mur
                    int limiteMur = vue.TAILLE_CASE - 8; 

                    // Cas d'un clic central interprété comme une requête de déplacement
                    if (resteX < limiteMur && resteY < limiteMur) {
                        if (moteur.estDeplacementValide(moteur.pionBlancLigne, moteur.pionBlancCol, ligneClic, colonneClic)) {
                            moteur.pionBlancLigne = ligneClic;
                            moteur.pionBlancCol = colonneClic;
                            pionBlancVisuel.setCenterX((colonneClic * tailleBloc) + (vue.TAILLE_CASE / 2.0));
                            pionBlancVisuel.setCenterY((ligneClic * tailleBloc) + (vue.TAILLE_CASE / 2.0));
                            commandeAEnvoyer = "MOVE:" + ligneClic + "," + colonneClic;
                        }
                    }
                    // Cas d'un clic latéral droit interprété comme la pose d'un mur vertical
                    else if (resteX >= limiteMur && resteY < limiteMur) {
                        if (colonneClic < moteur.NB_CASES - 1 && ligneClic < moteur.NB_CASES - 1) {
                            if (moteur.mursJoueur > 0 && moteur.murEstValide(ligneClic, colonneClic, false)) {
                                moteur.mursPlaques.add(moteur.new MurLogique(ligneClic, colonneClic, false));
                                vue.placerMurVisuel(ligneClic, colonneClic, false, Color.web("#b89c72")); 
                                moteur.mursJoueur--;
                                vue.mettreAJourMurs(vue.getConteneurMursJoueur(), moteur.mursJoueur);
                                commandeAEnvoyer = "MUR:" + ligneClic + "," + colonneClic + ",V";
                            }
                        }
                    }
                    // Cas d'un clic inférieur interprété comme la pose d'un mur horizontal
                    else if (resteX < limiteMur && resteY >= limiteMur) {
                        if (colonneClic < moteur.NB_CASES - 1 && ligneClic < moteur.NB_CASES - 1) {
                            if (moteur.mursJoueur > 0 && moteur.murEstValide(ligneClic, colonneClic, true)) {
                                moteur.mursPlaques.add(moteur.new MurLogique(ligneClic, colonneClic, true));
                                vue.placerMurVisuel(ligneClic, colonneClic, true, Color.web("#b89c72")); 
                                moteur.mursJoueur--;
                                vue.mettreAJourMurs(vue.getConteneurMursJoueur(), moteur.mursJoueur);
                                commandeAEnvoyer = "MUR:" + ligneClic + "," + colonneClic + ",H";
                            }
                        }
                    }
                }
            }

            // Expédition de la commande au serveur Python si l'action locale a été validée
            if (commandeAEnvoyer != null) {
                verifierVictoire();
                if (!moteur.partieTerminee) {
                    envoyerAction(commandeAEnvoyer);
                } else {
                    montrerIndicateurs();
                }
            } else {
                montrerIndicateurs();
            }
        });
    }

    // Délègue la requête réseau à un fil d'exécution secondaire pour maintenir la fluidité de JavaFX
    private void envoyerAction(String messageComplet) {
        tourIA = true;
        System.out.println("Envoi au serveur : " + messageComplet);

        new Thread(() -> {
            try {
                String reponseIA = clientPython.envoyerMessage(messageComplet);
                
                // Réintégration sécurisée de la réponse sur le fil d'exécution principal de l'interface
                Platform.runLater(() -> {
                    traiterReponseIA(reponseIA);
                    tourIA = false;
                    System.out.println("Au tour du joueur.");
                });
            } catch (Exception erreur) { erreur.printStackTrace(); }
        }).start();
    }

    // Analyse et applique la chaîne de caractères standardisée renvoyée par l'algorithme Python
    private void traiterReponseIA(String reponse) {
        if (reponse == null) return;
        String action = reponse.trim();

        // Traitement de la syntaxe correspondant à un déplacement de pion
        if (action.startsWith("MOVE:")) {
            try {
                String[] elements = action.split(":")[1].split(",");
                int ligne = Integer.parseInt(elements[0]);
                int colonne = Integer.parseInt(elements[1]);
                
                moteur.pionNoirLigne = ligne;
                moteur.pionNoirCol = colonne;
                pionNoirVisuel.setCenterX((colonne * (vue.TAILLE_CASE + vue.ESPACE_MUR)) + (vue.TAILLE_CASE / 2.0));
                pionNoirVisuel.setCenterY((ligne * (vue.TAILLE_CASE + vue.ESPACE_MUR)) + (vue.TAILLE_CASE / 2.0));

                if (ligne == 8) {
                    moteur.partieTerminee = true; 
                    afficherMenuFinDePartie(" L'IA a remporté la partie !"); 
                }
                
                if (!moteur.partieTerminee) montrerIndicateurs(); 
                
            } catch (Exception erreur) {}
        }
        // Traitement de la syntaxe correspondant à l'insertion d'un nouvel obstacle
        else if (action.startsWith("MUR:")) {
            try {
                String[] elements = action.split(":")[1].split(",");
                int ligne = Integer.parseInt(elements[0]);
                int colonne = Integer.parseInt(elements[1]);
                boolean estHorizontal = elements[2].equals("H");
                
                moteur.mursPlaques.add(moteur.new MurLogique(ligne, colonne, estHorizontal));
                vue.placerMurVisuel(ligne, colonne, estHorizontal, Color.web("#b89c72")); 
                
                if (moteur.mursIA > 0) {
                    moteur.mursIA--;
                    vue.mettreAJourMurs(vue.getConteneurMursIA(), moteur.mursIA);
                }
                
                if (!moteur.partieTerminee) montrerIndicateurs(); 
                
            } catch (Exception erreur) {}
        }
    }

    // Vérifie si les conditions de victoire sont atteintes pour le joueur humain
    private void verifierVictoire() {
        if (moteur.pionBlancLigne == 0) {
            moteur.partieTerminee = true;
            afficherMenuFinDePartie("🏆 Félicitations, vous avez gagné !");
        }
    }

    // Calcule et affiche visuellement les cases accessibles pour guider l'utilisateur
    private void montrerIndicateurs() {
        vue.cacherIndicateurs();
        for (int ligne = moteur.pionBlancLigne - 2; ligne <= moteur.pionBlancLigne + 2; ligne++) {
            for (int colonne = moteur.pionBlancCol - 2; colonne <= moteur.pionBlancCol + 2; colonne++) {
                if (ligne >= 0 && ligne < moteur.NB_CASES && colonne >= 0 && colonne < moteur.NB_CASES) {
                    if (moteur.estDeplacementValide(moteur.pionBlancLigne, moteur.pionBlancCol, ligne, colonne)) {
                        vue.ajouterIndicateur(ligne, colonne);
                    }
                }
            }
        }
    }

    // Boucle temporelle gérant les requêtes automatiques pour les affrontements algorithmiques
    private void lancerMatchArena() {
        boucleArena = new PauseTransition(Duration.seconds(0.8));
        boucleArena.setOnFinished(evenement -> {
            if (!moteur.partieTerminee) {
                new Thread(() -> {
                    try {
                        String reponseIA = clientPython.envoyerMessage("DEMANDER_COUP");
                        Platform.runLater(() -> {
                            appliquerCoupArena(reponseIA);
                            if (!moteur.partieTerminee) {
                                boucleArena.play(); 
                            }
                        });
                    } catch (Exception erreur) { erreur.printStackTrace(); }
                }).start();
            }
        });
        boucleArena.play();
    }

    // Traduit et applique les actions de jeu spécifiques au déroulement du mode Arène
    private void appliquerCoupArena(String reponse) {
        if (reponse == null || reponse.isEmpty()) return;
        String action = reponse.trim();

        if (action.startsWith("MOVE:")) {
            try {
                String[] elements = action.split(":")[1].split(",");
                int ligne = Integer.parseInt(elements[0]);
                int colonne = Integer.parseInt(elements[1]);
                
                double positionX = (colonne * (vue.TAILLE_CASE + vue.ESPACE_MUR)) + (vue.TAILLE_CASE / 2.0);
                double positionY = (ligne * (vue.TAILLE_CASE + vue.ESPACE_MUR)) + (vue.TAILLE_CASE / 2.0);

                // Alternance de l'application visuelle selon le tour du MCTS ou du Minimax
                if (tourMCTSArena) {
                    moteur.pionNoirLigne = ligne;
                    moteur.pionNoirCol = colonne;
                    pionNoirVisuel.setCenterX(positionX);
                    pionNoirVisuel.setCenterY(positionY);
                    
                    if (ligne == 8) {
                        moteur.partieTerminee = true;
                        afficherMenuFinDePartie(" Le MCTS a remporté le combat !"); 
                    }
                } else {
                    moteur.pionBlancLigne = ligne;
                    moteur.pionBlancCol = colonne;
                    pionBlancVisuel.setCenterX(positionX);
                    pionBlancVisuel.setCenterY(positionY);
                    
                    if (ligne == 0) {
                        moteur.partieTerminee = true;
                        afficherMenuFinDePartie(" Le Minimax a remporté le combat !"); 
                    }
                }
            } catch (Exception e) {}
            
        } else if (action.startsWith("MUR:")) {
            try {
                String[] elements = action.split(":")[1].split(",");
                int ligne = Integer.parseInt(elements[0]);
                int colonne = Integer.parseInt(elements[1]);
                boolean estHorizontal = elements[2].equals("H");
                
                moteur.mursPlaques.add(moteur.new MurLogique(ligne, colonne, estHorizontal));
                
                if (tourMCTSArena) {
                    vue.placerMurVisuel(ligne, colonne, estHorizontal, Color.web("#b89c72"));
                    if (moteur.mursIA > 0) {
                        moteur.mursIA--;
                        vue.mettreAJourMurs(vue.getConteneurMursIA(), moteur.mursIA);
                    }
                } else {
                    vue.placerMurVisuel(ligne, colonne, estHorizontal, Color.web("#b89c72"));
                    if (moteur.mursJoueur > 0) {
                        moteur.mursJoueur--;
                        vue.mettreAJourMurs(vue.getConteneurMursJoueur(), moteur.mursJoueur);
                    }
                }
            } catch (Exception e) {}
        }
        
        // Inversion du booléen pour basculer le contrôle à l'algorithme adverse
        tourMCTSArena = !tourMCTSArena;
    }

    // Affiche une fenêtre modale de conclusion permettant de relancer ou de quitter la session
    private void afficherMenuFinDePartie(String messageVictoire) {
        if (boucleArena != null) boucleArena.stop();

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL); 
        popup.setTitle("Fin de la partie");
        popup.setResizable(false);

        VBox conteneur = new VBox(20);
        conteneur.setAlignment(Pos.CENTER);
        conteneur.setStyle("-fx-background-color: #1b1f2e; -fx-padding: 30; -fx-border-color: #e0af68; -fx-border-width: 3;");

        Label texteVictoire = new Label(messageVictoire);
        texteVictoire.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Century Gothic';");

        Button boutonRejouer = creerBoutonFin("🔄 Recommencer");
        boutonRejouer.setOnAction(e -> {
            popup.close();
            relancerPartie();
        });

        Button boutonMenu = creerBoutonFin("🏠 Retour au Menu");
        boutonMenu.setOnAction(e -> {
            popup.close();
            retournerAuMenu();
        });

        popup.setOnCloseRequest(e -> retournerAuMenu());
        conteneur.getChildren().addAll(texteVictoire, boutonRejouer, boutonMenu);

        Scene scene = new Scene(conteneur, 400, 200);
        popup.setScene(scene);

        Stage fenetrePrincipale = (Stage) vue.getScene().getWindow();
        if (fenetrePrincipale != null) popup.initOwner(fenetrePrincipale);
        popup.show();
    }

    // Instancie un composant graphique interactif pour les choix de fin de partie
    private Button creerBoutonFin(String texte) {
        Button btn = new Button(texte);
        String styleBase = "-fx-background-color: #232838; -fx-text-fill: #e0af68; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 10; -fx-border-color: #e0af68; -fx-border-radius: 10; -fx-cursor: hand;";
        String styleSurvol = "-fx-background-color: #3b4d75; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 10; -fx-border-color: white; -fx-border-radius: 10; -fx-cursor: hand;";
        btn.setStyle(styleBase);
        btn.setOnMouseEntered(e -> btn.setStyle(styleSurvol));
        btn.setOnMouseExited(e -> btn.setStyle(styleBase));
        return btn;
    }

    // Réinitialise l'architecture et l'interface visuelle avec les paramètres actuels
    private void relancerPartie() {
        try {
            Moteur nouveauMoteur = new Moteur();
            Plateau nouveauPlateau = new Plateau();
            ControleurJeu nouveauControleur = new ControleurJeu(nouveauMoteur, nouveauPlateau, this.modeActuel, this.difficulteActuelle, this.premierJoueurActuel);

            StackPane racineJeu = new StackPane(nouveauPlateau);
            racineJeu.setStyle("-fx-padding: 20; -fx-background-color: linear-gradient(to bottom right, #3b4d75, #000000);");
            Scene sceneJeu = new Scene(racineJeu);

            Stage fenetrePrincipale = (Stage) vue.getScene().getWindow();
            fenetrePrincipale.setScene(sceneJeu);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Détruit la session active et redirige l'utilisateur vers l'écran d'accueil principal
    private void retournerAuMenu() {
        try {
            Stage fenetrePrincipale = (Stage) vue.getScene().getWindow();
            Menu menu = new Menu();
            menu.start(fenetrePrincipale);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
