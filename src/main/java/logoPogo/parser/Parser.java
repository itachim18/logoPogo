package logoPogo.parser;

import logoPogo.model.Ast;

import java.util.ArrayList;
import java.util.List;

public final class Parser {

    private final List<Token> tokens;
    private int i=0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Ast.Program parse(){
        Ast.Program p = new Ast.Program();
        while(!eof()){
            Token t=peek();
            if(t.type()==TokenType.TO){
                parseProcedure(p);
            } else if (t.type()==TokenType.VAR_REF) {
                p.varRefs.add(new Ast.VarRef(t,null));
                i++;
            } else if (t.type()==TokenType.IDENT) {
                p.procRef.add(new Ast.ProcRef(t));
                i++;
            }else{
                i++; //dont give up
            }
        }
        return p;
    }

    private void parseProcedure(Ast.Program p){
        Token toTok=consume();
        Token name = (peek().type()==TokenType.IDENT)? consume() : null;
        List<Token> params = new ArrayList<>();
        while (peek().type() == TokenType.VAR_REF){
            params.add(consume());
        }
        Ast.Procedure proc = new Ast.Procedure(toTok,name,params);
        p.Procedure.add(proc);
    }


    private Token peek() {
        return tokens.get(i);
    }
    private boolean eof() {
        return peek().type() == TokenType.EOF;
    }

    private Token consume() {
        return tokens.get(i++);
    }

}
