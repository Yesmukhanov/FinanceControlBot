package kz.yesmukhanov.finance.service;

import kz.yesmukhanov.finance.model.Expense;
import kz.yesmukhanov.finance.model.ExpenseType;
import kz.yesmukhanov.finance.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TelegramBotService extends TelegramLongPollingBot {

	private final static String START = "/start";
	private final static String EXPENSE = "/expense";
	private final static String GET_ALL_EXPENSES = "/allexpenses";
	@Value("${telegram.bot.token}")
	String botToken;
	@Autowired
	private UserService userService;
	@Autowired
	private ExpenseService expenseService;
	@Value("${telegram.username.bot}")
	private String botUsername;
	private String currentExpenseType = null;
	private String currentChatId = null;


	@Autowired
	public TelegramBotService(ExpenseService expenseService, UserService userService) {
		this.expenseService = expenseService;
		this.userService = userService;
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
		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageText = update.getMessage().getText();
			String chatId = update.getMessage().getChatId().toString();
			String userName = update.getMessage().getFrom().getUserName();


			if (messageText.equalsIgnoreCase(START)) {
				saveUserInformation(chatId, userName);

				handleStartCommand(chatId);
			} else if (messageText.equals(GET_ALL_EXPENSES)) {
				getAllExpenses(chatId);
			} else if (messageText.equalsIgnoreCase(EXPENSE)) {
				promptForExpenseType(chatId);
			} else if (isExpenseType(messageText)) {
				currentExpenseType = messageText;
				currentChatId = chatId;
				promptForExpenseAmount(chatId, messageText);
			} else if (currentChatId != null && currentChatId.equals(chatId)) {
				try {
					Double amount = Double.parseDouble(messageText);
					saveExpense(chatId, ExpenseType.valueOf(currentExpenseType), amount);
					sendMessage(chatId, "Expense recorded successfully!");
					currentChatId = null;
					currentExpenseType = null;
				} catch (NumberFormatException e) {
					sendMessage(chatId, "Invalid amount. Please enter a numeric value.");
				}
			} else {
				sendMessage(chatId, "Sorry, I don't recognize that command.");
			}
		}
	}

	private void getAllExpenses(String chatId) {
		List<Expense> allExpenses = expenseService.getAllExpensesByChatId(chatId);

		Map<String, Double> expenseSumByType = new HashMap<>();

		for (Expense expense : allExpenses) {
			ExpenseType type = expense.getType();
			Double amount = expense.getAmount(); // Assuming there's a method to get the amount

			expenseSumByType.merge(String.valueOf(type), amount, Double::sum);
		}

		StringBuilder res = new StringBuilder();
		for (Map.Entry<String, Double> entry : expenseSumByType.entrySet()) {
			res.append(entry.getKey())
					.append(": ").append(entry.getValue())
					.append("\n");
		}

		sendMessage(chatId, res.toString());
	}


	private void saveUserInformation(String chatId, String userName) {
		userService.saveUser(User
				.builder()
				.username(userName)
				.chatId(chatId)
				.build());
	}

	private void handleStartCommand(String chatId) {
		String welcomeMessage = "Welcome to the Finance Control Bot! You can track your expenses here.";
		sendMessage(chatId, welcomeMessage);
	}

	private void promptForExpenseType(String chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Choose an expense type:");

		ReplyKeyboardMarkup keyboardMarkup = getKeyboardConfig();

		keyboardMarkup.setKeyboard(getAllExpensesTypeForKeyBoard());
		message.setReplyMarkup(keyboardMarkup);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private boolean isExpenseType(String messageText) {
		try {
			ExpenseType.valueOf(messageText);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}


	private void promptForExpenseAmount(String chatId, String expenseType) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("You've selected " + expenseType + ". Please enter the amount:");

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private void saveExpense(String chatId, ExpenseType expenseType, Double amount) {
		Expense expense = Expense.builder()
				.chatId(chatId)
				.type(expenseType)
				.amount(amount)
				.expenseDate(LocalDateTime.now())
				.build();
		expenseService.saveExpense(expense);
	}

	private void sendMessage(String chatId, String text) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText(text);
		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private List<KeyboardRow> getAllExpensesTypeForKeyBoard() {
		List<KeyboardRow> keyboard = new ArrayList<>();
		int columns = 2;
		List<ExpenseType> expenseTypes = Arrays.asList(ExpenseType.values());
		for (int i = 0; i < expenseTypes.size(); i += columns) {
			KeyboardRow row = new KeyboardRow();
			for (int j = i; j < i + columns && j < expenseTypes.size(); j++) {
				row.add(new KeyboardButton(expenseTypes.get(j).name()));
			}
			keyboard.add(row);
		}

		return keyboard;
	}

	private ReplyKeyboardMarkup getKeyboardConfig() {
		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		keyboardMarkup.setResizeKeyboard(true);
		keyboardMarkup.setOneTimeKeyboard(true);
		return keyboardMarkup;
	}


}
