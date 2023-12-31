# Doc technique

## Stack

- Java (Langage de programmation)
- JavaFX (Framework d'affichage (et pour le son))
- Junit5 (Framework de tests unitaires)

## Architecture du code

Tout le code est dans le meme fichier, le fichier continent ces fonctions

- Main() -- Appel la fonction launch() (interne a JavaFX)
- start() -- Démarrage du jeu avec creation de la fenetre, titrage de la fenetre, création de la scène...
- game() -- Code principal du jeu
- resetGame() -- Réinitialisation du jeu, (position des raquettes, vitesse de la balle etc.)
- getScene() -- Crée une scène, effectue le traitement par rapport aux touches et retourne la scène
- displayWinPage() -- Affiche la page si il y a un vainqueur
