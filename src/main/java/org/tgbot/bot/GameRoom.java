package org.tgbot.bot;

import org.telegram.telegrambots.meta.api.objects.User;

public class GameRoom {
    private final String roomCode;
    private final Long ownerId;
    private final User player1;
    private User player2;
    private GameStatus status;
    private final TicTacToeGame game; // Добавляем игровое поле

    // Перечисление для статуса игры
    public enum GameStatus {
        WAITING_FOR_PLAYER,
        IN_PROGRESS,
        COMPLETED
    }

    // Конструктор, инициализирующий комнату с кодом и первым игроком
    public GameRoom(String roomCode, User player1) {
        this.roomCode = roomCode;
        this.player1 = player1;
        this.ownerId = player1.getId();
        this.status = GameStatus.WAITING_FOR_PLAYER;
        this.game = new TicTacToeGame(); // Инициализация игры
    }

    // Геттеры и сеттеры
    public String getRoomCode() {
        return roomCode;
    }

    public User getPlayer1() {
        return player1;
    }

    public User getPlayer2() {
        return player2;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setPlayer2(User player2) {
        this.player2 = player2;
        this.status = GameStatus.IN_PROGRESS; // Меняем статус на IN_PROGRESS, когда второй игрок присоединился
    }

    // Проверяет, заполнена ли комната
    public boolean isFull() {
        return player1 != null && player2 != null;
    }

    // Метод для получения текущего игрока
    public char getCurrentPlayer() {
        return game.getCurrentPlayer();
    }

    // Метод для выполнения хода игрока
    public boolean makeMove(int row, int col, User player) {
        if (status != GameStatus.IN_PROGRESS) {
            // Если комната еще не полная или игра не началась, не разрешаем делать ход
            return false;
        }

        // Убедимся, что ход делают именно тот игрок, чья очередь
        if (!isPlayerTurn(player)) {
            return false; // Игрок не может ходить, если это не его очередь
        }

        // Передаем ход в игру и проверяем его успешность
        if (game.makeMove(row, col)) {
            if (game.checkWin() || TicTacToeGame.isBoardFull()) {
                status = GameStatus.COMPLETED; // Завершаем игру, если победитель или ничья
            }
            return true; // Ход успешен
        }
        return false; // Ход не выполнен
    }

    // Метод для проверки, чей сейчас ход
    private boolean isPlayerTurn(User player) {
        if (game.getCurrentPlayer() == 'X') {
            return player.getId().equals(player1.getId());
        } else {
            return player.getId().equals(player2.getId());
        }
    }

    // Проверка победителя
    public boolean checkWin() {
        return game.checkWin();
    }

    // Проверка на ничью
    public boolean isDraw() {
        return game.isBoardFull() && !game.checkWin();
    }

    // Получение состояния игрового поля
    public String getBoardDisplay() {
        return game.getBoardDisplay();
    }

    // Проверка, завершена ли игра
    public boolean isGameFinished() {
        return game.isFinished(); // Используем метод игры для проверки завершённости
    }
}
