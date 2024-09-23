package uz.akbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;

/**
 * Bot
 */
public class Bot implements LongPollingSingleThreadUpdateConsumer {

	TelegramClient telegramClient = new OkHttpTelegramClient(
			"7231619010:AAGLmxBAJpk5l_7UkAWmgvCG8r8aAq1Q7Rg");

	@Override
	public void consume(Update update) {

		if (update.hasMessage() && update.getMessage().hasText()) {
			Long chatId = update.getMessage().getChatId();
			String text = update.getMessage().getText();
			String response = "in process";

			String videoId = isValidYoutubeLink(text);

			if (videoId != null) {
				YoutubeDownloader downloader = new YoutubeDownloader();
				RequestVideoInfo requestVideoInfo = new RequestVideoInfo(videoId);
				VideoInfo videoInfo = downloader.getVideoInfo(requestVideoInfo).data();
				// AudioFormat bestAudioFormat = videoInfo.bestAudioFormat();

				AudioFormat bestAudioFormat = videoInfo.audioFormats().stream()
						.max(Comparator.comparingInt(format -> {
							return format.averageBitrate();
						}))
						.orElse(null);

				if (bestAudioFormat != null) {
					System.out.println("Downloading audio: " + bestAudioFormat.audioQuality());

					File audioFile = downloadAudioToFile(bestAudioFormat);

					if (audioFile != null) {
						SendAudio sendAudio = SendAudio.builder()
								.chatId(chatId)
								.audio(new InputFile(audioFile))
								.build();

						executeAudio(sendAudio);

						audioFile.delete();
					} else {
						response = "Can not download audio! 🎵";
					}
				} else {
					response = "No audio formats available! 🤷";
				}

			} else {
				response = "Link is not valid! 🙅";
			}

			executeMessage(SendMessage.builder()
					.chatId(chatId)
					.text(response)
					.build());
		}

	}

	private File downloadAudioToFile(AudioFormat audioFormat) {
		File tempFile = null;
		try {
			URL url = new URL(audioFormat.url());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			int fileSize = connection.getContentLength();
			if (fileSize <= 0) {
				throw new IOException("Failed to get file size");
			}

			tempFile = File.createTempFile("audio", ".mp3");

			try (InputStream inputStream = connection.getInputStream();
					OutputStream outputStream = new FileOutputStream(tempFile)) {

				byte[] buffer = new byte[1024];
				int bytesRead;
				long totalBytesRead = 0;

				System.out.println("Starting download ... 📥");
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
					totalBytesRead += bytesRead;

					int progress = (int) ((totalBytesRead * 100) / fileSize);
					System.out.println("\rProgress: " + progress + "%");
				}
				System.out.println("Download completed! ✅");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("tugadi");
		return tempFile;
	}

	private String isValidYoutubeLink(String link) {
		if (link == null || link.isEmpty())
			return null;

		final String regex = "^(https?://)?(www\\.)?"
				+ "(youtube\\.com/(watch\\?v=|embed/|v/|.+\\?v=)|youtu\\.be/|youtube-nocookie\\.com/(embed/|watch\\?v=))"
				+ "([a-zA-Z0-9_-]{11})"
				+ "(\\?[^\\s]*)?$";

		final Pattern YOUTUBE_PATTERN = Pattern.compile(regex);

		Matcher matcher = YOUTUBE_PATTERN.matcher(link);

		if (matcher.matches()) {
			return matcher.group(6);
		}

		return null;
	}

	private void executeMessage(SendMessage sendMessage) {
		try {
			telegramClient.execute(sendMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void executeAudio(SendAudio sendAudio) {
		try {
			telegramClient.execute(sendAudio);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
