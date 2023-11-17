package com.example.pong;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.animation.AnimationTimer;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Classe principale qui permet de lancer le jeu avec les fenêtres
 * d'accueil au lancement du programme,
 * du jeu Pong en lui-même et de victoire.
 */
public class PongGame extends Application {
    /* Paramètres des différentes tailles */
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PADDLE_HEIGHT = 100;
    private static final int PADDLE_WIDTH = 10;
    private static final int BALL_RADIUS = 10;

    /* Paramètres de la balle */
    private double ballSpeedX = 1.5;
    private double ballSpeedY = 1.5;
    private double ballX = WIDTH / 2;
    private double ballY = HEIGHT / 2;

    /* Paramètres des raquettes */
    private double leftPaddleY = HEIGHT / 2 - PADDLE_HEIGHT / 2;
    private double rightPaddleY = HEIGHT / 2 - PADDLE_HEIGHT / 2;

    /* Paramètres d'action des joueurs */
    private boolean isUpKeyPressed1 = false;
    private boolean isDownKeyPressed1 = false;
    private boolean isUpKeyPressed2 = false;
    private boolean isDownKeyPressed2 = false;

    /* Paramètres de score */
    private int player1Score = 0;
    private int player2Score = 0;

    private Timeline timeline;

    /**
     * Programme gérant le jeu Pong avec les raquettes, la balle, le timer,
     * les points marqués, la gestion des collisions, etc ...
     * @param stage paramètre JavaFx qui affiche les contenus d'une fenêtre
     */
    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Les contrôles des raquettes (2 Joueurs)
        Scene scene = new Scene(new Pane(canvas), WIDTH, HEIGHT);
        scene.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            if (code == KeyCode.Z) {
                isUpKeyPressed1 = true;
            } else if (code == KeyCode.S) {
                isDownKeyPressed1 = true;
            } else if (code == KeyCode.UP) {
                isUpKeyPressed2 = true;
            } else if (code == KeyCode.DOWN) {
                isDownKeyPressed2 = true;
            }
        });

        scene.setOnKeyReleased(e -> {
            KeyCode code = e.getCode();
            if (code == KeyCode.Z) {
                isUpKeyPressed1 = false;
            } else if (code == KeyCode.S) {
                isDownKeyPressed1 = false;
            } else if (code == KeyCode.UP) {
                isUpKeyPressed2 = false;
            } else if (code == KeyCode.DOWN) {
                isDownKeyPressed2 = false;
            }
        });

        // Creation Score
        Text scoreText = new Text("0   0");
        scoreText.setFont(Font.font("Monospace", FontWeight.BOLD, 70));
        scoreText.setFill(Color.WHITE);
        double sceneWidth = scene.getWidth();
        double textWidth = scoreText.getBoundsInLocal().getWidth();
        scoreText.setX((sceneWidth - textWidth) / 2);
        scoreText.setY(HEIGHT /8);

        // Création de la ligne pointillée au centre
        Line centerLine = new Line();
        centerLine.setStartX(WIDTH / 2);
        centerLine.setStartY(0);
        centerLine.setEndX(WIDTH / 2);
        centerLine.setEndY(HEIGHT);
        centerLine.setStroke(Color.WHITE);
        centerLine.setStrokeWidth(2);
        centerLine.getStrokeDashArray().addAll(10.0, 5.0);

        ((Pane) scene.getRoot()).getChildren().addAll(scoreText, centerLine);

        // Gestion du gameplay
         timeline = new Timeline(new KeyFrame(Duration.millis(10), e -> {
            // Mettre à vide l'écran
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, WIDTH, HEIGHT);

            // Dessiner les raquettes
            gc.setFill(Color.WHITE);
            gc.fillRect(0, leftPaddleY, PADDLE_WIDTH, PADDLE_HEIGHT);
            gc.fillRect(WIDTH - PADDLE_WIDTH, rightPaddleY, PADDLE_WIDTH, PADDLE_HEIGHT);

            // Mettre à jour la position de la balle
            ballX += ballSpeedX;
            ballY += ballSpeedY;

            // Vérification des collisions
            if (ballY <= 0 || ballY >= HEIGHT - BALL_RADIUS) {
                ballSpeedY *= -1;
            }

            if (ballX <= PADDLE_WIDTH && ballY + BALL_RADIUS >= leftPaddleY && ballY <= leftPaddleY + PADDLE_HEIGHT) {
                ballSpeedX *= -1.25;
            }

            if (ballX >= WIDTH - PADDLE_WIDTH - BALL_RADIUS && ballY + BALL_RADIUS >= rightPaddleY && ballY <= rightPaddleY + PADDLE_HEIGHT) {
                ballSpeedX *= -1.25;
            }

            // Vérification du marquage d'un point par un joueur
            if (ballX <= 0) {
                player2Score++;
                scoreText.setText(player1Score + "   " + player2Score);

                // Relancer la balle au centre
                ballX = WIDTH / 2;
                ballY = HEIGHT / 2;
                if(ballSpeedX>0)
                    ballSpeedX = -1.5;
                else
                    ballSpeedX= 1.5;
                if(ballSpeedY>0)
                    ballSpeedY = -1.5;
                else
                    ballSpeedY= 1.5;

            } else if (ballX >= WIDTH - BALL_RADIUS) {
                player1Score++;
                scoreText.setText(player1Score + "   " + player2Score);

                // Relancer la balle au centre
                ballX = WIDTH / 2;
                ballY = HEIGHT / 2;
                ballSpeedX = -1.5;
                ballSpeedY = -1.5;
            }

            // Dessiner la balle
            gc.fillOval(ballX, ballY, BALL_RADIUS, BALL_RADIUS);

            // Mouvement des raquettes
            if (isUpKeyPressed1 && leftPaddleY > 0) {
                leftPaddleY -= 4;
            } else if (isDownKeyPressed1 && leftPaddleY < HEIGHT - PADDLE_HEIGHT) {
                leftPaddleY += 4;
            }

            if (isUpKeyPressed2 && rightPaddleY > 0) {
                rightPaddleY -= 4;
            } else if (isDownKeyPressed2 && rightPaddleY < HEIGHT - PADDLE_HEIGHT) {
                rightPaddleY += 4;
            }

            // Condition de fin de partie
            if (player1Score == 5 || player2Score == 5) {
                // Arrêter le jeu
                timeline.stop();

                // Afficher la page de victoire
                displayWinPage(player1Score == 5 ? "Joueur 1" : "Joueur 2", stage);
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Paramètre de la fenêtre
        stage.setScene(scene);
        stage.setTitle("Pong Game");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/iconPong.png")));
        stage.show();
    }

    /**
     * Processus qui affiche le gagnant de la partie.
     * @param winner le nom du gagnant, celui qui atteind 5 points en premier
     * @param stage paramètre JavaFX pour afficher les contenus d'une fenêtre
     */
    private void displayWinPage(String winner, Stage stage) {
        double width = WIDTH;
        double height = HEIGHT;

        // Nettoyage de la fenêtre de jeu
        Pane root = (Pane) stage.getScene().getRoot();
        root.getChildren().clear();

        // Contenus de la fenêtre de Victoire
        Text winText = new Text(winner + " a gagné!");
        winText.setFont(Font.font("Monospace", FontWeight.BOLD, 30));
        winText.setFill(Color.WHITE); // Texte en blanc
        Button closeButton = new Button("Fermer le jeu");
        closeButton.setOnAction(event -> Platform.exit());
        closeButton.setStyle("-fx-background-color: #808080; -fx-text-fill: white;");
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-background-color: #A9A9A9; -fx-text-fill: white;")); // Gris plus clair pour le survol
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-background-color: #808080; -fx-text-fill: white;"));

        // Création de la scène
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        // Affiche le bouton et le texte
        vbox.getChildren().addAll(winText, closeButton);

        // Gérer la taille
        vbox.setMinSize(width, height);
        vbox.setMaxSize(width, height);

        // Conditionne une fenêtre à la bonne taille
        root.getChildren().add(vbox);

        // Afficher la fenêtre avec son contenu
        stage.setWidth(width);
        stage.setHeight(height);
        stage.centerOnScreen();
    }

    /**
     * Lancement du programme Pong.
     * @param args paramètre par défaut du main
     */
    public static void main(String[] args) {
        launch();
    }
}
