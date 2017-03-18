package com.compiler;

import apple.laf.JRSUIUtils;

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
                        popStack(); // remove semi

                        curNode.setSibling(parseIntDecNode());
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

        popAndPush(); // pop left { onto stack
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
        //sibling dec variable or array nodes
        getNextToken();
        return dec;
    }

    private TreeNode parseFuncStateList() {
        TreeNode stateList = new TreeNode(STATEMENT_LIST);
        //siblings compound, if, while, return, read, write, call
        return stateList;
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
        Token nextTok = popStack(); // name
        if (compareTypes(nextTok, Types.NAME)) { // we have an int
            //add sibling
            TreeNode tempNode = new TreeNode(nextTok.lineNum, nextTok.val, VAR, 1);
            return tempNode;
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

        retNode.setsValue(nameTok.val);
        return retNode;
    }

    private boolean compareTypes (Token tok, Types t) {
        if(tok.type.equals(t)) {
            return true;
        }
        return false;
    }

    private Token getNextToken() {
        return tokenList.get(0);
    }

    private void popAndPush() {
        Token firstTok = tokenList.remove(0);
        stack.add(firstTok);
    }

    private Token getTopStack() {
        return stack.get(stack.size()-1);
    }

    private Token popStack() {
        return stack.remove(stack.size()-1);
    }
}
