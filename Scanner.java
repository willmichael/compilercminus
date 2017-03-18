package com.compiler;


import com.sun.org.apache.bcel.internal.generic.ARRAYLENGTH;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.lang.reflect.Array;
import java.util.ArrayList;


/**
 * Created by WillMichael on 3/16/17.
 */
public class Scanner {
    private File rf;
    private java.util.Scanner scannedFile;
    private ArrayList<Token> tokens = new ArrayList<Token>();


    public Scanner(String file) {
        //file is available
//        this.rf = file;
//        if ( file.canRead() && file.exists() ) {
//            try {
//                //parser to scan file
//                scannedFile = new java.util.Scanner(file);
//            } catch(FileNotFoundException e) {
//                return;
//            }

            //parses the scanned file into tokens based on white space

            try {
                LineNumberReader lr = new LineNumberReader(new FileReader(file));
                String line;
                while((line = lr.readLine()) != null) {
                    java.util.Scanner s = new java.util.Scanner(line);
                    String tempTok;
                    System.out.println(line);
                    while(s.hasNext()) {
                        tempTok = s.next();
                        ArrayList<Token> tempAlTok = parseToken(tempTok, lr.getLineNumber());
                        if(tempAlTok != null) {
                            tokens.addAll(tempAlTok);
                        } else {
                            System.out.println("else " + tempTok);
                        }
                    }
                }
            } catch (Exception e) {
                return;
            }


//            while(scannedFile.hasNext()) {
////                System.out.println(scannedFile.next());
//                String tempTok = scannedFile.next();
////                System.out.println("temp " + tempTok);
//                ArrayList<Token> tempALTok = parseToken(tempTok);
//                if(tempALTok != null) {
////                    System.out.println(tempALTok.var);
//                    tokens.addAll(parseToken(tempTok));
//                } else {
//                    System.out.println("else " + tempTok);
//                }
//            }

            System.out.println("tokens:");
            for (Token tok: tokens) {
                System.out.println(tok.val);
                System.out.println(tok.lineNum);
            }

//        } else {
//            System.out.print("file not found");
//        }
    }

    public ArrayList<Token> parseToken(String toToken, int lineNum) {
        ArrayList<Token> result = new ArrayList<Token>();
        Token resultToken;

        //single char
        if(toToken.length() == 1) {
            resultToken = createToken(toToken, lineNum);
            result.add(resultToken);
            return result;

        }
        //parse specific strings -- inefficient because we have to parse twice
        if(toToken.equals("int")) {
            result.add(new Token(Types.INT, toToken, lineNum));
            return result;
        } else if(toToken.equals("void")) {
            result.add(new Token(Types.VOID, toToken, lineNum));
            return result;
        } else if(toToken.equals("write")) {
            result.add(new Token(Types.WRITE, toToken, lineNum));
            return result;
        } else if(toToken.equals("read")) {
            result.add(new Token(Types.READ, toToken, lineNum));
            return result;
        } else if(toToken.equals("if")) {
            result.add(new Token(Types.IF, toToken, lineNum));
            return result;
        } else if(toToken.equals("else")) {
            result.add(new Token(Types.ELSE, toToken, lineNum));
            return result;
        } else if(toToken.equals("return")) {
            result.add(new Token(Types.RETURN, toToken, lineNum));
            return result;
        }
        //greater than a single char

        if (toToken.length() > 1) {
            String addChar = "";
            for (int i = 0; i < toToken.length(); i++) {
                char c = toToken.charAt(i);
                Token cTok = null;
                //check for double characters
                if(c == '<' || c == '>' || c == '!' || c == '=') {
                    char c1;
                    if((c1 = toToken.charAt(i + 1)) == '=') {
                        cTok = createToken(String.valueOf(c) + c1, lineNum);
                        //skip one character
                        i = i + 1;
                    }
                } else if(c == '/') {
                    char c1;
                    if((c1 = toToken.charAt(i + 1)) == '*') {
                        cTok = createToken(String.valueOf(c) + c1, lineNum);
                        //skip one character
                        i = i + 1;
                    }
                } else if(c == '*') {
                    char c1;
                    if((c1 = toToken.charAt(i + 1)) == '/') {
                        cTok = createToken(String.valueOf(c) + c1, lineNum);
                        //skip one character
                        i = i + 1;
                    }
                }
                String curChar = String.valueOf(c);
                //adds chars until we get a tok
                while(cTok == null && i < toToken.length()-1) {
                    //addChar is a number, we want the full number
                    if(curChar.matches("[0-9]+")) {
                        //if the next char is a number, then add i + 1
                        if(String.valueOf(toToken.charAt(i + 1)).matches("[0-9]")) {
                            i = i + 1;
                        //if the next char isn't a number, create tok
                        } else {
                            cTok = createToken(curChar, lineNum);
                            //fail create tok
                            if(cTok == null) {
                                return null;
                            }
                            //reset character and continue while loop
                            addChar = addChar + curChar;
                            curChar = "";
                            continue;
                        }
                    //add char is text, we want full text
                    } else if(curChar.matches("[a-zA-Z]+")) {
                        //if the next char is a number, then add i + 1
                        if(String.valueOf(toToken.charAt(i + 1)).matches("[a-zA-Z]")) {
                            i = i + 1;
                        //if the next char isn't a number, create tok
                        } else {
                            cTok = createToken(curChar, lineNum);
                            //fail create tok
                            if(cTok == null) {
                                System.out.println("Failing");
                                return null;
                            }
                            //reset character and continue while loop
                            addChar = addChar + curChar;
                            curChar = "";
                            continue;
                        }
                    } else {
                        cTok = createToken(String.valueOf(curChar), lineNum);
                        if(cTok == null) {
                            System.out.println("Failing");
                            return null;
                        }
                        addChar = addChar + curChar;
                        curChar = "";
                        continue;
                    }
//                    addChar = addChar + curChar;
                    curChar = curChar + toToken.charAt(i);
                }
                if(cTok != null && i >= toToken.length()-1) {
                    result.add(cTok);
                    return result;
                } else if (cTok == null && i == toToken.length()-1) {
                    cTok = createToken(curChar, lineNum);
                    if(cTok == null) {
                        System.out.println("Failing");
                        return null;
                    }
                    result.add(cTok);
                    return result;
                } else if (cTok != null) {
                    result.add(cTok);
                    if(addChar.length() + 1 == toToken.length()) {
                        cTok = createToken(String.valueOf(toToken.charAt(i+1)), lineNum);
                        if(cTok == null) {
                            System.out.println("Failing");
                            return null;
                        }
                        result.add(cTok);
                        return result;
                    }
                }
            }
        }
        return null;
    }

