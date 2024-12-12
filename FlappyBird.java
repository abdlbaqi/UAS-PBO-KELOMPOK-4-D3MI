import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

// Abstract class untuk elemen permainan
abstract class GameElement {
    protected int x, y, width, height;
    protected Image img;

    public GameElement(int x, int y, int width, int height, Image img) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img = img;
    }

    public abstract void draw(Graphics g);

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}

// Kelas untuk burung
class Bird extends GameElement {
    private int velocityY;
    private static final int GRAVITY = 1;

    public Bird(int x, int y, int width, int height, Image img) {
        super(x, y, width, height, img);
        this.velocityY = 0;
    }

    public void move() {
        velocityY += GRAVITY;
        y += velocityY;
        y = Math.max(y, 0); // Batasi agar burung tidak keluar dari atas
    }

    public void fly() {
        velocityY = -9;
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(img, x, y, width, height, null);
    }
}

// Kelas untuk pipa
class Pipe extends GameElement {
    private boolean passed;

    public Pipe(int x, int y, int width, int height, Image img) {
        super(x, y, width, height, img);
        this.passed = false;
    }

    public void move(int velocityX) {
        x += velocityX;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(img, x, y, width, height, null);
    }
}

// Kelas utama permainan
public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    private static final int BOARD_WIDTH = 360;
    private static final int BOARD_HEIGHT = 640;
    private static final int PIPE_WIDTH = 64;
    private static final int PIPE_HEIGHT = 512;
    private static final int PIPE_OPENING = BOARD_HEIGHT / 4;

    private Bird bird;
    private ArrayList<Pipe> pipes;
    private Timer gameLoop;
    private Timer placePipeTimer;
    private boolean gameOver = false;
    private double score = 0;

    private Image backgroundImg, birdImg, topPipeImg, bottomPipeImg;

    public FlappyBird() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        // Load gambar dengan pengecekan exception
        try {
            backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
            birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
            topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
            bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
        }

        // Inisialisasi elemen permainan
        bird = new Bird(BOARD_WIDTH / 8, BOARD_HEIGHT / 2, 34, 24, birdImg);
        pipes = new ArrayList<>();

        // Timer untuk menambahkan pipa
        placePipeTimer = new Timer(1500, e -> placePipes());
        placePipeTimer.start();

        // Timer untuk game loop
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    private void placePipes() {
        int randomY = -PIPE_HEIGHT / 4 - (int) (Math.random() * (PIPE_HEIGHT / 2));
        Pipe topPipe = new Pipe(BOARD_WIDTH, randomY, PIPE_WIDTH, PIPE_HEIGHT, topPipeImg);
        Pipe bottomPipe = new Pipe(BOARD_WIDTH, randomY + PIPE_HEIGHT + PIPE_OPENING, PIPE_WIDTH, PIPE_HEIGHT, bottomPipeImg);
        pipes.add(topPipe);
        pipes.add(bottomPipe);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImg, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, null);

        bird.draw(g);
        for (Pipe pipe : pipes) {
            pipe.draw(g);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        g.drawString(gameOver ? "Game Over: " + (int) score : String.valueOf((int) score), 10, 35);
    }

    private void move() {
        bird.move();

        for (Pipe pipe : pipes) {
            pipe.move(-4);

            if (!pipe.isPassed() && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.setPassed(true);
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        pipes.removeIf(pipe -> pipe.x + pipe.width < 0);

        if (bird.y > BOARD_HEIGHT) {
            gameOver = true;
        }
    }

    private boolean collision(Bird bird, Pipe pipe) {
        return bird.getBounds().intersects(pipe.getBounds());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
            repaint();
        } else {
            gameLoop.stop();
            placePipeTimer.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            bird.fly();

            if (gameOver) {
                restartGame();
            }
        }
    }

    private void restartGame() {
        bird = new Bird(BOARD_WIDTH / 8, BOARD_HEIGHT / 2, 34, 24, birdImg);
        pipes.clear();
        score = 0;
        gameOver = false;
        gameLoop.start();
        placePipeTimer.start();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBird gamePanel = new FlappyBird();

        frame.add(gamePanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
