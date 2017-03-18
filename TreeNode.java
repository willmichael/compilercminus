package com.compiler;

/**
 * Created by WillMichael on 3/17/17.
 *
 * Taken from AST Specifications given by Broeg
 */
public class TreeNode {
    public int lineNumber; //Line in program where this construct is found
    public int nValue; //Numerical value of a number
    public String sValue; //Lexeme or string value of an identifier
    public int nodeType; //PROGRAM, DECLARATION, etc.
    public int typeSpecifier; //VOID or INT
    public String rename; //Used by the Semantic Analyzer
    public boolean visited; //Initialized to false, used for traversals

    public TreeNode C1; //Pointer to Child 1
    public TreeNode C2; //Pointer to Child 2
    public TreeNode C3; //Pointer to Child 3
    public TreeNode sibling; //Pointer to Sibling
}
