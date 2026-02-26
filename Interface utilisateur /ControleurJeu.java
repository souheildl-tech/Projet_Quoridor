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
        // On écoute UNIQUEMENT la zone de jeu de la vue
        vue.getZoneJeu().setOnMouseClicked(event -> {
            if (modele.isPartieTerminee() || modele.isTourIA()) return; 
            
            vue.cacherIndicateurs();
            int colClic = (int) (event.getX() / (Plateau.TAILLE_CASE + Plateau.ESPACE_MUR));
            int ligClic = (int) (event.getY() / (Plateau.TAILLE_CASE + Plateau.ESPACE_MUR));
            String commandeAEnvoyer = null; 

            if (colClic >= 0 && colClic < Plateau.NB_CASES && ligClic >= 0 && ligClic < Plateau.NB_CASES) {
                
                if (event.getButton() == MouseButton.PRIMARY) { 
                    if (modele.estDeplacementValide(modele.getPionBlancLigne(), modele.getPionBlancCol(), ligClic, colClic)) {
                        modele.majPositionBlanc(ligClic, colClic);
                        vue.deplacerPionVisuel(true, ligClic, colClic);
                        commandeAEnvoyer = "MOVE:" + ligClic + "," + colClic;
                    }
                }
                else if (event.getButton() == MouseButton.SECONDARY || event.getButton() == MouseButton.MIDDLE) { 
                    boolean h = (event.getButton() == MouseButton.SECONDARY);
                    if (modele.getMursJoueur() > 0 && ligClic < 8 && colClic < 8 && modele.emplacementMurLibre(ligClic, colClic)) {
                        modele.utiliserMurJoueur(ligClic, colClic, h);
                        vue.placerMurVisuel(ligClic, colClic, h);
                        vue.mettreAJourMurs(true, modele.getMursJoueur());
                        commandeAEnvoyer = "MUR:" + ligClic + "," + colClic + "," + (h ? "H" : "V");
                    }
                }
            }

            if (commandeAEnvoyer != null) {
                if (modele.verifierVictoireBlanc()) {
                    System.out.println(" VICTOIRE !");
                } else {
                    envoyerAction(commandeAEnvoyer);
                }
            } else {
                calculerEtAfficherIndicateurs(); // Clic invalide, on remet les indicateurs
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
