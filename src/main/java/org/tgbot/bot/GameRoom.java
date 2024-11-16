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

    public GameStatus getStatus() {
        return status;
    }

    public void setPlayer2(User player2) {
        this.player2 = player2;
        this.status = GameStatus.IN_PROGRESS;
    }

    // Проверяет заполнена ли сама комната(room)
    public boolean isFull() {
        return player1 != null && player2 != null;
    }

    // Метод для получения текущего игрока
    public char getCurrentPlayer() {
        return game.getCurrentPlayer();
    }

    // Метод для выполнения хода игрока
    public boolean makeMove(int row, int col) {
        // Передаем ход в игру и проверяем его успешность
        if (game.makeMove(row, col)) {
            if (game.checkWin() || TicTacToeGame.isBoardFull()) {
                status = GameStatus.COMPLETED; // Завершаем игру, если победитель или ничья
            }
            return true; // Ход успешен
        }
        return false; // Ход не выполнен
    }

    // Метод для переключения хода
    // void switchPlayer() {
    //    game.switchPlayer();
    //}

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

    // Получение сообщения с текущим результатом игры
    // Обновленный метод getGameStatusMessage
    public String getGameStatusMessage() {
        if (game.isFinished()) {
            if (game.checkWin()) {
                // Если есть победитель, возвращаем имя победителя
                if (game.getCurrentPlayer() == 'X') {
                    return "Поздравляем, " + player1.getFirstName() + ", вы победили!";
                } else {
                    return "Поздравляем, " + player2.getFirstName() + ", вы победили!";
                }
            } else {
                return "Игра закончена. Ничья!";
            }
        }

        // Если игра не закончена, выводим имя следующего игрока, который ходит
        if (game.getCurrentPlayer() == 'X') {
            return "Ход следующего игрока: X - " + player1.getFirstName();
        } else {
            return "Ход следующего игрока: O - " + player2.getFirstName();
        }
    }


}
