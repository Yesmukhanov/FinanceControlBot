package kz.yesmukhanov.finance.repository;

import kz.yesmukhanov.finance.model.Expense;
import kz.yesmukhanov.finance.model.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

	List<Expense> findAllByChatId(String chatId);
}
