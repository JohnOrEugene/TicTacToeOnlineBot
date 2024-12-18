package org.tgbot.bot;

import org.telegram.telegrambots.meta.api.objects.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RoomManager {
    private final Map<String, GameRoom> activeRooms = new HashMap<>();
    private final Random random = new Random();

    // Метод для создания уникального кода комнаты
    public String generateRoomCode() {
        String code;
        do {
            code = String.valueOf(100 + random.nextInt(900)); // Генерация 6-значного кода
        } while (activeRooms.containsKey(code));
        return code;
    }

    // Метод для создания новой комнаты
    public GameRoom createRoom(User player1) {
        String roomCode = generateRoomCode();
        GameRoom newRoom = new GameRoom(roomCode, player1);
        activeRooms.put(roomCode, newRoom);
        return newRoom;
    }

    // Метод для подключения игрока к существующей комнате по коду
    public boolean joinRoom(String roomCode, User player2) {
        GameRoom room = activeRooms.get(roomCode);
        if (room != null && !room.isFull()) {
            // Проверка, чтобы владелец комнаты не мог к ней подключиться
            if (!room.getOwnerId().equals(player2.getId())) {
                room.setPlayer2(player2);
                return true;
            }
        }
        return false;
    }

    // Метод для получения комнаты по коду
    public GameRoom getRoom(String roomCode) {
        return activeRooms.get(roomCode);
    }

    // Метод для удаления завершённой комнаты
    public void removeRoom(String roomCode) {
        activeRooms.remove(roomCode);
    }

}
