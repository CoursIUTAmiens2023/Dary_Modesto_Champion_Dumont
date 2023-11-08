package com.example.pong;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;

public class PongGame extends Application {
    private static final int WIDTH = 8001;
    private static final int HEIGHT = 600;
    private static final int PADDLE_HEIGHT = 100;
    private static final int PADDLE_WIDTH = 10;
    private static final int BALL_RADIUS = 10;

    private double ballSpeedX = 1;
    private double ballSpeedY = 1;
    private double ballX = WIDTH / 2;
    private double ballY = HEIGHT / 2;

    private double leftPaddleY = HEIGHT / 2 - PADDLE_HEIGHT / 2;
    private double rightPaddleY = HEIGHT / 2 - PADDLE_HEIGHT / 2;

    private boolean isUpKeyPressed1 = false;
    private boolean isDownKeyPressed1 = false;
    private boolean isUpKeyPressed2 = false;
    private boolean isDownKeyPressed2 = false;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Les contrôles des raquettes (2 Joueurs)
        Scene scene = new Scene(new Pane(canvas), WIDTH, HEIGHT);
        scene.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            if (code == KeyCode.A) {
                isUpKeyPressed1 = true;
            } else if (code == KeyCode.Z) {
                isDownKeyPressed1 = true;
            } else if (code == KeyCode.K) {
                isUpKeyPressed2 = true;
            } else if (code == KeyCode.M) {
                isDownKeyPressed2 = true;
            }
        });

        scene.setOnKeyReleased(e -> {
            KeyCode code = e.getCode();
            if (code == KeyCode.A) {
                isUpKeyPressed1 = false;
            } else if (code == KeyCode.Z) {
                isDownKeyPressed1 = false;
            } else if (code == KeyCode.K) {
                isUpKeyPressed2 = false;
            } else if (code == KeyCode.M) {
                isDownKeyPressed2 = false;
            }
        });

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10), e -> {
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
                ballSpeedX *= -1;
            }

            if (ballX >= WIDTH - PADDLE_WIDTH - BALL_RADIUS && ballY + BALL_RADIUS >= rightPaddleY && ballY <= rightPaddleY + PADDLE_HEIGHT) {
                ballSpeedX *= -1;
            }

            // Dessiner la balle
            gc.fillOval(ballX, ballY, BALL_RADIUS, BALL_RADIUS);

            // Mouvement des raquettes
            if (isUpKeyPressed1 && leftPaddleY > 0) {
                leftPaddleY -= 2;
            } else if (isDownKeyPressed1 && leftPaddleY < HEIGHT - PADDLE_HEIGHT) {
                leftPaddleY += 2;
            }

            if (isUpKeyPressed2 && rightPaddleY > 0) {
                rightPaddleY -= 2;
            } else if (isDownKeyPressed2 && rightPaddleY < HEIGHT - PADDLE_HEIGHT) {
                rightPaddleY += 2;
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        stage.setScene(scene);
        stage.setTitle("Pong Game");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}