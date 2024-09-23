package uz.akbar;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

/**
 * Main
 */
public class Main {

	public static void main(String[] args) {

		try {
			String botToken = "7231619010:AAGLmxBAJpk5l_7UkAWmgvCG8r8aAq1Q7Rg";

			TelegramBotsLongPollingApplication botsLongPollingApplication = new TelegramBotsLongPollingApplication();
			botsLongPollingApplication.registerBot(botToken, new Bot());

			System.out.println();
			System.out.println("~~ Hq Bot has started ~~");
			System.out.println();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
