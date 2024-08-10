package kz.yesmukhanov.finance.service;

import kz.yesmukhanov.finance.model.Expense;
import kz.yesmukhanov.finance.model.ExpenseType;
import kz.yesmukhanov.finance.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService{
	private final ExpenseRepository expenseRepository;

	public void saveExpense(Expense expense) {
		expenseRepository.save(expense);
	}

	public List<Expense> getAllExpensesByChatId(String chatId) {
		return expenseRepository.findAllByChatId(chatId);
	}
}
