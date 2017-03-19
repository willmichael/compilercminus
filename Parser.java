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
        parseStateProgram();
        System.out.println(this.program);


        int i = 0;
        return;
    }

    private TreeNode parseStateProgram() {
        while(tokenList.size() != 0 ) {
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
                        parseFunc();
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
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
        return null;
    }

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

    private TreeNode parseFuncStateList() {
        TreeNode stateList = new TreeNode(STATEMENT_LIST);
        //siblings expr, compound, if, while, return, read, write, call
        //expr, read, write, are done
        //if, while next
        //then return call
//        stateList.setSibling(parseExpr());
        parseIfState();


        return stateList;
    }

    /**
     * parses from the beginning of the if statement, nothing must be on the stack
     * if( condition ) {} in the token list
     * @return
     */
    private TreeNode parseIfState() {
        TreeNode ifState = new TreeNode(IF);

        Token nextToken = getNextToken();
        nextToken = getNextToken();
        if(compareTypes(nextToken,Types.IF)) {
            if(compareTypes(getToken(1),Types.LEFTP)) {
                popAndPush(); // if statement
                popStack();
                popAndPush(); // left parenthesis
                popStack();

                TreeNode condition = parseExpr(0);
                ifState.setC1(condition);
            }
        }
        return ifState;
    }

    private TreeNode parseWhileState() {
        TreeNode whileState = new TreeNode(WHILE);

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
            if(compareTypes(operator, Types.ASSIGN) || compareTypes(operator, Types.DIV)|| compareTypes(operator, Types.PLUS)|| compareTypes(operator, Types.MINUS)|| compareTypes(operator, Types.MULT)||  compareTypes(operator, Types.DIV)|| compareTypes(operator, Types.LESS)|| compareTypes(operator, Types.LESSEQ)|| compareTypes(operator, Types.GREATEQ)|| compareTypes(operator, Types.GREAT)) {
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
        if(compareTypes(getTopStack(), Types.SEMI) ) {
            Token nameTok = getTopStack(1); // name
            if(compareTypes(nameTok, Types.RIGHTB)) { // we have array
                TreeNode array = parseArray(1);
                readNode.setC1(array);
                return readNode;
            } else if(compareTypes(nameTok, Types.NAME)) { // we have NAME
                if(compareTypes(getTopStack(2), Types.READ)) {
                    TreeNode Name = new TreeNode(nameTok.lineNum, nameTok.val, VAR, 0);
                    popStack(); // pop semi
                    popStack(); // pop var name
                    popStack(); // pop read
                    readNode.setC1(Name);
                    return readNode;
                }
            }
        }
        return null;
    }

    /**
     * parses "write x;" on stack
     * @return writeNode
     */
    private TreeNode parseWrite() {
        TreeNode readNode = new TreeNode(WRITE);
        if(compareTypes(getTopStack(), Types.SEMI) ) {
            Token nameTok = getTopStack(1); // name
            if(compareTypes(nameTok, Types.RIGHTB)) { // we have array
                TreeNode array = parseArray(1);
                readNode.setC1(array);
                return readNode;
            } else if(compareTypes(nameTok, Types.NAME)) { // we have NAME
                if(compareTypes(getTopStack(2), Types.WRITE)) {
                    TreeNode Name = new TreeNode(nameTok.lineNum, nameTok.val, VAR, 0);
                    popStack(); // pop semi
                    popStack(); // pop var name
                    popStack(); // pop read
                    readNode.setC1(Name);
                    return readNode;
                }
            }
        }
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
//        popAndPush();

        TreeNode paramList = new TreeNode(PARAMETER_LIST);
        TreeNode pNode = paramList;

//        while (compareTypes(nextTok, Types.VOID) || compareTypes(nextTok, Types.INT) || compareTypes(nextTok, Types.COM) || compareTypes(nextTok, Types.NAME) || compareTypes(nextTok, Types.NUM) || compareTypes(nextTok, Types.)) {
//        while(!compareTypes(nextTok, Types.RIGHTP)) {
//            popAndPush();
//            nextTok = getNextToken();
//        }

        Token nextTok = getTopStack();
        paramList.setLineNumber(nextTok.lineNum);
        // options are VOID, int a, or int a[]
        if(compareTypes(nextTok, Types.VOID)) {
            paramList.setTypeSpecifier(0);
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
                        nextTok = popStack(); //get next value thats not comma
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
                System.out.println("why are we here");
                return null;
            }
        }
        return paramList;
    }

    private TreeNode parseIntDecNode() {
        Token nextTok = getTopStack(); // name
        if (compareTypes(nextTok, Types.NAME)) { // we have an int
            //add sibling
            popStack(); // pop off name
            popStack(); // pop off int
            TreeNode intNode = new TreeNode(nextTok.lineNum, nextTok.val, VAR, 1);
            return intNode;
        } else {
            return null;
        }
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
