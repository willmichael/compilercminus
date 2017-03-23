package com.compiler;

import javax.sound.midi.SysexMessage;

/**
 * Created by WillMichael on 3/17/17.
 *
 * Taken from AST Specifications given by Broeg
 */
public class TreeNode extends nType {
    private int lineNumber; //Line in program where this construct is found
    private int nValue; //Numerical value of a number
    private String sValue; //Lexeme or string value of an identifier
    private int nodeType; //PROGRAM, DECLARATION, etc.
    private int typeSpecifier; //VOID or INT
    private String rename; //Used by the Semantic Analyzer
    private boolean visited; //Initialized to false, used for traversals

    private TreeNode C1; //Pointer to Child 1
    private TreeNode C2; //Pointer to Child 2
    private TreeNode C3; //Pointer to Child 3
    private TreeNode sibling; //Pointer to Sibling

    public TreeNode() {
        this.lineNumber = -1;
        this.nValue = -1;
        this.sValue = "";
        this.nodeType = -1;
        this.typeSpecifier = -1;
        this.rename = "";
        this.visited = false;

        this.C1 = null;
        this.C2 = null;
        this.C3 = null;
        this.sibling = null;
    }

    public void printNode(TreeNode val, int i) {
        for (int j = 0; j < i; j++) {
            System.out.print("\t");
        }
        switch (val.nodeType) {
            case 0: {
                System.out.println("Node Type: Program");
                break;
            }
            case 1: {
                System.out.println("Node Type: Variable");
                break;
            }
            case 2: {
                System.out.println("Node Type: Array");
                break;
            }
            case 3: {
                System.out.println("Node Type: Function");
                break;
            }
            case 4: {
                System.out.println("Node Type: Compound");
                break;
            }
            case 5: {
                System.out.println("Node Type: Parameter List");
                break;
            }
            case 6: {
                System.out.println("Node Type: Declarations");
                break;
            }
            case 7: {
                System.out.println("Node Type: Statement List");
                break;
            }
            case 8: {
                System.out.println("Node Type: If");
                break;
            }
            case 9: {
                System.out.println("Node Type: While");
                break;
            }
            case 10: {
                System.out.println("Node Type: Return");
                break;
            }
            case 11: {
                System.out.println("Node Type: Read");
                break;
            }
            case 12: {
                System.out.println("Node Type: Write");
                break;
            }
            case 13: {
                System.out.println("Node Type: Call");
                break;
            }
            case 14: {
                System.out.println("Node Type: Equality");
                break;
            }
            case 15: {
                System.out.println("Node Type: Addition");
                break;
            }
            case 16: {
                System.out.println("Node Type: Subtraction");
                break;
            }
            case 17: {
                System.out.println("Node Type: Multiplication");
                break;
            }
            case 18: {
                System.out.println("Node Type: Division");
                break;
            }
            case 19: {
                System.out.println("Node Type: Expression");
                break;
            }
            case 20: {
                System.out.println("Node Type: Integer");
                break;
            }
            case 21: {
                System.out.println("Node Type: Arguments");
                break;
            }

        }
        for (int j = 0; j < i; j++) {
            System.out.print("\t");
        }
        System.out.println("line number = " + String.valueOf(val.getLineNumber()));
        for (int j = 0; j < i; j++) {
            System.out.print("\t");
        }
        System.out.println("nValue = " + String.valueOf(val.getnValue()));
        for (int j = 0; j < i; j++) {
            System.out.print("\t");
        }
        System.out.println("sValue = " + (val.getsValue()));
        for (int j = 0; j < i; j++) {
            System.out.print("\t");
        }
        System.out.println("Type Specifier = " +  String.valueOf(val.getTypeSpecifier()));

        System.out.println();
        if(val.getC1() != null) {
            for (int j = 0; j < i+1; j++) {
                System.out.print("\t");
            }
            System.out.println("C1");
            val.printNode(val.getC1(), i + 1);
        }
        if(val.getC2() != null) {
            for (int j = 0; j < i+1; j++) {
                System.out.print("\t");
            }
            System.out.println("C2");
            val.printNode(val.getC2(), i + 1);

        }
        if(val.getC3() != null) {
            for (int j = 0; j < i+1; j++) {
                System.out.print("\t");
            }
            System.out.println("C3");
            val.printNode(val.getC3(), i + 1);

        }
        if(val.getSibling() != null) {
            for (int j = 0; j < i; j++) {
                System.out.print("\t");
            }
            System.out.println("Sibling");
            val.printNode(val.getSibling(), i);
        }
    }

    public TreeNode(int nodeType) {
        this.nodeType = nodeType;
    }

    public TreeNode(int lineNumber, String sValue, int nodeType, int typeSpecifier) {
        this.lineNumber = lineNumber;
        this.sValue = sValue;
        this.nodeType = nodeType;
        this.typeSpecifier = typeSpecifier;
    }

    public TreeNode(int lineNumber, int nValue, String sValue, int nodeType, int typeSpecifier) {
        this.lineNumber = lineNumber;
        this.nValue = nValue;
        this.sValue = sValue;
        this.nodeType = nodeType;
        this.typeSpecifier = typeSpecifier;
    }

    public TreeNode(int lineNumber, int nValue, String sValue, int nodeType, int typeSpecifier, String rename, boolean visited, TreeNode c1, TreeNode c2, TreeNode c3, TreeNode sibling) {
        this.lineNumber = lineNumber;
        this.nValue = nValue;
        this.sValue = sValue;
        this.nodeType = nodeType;
        this.typeSpecifier = typeSpecifier;
        this.rename = rename;
        this.visited = visited;
        C1 = c1;
        C2 = c2;
        C3 = c3;
        this.sibling = sibling;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getnValue() {
        return nValue;
    }

    public String getsValue() {
        return sValue;
    }

    public int getNodeType() {
        return nodeType;
    }

    public int getTypeSpecifier() {
        return typeSpecifier;
    }

    public String getRename() {
        return rename;
    }

    public boolean isVisited() {
        return visited;
    }

    public TreeNode getC1() {
        return C1;
    }

    public TreeNode getC2() {
        return C2;
    }

    public TreeNode getC3() {
        return C3;
    }

    public TreeNode getSibling() {
        return sibling;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setnValue(int nValue) {
        this.nValue = nValue;
    }

    public void setsValue(String sValue) {
        this.sValue = sValue;
    }

    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }

    public void setTypeSpecifier(int typeSpecifier) {
        this.typeSpecifier = typeSpecifier;
    }

    public void setRename(String rename) {
        this.rename = rename;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public void setC1(TreeNode c1) {
        C1 = c1;
    }

    public void setC2(TreeNode c2) {
        C2 = c2;
    }

    public void setC3(TreeNode c3) {
        C3 = c3;
    }

    public void setSibling(TreeNode sibling) {
        this.sibling = sibling;
    }



}
