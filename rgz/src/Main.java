import javax.swing.SwingUtilities;


public class Main {  // Главный класс должен быть объявлен
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BlackjackGame game = new BlackjackGame();
            game.setVisible(true);
        });
    }
}