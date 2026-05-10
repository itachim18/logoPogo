package logoPogo;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class LogoLanguageServer implements LanguageServer, LanguageClientAware {

    private final LogoTextDocumentService textDocumentService = new LogoTextDocumentService();
    private final LogoWorkspaceService workspaceService = new LogoWorkspaceService();
    private LanguageClient client;

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params){
        ServerCapabilities cap = new ServerCapabilities();
        cap.setTextDocumentSync(TextDocumentSyncKind.Full);
        cap.setDeclarationProvider(true);
        cap.setDefinitionProvider(true);

        SemanticTokensLegend legend = new SemanticTokensLegend(
                List.of("keyword", "function", "parameter", "variable",
                        "number", "string", "operator", "comment"),
                List.of());

        SemanticTokensWithRegistrationOptions sem = new SemanticTokensWithRegistrationOptions(legend);
        sem.setFull(true);
        sem.setRange(false);
        cap.setSemanticTokensProvider(sem);

        return CompletableFuture.completedFuture(new InitializeResult(cap));


    }
    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(new Object());
    }

    @Override public void exit(){
        System.exit(0);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }
    @Override
    public WorkspaceService getWorkspaceService()   {
        return workspaceService;
    }

    @Override public void connect(LanguageClient client) {
        this.client = client;
        textDocumentService.connect(client);
    }






}
