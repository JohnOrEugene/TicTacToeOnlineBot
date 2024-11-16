package org.tgbot.bot;

public class TicTacToeGame {
    public static char[][] board = new char[3][3];
    private char currentPlayer = 'X'; // Начинает игрок X
    private boolean gameFinished = false; // Флаг завершения игры

    public TicTacToeGame() {
        board = new char[3][3];
        currentPlayer = 'X'; // 'X' начинает первым
        gameFinished = false;
        initializeBoard();
    }

    // Инициализация пустого игрового поля
    private void initializeBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '-'; // '-' символ пустой клетки
            }
        }
    }

    // Получение текущего игрока
    public char getCurrentPlayer() {
        return currentPlayer;
    }

    // Проверка, завершена ли игра
    public boolean isFinished() {
        return gameFinished;
    }

    // Выполнение хода игрока
    public boolean makeMove(int row, int col) {
        // Проверка, если игра уже завершена
        if (gameFinished) {
            return false;  // Ход невозможен, так как игра окончена
        }

        // Проверка на корректность координат и занятость клетки
        if (row < 0 || row > 2 || col < 0 || col > 2 || board[row][col] != '-') {
            return false; // Ход невозможен из-за неверных данных
        }

        // Совершаем ход
        board[row][col] = currentPlayer;

        // Проверка на победу или ничью
        if (checkWin()) {
            gameFinished = true;
            return true; // Возвращаем true для успешного хода
        } else if (TicTacToeGame.isBoardFull()) {
            gameFinished = true;
            return true; // Возвращаем true, чтобы отметить завершение игры
        }

        // Переключение хода между игроками
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
        return true; // Ход выполнен успешно
    }

    // Переключение хода между игроками
    public void switchPlayer() {
        if (currentPlayer == 'X') {
            currentPlayer = 'O';
        } else {
            currentPlayer = 'X';
        }
    }

    // Проверка победителя
    public boolean checkWin() {
        return (checkRows() || checkColumns() || checkDiagonals());
    }

    // Проверка, что доска полностью заполнена (ничья)
    public static boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == '-') { // Если хотя бы одна ячейка пустая
                    return false;
                }
            }
        }
        return true; // Все ячейки заполнены
    }

    // Проверка строк на наличие трёх одинаковых символов
    private boolean checkRows() {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == currentPlayer && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return true;
            }
        }
        return false;
    }

    // Проверка столбцов
    private boolean checkColumns() {
        for (int i = 0; i < 3; i++) {
            if (board[0][i] == currentPlayer && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return true;
            }
        }
        return false;
    }

    // Проверка диагоналей
    private boolean checkDiagonals() {
        return (board[0][0] == currentPlayer && board[0][0] == board[1][1] && board[1][1] == board[2][2]) ||
                (board[0][2] == currentPlayer && board[0][2] == board[1][1] && board[1][1] == board[2][0]);
    }

    // Получение состояния доски для отображения
    public String getBoardDisplay() {
        StringBuilder display = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                display.append(board[i][j]).append(" ");
            }
            display.append("\n");
        }
        return display.toString();
    }
}
