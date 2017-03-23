package com.compiler;

import java.io.File;

public class Main {

    public static void main(String[] args) {

        System.out.println(new File(".").getAbsolutePath());

        String xFile = "/Users/WillMichael/Documents/ClassWinter2017/Compilers/sample6.cm";
        Scanner scanner = new Scanner(xFile);

        if(scanner.getTokens() != null) {
            Parser parser = new Parser(scanner.getTokens());
        }

    }
}
