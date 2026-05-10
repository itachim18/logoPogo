package logoPogo;

import logoPogo.model.Ast;
import logoPogo.model.SymbolIndex;
import logoPogo.parser.Lexer;
import logoPogo.parser.Parser;
import logoPogo.parser.Token;

import java.util.List;

public class DocumentModel {

    private String text;
    private List<Token> tokens;
    private Ast.Program ast;
    private SymbolIndex index;
    private Ast.Program lastGoodAst; //last know good halka aur reilable\
    private SymbolIndex lastGoodIndex;


    public DocumentModel(String text) {
        update(text);
    }

    public synchronized void update(String newText){
        this.text = newText;
        this.tokens = new Lexer(newText).tokenize();
        Ast.Program p = new Parser(tokens).parse();
        this.ast = p;
        this.index = SymbolIndex.from(p);
        if (!p.Procedure.isEmpty() || !p.procRef.isEmpty() || !p.varRefs.isEmpty()) {
            this.lastGoodAst = p;
            this.lastGoodIndex = index;
        }

    }

    public String text(){ 
        return text;
    }
    public List<Token> tokens(){
        return tokens;
    }
    public Ast.Program ast(){
        return ast;
    }
    public SymbolIndex index(){
        return index;
    }
    public Ast.Program lastGoodAst() {
        return lastGoodAst != null ? lastGoodAst : ast;
    }
    public SymbolIndex lastGoodIndex(){
        return lastGoodIndex != null ? lastGoodIndex : index;
    }






}
