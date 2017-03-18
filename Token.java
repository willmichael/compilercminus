package com.compiler;

/**
 * Created by WillMichael on 3/16/17.
 */
public class Token {
    public String val;
    public Types type;
    public int lineNum;

    public Token () {
        type = Types.ERR;
        val = "";
        lineNum = 0;
    }

    public Token (Types aType, String aVal, int aln) {
        type = aType;
        val = aVal;
        lineNum = aln;
    }

    public Token (Types atype) {
        type = atype;
        val = "";
        lineNum = -1;
    }



    public String printToken() {
        return val;
    }
}

