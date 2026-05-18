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

public class ControleurJeu {
    
    private Moteur moteur;
    private Plateau vue;
    private QuoridorClient clientPython;
    
    private boolean tourIA = false;
    
    private Circle pionBlancVisuel;
    private Circle pionNoirVisuel;

    private String modeActuel;
    private int difficulteActuelle; 
    private String premierJoueurActuel; 
    
    // NOUVEAU : On ne met plus "true" par défaut, le constructeur va décider !
    private boolean tourMCTSArena; 
    private PauseTransition boucleArena;

    public ControleurJeu(Moteur moteur, Plateau vue, String modeIA, int difficulte, String premierJoueur) {
        this.moteur = moteur;
        this.vue = vue;
        this.modeActuel = modeIA;
        this.difficulteActuelle = difficulte; 
        
        String nomHaut = modeIA.equals("ARENA") ? "MCTS" : modeIA;
        String nomBas = modeIA.equals("ARENA") ? "MINIMAX" : "Joueur 1";
        vue.setNomsJoueurs(nomHaut, nomBas);

        // --- NOUVEAU : Résolution du mode aléatoire ---
        if (premierJoueur.equals("ALEATOIRE")) {
            if (modeIA.equals("ARENA")) {
                premierJoueur = Math.random() < 0.5 ? "MCTS" : "MINIMAX";
            } else {
                premierJoueur = Math.random() < 0.5 ? "HUMAIN" : "IA";
            }
        }
        this.premierJoueurActuel = premierJoueur;
        
        // Initialisation de qui joue en premier visuellement pour l'Arène
        if (modeIA.equals("ARENA")) {
            this.tourMCTSArena = this.premierJoueurActuel.equals("MCTS");
        }
        
        pionBlancVisuel = vue.creerPionVisuel(moteur.pionBlancLigne, moteur.pionBlancCol, Color.web("#88a0c0")); 
        pionNoirVisuel = vue.creerPionVisuel(moteur.pionNoirLigne, moteur.pionNoirCol, Color.web("#cd7070"));   

        clientPython = new QuoridorClient();
        clientPython.lancerServeurPythonLocal();

        try {
            System.out.println("Tentative de connexion au serveur Python...");
            clientPython.demarrerConnexion("127.0.0.1", 65432);
            
            // --- NOUVEAU : On envoie aussi le "premier joueur" à Python ! ---
            clientPython.envoyerMessage("MODE:" + modeIA + ":" + difficulte + ":" + this.premierJoueurActuel);
        } catch (Exception erreur) {
            System.err.println("CRITIQUE : La connexion au cerveau Python a échoué !");
            erreur.printStackTrace();
        }
        
        if (modeIA.equals("ARENA")) {
            vue.cacherIndicateurs();
            vue.getZoneJeu().setOnMouseClicked(null); 
            lancerMatchArena(); 
        } else {
            ajouterEcouteurSouris();
            
            if (this.premierJoueurActuel.equals("IA")) {
                vue.cacherIndicateurs();
                System.out.println("L'IA prend le premier tour.");
                envoyerAction("START_IA");
            } else {
                montrerIndicateurs();
            }
        }
    }

    private void ajouterEcouteurSouris() {
        vue.getZoneJeu().setOnMouseClicked(evenement -> {
            if (moteur.partieTerminee || tourIA) return; 
            
            vue.cacherIndicateurs();

            double positionX = evenement.getX();
            double positionY = evenement.getY();
            int tailleBloc = vue.TAILLE_CASE + vue.ESPACE_MUR; 
            int colonneClic = (int) (positionX / tailleBloc);
            int ligneClic = (int) (positionY / tailleBloc);
            
            double resteX = positionX % tailleBloc;
            double resteY = positionY % tailleBloc;
            
            String commandeAEnvoyer = null; 

            if (colonneClic >= 0 && colonneClic < moteur.NB_CASES && ligneClic >= 0 && ligneClic < moteur.NB_CASES) {
                if (evenement.getButton() == MouseButton.PRIMARY) {
                    int limiteMur = vue.TAILLE_CASE - 8; 

                    if (resteX < limiteMur && resteY < limiteMur) {
                        if (moteur.estDeplacementValide(moteur.pionBlancLigne, moteur.pionBlancCol, ligneClic, colonneClic)) {
                            moteur.pionBlancLigne = ligneClic;
                            moteur.pionBlancCol = colonneClic;
                            pionBlancVisuel.setCenterX((colonneClic * tailleBloc) + (vue.TAILLE_CASE / 2.0));
                            pionBlancVisuel.setCenterY((ligneClic * tailleBloc) + (vue.TAILLE_CASE / 2.0));
                            commandeAEnvoyer = "MOVE:" + ligneClic + "," + colonneClic;
                        }
                    }
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

    private void envoyerAction(String messageComplet) {
        tourIA = true;
        System.out.println("Envoi au serveur : " + messageComplet);

        new Thread(() -> {
            try {
                String reponseIA = clientPython.envoyerMessage(messageComplet);
                Platform.runLater(() -> {
                    traiterReponseIA(reponseIA);
                    tourIA = false;
                    System.out.println("Au tour du joueur.");
                });
            } catch (Exception erreur) { erreur.printStackTrace(); }
        }).start();
    }

    private void traiterReponseIA(String reponse) {
        if (reponse == null) return;
        String action = reponse.trim();

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

    private void verifierVictoire() {
        if (moteur.pionBlancLigne == 0) {
            moteur.partieTerminee = true;
            afficherMenuFinDePartie("Félicitations, vous avez gagné !");
        }
    }

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

                if (tourMCTSArena) {
                    moteur.pionNoirLigne = ligne;
                    moteur.pionNoirCol = colonne;
                    pionNoirVisuel.setCenterX(positionX);
                    pionNoirVisuel.setCenterY(positionY);
                    
                    if (ligne == 8) {
                        moteur.partieTerminee = true;
                        afficherMenuFinDePartie("🤖 Le MCTS a remporté le combat !"); 
                    }
                } else {
                    moteur.pionBlancLigne = ligne;
                    moteur.pionBlancCol = colonne;
                    pionBlancVisuel.setCenterX(positionX);
                    pionBlancVisuel.setCenterY(positionY);
                    
                    if (ligne == 0) {
                        moteur.partieTerminee = true;
                        afficherMenuFinDePartie("💻 Le Minimax a remporté le combat !"); 
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
        tourMCTSArena = !tourMCTSArena;
    }

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

    private Button creerBoutonFin(String texte) {
        Button btn = new Button(texte);
        String styleBase = "-fx-background-color: #232838; -fx-text-fill: #e0af68; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 10; -fx-border-color: #e0af68; -fx-border-radius: 10; -fx-cursor: hand;";
        String styleSurvol = "-fx-background-color: #3b4d75; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 10; -fx-border-color: white; -fx-border-radius: 10; -fx-cursor: hand;";
        btn.setStyle(styleBase);
        btn.setOnMouseEntered(e -> btn.setStyle(styleSurvol));
        btn.setOnMouseExited(e -> btn.setStyle(styleBase));
        return btn;
    }

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

    private void retournerAuMenu() {
        try {
            Stage fenetrePrincipale = (Stage) vue.getScene().getWindow();
            Menu menu = new Menu();
            menu.start(fenetrePrincipale);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
