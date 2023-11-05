package com.hhl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.hhl.dto.TelegramBot;


@Configuration
public class TelegramBotConfig {

    @Bean
    TelegramBot telegrambot() {
    	try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            TelegramBot bot = new TelegramBot();
            botsApi.registerBot(bot);
            return bot;
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }
}