package com.quoridor;

public class Moteur {


    public Moteur() {

    }

    public boolean estDeplacementValide(int departLigne, int departCol, int arriveeLigne, int arriveeCol) {
        // Calcul de la différence 
        int diffLigne = Math.abs(departLigne - arriveeLigne);
        int diffCol = Math.abs(departCol - arriveeCol);
        return (diffLigne + diffCol) == 1;
    }
}