    public Token createToken(String toToken, int lineNum) {
        Token result = new Token (Types.ERR, toToken, lineNum);

        // if matches a number
        if(toToken.matches("[0-9]+")) {

            result = new Token(Types.NUM, toToken, lineNum);

        } else {

            switch(toToken) {
                case "": {
                    result = null;
                    break;
                }
                case "else": {
                    result = new Token(Types.ELSE, toToken, lineNum);
                    break;
                }
                case "if": {
                    result = new Token(Types.IF, toToken, lineNum);
                    break;
                }
                case "int": {
                    result = new Token(Types.INT, toToken, lineNum);
                    break;
                }
                case "return": {
                    result = new Token(Types.RETURN, toToken, lineNum);
                    break;
                }
                case "void": {
                    result = new Token(Types.VOID, toToken, lineNum);
                    break;
                }
                case "while": {
                    result = new Token(Types.WHILE, toToken, lineNum);
                    break;
                }
                case "+": {
                    result = new Token(Types.PLUS, toToken, lineNum);
                    break;
                }
                case "-": {
                    result = new Token(Types.MINUS, toToken, lineNum);
                    break;
                }
                case "*": {
                    result = new Token(Types.MULT, toToken, lineNum);
                    break;
                }
                case "/": {
                    result = new Token(Types.DIV, toToken, lineNum);
                    break;
                }
                case "<": {
                    result = new Token(Types.LESS, toToken, lineNum);
                    break;
                }
                case "<=": {
                    result = new Token(Types.LESSEQ, toToken, lineNum);
                    break;
                }
                case ">": {
                    result = new Token(Types.GREAT, toToken, lineNum);
                    break;
                }
                case ">=": {
                    result = new Token(Types.GREATEQ, toToken, lineNum);
                    break;
                }
                case "==": {
                    result = new Token(Types.EQ, toToken, lineNum);
                    break;
                }
                case "!=": {
                    result = new Token(Types.NOTEQ, toToken, lineNum);
                    break;
                }
                case "=": {
                    result = new Token(Types.ASSIGN, toToken, lineNum);
                    break;
                }
                case ";": {
                    result = new Token(Types.SEMI, toToken, lineNum);
                    break;
                }
                case ",": {
                    result = new Token(Types.COM, toToken, lineNum);
                    break;
                }
                case "(": {
                    result = new Token(Types.LEFTP, toToken, lineNum);
                    break;
                }
                case ")": {
                    result = new Token(Types.RIGHTP, toToken, lineNum);
                    break;
                }
                case "[": {
                    result = new Token(Types.LEFTB, toToken, lineNum);
                    break;
                }
                case "]": {
                    result = new Token(Types.RIGHTB, toToken, lineNum);
                    break;
                }
                case "{": {
                    result = new Token(Types.LEFTC, toToken, lineNum);
                    break;
                }
                case "}": {
                    result = new Token(Types.RIGHTC, toToken, lineNum);
                    break;
                }
                case "read": {
                    result = new Token(Types.READ, toToken, lineNum);
                    break;
                }
                case "write": {
                    result = new Token(Types.WRITE, toToken, lineNum);
                    break;
                }
                case "/*": {
                    result = new Token(Types.STARTC, toToken, lineNum);
                    break;
                }
                case "*/": {
                    result = new Token(Types.ENDC, toToken, lineNum);
                    break;
                }
                default: {
                    result = new Token(Types.NAME, toToken, lineNum);
//                    result = null;
                    break;
                }
            }
        }
        return result;
    }


}

