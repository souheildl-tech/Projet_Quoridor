package com.quoridor;

import javafx.application.Platform;
import javafx.scene.input.MouseButton;

public class ControleurJeu {
    private Plateau vue;
    private Moteur modele;

    public ControleurJeu(Plateau vue, Moteur modele) {
        this.vue = vue;
        this.modele = modele;
        initialiserJeu();
        attacherEcouteurs();
    }

    private void initialiserJeu() {
        vue.placerPionVisuel(8, 4, javafx.scene.paint.Color.WHITE); // Initialisation visuelle
    }

    private void attacherEcouteurs() {
        vue.setOnMouseClicked(event -> {
            if (modele.isPartieTerminee() || modele.isTourIA()) return;

            int colClic = (int) (event.getX() / (Plateau.TAILLE_CASE + Plateau.ESPACE_MUR));
            int ligClic = (int) (event.getY() / (Plateau.TAILLE_CASE + Plateau.ESPACE_MUR));
            boolean actionValide = false;

            if (colClic >= 0 && colClic < Plateau.NB_CASES && ligClic >= 0 && ligClic < Plateau.NB_CASES) {
                
                // Clic GAUCHE : Déplacement
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (modele.estDeplacementValide(ligClic, colClic)) {
                        modele.majPositionBlanc(ligClic, colClic);
                        vue.deplacerPionBlancVisuel(ligClic, colClic);
                        actionValide = true;
                    }
                }
                // Clic DROIT : Pose de mur
                else if (event.getButton() == MouseButton.SECONDARY || event.getButton() == MouseButton.MIDDLE) {
                    if (ligClic >= Plateau.NB_CASES - 1 || colClic >= Plateau.NB_CASES - 1) return;
                    boolean horizontal = (event.getButton() == MouseButton.SECONDARY);
                    
                    if (modele.emplacementMurLibre(ligClic, colClic)) {
                        modele.ajouterMur(ligClic, colClic, horizontal);
                        vue.placerMurVisuel(ligClic, colClic, horizontal);
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
