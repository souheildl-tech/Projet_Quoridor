package com.quoridor;

import javafx.application.Platform;
import javafx.scene.input.MouseButton;

public class ControleurJeu {
    private Plateau vue;
    private Moteur modele;
    private QuoridorClient clientPython;
    private Process pythonProcess;

    public ControleurJeu(Plateau vue, Moteur modele) {
        this.vue = vue;
        this.modele = modele;
        
        initialiserProcessusPython();
        initialiserReseau();
        initialiserAffichageDepart();
        attacherEcouteurs();
    }

    private void initialiserProcessusPython() {
        try {
            System.out.println("Lancement automatique du serveur Python...");
            // Modifié pour un chemin relatif plus propre
            ProcessBuilder pb = new ProcessBuilder("python3", "serveur.py");
            pb.redirectErrorStream(true);
            pythonProcess = pb.start();
            Thread.sleep(1500); 
        } catch (Exception e) {
            System.err.println("Erreur : Impossible de lancer python3 !");
        }
    }

    private void initialiserReseau() {
        try {
            clientPython = new QuoridorClient();
            clientPython.startConnection("127.0.0.1", 65432);
            System.out.println("Connecté au Cerveau Python !");
        } catch (Exception e) {
            System.err.println("ERREUR : Impossible de se connecter à Python.");
        }
    }

    private void initialiserAffichageDepart() {
        vue.placerPionVisuel(8, 4, javafx.scene.paint.Color.WHITE);
        vue.placerPionVisuel(0, 4, javafx.scene.paint.Color.BLACK);
        vue.mettreAJourMurs(true, modele.getMursJoueur());
        vue.mettreAJourMurs(false, modele.getMursIA());
        calculerEtAfficherIndicateurs();
    }

    private void attacherEcouteurs() {
        vue.getZoneJeu().setOnMouseClicked(event -> {
            if (modele.isPartieTerminee() || modele.isTourIA()) return; 
            
            vue.cacherIndicateurs();

            // 1. Récupération des coordonnées exactes en pixels
            double x = event.getX();
            double y = event.getY();
            int tailleBloc = Plateau.TAILLE_CASE + Plateau.ESPACE_MUR;
            
            // 2. Calcul de la case logique (0 à 8)
            int colClic = (int) (x / tailleBloc);
            int ligClic = (int) (y / tailleBloc);
            
            // 3. Calcul du reste (Modulo) pour la Hitbox interne
            double resteX = x % tailleBloc;
            double resteY = y % tailleBloc;
            
            String commandeAEnvoyer = null; 
            
            if (colClic >= 0 && colClic < Plateau.NB_CASES && ligClic >= 0 && ligClic < Plateau.NB_CASES) {
                
                if (event.getButton() == MouseButton.PRIMARY) {
                    
                    // DÉPLACEMENT
                    if (resteX < Plateau.TAILLE_CASE && resteY < Plateau.TAILLE_CASE) {
                        if (modele.estDeplacementValide(modele.getPionBlancLigne(), modele.getPionBlancCol(), ligClic, colClic)) {
                            modele.majPositionBlanc(ligClic, colClic);
                            vue.deplacerPionVisuel(true, ligClic, colClic);
                            commandeAEnvoyer = "MOVE:" + ligClic + "," + colClic;
                        } else {
                            System.out.println(" Déplacement invalide !");
                        }
                    }
                    
                    // MUR VERTICAL
                    else if (resteX >= Plateau.TAILLE_CASE && resteY < Plateau.TAILLE_CASE) {
                        if (colClic < Plateau.NB_CASES - 1 && ligClic < Plateau.NB_CASES - 1) {
                            if (modele.getMursJoueur() > 0 && modele.murEstValide(ligClic, colClic, false)) {
                                modele.utiliserMurJoueur(ligClic, colClic, false);
                                vue.placerMurVisuel(ligClic, colClic, false);
                                vue.mettreAJourMurs(true, modele.getMursJoueur());
                                commandeAEnvoyer = "MUR:" + ligClic + "," + colClic + ",V";
                            } else {
                                System.out.println("❌ Mur Vertical invalide ou enferme un joueur !");
                            }
                        }
                    }
                    
                    // MUR HORIZONTAL
                    else if (resteX < Plateau.TAILLE_CASE && resteY >= Plateau.TAILLE_CASE) {
                        if (colClic < Plateau.NB_CASES - 1 && ligClic < Plateau.NB_CASES - 1) {
                            if (modele.getMursJoueur() > 0 && modele.murEstValide(ligClic, colClic, true)) {
                                modele.utiliserMurJoueur(ligClic, colClic, true);
                                vue.placerMurVisuel(ligClic, colClic, true);
                                vue.mettreAJourMurs(true, modele.getMursJoueur());
                                commandeAEnvoyer = "MUR:" + ligClic + "," + colClic + ",H";
                            } else {
                                System.out.println(" Mur Horizontal invalide ou enferme un joueur !");
                            }
                        }
                    }
                    // Le clic sur l'intersection des 4 cases (resteX >= 40 ET resteY >= 40) est volontairement ignoré.
                }
            }

            // 4. Envoi de l'action ou remise des indicateurs visuels
            if (commandeAEnvoyer != null) {
                if (modele.verifierVictoireBlanc()) {
                    System.out.println(" VICTOIRE !");
                } else {
                    envoyerAction(commandeAEnvoyer);
                }
            } else {
                calculerEtAfficherIndicateurs(); 
            }
        });
    }
    private void envoyerAction(String messageComplet) {
        modele.setTourIA(true);
        new Thread(() -> {
            try {
                String reponseIA = clientPython.sendMessage(messageComplet);
                Platform.runLater(() -> traiterReponseIA(reponseIA));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void traiterReponseIA(String reponse) {
        if (reponse == null) return;
        String action = reponse.trim();

        try {
            if (action.startsWith("MOVE:")) {
                String[] parts = action.split(":")[1].split(",");
                int l = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                
                modele.majPositionNoir(l, c);
                vue.deplacerPionVisuel(false, l, c);

                if (l == 8) {
                    System.out.println(" L'IA A GAGNÉ !");
                    modele.setPartieTerminee(true);
                }
            } else if (action.startsWith("MUR:")) {
                String[] parts = action.split(":")[1].split(",");
                int l = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                boolean h = parts[2].equals("H");
                
                modele.utiliserMurIA(l, c, h);
                vue.placerMurVisuel(l, c, h);
                vue.mettreAJourMurs(false, modele.getMursIA());
            }
        } catch (Exception e) {
            System.err.println("Erreur IA : " + action);
        }
        
        if (!modele.isPartieTerminee()) {
            modele.setTourIA(false);
            calculerEtAfficherIndicateurs();
        }
    }

    private void calculerEtAfficherIndicateurs() {
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : dirs) {
            int l = modele.getPionBlancLigne() + dir[0];
            int c = modele.getPionBlancCol() + dir[1];
            if (l >= 0 && l < Plateau.NB_CASES && c >= 0 && c < Plateau.NB_CASES) {
                if (modele.estDeplacementValide(modele.getPionBlancLigne(), modele.getPionBlancCol(), l, c)) {
                    vue.afficherIndicateur(l, c);
                }
            }
        }
    }
}
