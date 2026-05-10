package logoPogo.parser;

public record Token (
    TokenType type, String text,
    int line, int column, int length
) {
    public int endColumn() {
        return column + length;
    }
}