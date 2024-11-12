package org.tgbot.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
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

    // Экземпляр RoomManager для управления комнатами
    private final RoomManager roomManager = new RoomManager();

    // Загрузка конфигурации
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

                // Обработка команды /start
                if (userMessage.equalsIgnoreCase("/start")) {
                    sendWelcomeMessage(message);
                }
                // Обработка команды Info
                else if (userMessage.equalsIgnoreCase("Info")) {
                    sendInfoMessage(message);
                }
                // Обработка команды Start
                else if (userMessage.equalsIgnoreCase("Start")) {
                    sendStartMessage(message);
                }
                // Обработка команды создания комнаты
                else if (userMessage.equalsIgnoreCase("/create")) {
                    createRoom(message);
                }
                // Обработка команды подключения к комнате
                else if (userMessage.startsWith("/join")) {
                    joinRoom(message);
                }
                // Обработка команды помощи
                else if (userMessage.equalsIgnoreCase("/help") || userMessage.equalsIgnoreCase("help")) {
                    sendHelpMessage(message);
                }
                // Обработка неверных команд
                else {
                    sendInvalidCommandMessage(message);
                }
            }
        }
    }

    // Метод отправки приветственного сообщения и отображения кнопок
    private void sendWelcomeMessage(Message message) {
        SendMessage welcomeMessage = new SendMessage();
        welcomeMessage.setChatId(message.getChatId().toString());
        welcomeMessage.setText("Добро пожаловать! Выберите команду:");

        // Устанавливаем клавиатуру с кнопками
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);  // Клавиатура подстраивается под размер экрана

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // Создаем строки с кнопками
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

    // Метод для отправки информации об авторе
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

    // Метод для отправки сообщения о начале игры
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

    // Метод для создания комнаты
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

    // Метод для подключения к комнате по коду
    private void joinRoom(Message message) {
        String[] parts = message.getText().split(" ");
        if (parts.length < 2) {
            sendTextMessage(message.getChatId(), "Пожалуйста, укажите код комнаты, например: /join 123456");
            return;
        }

        String roomCode = parts[1];
        Long userId = message.getFrom().getId();
        GameRoom room = roomManager.getRoom(roomCode);

        // Проверка на то, является ли пользователь владельцем комнаты
        if (room != null && room.getOwnerId().equals(userId)) {
            sendTextMessage(message.getChatId(), "Вы являетесь создателем этой комнаты и не можете к ней присоединиться.");
            return;
        }

        boolean joined = roomManager.joinRoom(roomCode, message.getFrom());
        if (joined) {
            sendTextMessage(message.getChatId(), "Вы успешно присоединились к комнате с кодом " + roomCode);
        } else {
            sendTextMessage(message.getChatId(), "Не удалось присоединиться. Комната не найдена или уже заполнена.");
        }
    }

    // Метод для отправки сообщения помощи
    private void sendHelpMessage(Message message) {
        String helpText = """
                Доступные команды:
                /start - Перезапустить бота
                /create - Создать новую комнату для игры
                /join <код комнаты> - Присоединиться к существующей комнате
                /help - Показать доступные команды
                """;
        sendTextMessage(message.getChatId(), helpText);
    }

    // Метод для обработки неверных команд
    private void sendInvalidCommandMessage(Message message) {
        sendTextMessage(message.getChatId(), "Неверная команда. Напишите 'help' для помощи.");
    }

    // Метод для отправки текстовых сообщений
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
