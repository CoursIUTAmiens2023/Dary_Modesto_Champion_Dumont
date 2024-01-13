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
    private static final double m_WIDTH = 850;
    private static final double m_HEIGHT = 650;
    private static final double m_PADDLE_HEIGHT = 100;
    private static final double m_PADDLE_WIDTH = 10;
    private static final double m_BALL_RADIUS = 10;

    /* Paramètres de la balle */
    private double m_ballSpeedX = 1.5;
    private double m_ballSpeedY = 1.5;

    private double m_speedPaddle = 4;
    private double m_ballX = m_WIDTH / 2;
    private double m_ballY = m_HEIGHT / 2;

    /* Paramètres des raquettes */
    private double m_leftPaddleY = m_HEIGHT / 2 - m_PADDLE_HEIGHT / 2;
    private double m_rightPaddleY = m_HEIGHT / 2 - m_PADDLE_HEIGHT / 2;

    /* Paramètres d'action des joueurs */
    private boolean m_isUpKeyPressed1 = false;
    private boolean m_isDownKeyPressed1 = false;
    private boolean m_isUpKeyPressed2 = false;
    private boolean m_isDownKeyPressed2 = false;

    /* Paramètres de score */
    private int m_player1Score = 0;
    private int m_player2Score = 0;

    private Timeline m_timeline;

    /* Paramètres de style */
    private final String m_buttonStyle = "-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 10px; -fx-border-radius: 0px; -fx-background-radius: 0px; -fx-font-family: 'Monospace'; -fx-font-size: 50; -fx-font-weight: bold;";
    private final String m_buttonHoveredStyle = "-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white; -fx-border-width: 10px; -fx-border-radius: 0px; -fx-background-radius: 0px; -fx-font-family: 'Monospace'; -fx-font-size: 50; -fx-font-weight: bold;";

    /**
     * Générateur de bouton avec son message qui indique l'action du bouton
     * @param p_text texte afficher dans le bouton
     * @return button dans les menus
     */
    public Button stylisedButton(String p_text) {
        Button v_button = new Button(p_text);
        v_button.setStyle(m_buttonStyle);
        v_button.setOnMouseEntered(e -> v_button.setStyle(m_buttonHoveredStyle));
        v_button.setOnMouseExited(e -> v_button.setStyle(m_buttonStyle));
        return v_button;
    }

    /**
     * Fonction de jeu
     * @param p_stage Le stage du jeu
     */
    @Override
    public void start(Stage p_stage) {
       p_stage.setTitle("Pong");

       // Texte pong
       Text v_pongText = new Text("Pong");
       v_pongText.setFont(Font.font("Monospace", FontWeight.BOLD, 200));
       v_pongText.setFill(Color.WHITE);

       // Bouton start (lance la fonction game())
       Button v_startButton = stylisedButton("Jouer");
       v_startButton.setOnAction(e -> {resetGame(); playGame(p_stage);});

       // Bouton quitter (litterally arrête le jeu)
       Button v_leaveButton = stylisedButton("Quitter");
       v_leaveButton.setOnAction(e -> Platform.exit());

       // Empilement des trois bazars au-dessus dans un layout vertical
       FlowPane v_layout = new FlowPane(Orientation.VERTICAL);
       v_layout.setAlignment(Pos.CENTER);
       v_layout.getChildren().addAll(v_pongText, v_startButton, v_leaveButton);
       v_layout.setHgap(15);
       v_layout.setVgap(15);
       v_layout.setColumnHalignment(HPos.CENTER);

       // Création d'un fond noir
       BackgroundFill v_backgroundFill = new BackgroundFill(Color.BLACK, null, null);
       Background v_background = new Background(v_backgroundFill);
       v_layout.setBackground(v_background);

       // Affichage
       Scene v_scene = new Scene(v_layout, m_WIDTH, m_HEIGHT);
       try {
           p_stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/iconPong.png"))));
       } catch (Exception e) {
           System.out.println("Icon missing\n" + e);
       }
       p_stage.setScene(v_scene);
       p_stage.setWidth(m_WIDTH);
       p_stage.setHeight(m_HEIGHT);
       p_stage.show();
    }

    /**
     * Remet les paramètres de jeu à 0.
     */
    private void resetGame() {
        // Réinitialisez toutes les variables de jeu ici
        m_ballSpeedX = 1.5;
        m_ballSpeedY = 1.5;
        m_ballX = m_WIDTH / 2;
        m_ballY = m_HEIGHT / 2;

        m_leftPaddleY = m_HEIGHT / 2 - m_PADDLE_HEIGHT / 2;
        m_rightPaddleY = m_HEIGHT / 2 - m_PADDLE_HEIGHT / 2;
        m_speedPaddle = 4;

        m_isUpKeyPressed1 = false;
        m_isDownKeyPressed1 = false;
        m_isUpKeyPressed2 = false;
        m_isDownKeyPressed2 = false;

        m_player1Score = 0;
        m_player2Score = 0;
    }

    /**
     * Programme gérant le jeu Pong avec les raquettes, la balle, le timer,
     * les points marqués, la gestion des collisions, etc ...
     * @param p_stage paramètre JavaFx qui affiche les contenus d'une fenêtre
     */
    public void playGame(Stage p_stage) {
        Canvas v_canvas = new Canvas(m_WIDTH, m_HEIGHT);
        GraphicsContext v_gc = v_canvas.getGraphicsContext2D();

        // Les contrôles des raquettes (2 Joueurs)
        StackPane v_layout = new StackPane(v_canvas);
        Scene v_scene = getScene(v_layout);

        // Création d'un fond noir
        BackgroundFill v_backgroundFill = new BackgroundFill(Color.BLACK, null, null);
        Background v_background = new Background(v_backgroundFill);
        v_layout.setBackground(v_background);

        // Creation Score
        Text v_scoreText = new Text("0   0");
        v_scoreText.setFont(Font.font("Monospace", FontWeight.BOLD, 70));
        v_scoreText.setFill(Color.WHITE);

        // Affichage du score par rapport à la taille de fenêtre
        DoubleBinding v_scoreTextYBinding = Bindings.when(p_stage.fullScreenProperty()).then(m_HEIGHT / -5.0).otherwise(m_HEIGHT / -3.0);
        v_scoreText.translateYProperty().bind(v_scoreTextYBinding);

        // Création de la ligne pointillée au centre
        Line v_centerLine = new Line();
        v_centerLine.setStartX(m_WIDTH / 2);
        v_centerLine.setStartY(0);
        v_centerLine.setEndX(m_WIDTH / 2);
        v_centerLine.setEndY(m_HEIGHT);
        v_centerLine.setStroke(Color.WHITE);
        v_centerLine.setStrokeWidth(2);
        v_centerLine.getStrokeDashArray().addAll(10.0, 5.0);

        v_layout.getChildren().addAll(v_scoreText, v_centerLine);

        // Gestion du gameplay
         m_timeline = new Timeline(new KeyFrame(Duration.millis(10), e -> {
            // Mettre à vide l'écran
            v_gc.setFill(Color.BLACK);
            v_gc.fillRect(0, 0, m_WIDTH, m_HEIGHT);

            // Dessiner un contour blanc autour du rectangle noir
            v_gc.setStroke(Color.WHITE);
            v_gc.strokeRect(0, 0, m_WIDTH, m_HEIGHT);

            // Dessiner les raquettes
            v_gc.setFill(Color.WHITE);
            v_gc.fillRect(0, m_leftPaddleY, m_PADDLE_WIDTH, m_PADDLE_HEIGHT);
            v_gc.fillRect(m_WIDTH - m_PADDLE_WIDTH, m_rightPaddleY, m_PADDLE_WIDTH, m_PADDLE_HEIGHT);

            // Mettre à jour la position de la balle
            m_ballX += m_ballSpeedX;
            m_ballY += m_ballSpeedY;

            // Vérification des collisions
            if (m_ballY <= 0 || m_ballY >= m_HEIGHT - m_BALL_RADIUS) {
                m_ballSpeedY *= -1.1;
            }

            if (m_ballX <= m_PADDLE_WIDTH && m_ballY + m_BALL_RADIUS >= m_leftPaddleY && m_ballY <= m_leftPaddleY + m_PADDLE_HEIGHT) {
                m_ballSpeedX *= -1.25;
                m_speedPaddle *= 1.15;
            }

            if (m_ballX >= m_WIDTH - m_PADDLE_WIDTH - m_BALL_RADIUS && m_ballY + m_BALL_RADIUS >= m_rightPaddleY && m_ballY <= m_rightPaddleY + m_PADDLE_HEIGHT) {
                m_ballSpeedX *= -1.25;
                m_speedPaddle *= 1.15;
            }

            // Vérification du marquage d'un point par un joueur
            if (m_ballX <= 0) {
                m_player2Score++;
                v_scoreText.setText(m_player1Score + "   " + m_player2Score);

                // Relancer la balle au centre
                m_speedPaddle = 4;
                m_ballX = m_WIDTH / 2;
                m_ballY = m_HEIGHT / 2;
                if(m_ballSpeedX >0)
                    m_ballSpeedX = -1.5;
                else
                    m_ballSpeedX = 1.5;
                if(m_ballSpeedY >0)
                    m_ballSpeedY = -1.5;
                else
                    m_ballSpeedY = 1.5;

            } else if (m_ballX >= m_WIDTH - m_BALL_RADIUS) {
                m_player1Score++;
                v_scoreText.setText(m_player1Score + "   " + m_player2Score);

                // Relancer la balle au centre
                m_speedPaddle = 4;
                m_ballX = m_WIDTH / 2;
                m_ballY = m_HEIGHT / 2;
                m_ballSpeedX = -1.5;
                m_ballSpeedY = -1.5;
            }

            // Dessiner la balle
            v_gc.fillOval(m_ballX, m_ballY, m_BALL_RADIUS, m_BALL_RADIUS);

            // Mouvement des raquettes
            if (m_isUpKeyPressed1 && m_leftPaddleY > 0) {
                m_leftPaddleY -= m_speedPaddle;
            } else if (m_isDownKeyPressed1 && m_leftPaddleY < m_HEIGHT - m_PADDLE_HEIGHT) {
                m_leftPaddleY += m_speedPaddle;
            }

            if (m_isUpKeyPressed2 && m_rightPaddleY > 0) {
                m_rightPaddleY -= m_speedPaddle;
            } else if (m_isDownKeyPressed2 && m_rightPaddleY < m_HEIGHT - m_PADDLE_HEIGHT) {
                m_rightPaddleY += m_speedPaddle;
            }

            // Condition de fin de partie
            if (m_player1Score == 5 || m_player2Score == 5) {
                // Stopper le jeu
                m_timeline.stop();

                p_stage.setWidth(m_WIDTH);
                p_stage.setHeight(m_HEIGHT);

                // Afficher la page de victoire
                displayWinPage(m_player1Score == 5 ? "Joueur 1" : "Joueur 2", p_stage);
            }
        }));

        m_timeline.setCycleCount(Timeline.INDEFINITE);
        m_timeline.play();

        // Paramètre de la fenêtre
        p_stage.setScene(v_scene);
        p_stage.setWidth(m_WIDTH + 50);
        p_stage.setHeight(m_HEIGHT + 50);

        p_stage.show();
    }

    private Scene getScene(StackPane p_layout) {
        Scene v_scene = new Scene(p_layout, m_WIDTH, m_HEIGHT);

        v_scene.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            if (code == KeyCode.Z) {
                m_isUpKeyPressed1 = true;
            } else if (code == KeyCode.S) {
                m_isDownKeyPressed1 = true;
            } else if (code == KeyCode.UP) {
                m_isUpKeyPressed2 = true;
            } else if (code == KeyCode.DOWN) {
                m_isDownKeyPressed2 = true;
            }
        });

        v_scene.setOnKeyReleased(e -> {
            KeyCode code = e.getCode();
            if (code == KeyCode.Z) {
                m_isUpKeyPressed1 = false;
            } else if (code == KeyCode.S) {
                m_isDownKeyPressed1 = false;
            } else if (code == KeyCode.UP) {
                m_isUpKeyPressed2 = false;
            } else if (code == KeyCode.DOWN) {
                m_isDownKeyPressed2 = false;
            }
        });
        return v_scene;
    }

    /**
     * Processus qui affiche le gagnant de la partie.
     * @param p_winner le nom du gagnant, celui qui atteind 5 points en premier
     * @param p_stage paramètre JavaFX pour afficher les contenus d'une fenêtre
     */
    private void displayWinPage(String p_winner, Stage p_stage) {
        double v_width = m_WIDTH + 50;
        double v_height = m_HEIGHT + 50;

        // Nettoyage de la fenêtre de jeu
        Pane v_root = (Pane) p_stage.getScene().getRoot();
        v_root.getChildren().clear();

        // Contenus de la fenêtre de Victoire
        Text v_winText = new Text(p_winner + " a gagné!");
        v_winText.setFont(Font.font("Monospace", FontWeight.BOLD, 75));
        v_winText.setFill(Color.WHITE); // Texte en blanc

        Button v_closeButton = stylisedButton("Retourner à l'accueil");
        v_closeButton.setOnAction(event -> start(p_stage));

        // Création de la scène
        VBox v_vbox = new VBox(20);
        v_vbox.setAlignment(Pos.CENTER);
        v_vbox.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        // Affiche le bouton et le texte
        v_vbox.getChildren().addAll(v_winText, v_closeButton);

        // Gérer la taille
        v_vbox.setMinSize(v_width, v_height);
        v_vbox.setMaxSize(v_width, v_height);

        // Conditionne une fenêtre à la bonne taille
        v_root.getChildren().add(v_vbox);

        // Afficher la fenêtre avec son contenu
        p_stage.setWidth(v_width - 50);
        p_stage.setHeight(v_height - 50);
        p_stage.centerOnScreen();
    }

    /**
     * Lancement du programme Pong.
     * @param args paramètre par défaut du Main
     */
    public static void main(String[] args) {
        launch();
    }
}
