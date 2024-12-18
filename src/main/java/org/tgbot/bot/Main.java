package org.tgbot.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main extends TelegramLongPollingBot {
    private static String botUsername;
    private static String botToken;

    private final RoomManager roomManager = new RoomManager();

    static {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
            botUsername = properties.getProperty("BOT_USERNAME");
            botToken = properties.getProperty("BOT_TOKEN");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                String userMessage = message.getText();

                if (userMessage.equalsIgnoreCase("/start")) {
                    sendWelcomeMessage(message);
                } else if (userMessage.equalsIgnoreCase("Info")) {
                    sendInfoMessage(message);
                } else if (userMessage.equalsIgnoreCase("Start")) {
                    sendStartMessage(message);
                } else if (userMessage.equalsIgnoreCase("/create")) {
                    createRoom(message);
                } else if (userMessage.startsWith("/join")) {
                    joinRoom(message);
                } else if (userMessage.equalsIgnoreCase("/help") || userMessage.equalsIgnoreCase("help")) {
                    sendHelpMessage(message);
                } else if (userMessage.startsWith("/move")) { // Новая команда для хода
                    handleMove(message);
                } else {
                    sendInvalidCommandMessage(message);
                }
            }
        }
    }

    // Обработчик хода
    private void handleMove(Message message) {
        String[] parts = message.getText().split(" ");
        if (parts.length < 4) {
            sendTextMessage(message.getChatId(), "Введите ход в формате: /move <код комнаты> <номер строки> <номер столбца>");
            return;
        }

        String roomCode = parts[1];
        int row, col;

        try {
            row = Integer.parseInt(parts[2]);
            col = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            sendTextMessage(message.getChatId(), "Введите числа для строки и столбца.");
            return;
        }

        GameRoom room = roomManager.getRoom(roomCode);
        if (room == null) {
            sendTextMessage(message.getChatId(), "Комната не найдена. Убедитесь в правильности кода комнаты.");
            return;
        }

        User player = message.getFrom();
        if (!isPlayerTurn(room, player)) {
            sendTextMessage(message.getChatId(), "Сейчас не ваш ход.");
            return;
        }

        boolean moveSuccessful = room.makeMove(row, col, player);
        if (!moveSuccessful) {
            sendTextMessage(message.getChatId(), "Этот ход недоступен(Комната ожидает второго пользователя / Попробуйте другое место");
            return;
        }

        // Отправляем статус доски обоим игрокам
        sendTextMessage(room.getPlayer1().getId(), room.getBoardDisplay());
        sendTextMessage(room.getPlayer2().getId(), room.getBoardDisplay());

        // Проверяем, завершена ли игра
        if (room.checkWin()) {
            // Выясняем победителя
            if (room.getCurrentPlayer() == 'X') {
                sendTextMessage(room.getPlayer1().getId(), "Поздравляем, " + room.getPlayer1().getFirstName() + ", вы выиграли!");
                sendTextMessage(room.getPlayer2().getId(), "К сожалению, вы проиграли. Победил игрок " + room.getPlayer1().getFirstName());
            } else {
                sendTextMessage(room.getPlayer2().getId(), "Поздравляем, " + room.getPlayer2().getFirstName() + ", вы выиграли!");
                sendTextMessage(room.getPlayer1().getId(), "К сожалению, вы проиграли. Победил игрок " + room.getPlayer2().getFirstName());
            }
            roomManager.removeRoom(room.getRoomCode());
        } else if (room.isDraw()) {
            sendTextMessage(room.getPlayer1().getId(), "Игра окончена. Ничья!");
            sendTextMessage(room.getPlayer2().getId(), "Игра окончена. Ничья!");
            roomManager.removeRoom(room.getRoomCode());
        } else {
            // Сообщаем, чей ход следующий
            if (room.getCurrentPlayer() == 'X') {
                sendTextMessage(room.getPlayer1().getId(), "Ваш ход, " + room.getPlayer1().getFirstName() + " (X).");
                sendTextMessage(room.getPlayer2().getId(), "Ход игрока " + room.getPlayer1().getFirstName() + " (X).");
            } else {
                sendTextMessage(room.getPlayer2().getId(), "Ваш ход, " + room.getPlayer2().getFirstName() + " (O).");
                sendTextMessage(room.getPlayer1().getId(), "Ход игрока " + room.getPlayer2().getFirstName() + " (O).");
            }
        }
    }




    // Проверка, чей ход
    private boolean isPlayerTurn(GameRoom room, User player) {
        if (room.getCurrentPlayer() == 'X') {
            return room.getPlayer1().getId().equals(player.getId());
        } else {
            return room.getPlayer2().getId().equals(player.getId());
        }
    }

    private void sendWelcomeMessage(Message message) {
        SendMessage welcomeMessage = new SendMessage();
        welcomeMessage.setChatId(message.getChatId().toString());
        welcomeMessage.setText("Добро пожаловать! Выберите команду:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("info"));
        row.add(new KeyboardButton("start"));
        row.add(new KeyboardButton("help"));

        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);

        welcomeMessage.setReplyMarkup(keyboardMarkup);

        try {
            execute(welcomeMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendInfoMessage(Message message) {
        SendMessage infoMessage = new SendMessage();
        infoMessage.setChatId(message.getChatId().toString());
        infoMessage.setText("Данный бот создан для игры в крестики-нолики по сети.\nАвторы: @notsoclose, @ashensix");

        try {
            execute(infoMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendStartMessage(Message message) {
        SendMessage startMessage = new SendMessage();
        startMessage.setChatId(message.getChatId().toString());
        startMessage.setText("Создайте или присоединитесь к комнате по коду");

        try {
            execute(startMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void createRoom(Message message) {
        GameRoom newRoom = roomManager.createRoom(message.getFrom());
        String roomCode = newRoom.getRoomCode();
        SendMessage createMessage = new SendMessage();
        createMessage.setChatId(message.getChatId().toString());
        createMessage.setText("Комната создана. Код комнаты: " + roomCode);

        try {
            execute(createMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void joinRoom(Message message) {
        String[] parts = message.getText().split(" ");
        if (parts.length < 2) {
            sendTextMessage(message.getChatId(), "Пожалуйста, укажите код комнаты, например: /join 123456");
            return;
        }

        String roomCode = parts[1];
        Long userId = message.getFrom().getId();
        GameRoom room = roomManager.getRoom(roomCode);

        if (room != null && room.getOwnerId().equals(userId)) {
            sendTextMessage(message.getChatId(), "Вы являетесь создателем этой комнаты и не можете к ней присоединиться.");
            return;
        }

        boolean joined = roomManager.joinRoom(roomCode, message.getFrom());
        if (joined) {
            // Уведомляем обоих игроков о начале игры
            String playerName = message.getFrom().getFirstName();

            // Вместо room.getPlayer1().getChatId(), используем message.getChatId() для получения чата
            sendTextMessage(room.getPlayer1().getId(), "Вы успешно присоединились к комнате с кодом " + roomCode + ". Игра началась! Ход первого игрока: " + room.getCurrentPlayer() + " " + room.getPlayer1().getFirstName() + "\nДля хода введите /move_<код комнаты>_<строчка>_<столбец>");
            sendTextMessage(room.getPlayer2().getId(), "Вы успешно присоединились к комнате с кодом " + roomCode + ". Игра началась! Ход первого игрока: " + room.getCurrentPlayer() + " " + room.getPlayer1().getFirstName() + "\nДля хода введите /move_<код комнаты>_<строчка>_<столбец>");
        } else {
            sendTextMessage(message.getChatId(), "Не удалось присоединиться. Комната не найдена или уже заполнена.");
        }

    }


    private void sendHelpMessage(Message message) {
        String helpText = """
                Доступные команды:
                /start - Перезапустить бота
                /create - Создать новую комнату для игры
                /join <код комнаты> - Присоединиться к существующей комнате
                /move <код комнаты> <строка> <столбец> - Сделать ход
                /help - Показать доступные команды
                """;
        sendTextMessage(message.getChatId(), helpText);
    }

    private void sendInvalidCommandMessage(Message message) {
        sendTextMessage(message.getChatId(), "Неверная команда. Напишите 'help' для помощи.");
    }

    private void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
