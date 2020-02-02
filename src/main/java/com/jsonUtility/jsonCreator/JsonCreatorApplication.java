package com.jsonUtility.jsonCreator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class JsonCreatorApplication {
	private static String[] args;
	private static ConfigurableApplicationContext context;

	/*public static void main(String[] args) { // This will be used when auto restart functionality is being used
		JsonCreatorApplication.args = args;
		JsonCreatorApplication.context = SpringApplication.run(JsonCreatorApplication.class, args);
	}*/
	public static void main(String[] args) throws Exception{
		SpringApplication.run(JsonCreatorApplication.class, args);
	}

    /*public static void restart() { //TO restart the application automatically
		// close previous context
		context.close();

		// and build new one
		JsonCreatorApplication.context = SpringApplication.run(JsonCreatorApplication.class, args);
    }*/
}
