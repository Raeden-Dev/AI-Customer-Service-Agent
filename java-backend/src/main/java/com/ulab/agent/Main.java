package com.ulab.agent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ulab.agent.utils.Console;
import com.ulab.agent.utils.ConsoleTerminal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class Main {

    public static final String VERSION = "1.0.0";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Console console = new Console();

    public static final Path DATA_DIRECTORY = Paths.get("data");
    public static final Path CONFIG_FILE = DATA_DIRECTORY.resolve("config.json");
    public static final Path BUSINESSES_DIRECTORY = DATA_DIRECTORY.resolve("businesses");

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Main.class, args);
        new ConsoleTerminal(ctx).run();
    }
}
