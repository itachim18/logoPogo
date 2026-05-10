package logoPogo;

import logoPogo.model.Ast;
import logoPogo.model.SymbolIndex;
import logoPogo.parser.Token;
import logoPogo.parser.TokenType;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LogoTextDocumentService implements TextDocumentService {


    private final Map<String, DocumentModel> docs = new HashMap<>();
    @SuppressWarnings("unused")
    private LanguageClient client;
    void connect(LanguageClient c ){
        this.client = c;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        docs.put(params.getTextDocument().getUri(),new DocumentModel(params.getTextDocument().getText()));
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params){
        DocumentModel d = docs.get(params.getTextDocument().getUri());
        if(d==null){
            return;
        }
        for(TextDocumentContentChangeEvent e : params.getContentChanges()){
            d.update(e.getText());
        }
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        docs.remove(params.getTextDocument().getUri());
    }
    @Override
    public void didSave(DidSaveTextDocumentParams params) {
    }

    private static final int T_KEYWORD = 0, T_FUNCTION = 1, T_PARAMETER = 2,
            T_VARIABLE = 3, T_NUMBER = 4, T_STRING = 5,
            T_OPERATOR = 6, T_COMMENT = 7;

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        DocumentModel d = docs.get(params.getTextDocument().getUri());
        if (d == null) {
            return CompletableFuture.completedFuture(new SemanticTokens(List.of()));
        }

        Set<String> paramNames=new HashSet<>();
        for(Ast.Procedure proc : d.ast().Procedure){
            for(Token p : proc.params){
                paramNames.add(p.text().substring(1).toUpperCase());
            }
        }

        List<int[]> raw = new ArrayList<>();
        for(Token t : d.tokens()){
            int kind = classify(t,d.ast(),paramNames);
            if(kind<0){
                continue;
            }
            raw.add(new int[]{
                    t.line(),t.column(),t.length(),kind,0
            });
        }
        raw.sort((a, b) -> a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(a[1], b[1]));
        List<Integer> data=new ArrayList<>(raw.size()*5);
        int prevLine=0,prevCol=0;

        for(int[] r:raw){
            int dLine=r[0]-prevLine;
            int dCol= dLine == 0 ? r[1]-prevCol : r[1];
            data.add(dLine);
            data.add(dCol);
            data.add(r[2]);
            data.add(r[3]);
            data.add(r[4]);
            prevLine=r[0];
            prevCol=r[1];

        }
        return CompletableFuture.completedFuture(new SemanticTokens(data));


    }

    private int classify(Token t, Ast.Program ast, Set<String> paramNames){
        return switch(t.type()){
            case TO, END, IF, IFELSE, MAKE, REPEAT, OUTPUT, STOP->T_KEYWORD;
            case NUMBER-> T_NUMBER;
            case STRING_LITERAL -> T_STRING;
            case COMMENT -> T_COMMENT;
            case VAR_REF -> paramNames.contains(t.text().substring(1).toUpperCase())?T_PARAMETER:T_VARIABLE;
            case PLUS,MINUS,STAR,SLASH,EQ,LT,GT,LE,GE,NE -> T_OPERATOR;
            case IDENT -> isProcedureName(t,ast)?T_FUNCTION:T_FUNCTION;
            default -> -1;


        };
    }
    private boolean isProcedureName(Token t, Ast.Program ast){
        return true; // CHANGE IT LATER ALERT ALERT
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
    declaration(DeclarationParams params) {
        return resolveDefinition(params.getTextDocument().getUri(), params.getPosition());
    }
    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
    definition(DefinitionParams params) {
        return resolveDefinition(params.getTextDocument().getUri(), params.getPosition());
    }
    private CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
    resolveDefinition(String uri, Position pos) {
        DocumentModel d = docs.get(uri);
        if (d == null) return CompletableFuture.completedFuture(Either.forLeft(List.of()));

        Token hit = tokenAt(d, pos);
        if (hit == null) return CompletableFuture.completedFuture(Either.forLeft(List.of()));

        List<Location> live = findDefinition(hit, d.ast(), d.index(), uri);
        if (!live.isEmpty())
            return CompletableFuture.completedFuture(Either.forLeft(live));
        List<Location> fallback = findDefinition(hit, d.lastGoodAst(), d.lastGoodIndex(), uri);
        return CompletableFuture.completedFuture(Either.forLeft(fallback));
    }

    private List<Location> findDefinition(Token hit, Ast.Program ast, SymbolIndex idx,
                                          String uri) {
        if (hit.type() == TokenType.IDENT) {
            var proc = idx.procedures.get(hit.text().toUpperCase());
            if (proc != null && proc.nameToken != null)
                return List.of(new Location(uri, rangeOf(proc.nameToken)));
        } else if (hit.type() == TokenType.VAR_REF) {
            String name = hit.text().substring(1).toUpperCase();
            // Find enclosing procedure by line range.
            Ast.Procedure enclosing = enclosingProcedureOf(hit, ast);
            if (enclosing != null) {
                for (Token p : enclosing.params) {
                    if (p.text().substring(1).equalsIgnoreCase(name))
                        return List.of(new Location(uri, rangeOf(p)));
                }
            }
            for (Ast.Procedure pr : ast.Procedure)
                for (Token p : pr.params)
                    if (p.text().substring(1).equalsIgnoreCase(name))
                        return List.of(new Location(uri, rangeOf(p)));
        }
        return List.of();
    }

    private Ast.Procedure enclosingProcedureOf(Token t, Ast.Program ast) {
        for (Ast.Procedure proc : ast.Procedure) {
            int startLine = proc.toToken.line();
            int endLine = proc.endToken != null ? proc.endToken.line() : Integer.MAX_VALUE;
            if (t.line() >= startLine && t.line() <= endLine) return proc;
        }
        return null;
    }

    private Token tokenAt(DocumentModel d, Position pos) {
        for (Token t : d.tokens()) {
            if (t.line() == pos.getLine()
                    && pos.getCharacter() >= t.column()
                    && pos.getCharacter() <= t.endColumn()
                    && t.type() != TokenType.EOF) {
                return t;
            }
        }
        return null;
    }

    private static Range rangeOf(Token t) {
        return new Range(
                new Position(t.line(), t.column()),
                new Position(t.line(), t.endColumn()));
    }









}
