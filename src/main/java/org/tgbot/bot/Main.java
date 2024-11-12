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

                // Если пользователь только что присоединился или ввел /start
                if (userMessage.equals("/start")) {
                    sendWelcomeMessage(message);
                }
                // Если пользователь нажал кнопку Info
                else if (userMessage.equals("Info")) {
                    sendInfoMessage(message);
                }
                // Если пользователь нажал кнопку Start
                else if (userMessage.equals("Start")) {
                    sendStartMessage(message);
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
        row.add(new KeyboardButton("Info"));
        row.add(new KeyboardButton("Start"));

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
}
