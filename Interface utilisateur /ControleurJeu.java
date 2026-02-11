package com.quoridor;

import javafx.application.Platform;
import javafx.scene.input.MouseButton;

public class ControleurJeu {
    private Plateau vue;
    private Moteur modele;
    
    private QuoridorClient clientPython;

    public ControleurJeu(Plateau vue, Moteur modele) {
        this.vue = vue;
        this.modele = modele;
        
        // 1. On démarre la connexion réseau
        initialiserReseau();
        
        // 2. On lance le jeu visuel
        initialiserJeu();
        attacherEcouteurs();
    }

    private void initialiserReseau() {
        try {
            clientPython = new QuoridorClient();

            boolean connecte = clientPython.startConnection("127.0.0.1", 65432);
            
            if (connecte) {
                System.out.println(" Connecté avec succès au Cerveau Python !");
            } else {
                System.err.println(" ERREUR : Le serveur Python n'a pas répondu.");
            }
        } catch (Exception e) {
            System.err.println("ERREUR CRITIQUE : Avez-vous lancé serveur.py ?");
            e.printStackTrace();
        }
    }

    private void initialiserJeu() {
        vue.placerPionVisuel(8, 4, javafx.scene.paint.Color.WHITE); 
        vue.placerPionVisuel(0, 4, javafx.scene.paint.Color.BLACK); 
    }

    private void attacherEcouteurs() {
        vue.setOnMouseClicked(event -> {
            if (modele.isPartieTerminee() || modele.isTourIA()) return;

            int colClic = (int) (event.getX() / (Plateau.TAILLE_CASE + Plateau.ESPACE_MUR));
            int ligClic = (int) (event.getY() / (Plateau.TAILLE_CASE + Plateau.ESPACE_MUR));
            boolean actionValide = false;

            if (colClic >= 0 && colClic < Plateau.NB_CASES && ligClic >= 0 && ligClic < Plateau.NB_CASES) {
                
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (modele.estDeplacementValide(ligClic, colClic)) {
                        modele.majPositionBlanc(ligClic, colClic);
                        vue.deplacerPionBlancVisuel(ligClic, colClic);
                        actionValide = true;
                    }
                }
                else if (event.getButton() == MouseButton.SECONDARY) {
                    if (ligClic >= Plateau.NB_CASES - 1 || colClic >= Plateau.NB_CASES - 1) return;
                    if (modele.emplacementMurLibre(ligClic, colClic)) {
                        modele.ajouterMur(ligClic, colClic, true);
                        vue.placerMurVisuel(ligClic, colClic, true);
                        actionValide = true;
                    }
                }
            }

            if (actionValide) {
                if (modele.verifierVictoire()) {
                    System.out.println(" VICTOIRE !");
                } else {
                    passerLeTour();
                }
            }
        });
    }


    private void passerLeTour() {
        modele.setTourIA(true);
        System.out.println(" Tour de l'IA...");

        new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            Platform.runLater(() -> {
                modele.setTourIA(false);
                System.out.println(" C'est à vous !");
            });
        }).start();
    }
}
