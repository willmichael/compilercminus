package com.compiler;

import apple.laf.JRSUIUtils;

import javax.print.attribute.standard.MediaSize;
import java.util.ArrayList;

/**
 * Created by WillMichael on 3/17/17.
 */
public class Parser extends nType {
    public TreeNode program;
    public TreeNode curNode;
    private ArrayList<Token> tokenList;
    private ArrayList<Token> stack;

    public Parser(ArrayList<Token> tL) {
        this.program = new TreeNode(PROGRAM);
        this.tokenList = tL;
        this.stack = new ArrayList<Token>();
        initialParse();
    }

    private void initialParse(){
        curNode = program;
        while(tokenList.size() != 0) {
            parseStateProgram();
        }
        this.program.printNode(this.program, 0);

        int i = 0;
        return;
    }

    private TreeNode parseStateProgram() {
            Token nextTok = getNextToken();
            if (compareTypes(nextTok, Types.INT) || compareTypes(nextTok, Types.VOID)) {
                popAndPush();
                nextTok = getNextToken();
                if (compareTypes(nextTok, Types.NAME)) {
                    popAndPush();
                    nextTok = getNextToken();
                    if (compareTypes(nextTok, Types.SEMI)) {
                        popAndPush(); // add semi

                        curNode.setSibling(parseIntDec(1));
                        curNode = curNode.getSibling();

                    } else if (compareTypes(nextTok, Types.LEFTB)) { // we have an array
                        popAndPush();
                        nextTok = getNextToken();
                        if(compareTypes(nextTok, Types.NUM) || compareTypes(nextTok, Types.NAME)) {
                            popAndPush();
                            nextTok = getNextToken();

                            if(compareTypes(nextTok, Types.RIGHTB)) {
                                popAndPush();
                                nextTok = getNextToken();

                                if(compareTypes(nextTok, Types.SEMI)) {
                                    popAndPush();
                                    popStack(); // semi

                                    curNode.setSibling(parseArrayDecNode());
                                    curNode = curNode.getSibling();
                                }
                            }
                        }
                    } else if (compareTypes(nextTok, Types.LEFTP)) { // we have a function
                        curNode.setSibling(parseFunc());
                        curNode = curNode.getSibling();
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        return null;
    }

    private TreeNode parseFunc() {
        TreeNode func = new TreeNode(FUNCTION);
        Token nextTok = popStack(); // function name
        if(compareTypes(nextTok, Types.NAME)) {
            func.setsValue(nextTok.val);
            nextTok = popStack(); // function type void or int
            if(compareTypes(nextTok,Types.INT)) {
                func.setTypeSpecifier(1);
            } else if (compareTypes(nextTok, Types.VOID)) {
                func.setTypeSpecifier(0);
            } else {
                return null;
            }
        }
        popAndPush();
        nextTok = getNextToken();
        while(!compareTypes(nextTok, Types.RIGHTP)) {
            popAndPush();
            nextTok = getNextToken();
        }
        TreeNode params = parseFuncNodeParams();
        func.setC1(params);

        popAndPush(); // pop right ) onto stack
        popStack();
        popAndPush(); // pop left { onto stack
        popStack();
        TreeNode compound = parseFuncCompound();
        func.setC2(compound);
        return func;
    }

    /**
     * parse declaration and statelist, nothing on stack right after '{' in the function
     * parses until it hits '}' end of function
     * @return
     */
    private TreeNode parseFuncCompound() {
        TreeNode compound = new TreeNode(COMPOUND);
        //c1 set to declarations
        compound.setC1(parseFuncDeclarations());
        //c2 set to statementList
        compound.setC2(parseFuncStateList());
        return compound;
    }

    private TreeNode parseFuncDeclarations() {
        TreeNode dec = new TreeNode(DECLARATION);
        TreeNode curD = dec; //pointer to loop through siblings
        dec.setTypeSpecifier(0);
        //sibling dec variable or array nodes
        if(compareTypes(getNextToken(), Types.INT)) { //we have declarations to parse
            while(tokenList.size() != 0 && compareTypes(getNextToken(), Types.INT)){
                popAndPush(); // pop int onto stack
                Token nextTok = getNextToken();
                while (!compareTypes(nextTok, Types.SEMI)) {
                    popAndPush();
                    nextTok = getNextToken();
                }
                TreeNode d = parseIntDec(0); //parse out int
                if (d == null) { //parse fail try parsing array
                    d = parseArrayDecNode();
                    if (d == null) {
                        return null;// fail to parse an array
                    }
                    curD.setSibling(d);
                    curD = curD.getSibling();
                } else {
                    curD.setSibling(d);
                    curD = curD.getSibling();
                }
                popAndPush(); // pop off semi colon off tokens
                popStack(); // pop off stack
            }
        } else { // no declarations to parse (void)
            dec.setSibling(null);
        }
        return dec;
    }

    /**
     * declaration already called, parse until until '}' is hit
     * @return
     */
    private TreeNode parseFuncStateList() {
        TreeNode stateList = new TreeNode(STATEMENT_LIST);
        TreeNode sibPoint = stateList;
        //siblings expr, compound, if, while, return, read, write, call
        //expr, read, write, are done
        //if, while next
        //then return call
//        stateList.setSibling(parseExpr());
        Token nextToken = getNextToken();
        while(!compareTypes(nextToken, Types.RIGHTC)) {
            switch (nextToken.type) {
                case IF:  {
                    TreeNode result = parseIfState();
                    sibPoint.setSibling(result);
                    sibPoint = sibPoint.getSibling();
                    break;
                }
                case WHILE:  {
                    TreeNode result = parseWhileState();
                    sibPoint.setSibling(result);
                    sibPoint = sibPoint.getSibling();
                    break;
                }
                case NAME:  {
                    //expr or call?
                    Token testTok = getToken(1);
                    if(compareTypes(testTok, Types.LEFTP)){
                        //call function
                        parseCall();
                    }
                    //we have expr
                        TreeNode result = parseExpr(0);
                        sibPoint.setSibling(result);
                        sibPoint = sibPoint.getSibling();
                        popAndPush(); // semi colon
                        popStack();
                    break;
                }
                case READ:  {
                    TreeNode result = parseRead();
                    sibPoint.setSibling(result);
                    sibPoint = sibPoint.getSibling();
                    break;
                }
                case WRITE:  {
                    TreeNode result = parseWrite();
                    sibPoint.setSibling(result);
                    sibPoint = sibPoint.getSibling();
                    break;
                }
                case RETURN:  {
                    TreeNode result = parseReturn();
                    sibPoint.setSibling(result);
                    sibPoint = sibPoint.getSibling();
                    break;
                }
                case SEMI: {
                    popAndPush(); // pop semi
                    popStack();
                }
                default: {
                    System.out.println("error parsing");
                    return stateList;
                }
            }
            nextToken = getNextToken();
        }
        popAndPush(); // semi colon
        popStack();

        return stateList;
    }


    private TreeNode parseCall() {
        TreeNode callNode = new TreeNode(CALL);
        TreeNode args = new TreeNode(ARGUMENTS);
        callNode.setC1(args);

        Token nextTok = getNextToken();
        Token nameTok;

        if(compareTypes(nextTok, Types.NAME)) {
            nameTok = nextTok;
            popAndPush();
            popStack();
            if (compareTypes(getNextToken(), Types.LEFTP)) {
                popAndPush();
                popStack();
                nextTok = getNextToken();

                while (!compareTypes(nextTok, Types.RIGHTP)) {

                    if (compareTypes(nextTok, Types.NAME)) {
                        String name = nextTok.val;
                        nextTok = getToken(1);

                        if(compareTypes(nextTok, Types.COM)) {
                            TreeNode paramSib = new TreeNode(nextTok.lineNum, name, VAR, 1);
                            args.setSibling(paramSib);
                            args = args.getSibling();

                            popAndPush(); // pop name
                            popStack();
                            popAndPush(); // pop comma
                            popStack();

                        } else if(compareTypes(nextTok, Types.RIGHTP)) {
                            TreeNode paramSib = new TreeNode(nextTok.lineNum, name, VAR, 1);
                            args.setSibling(paramSib);
                            args = args.getSibling();

                            popAndPush(); // pop name
                            popStack();

                        } else if (compareTypes(nextTok, Types.LEFTB)) {
                            popAndPush(); // pop name
                            popAndPush(); // pop left brack

                            while (!compareTypes(getNextToken(), Types.COM) && !compareTypes(getNextToken(), Types.RIGHTP)) {
                                popAndPush();
                            }

                            args.setSibling(parseArray(0));
                            args = args.getSibling();
                        } else {
                            return null;
                        }
                    } else if (compareTypes(nextTok, Types.NUM)) {
                        popAndPush();
                        nextTok = popStack(); // number

                        if(compareTypes(getNextToken(), Types.COM) ) {
                            TreeNode paramSib = new TreeNode(nextTok.lineNum, nextTok.val, INTEGER, 1);
                            args.setSibling(paramSib);
                            args = args.getSibling();

                            popAndPush(); // pop com
                            popStack();

                        } else if(compareTypes(getNextToken(),Types.RIGHTP)  ) {
                            TreeNode paramSib = new TreeNode(nextTok.lineNum, nextTok.val, INTEGER, 1);
                            args.setSibling(paramSib);
                            args = args.getSibling();

                        } else {
                            return null;
                        }



//                        TreeNode nodeArray = parseArrayDecNode();
//                        nextTok = popStack(); //pop off the comma
//                        if (compareTypes(nextTok, Types.COM)) {
//                            args.setSibling(nodeArray);
//                            args = args.getSibling();
//                            nextTok = popStack(); //get next value thats not comma
//                        }
//                        if (compareTypes(nextTok, Types.LEFTP) && stack.size() == 0) {
//                            args.setSibling(nodeArray);
//                            args = args.getSibling();
//                            return callNode;
//                        }
                    }
                    nextTok = getNextToken();
                }

            }
        }
        popAndPush(); // right parenthesis
        popStack();
        popAndPush(); // semi colon
        popStack();

        return callNode;
    }

    private TreeNode parseReturn() {
        TreeNode returnNode = new TreeNode(RETURN);

        Token nextToken = getNextToken();
        if(compareTypes(nextToken, Types.RETURN)) {
            popAndPush(); //pop return val
            popStack();

            TreeNode expr = parseExpr(0);
            returnNode.setC1(expr);

            popAndPush(); //pop semi colon on end
            popStack();
        }

        return returnNode;
    }

    /**
     * parses from the beginning of the if statement, nothing must be on the stack
     * if( condition ) {} in the token list
     * @return
     */
    private TreeNode parseIfState() {
        TreeNode ifState = new TreeNode(IF);

        Token nextToken = getNextToken();
        if(compareTypes(nextToken,Types.IF)) {
            if(compareTypes(getToken(1),Types.LEFTP)) {
                popAndPush(); // if statement
                popStack();
                popAndPush(); // left parenthesis
                popStack();

                TreeNode condition = parseExpr(0);
                ifState.setC1(condition);

                if(compareTypes(getNextToken(), Types.RIGHTP)) { // parse everything in semi colons
                    popAndPush(); //right parenthesis
                    popStack();
                    if (compareTypes(getNextToken(), Types.LEFTC)) {
                        popAndPush(); //left curly brack
                        popStack();
                        TreeNode trueState = parseFuncCompound();
                        ifState.setC2(trueState);
                        if (compareTypes(getNextToken(), Types.ELSE)) {
                            popAndPush(); // else
                            popStack();
                            if (compareTypes(getNextToken(), Types.LEFTC)) {
                                popAndPush(); //left curly
                                popStack();

                                TreeNode falseState = parseFuncCompound();
                                ifState.setC3(falseState);
                            }
                        }
                    } else { // parse one line (hacky way to do it)
                        try {
                            int i = 0;
                            Token semi = getToken(i);
                            while (!compareTypes(semi, Types.SEMI)) {
                                i++;
                                semi = getToken(i);
                            }
                            tokenList.add(i + 1, new Token(Types.RIGHTC)); // adds in curly bracket after one line
                            TreeNode trueState = parseFuncCompound();
                            ifState.setC2(trueState);
                            if (compareTypes(getNextToken(), Types.ELSE)) {
                                popAndPush(); // else
                                popStack();
                                i = 0;
                                semi = getToken(i);
                                while (!compareTypes(semi, Types.SEMI)) {
                                    i++;
                                    semi = getToken(i);
                                }
                                tokenList.add(i + 1, new Token(Types.RIGHTC));
                                TreeNode falseState = parseFuncCompound();
                                ifState.setC3(falseState);
                            }
                        } catch (Exception e) {
                            return ifState;
                        }
                    }
                }

            }
        }
        return ifState;
    }

    private TreeNode parseWhileState() {
        TreeNode whileState = new TreeNode(WHILE);

        Token nextToken = getNextToken();
        if(compareTypes(nextToken,Types.WHILE)) {
            if(compareTypes(getToken(1),Types.LEFTP)) {
                popAndPush(); // while statement
                popStack();
                popAndPush(); // left parenthesis
                popStack();

                TreeNode condition = parseExpr(0);
                whileState.setC1(condition);

                if(compareTypes(getNextToken(), Types.RIGHTP)) {
                    popAndPush(); //right parenthesis
                    popStack();
                    if (compareTypes(getNextToken(), Types.LEFTC)) {
                        popAndPush(); //left curly brack
                        popStack();
                        TreeNode innerWhile = parseFuncCompound();
                        whileState.setC2(innerWhile);
                    }
                }

            }
        }
        return whileState;
    }

    /**
     * parses expression "x = x;" where x is var, array or another expr
     * goes from left to right on the token
      * @return expr node
     */
    private TreeNode parseExpr(int i) {
        TreeNode expr = new TreeNode(EXPR);
        TreeNode e = expr;
        Token loopTok;
        TreeNode rightSide = null;
        TreeNode leftSide = null;
        Token operator = null;

        if(i == 1) {
            popStack();
        }
        if(leftSide == null) {
            if(compareTypes(tokenList.get(i+1), Types.LEFTB)) { // we have array dec
                popAndPush();
                loopTok = getNextToken();
                while(!compareTypes(loopTok, Types.RIGHTB)) {
                    popAndPush();
                    loopTok = getNextToken();
                }
                popAndPush();
                //full array should be on stack x[10] or x[var]
                leftSide = parseArray(0);
            } else if(compareTypes(tokenList.get(i), Types.NAME) || compareTypes(tokenList.get(i), Types.NUM)) {
                popAndPush(); // name on to stack
                loopTok = popStack();

                if(compareTypes(loopTok, Types.NUM)) {
                    leftSide = new TreeNode(loopTok.lineNum, Integer.parseInt(loopTok.val), null, INTEGER, 1);
                } else {
                    leftSide = new TreeNode(loopTok.lineNum, loopTok.val, VAR, 1);
                }
            } else {
                return null;
            }
            e.setC1(leftSide);
        }
        if (operator == null) {
            operator = getNextToken();
            if(compareTypes(operator, Types.ASSIGN) || compareTypes(operator,Types.EQ)  || compareTypes(operator, Types.NOTEQ) || compareTypes(operator, Types.DIV)|| compareTypes(operator, Types.PLUS)|| compareTypes(operator, Types.MINUS)|| compareTypes(operator, Types.MULT)||  compareTypes(operator, Types.DIV)|| compareTypes(operator, Types.LESS)|| compareTypes(operator, Types.LESSEQ)|| compareTypes(operator, Types.GREATEQ)|| compareTypes(operator, Types.GREAT)) {
                popAndPush();
                popStack();
                e.setsValue(operator.val);
                e.setnValue(EXPR);
                e.setTypeSpecifier(0);
                e.setLineNumber(operator.lineNum);
            } else {
                rightSide = leftSide;
                return rightSide;
            }
        }
        if (rightSide == null) {
            rightSide = parseExpr(0);
            e.setC2(rightSide);
            return expr;
        }
        return null;
    }

    /**
     * full array should be on stack x[10] or x[var]
     * param will pop semi colon off of variable
     * @param i
     * @return
     */
    private TreeNode parseArray(int i) {
        if(i == 1) {
            popStack(); //pop the semicolon
        }
        TreeNode array = new TreeNode(ARRAY);
        if(compareTypes(getTopStack(), Types.RIGHTB)) {
            Token innerBrack = getTopStack(1);
            if(compareTypes(innerBrack, Types.NUM) || compareTypes(innerBrack, Types.NAME)) {
                if(compareTypes(getTopStack(2), Types.LEFTB)) {
                    if(compareTypes(getTopStack(3), Types.NAME)) {
                        array.setTypeSpecifier(1);

                        if(compareTypes(innerBrack, Types.NAME)) {
                            TreeNode childNode = new TreeNode(innerBrack.lineNum, innerBrack.val, VAR, 1);
                            array.setC1(childNode);
                        } else {
                            array.setnValue(Integer.parseInt(innerBrack.val));
                        }
                        popStack(); // right brack
                        popStack(); // inner val
                        popStack(); // left brack
                        popStack(); // name
                        return array;
                    }
                }
            }
        }
        return null;
    }

    /**
     * parses "read x;" or "read[x];" on stack
     * @return readNode
     */
    private TreeNode parseRead() {
        TreeNode readNode = new TreeNode(READ);
        if(compareTypes(getNextToken(), Types.READ) ) {
            popAndPush();
            if(compareTypes(getNextToken(), Types.NAME)) {
                popAndPush();
                Token nTok = getTopStack();
                if(compareTypes(getNextToken(), Types.LEFTB )) { // we have array
                    while(!compareTypes(nTok,Types.SEMI)) {
                        nTok = getNextToken();
                        popAndPush();
                    }
                    TreeNode array = parseArray(1);
                    readNode.setC1(array);
                    return readNode;
                } else if(compareTypes(getNextToken(), Types.SEMI)) {
                    TreeNode name = new TreeNode(nTok.lineNum, nTok.val, VAR, 1);
                    popAndPush(); // semi
                    popStack(); // pop semi
                    popStack(); // name
                    popStack(); // read
                    readNode.setC1(name);
                    return readNode;
                }
            }
        }
        return null;


//            Token nameTok = getTopStack(1); // name
//            if(compareTypes(nameTok, Types.RIGHTB)) { // we have array
//                TreeNode array = parseArray(1);
//                readNode.setC1(array);
//                return readNode;
//            } else if(compareTypes(nameTok, Types.NAME)) { // we have NAME
//                if(compareTypes(getTopStack(2), Types.READ)) {
//                    TreeNode Name = new TreeNode(nameTok.lineNum, nameTok.val, VAR, 0);
//                    popStack(); // pop semi
//                    popStack(); // pop var name
//                    popStack(); // pop read
//                    readNode.setC1(Name);
//                    return readNode;
//                }
//            }

    }

    /**
     * parses "write x;" on stack
     * @return writeNode
     */
    private TreeNode parseWrite() {
        TreeNode writeNode = new TreeNode(WRITE);
        if(compareTypes(getNextToken(), Types.WRITE) ) {
            popAndPush();
            if (compareTypes(getNextToken(), Types.NAME)) {
                popAndPush();
                Token nTok = getTopStack();
                if (compareTypes(getNextToken(), Types.LEFTB)) { // we have array
                    while (!compareTypes(nTok, Types.SEMI)) {
                        nTok = getNextToken();
                        popAndPush();
                    }
                    TreeNode array = parseArray(1);
                    writeNode.setC1(array);
                    return writeNode;
                } else if (compareTypes(getNextToken(), Types.SEMI)) {
                    TreeNode name = new TreeNode(nTok.lineNum, nTok.val, VAR, 1);
                    popAndPush(); // semi
                    popStack(); // pop semi
                    popStack(); // name
                    popStack(); // read
                    writeNode.setC1(name);
                    return writeNode;
                }
            }
        }
//        if(compareTypes(getTopStack(), Types.SEMI) ) {
//            Token nameTok = getTopStack(1); // name
//            if(compareTypes(nameTok, Types.RIGHTB)) { // we have array
//                TreeNode array = parseArray(1);
//                readNode.setC1(array);
//                return readNode;
//            } else if(compareTypes(nameTok, Types.NAME)) { // we have NAME
//                if(compareTypes(getTopStack(2), Types.WRITE)) {
//                    TreeNode Name = new TreeNode(nameTok.lineNum, nameTok.val, VAR, 0);
//                    popStack(); // pop semi
//                    popStack(); // pop var name
//                    popStack(); // pop read
//                    readNode.setC1(Name);
//                    return readNode;
//                }
//            }
//        }
        return null;
    }

    /**
     * @param i is 1 when semi colon is present
     * parses "int x;" on stack or "int x"
     * @return intNode
     */
    private TreeNode parseIntDec(int i) {
        if( i == 1) {
            if (compareTypes(getTopStack(), Types.SEMI)) {
                Token nameTok = getTopStack(1); // name
                if (compareTypes(nameTok, Types.NAME)) { // we have NAME
                    if (compareTypes(getTopStack(2), Types.INT)) {
                        TreeNode intNode = new TreeNode(nameTok.lineNum, nameTok.val, VAR, 0);
                        popStack(); // pop semi
                        popStack(); // pop var name
                        popStack(); // pop read
                        return intNode;
                    }
                }
            }
        } else {
            Token nameTok = getTopStack(); // name
            if (compareTypes(nameTok, Types.NAME)) { // we have NAME
                if (compareTypes(getTopStack(1), Types.INT)) {
                    TreeNode intNode = new TreeNode(nameTok.lineNum, nameTok.val, VAR, 0);
                    popStack(); // pop var name
                    popStack(); // pop read
                    return intNode;
                }
            }
        }
        return null;
    }

    private TreeNode parseFuncNodeParams() {
        TreeNode paramList = new TreeNode(PARAMETER_LIST);
        TreeNode pNode = paramList;

        Token nextTok = getTopStack();
        paramList.setLineNumber(nextTok.lineNum);
        // options are VOID, int a, or int a[]
        if(compareTypes(nextTok, Types.VOID)) {
            paramList.setTypeSpecifier(0);
            popStack(); // pop void
            popStack(); // pop left parenthesis off stack
            return paramList;
        }
        while (!compareTypes(nextTok, Types.LEFTP)) {
            if(compareTypes(nextTok, Types.NAME)) {
                String name = nextTok.val;
                nextTok = popStack();
                if(compareTypes(nextTok, Types.INT)) {
                    TreeNode paramSib = new TreeNode(nextTok.lineNum, name, VAR, 1);
                    nextTok = popStack(); //pop off the comma
                    if(compareTypes(nextTok, Types.COM))  {
                        pNode.setSibling(paramSib);
                        pNode = pNode.getSibling();
                        nextTok = getTopStack(); //get next value thats not comma
                    }
                    if (compareTypes(nextTok, Types.LEFTP) && stack.size() == 0){
                        pNode.setSibling(paramSib);
                        pNode = pNode.getSibling();
                        return paramList;
                    }
                }
            } else if (compareTypes(nextTok, Types.RIGHTB)) {
                TreeNode nodeArray = parseArrayDecNode();
                nextTok = popStack(); //pop off the comma
                if(compareTypes(nextTok, Types.COM)) {
                    pNode.setSibling(nodeArray);
                    pNode = pNode.getSibling();
                    nextTok = popStack(); //get next value thats not comma
                }
                if (compareTypes(nextTok, Types.LEFTP) && stack.size() == 0){
                    pNode.setSibling(nodeArray);
                    pNode = pNode.getSibling();
                    return paramList;
                }
            } else {
                System.out.println("not an array or variable");
                return null;
            }
        }
        return paramList;
    }

    private TreeNode parseArrayDecNode() {
        popStack(); // right bracket
        Token numberTok = popStack(); //number or variable Tok or Left Bracket

        TreeNode retNode;
        retNode = new TreeNode(ARRAY);
        retNode.setLineNumber(numberTok.lineNum);
        retNode.setTypeSpecifier(1);

        if(compareTypes(numberTok, Types.NUM)) { // int x[10]
            retNode.setnValue(Integer.parseInt(numberTok.val));
        } else if (compareTypes(numberTok, Types.LEFTB)) { // int x[]
            Token nameTok = popStack(); // name val
            popStack(); // pop off the int
            retNode.setsValue(nameTok.val);
            return retNode;
        } else if (compareTypes(numberTok, Types.NAME)) { // int x[name]
            TreeNode childNode = new TreeNode(numberTok.lineNum, numberTok.val, VAR, 1);
            retNode.setC1(childNode);
        }

        popStack(); // left bracket
        Token nameTok = popStack(); // name value
        popStack(); // pop off int

        retNode.setsValue(nameTok.val);
        return retNode;
    }

    private boolean compareTypes (Token tok, Types t) {
        if(tok.type.equals(t)) {
            return true;
        }
        return false;
    }

    /**
     * returns first in line of tokenList
     * @return
     */
    private Token getNextToken() {
        return tokenList.get(0);
    }

    private Token getToken(int i) {
        return tokenList.get(i);
    }

    private void popAndPush() {
        Token firstTok = tokenList.remove(0);
        stack.add(firstTok);
    }

    private Token getTopStack() {
        return stack.get(stack.size()-1);
    }


    private Token getTopStack(int i) {
        return stack.get(stack.size()-(1 + i));
    }

    private Token popStack() {
        return stack.remove(stack.size()-1);
    }

}
