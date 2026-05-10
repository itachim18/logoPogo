package logoPogo.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Lexer {

    private static final Map<String, TokenType> KEYWORDS = Map.of(
            "TO", TokenType.TO,
            "END", TokenType.END,
            "IF", TokenType.IF,
            "IFELSE", TokenType.IFELSE,
            "REPEAT", TokenType.REPEAT,
            "MAKE", TokenType.MAKE,
            "OUTPUT", TokenType.OUTPUT,
            "OP", TokenType.OUTPUT,
            "STOP", TokenType.STOP
    );

    private final String src;
    private int pos=0;
    private int line=0;
    private int lineStart=0;

    public Lexer(String src) {
        this.src = src;
    }

    public List<Token> tokenize(){

        List<Token> out = new ArrayList<>();
        while(pos<src.length()){
            char c = src.charAt(pos);
            if(c=='\n'){
                line++;
                pos++;
                lineStart=pos;
                continue;
            }
            if (Character.isWhitespace(c)) { pos++; continue; }
            if (c == ';') { readComment(out); continue; }
            if (c == '[') { addSingle(out, TokenType.LBRACKET); continue; }
            if (c == ']') { addSingle(out, TokenType.RBRACKET); continue; }
            if (c == '(') { addSingle(out, TokenType.LPAREN); continue; }
            if (c == ')') { addSingle(out, TokenType.RPAREN); continue; }
            if (c == '+') { addSingle(out, TokenType.PLUS); continue; }
            if (c == '*') { addSingle(out, TokenType.STAR); continue; }
            if (c == '/') { addSingle(out, TokenType.SLASH); continue; }
            if (c == '=') { addSingle(out, TokenType.EQ); continue; }
            if (c == '<') { readLtOrLe(out); continue; }
            if (c == '>') { readGtOrGe(out); continue; }
            if (c == '-') { addSingle(out, TokenType.MINUS); continue; }
            if (c == '"') { readStringLiteral(out); continue; }
            if (c == ':') { readVarRef(out); continue; }
            if (Character.isDigit(c)) { readNumber(out); continue; }
            if (isIdentStart(c)) { readIdent(out); continue; }

            // Unknown — emit and advance (error recovery)
            out.add(new Token(TokenType.UNKNOWN, String.valueOf(c),
                    line, pos - lineStart, 1));
            pos++;

        }
        out.add(new Token(TokenType.EOF, "", line, pos - lineStart, 0));
        return out;


    }

    private void addSingle(List<Token> out, TokenType t) {
        out.add(new Token(t, String.valueOf(src.charAt(pos)),line, pos-lineStart,1));
    pos++;
    }

    private void readLtOrLe(List<Token> out) {
        int col = pos-lineStart;
        if(pos +1<src.length() && src.charAt(pos+1) == '='){
            out.add(new Token(TokenType.LE,"<=",line, col,2));
            pos=pos+2;
        }else{
            out.add(new Token(TokenType.LT,"<",line, col,1));
            pos++;
        }
    }

    private void readGtOrGe(List<Token> out) {
        int col = pos-lineStart;
        if(pos +1<src.length() && src.charAt(pos+1) == '='){
            out.add(new Token(TokenType.GE,">=",line, col,2));
            pos=pos+2;
        }else{
            out.add(new Token(TokenType.GT,">",line, col,1));
        }
    }

    private void readComment(List<Token> out) {
        int start =pos, col=pos-lineStart;
        while(pos<src.length() && src.charAt(pos)!='\n'){
            pos++;
        }
        out.add(new Token(TokenType.COMMENT, src.substring(start, pos), line, col, pos-start));
    }

    private void readStringLiteral(List<Token> out){
        int start = pos, col = pos-lineStart;
        pos++;
        while(pos<src.length() && !Character.isWhitespace(src.charAt(pos)) && "[]()".indexOf(src.charAt(pos)) < 0)
        {
            pos++; //funny one line hahahaha, loser loner
        }
        out.add(new Token(TokenType.STRING_LITERAL,src.substring(start,pos),line,col,pos-start));
    }

    private void readVarRef(List<Token> out){
        int start = pos, col=pos-lineStart;
        pos++;
        while(pos<src.length() && isIdentPart(src.charAt(pos))) pos++;
        out.add(new Token(TokenType.VAR_REF,src.substring(start,pos),line,col,pos-start));
    }

    private void readNumber(List<Token> out){
        int start = pos, col=pos- lineStart;

        while(pos<src.length() && Character.isDigit(src.charAt(pos))){
            pos++; //funny one line hahahaha, loser loner
        }
        if(pos<src.length() && src.charAt(pos) == '.'){
            pos++;//ok now its not that funny
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) {
                pos++;
            }
        }
        out.add(new Token(TokenType.NUMBER,src.substring(start,pos),line,col,pos-start));

    }

    private void readIdent(List<Token> out){
        int start = pos, col=pos - lineStart;
        while(pos<src.length() && isIdentPart(src.charAt(pos))){
            pos++;//repetitive but i like it being in paranthesis
        }
        String text=src.substring(start,pos);
        TokenType type = KEYWORDS.getOrDefault(text.toUpperCase(), TokenType.IDENT);
        out.add(new Token(type,text,line,col,pos-start));
    }

    private static boolean isIdentStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '.' || c == '?';
    }
    private static boolean isIdentPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '?';
    }




}
