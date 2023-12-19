package Pong;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Objects;

/**
 * Classe principale qui permet de lancer le jeu avec les fenêtres
 * d'accueil au lancement du programme,
 * du jeu Pong en lui-même et de victoire.
 */
public class PongGame extends Application {
    /* Paramètres des différentes tailles */
    private static final double WIDTH = 850;
    private static final double HEIGHT = 650;
    private static final double PADDLE_HEIGHT = 100;
    private static final double PADDLE_WIDTH = 10;
    private static final double BALL_RADIUS = 10;

    /* Paramètres de la balle */
    private double ballSpeedX = 1.5;
    private double ballSpeedY = 1.5;

    private double speedPaddle = 4;
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

    /* Paramètres de style */
    private final String buttonStyle = "-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 10px; -fx-border-radius: 0px; -fx-background-radius: 0px; -fx-font-family: 'Monospace'; -fx-font-size: 50; -fx-font-weight: bold;";
    private final String buttonHoveredStyle = "-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white; -fx-border-width: 10px; -fx-border-radius: 0px; -fx-background-radius: 0px; -fx-font-family: 'Monospace'; -fx-font-size: 50; -fx-font-weight: bold;";

    public Button stylisedButton(String text) {
        Button button = new Button(text);
        button.setStyle(buttonStyle);
        button.setOnMouseEntered(e -> button.setStyle(buttonHoveredStyle));
        button.setOnMouseExited(e -> button.setStyle(buttonStyle));
        return button;
    }

    /**
     * Fonction de jeu
     * @param stage Le stage du jeu
     */
    @Override
    public void start(Stage stage) {
       stage.setTitle("Pong");

       // Texte pong
       Text pongText = new Text("Pong");
       pongText.setFont(Font.font("Monospace", FontWeight.BOLD, 200));
       pongText.setFill(Color.WHITE);

       // Bouton start (lance la fonction game())
       Button startButton = stylisedButton("Jouer");
       startButton.setOnAction(e -> {resetGame(); game(stage);});

       // Bouton quitter (litterally arrête le jeu)
       Button leaveButton = stylisedButton("Quitter");
       leaveButton.setOnAction(e -> Platform.exit());

       // Empilement des trois bazars au-dessus dans un layout vertical
       FlowPane layout = new FlowPane(Orientation.VERTICAL);
       layout.setAlignment(Pos.CENTER);
       layout.getChildren().addAll(pongText, startButton, leaveButton);
       layout.setHgap(15);
       layout.setVgap(15);
       layout.setColumnHalignment(HPos.CENTER);

       // Création d'un fond noir
       BackgroundFill backgroundFill = new BackgroundFill(Color.BLACK, null, null);
       Background background = new Background(backgroundFill);
       layout.setBackground(background);

       // Affichage
       Scene scene = new Scene(layout, WIDTH, HEIGHT);
       try {
           stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/iconPong.png"))));
       } catch (Exception e) {
           System.out.println("Icon missing\n" + e);
       }
       stage.setScene(scene);
       stage.setWidth(WIDTH);
       stage.setHeight(HEIGHT);
       stage.show();
    }

    /**
     * Remet les paramètres de jeu à 0.
     */
    private void resetGame() {
        // Réinitialisez toutes les variables de jeu ici
        ballSpeedX = 1.5;
        ballSpeedY = 1.5;
        ballX = WIDTH / 2;
        ballY = HEIGHT / 2;

        leftPaddleY = HEIGHT / 2 - PADDLE_HEIGHT / 2;
        rightPaddleY = HEIGHT / 2 - PADDLE_HEIGHT / 2;

        isUpKeyPressed1 = false;
        isDownKeyPressed1 = false;
        isUpKeyPressed2 = false;
        isDownKeyPressed2 = false;

        player1Score = 0;
        player2Score = 0;
    }

