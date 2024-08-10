package kz.yesmukhanov.finance.repository;

import kz.yesmukhanov.finance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	User findByChatId(String chatId);
}
