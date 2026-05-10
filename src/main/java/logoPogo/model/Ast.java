package logoPogo.model;

import logoPogo.parser.Token;
import org.eclipse.lsp4j.Command;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public final class Ast {

    public static final class Program{
        public final List<Procedure> Procedure = new ArrayList<>();
        public final List<VarRef> varRefs = new ArrayList<>();
        public final List<ProcRef> procRef = new ArrayList<>();
    }

    public static final class Procedure{
        public final Token nameToken;
        public final String name;
        public final List<Token> params;
        public final Token toToken;
        public Token endToken;

        public Procedure(Token toToken, Token nameToken, List<Token> params) {
            this.toToken = toToken;
            this.nameToken = nameToken;
            this.name = nameToken == null ? "" : nameToken.text().toUpperCase();
            this.params = params;
        }


    }

    public static final class ProcRef{
        public final Token token;
        public final String name;
        public ProcRef(Token t){
            this.token = t;
            this.name = t.text().toUpperCase();
        }
    }
    public static final class VarRef{
        public final Token token;
        public final String name;
        public final Procedure enclosing;
        public VarRef(Token t, Procedure enclosing){
            this.token = t;
            this.name = t.text().toUpperCase();
            this.enclosing = enclosing;
        }
    }


}
