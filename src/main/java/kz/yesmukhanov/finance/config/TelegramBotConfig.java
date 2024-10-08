package kz.yesmukhanov.finance.config;

import kz.yesmukhanov.finance.service.TelegramBotService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@PropertySource("classpath:application.properties")
public class TelegramBotConfig {

	@Bean
	public TelegramBotsApi telegramBotsApi(TelegramBotService telegramBotService) throws TelegramApiException {
		TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
		api.registerBot(telegramBotService);

		return api;
	}
}
