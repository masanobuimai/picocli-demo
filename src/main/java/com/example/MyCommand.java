package com.example;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Component
@Command(name = "myCommand")
public class MyCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        System.out.println("hello world");
        return 0;
    }
}
