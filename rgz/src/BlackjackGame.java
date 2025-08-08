import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class BlackjackGame extends JFrame {
    private ArrayList<Card> deck;
    private ArrayList<Card> playerHand;
    private ArrayList<Card> dealerHand;
    private JLabel playerScoreLabel;
    private JLabel dealerScoreLabel;
    private JLabel resultLabel;
    private JLabel balanceLabel;
    private JTextField betField;
    private JButton hitButton;
    private JButton standButton;
    private JButton newGameButton;
    private JButton sitButton;
    private JPanel gamePanel;
    private JPanel startPanel;
    private int playerScore;
    private int dealerScore;
    private int balance;
    private int currentBet;
    private Timer animationTimer;
    private ArrayList<JLabel> cardLabels;
    private int animationStep;
    private Card animatingCard;
    private boolean isPlayerCard;
    private Point deckPosition;
    private Point targetPosition;
    private int cardsToDeal;
    private boolean dealingInitialCards;
    private HashMap<String, ImageIcon> cardImages;
    private final AnimationQueue animationQueue = new AnimationQueue();
    private boolean gameEnded = false;

    private static class Card {
        String suit;
        String value;
        int points;
        JLabel label;
        String imageName;
        ImageIcon cardImage;
        ImageIcon cardBack;
        Point position;

        Card(String suit, String value, int points, ImageIcon cardImage, HashMap<String, ImageIcon> cardImages) {
            this.suit = suit;
            this.value = value;
            this.points = points;
            this.imageName = value + suit + ".png";
            this.cardImage = cardImage;
            this.cardBack = cardImages.get("/cards/card_back.png");
            this.position = new Point(0, 0);

            this.label = new JLabel(this.cardBack);
            this.label.setPreferredSize(new Dimension(cardImage.getIconWidth(), cardImage.getIconHeight()));
            this.label.setBorder(BorderFactory.createEmptyBorder());
        }

        public void reveal() {
            label.setIcon(cardImage);
        }

        public void setPosition(int x, int y) {
            position.setLocation(x, y);
            label.setBounds(x, y, label.getWidth(), label.getHeight());
        }
    }

    private static class AnimationQueue {
        private final Queue<Runnable> animations = new LinkedList<>();
        private boolean isAnimating = false;

        void addAnimation(Runnable animation) {
            animations.offer(animation);
            if (!isAnimating) {
                runNext();
            }
        }

        private void runNext() {
            Runnable next = animations.poll();
            if (next != null) {
                isAnimating = true;
                next.run();
            } else {
                isAnimating = false;
            }
        }

        void completeAnimation() {
            isAnimating = false;
            runNext();
        }
    }

    public BlackjackGame() {
        setTitle("Блэкджек (21 очко)");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        balance = 1000;
        deckPosition = new Point(50, 50);
        cardImages = new HashMap<>();
        cardLabels = new ArrayList<>();
        deck = new ArrayList<>();
        playerHand = new ArrayList<>();
        dealerHand = new ArrayList<>();

        preloadCardImages();

        playerScoreLabel = new JLabel("Ваша рука: 0");
        dealerScoreLabel = new JLabel("Рука дилера: ?");
        resultLabel = new JLabel("Добро пожаловать в Блэкджек!");
        balanceLabel = new JLabel("Баланс: " + balance);
        betField = new JTextField("100", 5);
        hitButton = new JButton("Взять карту");
        standButton = new JButton("Хватит");
        newGameButton = new JButton("Новая игра");
        sitButton = new JButton("Сесть за стол");
        JButton rulesButton = new JButton("Правила");

        initializeStartScreen(rulesButton);

        gamePanel = new BackgroundPanel("/cards/background.png");
        gamePanel.setLayout(null);
        gamePanel.setDoubleBuffered(true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(hitButton);
        buttonPanel.add(standButton);
        buttonPanel.add(newGameButton);
        buttonPanel.add(rulesButton);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new GridLayout(4, 1));
        textPanel.add(dealerScoreLabel);
        textPanel.add(playerScoreLabel);
        textPanel.add(balanceLabel);
        textPanel.add(resultLabel);

        add(startPanel, BorderLayout.CENTER);

        sitButton.addActionListener(e -> {
            remove(startPanel);
            add(gamePanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
            add(textPanel, BorderLayout.NORTH);
            revalidate();
            repaint();
            promptForBet();
        });

        hitButton.addActionListener(e -> {
            disableButtons();
            Card card = drawCard();
            playerHand.add(card);
            animateCard(card, true, false, () -> {});
        });

        standButton.addActionListener(e -> {
            disableButtons();
            playDealer();
        });

        newGameButton.addActionListener(e -> promptForBet());

        rulesButton.addActionListener(e -> showGameRules());

        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        newGameButton.setEnabled(false);

        setVisible(true);

        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Хотите посмотреть правила игры перед началом?",
                    "Правила Блэкджека",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                showGameRules();
            }
        });
    }

    private void preloadCardImages() {
        new Thread(() -> {
            String[] suits = {"H", "D", "C", "S"};
            String[] values = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
            for (String suit : suits) {
                for (String value : values) {
                    String path = "/cards/" + value + suit + ".png";
                    loadAndScaleCardImage(path);
                }
            }
            String backPath = "/cards/card_back.png";
            if (!cardImages.containsKey(backPath)) {
                ImageIcon originalBack = new ImageIcon(getClass().getResource(backPath));
                if (originalBack.getImage() == null) {
                    throw new RuntimeException("Не удалось загрузить изображение рубашки карты: " + backPath);
                }
                Image scaledBack = originalBack.getImage().getScaledInstance(90, 120, Image.SCALE_SMOOTH);
                cardImages.put(backPath, new ImageIcon(scaledBack));
            }
        }).start();
    }

    private ImageIcon loadAndScaleCardImage(String path) {
        if (cardImages.containsKey(path)) {
            return cardImages.get(path);
        }

        ImageIcon originalIcon = new ImageIcon(getClass().getResource(path));
        if (originalIcon.getImage() == null) {
            throw new RuntimeException("Не удалось загрузить изображение карты: " + path);
        }

        int width = 90;
        int height = 120;
        Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        cardImages.put(path, scaledIcon);
        return scaledIcon;
    }

    private void initializeStartScreen(JButton rulesButton) {
        startPanel = new JPanel(new BorderLayout(10, 10));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(sitButton);
        buttonPanel.add(rulesButton);
        startPanel.add(new JLabel("Добро пожаловать в Блэкджек!", SwingConstants.CENTER), BorderLayout.CENTER);
        startPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showGameRules() {
        String rulesText = "<html><div style='width:400px;'><h2>Правила Блэкджека</h2>"
                + "<p><b>Цель игры:</b> Набрать сумму очков, максимально близкую к 21, но не больше.</p>"
                + "<p><b>Ход игры:</b></p>"
                + "<ul>"
                + "<li>Игрок и дилер получают по 2 карты</li>"
                + "<li>Карты дилера: одна открыта, одна скрыта</li>"
                + "<li>Игрок может брать дополнительные карты (Hit) или остановиться (Stand)</li>"
                + "<li>Дилер обязан брать карты, пока не наберет 17 или более очков</li>"
                + "</ul>"
                + "<p><b>Значения карт:</b></p>"
                + "<table border='1' style='width:100%; border-collapse:collapse;'>"
                + "<tr><th>Карта</th><th>Очки</th></tr>"
                + "<tr><td>2-10</td><td>По номиналу</td></tr>"
                + "<tr><td>Валет (J)</td><td>10</td></tr>"
                + "<tr><td>Дама (Q)</td><td>10</td></tr>"
                + "<tr><td>Король (K)</td><td>10</td></tr>"
                + "<tr><td>Туз (A)</td><td>1 или 11</td></tr>"
                + "</table>"
                + "<p><b>Выигрыш:</b></p>"
                + "<ul>"
                + "<li>Обычная победа: выплата 1:1</li>"
                + "<li>Блэкджек (21 из 2 карт): выплата 3:2</li>"
                + "</ul></div></html>";

        JOptionPane.showMessageDialog(this, rulesText,
                "Правила игры", JOptionPane.INFORMATION_MESSAGE);
    }

    private void promptForBet() {
        disableButtons();
        String[] options = {"10", "50", "100", "500", "Другая сумма"};
        int choice = JOptionPane.showOptionDialog(this,
                "Выберите ставку:",
                "Ставка",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);

        try {
            if (choice == 4) {
                String input = JOptionPane.showInputDialog(this,
                        "Введите сумму ставки (1-" + balance + "):",
                        "100");
                if (input == null) return;
                currentBet = Integer.parseInt(input.trim());
            } else if (choice >= 0 && choice < 4) {
                currentBet = Integer.parseInt(options[choice]);
            } else {
                return;
            }

            if (currentBet <= 0 || currentBet > balance) {
                resultLabel.setText("Недопустимая ставка! Введите число от 1 до " + balance);
                promptForBet();
                return;
            }

            balance -= currentBet;
            updateScores();
            startNewGame();
        } catch (NumberFormatException e) {
            resultLabel.setText("Введите корректное число!");
            promptForBet();
        }
    }

    private void disableButtons() {
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        newGameButton.setEnabled(false);
    }

    private void endGameButtons() {
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        newGameButton.setEnabled(true);
    }

    private void createDeck() {
        deck.clear();
        String[] suits = {"H", "D", "C", "S"};
        String[] values = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        int[] points = {2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 11};

        for (String suit : suits) {
            for (int i = 0; i < values.length; i++) {
                String imageName = values[i] + suit + ".png";
                ImageIcon cardImage = cardImages.get("/cards/" + imageName);
                if (cardImage == null) {
                    cardImage = loadAndScaleCardImage("/cards/" + imageName);
                }
                deck.add(new Card(suit, values[i], points[i], cardImage, cardImages));
            }
        }
        Collections.shuffle(deck, new Random());
    }

    private List<Card> createNewDeck() {
        List<Card> newDeck = new ArrayList<>();
        String[] suits = {"H", "D", "C", "S"};
        String[] values = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        int[] points = {2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 11};

        for (String suit : suits) {
            for (int i = 0; i < values.length; i++) {
                String imageName = values[i] + suit + ".png";
                ImageIcon cardImage = cardImages.get("/cards/" + imageName);
                if (cardImage == null) {
                    cardImage = loadAndScaleCardImage("/cards/" + imageName);
                }
                newDeck.add(new Card(suit, values[i], points[i], cardImage, cardImages));
            }
        }
        return newDeck;
    }

    private void resetDeck() {
        deck.clear();
        deck.addAll(createNewDeck());
        Collections.shuffle(deck, new Random());
    }

    private Card drawCard() {
        if (deck.isEmpty()) {
            resetDeck();
        }
        return deck.remove(0);
    }

    private void updateScores() {
        playerScore = calculateScore(playerHand);
        dealerScore = calculateScore(dealerHand);
        playerScoreLabel.setText("Ваша рука: " + playerScore);

        // Показываем счет дилера только если игра закончена
        if (gameEnded) {
            dealerScoreLabel.setText("Рука дилера: " + dealerScore);
        } else {
            dealerScoreLabel.setText("Рука дилера: ?");
        }

        balanceLabel.setText("Баланс: " + balance);
    }

    private int calculateScore(ArrayList<Card> hand) {
        int score = 0;
        int aces = 0;

        for (Card card : hand) {
            if (card.value.equals("A")) {
                aces++;
                score += 11;
            } else {
                score += card.points;
            }
        }

        while (score > 21 && aces > 0) {
            score -= 10;
            aces--;
        }

        return score;
    }

    private void animateCard(Card card, boolean isPlayer, boolean isHidden, Runnable onComplete) {
        animationQueue.addAnimation(() -> {
            animationStep = 0;
            isPlayerCard = isPlayer;
            animatingCard = card;

            int cardWidth = card.cardImage.getIconWidth();
            int cardHeight = card.cardImage.getIconHeight();

            card.label.setBounds(deckPosition.x, deckPosition.y, cardWidth, cardHeight);
            gamePanel.add(card.label);
            cardLabels.add(card.label);
            gamePanel.setComponentZOrder(card.label, 0);

            card.label.setIcon(card.cardBack);

            targetPosition = isPlayer ?
                    new Point(100 + playerHand.size() * (cardWidth - 20), 400) :
                    new Point(100 + dealerHand.size() * (cardWidth - 20), 100);

            animationTimer = new Timer(16, e -> {
                animationStep++;
                double progress = Math.min(animationStep / 15.0, 1.0);
                double easedProgress = easeOutQuad(progress);

                int x = (int) (deckPosition.x + (targetPosition.x - deckPosition.x) * easedProgress);
                int y = (int) (deckPosition.y + (targetPosition.y - deckPosition.y) * easedProgress);
                card.label.setBounds(x, y, cardWidth, cardHeight);

                if (progress >= 0.9 && !isHidden) {
                    card.reveal();
                }

                if (progress >= 1.0) {
                    ((Timer)e.getSource()).stop();
                    handleAnimationComplete(isPlayer);
                    onComplete.run();
                    animationQueue.completeAnimation();
                }

                gamePanel.repaint();
            });
            animationTimer.start();
        });
    }

    private double easeOutQuad(double t) {
        return t * (2 - t);
    }

    private void handleAnimationComplete(boolean isPlayer) {
        updateScores();

        if (isPlayer && playerScore > 21) {
            resultLabel.setText("Перебор! Дилер победил!");
            endGame();
            checkGameOver();
        } else if (dealingInitialCards) {
            cardsToDeal--;
            if (cardsToDeal == 0) {
                dealingInitialCards = false;
                // Удалено автоматическое завершение игры при блэкджеке дилера
                enablePlayerTurn();
            }
        } else {
            enablePlayerTurn();
        }

        arrangeCards(isPlayer);
    }

    private void arrangeCards(boolean isPlayer) {
        int cardWidth = 90;
        int cardHeight = 120;
        int overlap = 20;

        ArrayList<Card> hand = isPlayer ? playerHand : dealerHand;
        int startX = 100;
        int y = isPlayer ? 400 : 100;

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            card.setPosition(startX + i * (cardWidth - overlap), y);
        }

        gamePanel.repaint();
    }

    private void enablePlayerTurn() {
        hitButton.setEnabled(true);
        standButton.setEnabled(true);
        newGameButton.setEnabled(true);
        resultLabel.setText("Ваш ход");
        gameEnded = false;
    }

    private void endGame() {
        gameEnded = true;
        endGameButtons();
        // Раскрываем все карты дилера
        for (Card card : dealerHand) {
            card.reveal();
        }
        updateScores();
    }

    private void startNewGame() {
        gameEnded = false;
        playerHand.clear();
        dealerHand.clear();
        for (JLabel label : cardLabels) {
            gamePanel.remove(label);
        }
        cardLabels.clear();
        gamePanel.repaint();

        createDeck();
        resultLabel.setText("Раздача карт...");
        dealingInitialCards = true;
        cardsToDeal = 4;

        dealInitialCards(0);
    }

    private void dealInitialCards(int index) {
        if (index >= 4) {
            dealingInitialCards = false;
            updateScores();
            enablePlayerTurn();
            return;
        }

        Card card = drawCard();
        boolean isPlayer = index % 2 == 0;
        boolean isHidden = !isPlayer && index == 3;
        (isPlayer ? playerHand : dealerHand).add(card);

        animateCard(card, isPlayer, isHidden, () -> {
            new Timer(500, e -> {
                ((Timer)e.getSource()).stop();
                dealInitialCards(index + 1);
            }).start();
        });
    }

    private void playDealer() {
        endGame(); // Завершаем игру, раскрываем карты дилера

        if (dealerScore < 17) {
            Card card = drawCard();
            dealerHand.add(card);
            animateCard(card, false, false, () -> {
                new Timer(500, e -> {
                    ((Timer)e.getSource()).stop();
                    playDealer();
                }).start();
            });
        } else {
            determineWinner();
            checkGameOver();
        }
    }

    private void determineWinner() {
        updateScores();
        if (playerScore > 21) {
            resultLabel.setText("Перебор! Дилер победил!");
        } else if (dealerScore > 21) {
            resultLabel.setText("Дилер перебрал! Вы победили!");
            balance += currentBet * 2;
        } else if (playerScore == 21 && playerHand.size() == 2 && !(dealerScore == 21 && dealerHand.size() == 2)) {
            resultLabel.setText("Блэкджек! Вы победили!");
            balance += (int)(currentBet * 2.5);
        } else if (playerScore > dealerScore) {
            resultLabel.setText("Вы победили! " + playerScore + " против " + dealerScore);
            balance += currentBet * 2;
        } else if (dealerScore > playerScore) {
            resultLabel.setText("Дилер победил! " + dealerScore + " против " + playerScore);
        } else {
            resultLabel.setText("Ничья! " + playerScore + " против " + playerScore);
            balance += currentBet;
        }
        balanceLabel.setText("Баланс: " + balance);
    }

    private void checkGameOver() {
        if (balance <= 0) {
            resultLabel.setText("Игра окончена! У вас закончились фишки.");
            newGameButton.setEnabled(false);
            int response = JOptionPane.showOptionDialog(
                    this,
                    "У вас закончились фишки! Начать новую игру?",
                    "Игра окончена",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Да", "Нет"},
                    "Да"
            );
            if (response == JOptionPane.YES_OPTION) {
                balance = 1000;
                balanceLabel.setText("Баланс: " + balance);
                startNewGame();
            } else {
                System.exit(0);
            }
        }
    }

}

