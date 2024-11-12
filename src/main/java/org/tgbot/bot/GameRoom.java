package org.tgbot.bot;

import org.telegram.telegrambots.meta.api.objects.User;

public class GameRoom {
    private final String roomCode;
    private final Long ownerId; // Добавляем поле ownerId для хранения ID владельца
    private User player1;
    private User player2;
    private GameStatus status;

    // Перечисление для отслеживания статуса игры
    public enum GameStatus {
        WAITING_FOR_PLAYER,
        IN_PROGRESS,
        COMPLETED
    }

    // Конструктор, инициализирующий комнату с кодом и первым игроком
    public GameRoom(String roomCode, User player1) {
        this.roomCode = roomCode;
        this.player1 = player1;
        this.ownerId = player1.getId(); // Сохраняем ID владельца
        this.status = GameStatus.WAITING_FOR_PLAYER;
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
        return ownerId; // Возвращаем ID владельца комнаты
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setPlayer2(User player2) {
        this.player2 = player2;
        this.status = GameStatus.IN_PROGRESS; // Статус меняется при присоединении второго игрока
    }

    public boolean isFull() {
        return player1 != null && player2 != null;
    }
}
