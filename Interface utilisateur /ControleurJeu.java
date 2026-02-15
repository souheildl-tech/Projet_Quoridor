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
        initialiserReseau();
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
            System.err.println("ERREUR : Avez-vous lancé serveur.py ?");
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

            if (colClic >= 0 && colClic < Plateau.NB_CASES && ligClic >= 0 && ligClic < Plateau.NB_CASES) {
                
                // 1. Clic: Le joueur se déplace
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (modele.estDeplacementValide(ligClic, colClic)) {
                        modele.majPositionBlanc(ligClic, colClic);
                        vue.deplacerPionBlancVisuel(ligClic, colClic);
                        
                        envoyerAction("MOVE:" + ligClic + "," + colClic);
                    }
                }
                // 2. Clic: Le joueur pose un mur
                else if (event.getButton() == MouseButton.SECONDARY || event.getButton() == MouseButton.MIDDLE) {
                    if (ligClic >= Plateau.NB_CASES - 1 || colClic >= Plateau.NB_CASES - 1) return;
                    boolean horizontal = (event.getButton() == MouseButton.SECONDARY);
                    
                    if (modele.emplacementMurLibre(ligClic, colClic)) {
                        modele.ajouterMur(ligClic, colClic, horizontal);
                        vue.placerMurVisuel(ligClic, colClic, horizontal);
                        
                        String orientation = horizontal ? "H" : "V";
                        envoyerAction("WALL:" + ligClic + "," + colClic + "," + orientation);
                    }
                }
            }
        });
    }


    private void envoyerAction(String action) {
        modele.setTourIA(true);
        System.out.println(" Envoi à Python : " + action);

        new Thread(() -> {
            try {
                String reponseIA = clientPython.sendMessage(action);
                System.out.println("Reçu de Python : " + reponseIA);

                Platform.runLater(() -> {
                    traiterReponseIA(reponseIA);
                    
                    if (modele.verifierVictoire()) {
                        System.out.println(" FIN DE LA PARTIE !");
                    } else {
                        modele.setTourIA(false);
                        System.out.println(" À vous de jouer !");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> modele.setTourIA(false));
            }
        }).start();
    }

   
    private void traiterReponseIA(String commande) {
        if (commande == null || commande.isEmpty()) return;

        if (commande.startsWith("MOVE:")) {
            String[] parts = commande.split(":")[1].split(",");
            int l = Integer.parseInt(parts[0].trim());
            int c = Integer.parseInt(parts[1].trim());
            
            modele.majPositionNoir(l, c);
            vue.deplacerPionNoirVisuel(l, c);
        }

        else if (commande.startsWith("WALL:")) {
           
        }
    }
}
