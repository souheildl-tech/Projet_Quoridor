package com.quoridor;

public class ControleurJeu {

    private Plateau vue;
    private Moteur modele;

    public ControleurJeu(Plateau vue, Moteur modele) {
        this.vue = vue;
        this.modele = modele;
        attacherEcouteurs();
    }

    private void attacherEcouteurs() {
        // C'est le contrôleur qui capte le clic sur le plateau
        vue.setOnMouseClicked(event -> {
            
            int colClic = (int) (event.getX() / (Plateau.TAILLE_CASE + Plateau.ESPACE_MUR));
            int ligClic = (int) (event.getY() / (Plateau.TAILLE_CASE + Plateau.ESPACE_MUR));
            
            // Sécurité : clic dans la grille
            if (colClic >= 0 && colClic < Plateau.NB_CASES && ligClic >= 0 && ligClic < Plateau.NB_CASES) {
                
                // 1. On demande au Moteur si on a le droit
                if (modele.estDeplacementValide(ligClic, colClic)) {
                    System.out.println("Déplacement VALIDE par le contrôleur !");
                    
                    // 2. On met à jour la mémoire du Moteur
                    modele.majPositionBlanc(ligClic, colClic);
                    
                    // 3. On ordonne à la Vue de se redessiner
                    vue.deplacerPionBlancVisuel(ligClic, colClic);
                    
                } else {
                    System.out.println("Mouvement INTERDIT !");
                }
            }
        });
    }
}
