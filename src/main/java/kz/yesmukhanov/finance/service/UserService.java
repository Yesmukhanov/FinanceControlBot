package kz.yesmukhanov.finance.service;

import kz.yesmukhanov.finance.model.User;
import kz.yesmukhanov.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	public void saveUser(User user) {
		if(Objects.nonNull(userRepository.findByChatId(user.getChatId()))) {
			return;
		}
		userRepository.save(user);
	}
}