    /**
     * Programme gérant le jeu Pong avec les raquettes, la balle, le timer,
     * les points marqués, la gestion des collisions, etc ...
     * @param stage paramètre JavaFx qui affiche les contenus d'une fenêtre
     */
    public void game(Stage stage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Les contrôles des raquettes (2 Joueurs)
        StackPane layout = new StackPane(canvas);
        Scene scene = getScene(layout);

        // Création d'un fond noir
        BackgroundFill backgroundFill = new BackgroundFill(Color.BLACK, null, null);
        Background background = new Background(backgroundFill);
        layout.setBackground(background);

        // Creation Score
        Text scoreText = new Text("0   0");
        scoreText.setFont(Font.font("Monospace", FontWeight.BOLD, 70));
        scoreText.setFill(Color.WHITE);

        // Affichage du score par rapport à la taille de fenêtre
        DoubleBinding scoreTextYBinding = Bindings.when(stage.fullScreenProperty()).then(HEIGHT / -5.0).otherwise(HEIGHT / -3.0);
        scoreText.translateYProperty().bind(scoreTextYBinding);

        // Création de la ligne pointillée au centre
        Line centerLine = new Line();
        centerLine.setStartX(WIDTH / 2);
        centerLine.setStartY(0);
        centerLine.setEndX(WIDTH / 2);
        centerLine.setEndY(HEIGHT);
        centerLine.setStroke(Color.WHITE);
        centerLine.setStrokeWidth(2);
        centerLine.getStrokeDashArray().addAll(10.0, 5.0);

        layout.getChildren().addAll(scoreText, centerLine);

        // Gestion du gameplay
         timeline = new Timeline(new KeyFrame(Duration.millis(10), e -> {
            // Mettre à vide l'écran
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, WIDTH, HEIGHT);

            // Dessiner un contour blanc autour du rectangle noir
            gc.setStroke(Color.WHITE);
            gc.strokeRect(0, 0, WIDTH, HEIGHT);

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
                speedPaddle *= 1.15;
            }

            if (ballX >= WIDTH - PADDLE_WIDTH - BALL_RADIUS && ballY + BALL_RADIUS >= rightPaddleY && ballY <= rightPaddleY + PADDLE_HEIGHT) {
                ballSpeedX *= -1.25;
                speedPaddle *= 1.15;
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
                speedPaddle = 4;
                ballX = WIDTH / 2;
                ballY = HEIGHT / 2;
                ballSpeedX = -1.5;
                ballSpeedY = -1.5;
            }

            // Dessiner la balle
            gc.fillOval(ballX, ballY, BALL_RADIUS, BALL_RADIUS);

            // Mouvement des raquettes
            if (isUpKeyPressed1 && leftPaddleY > 0) {
                leftPaddleY -= speedPaddle;
            } else if (isDownKeyPressed1 && leftPaddleY < HEIGHT - PADDLE_HEIGHT) {
                leftPaddleY += speedPaddle;
            }

            if (isUpKeyPressed2 && rightPaddleY > 0) {
                rightPaddleY -= speedPaddle;
            } else if (isDownKeyPressed2 && rightPaddleY < HEIGHT - PADDLE_HEIGHT) {
                rightPaddleY += speedPaddle;
            }

            // Condition de fin de partie
            if (player1Score == 5 || player2Score == 5) {
                // Stopper le jeu
                timeline.stop();

                stage.setWidth(WIDTH);
                stage.setHeight(HEIGHT);

                // Afficher la page de victoire
                displayWinPage(player1Score == 5 ? "Joueur 1" : "Joueur 2", stage);
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Paramètre de la fenêtre
        stage.setScene(scene);
        stage.setWidth(WIDTH + 50);
        stage.setHeight(HEIGHT + 50);

        stage.show();
    }

    private Scene getScene(StackPane layout) {
        Scene scene = new Scene(layout, WIDTH, HEIGHT);

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
        return scene;
    }

    /**
     * Processus qui affiche le gagnant de la partie.
     * @param winner le nom du gagnant, celui qui atteind 5 points en premier
     * @param stage paramètre JavaFX pour afficher les contenus d'une fenêtre
     */
    private void displayWinPage(String winner, Stage stage) {
        double width = WIDTH + 50;
        double height = HEIGHT + 50;

        // Nettoyage de la fenêtre de jeu
        Pane root = (Pane) stage.getScene().getRoot();
        root.getChildren().clear();

        // Contenus de la fenêtre de Victoire
        Text winText = new Text(winner + " a gagné!");
        winText.setFont(Font.font("Monospace", FontWeight.BOLD, 75));
        winText.setFill(Color.WHITE); // Texte en blanc

        Button closeButton = stylisedButton("Retourner à l'accueil");
        closeButton.setOnAction(event -> start(stage));

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
        stage.setWidth(width - 50);
        stage.setHeight(height - 50);
        stage.centerOnScreen();
    }

    /**
     * Lancement du programme Pong.
     * @param args paramètre par défaut du Main
     */
    public static void main(String[] args) {
        launch();
    }
}
