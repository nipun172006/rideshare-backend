package org.example.rideshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class RideshareApplication {

	public static void main(String[] args) {
		// Load .env if present and set System properties expected by Spring placeholders
		try {
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMalformed()
					.ignoreIfMissing()
					.load();
			setIfPresent(dotenv, "MONGODB_URI");
			setIfPresent(dotenv, "MONGODB_DATABASE");
			setIfPresent(dotenv, "JWT_SECRET");
			setIfPresent(dotenv, "JWT_EXP_SECONDS");
		} catch (Throwable ignored) {
			// proceed even if dotenv isn't available
		}
		SpringApplication.run(RideshareApplication.class, args);
	}

	private static void setIfPresent(Dotenv dotenv, String key) {
		String val = dotenv.get(key);
		if (val != null && !val.isEmpty()) {
			System.setProperty(key, val);
		}
	}

}
