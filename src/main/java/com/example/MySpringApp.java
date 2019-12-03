package com.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@SpringBootApplication
public class MySpringApp implements CommandLineRunner, ExitCodeGenerator {
    private IFactory factory;
    private MyCommand command;
    private int exitCode;

    MySpringApp(IFactory factory, MyCommand command) {
        this.factory = factory;
        this.command = command;
    }

    @Override
    public void run(String... args) throws Exception {
        exitCode = new CommandLine(command, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(MySpringApp.class, args)));
    }
}
