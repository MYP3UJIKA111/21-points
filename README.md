<!-- markdownlint-disable MD022 MD012 MD032 MD031 MD047-->

# Blackjack Game

## Overview
This is a Java-based Blackjack (21) game implemented using Swing for the graphical user interface. The game features card animations, a betting system, and a user-friendly interface with a background image and card visuals. Players can hit, stand, start a new game, and place bets, while the dealer follows standard Blackjack rules.

## Features
- **Graphical Interface**: Displays cards with animations for dealing and revealing.
- **Betting System**: Players start with a balance of 1000 and can place bets before each round.
- **Game Logic**: Implements standard Blackjack rules, including:
  - Dealer must hit until reaching 17 or more.
  - Aces can count as 1 or 11.
  - Blackjack (21 with two cards) pays 3:2.
- **Card Animations**: Smooth card movement from the deck to the player's or dealer's hand.
- **Rules Display**: Option to view game rules at the start or during the game.
- **Game Over Handling**: Prompts to restart or exit when the balance reaches zero.

## Requirements
- **Java Development Kit (JDK)**: Version 8 or higher.
- **Card Images**: The game requires card images located in the `/cards/` directory within the resources folder. Each card image should be named as `<value><suit>.png` (e.g., `2H.png` for Two of Hearts) and a `card_back.png` for the card back.
- **Dependencies**: Standard Java libraries (javax.swing, java.awt).

## How to Run
1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   ```
2. **Ensure Resources**:
   - Place card images in the `src/main/resources/cards/` directory.
   - Ensure the background image is in `src/main/resources/cards/background.png`.
3. **Compile and Run**:
   - Compile the Java files:
     ```bash
     javac *.java
     ```
   - Run the main class:
     ```bash
     java Main
     ```
4. **Alternatively, Use an IDE**:
   - Import the project into an IDE like IntelliJ IDEA or Eclipse.
   - Ensure the resources folder is correctly set up.
   - Run the `Main` class.

## Project Structure
- **Main.java**: Entry point that initializes the game.
- **BlackjackGame.java**: Core game logic, UI setup, and game mechanics.
- **BackgroundPanel.java**: Custom JPanel for rendering the background image.
- **/cards/**: Directory containing card images and the background image.

## Game Rules
- **Objective**: Get a hand value as close to 21 as possible without going over.
- **Card Values**:
  - 2-10: Face value.
  - Jack, Queen, King: 10 points.
  - Ace: 1 or 11 points (automatically adjusted to avoid busting).
- **Gameplay**:
  - Player and dealer receive two cards each; one dealer card is hidden.
  - Player can "Hit" (take another card) or "Stand" (end turn).
  - Dealer must hit until their score is 17 or higher.
- **Winning**:
  - Standard win: 1:1 payout.
  - Blackjack (21 with two cards): 3:2 payout.
  - Tie: Bet returned.
  - Bust (over 21): Player loses the bet.

## Known Issues
- Ensure card images are present in the `/cards/` directory, or the game will throw a `RuntimeException`.
- The game assumes a screen resolution that fits an 800x600 window; resizing may affect card positioning.

## Future Improvements
- Add sound effects for card dealing and game events.
- Implement double-down and split options.
- Support customizable card decks or themes.
- Add multiplayer functionality.

## License
This project is licensed under the MIT License.