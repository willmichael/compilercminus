package com.compiler;

import java.io.File;

public class Main {

    public static void main(String[] args) {
	// write your code here

//        Scanner test = new Scanner();
//        test.createToken("0909");

        System.out.println(new File(".").getAbsolutePath());
        File pfile = new File("/Users/WillMichael/Documents/ClassWinter2017/Compilers/sample3.cm");

        String xFile = "/Users/WillMichael/Documents/ClassWinter2017/Compilers/sample5.cm";
        Scanner scanner = new Scanner(xFile);

        if(scanner.getTokens() != null) {
            Parser parser = new Parser(scanner.getTokens());
        }

    }
}
