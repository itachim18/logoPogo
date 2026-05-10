# logoPogo
BEEP BEEP BOOP BO !ERROR CAN'T AUTOCOMPLETE
# LOGO Language Server

A Java LSP server for the LOGO programming language, built with LSP4J 0.24.0.

## Features

- **Syntax highlighting** for keywords (`TO`, `END`, `REPEAT`, `IF`, `IFELSE`,
  `MAKE`, `OUTPUT`, `STOP`), numbers, strings (`"foo`), variables (`:x`),
  parameters, operators, brackets, parentheses and comments (`;`) — delivered
  via LSP Semantic Tokens.
- **Go-to-declaration / go-to-definition** for
  - user-defined procedures (`TO square ... END` → any `square` call)
  - variable references (`:size` → the `:size` parameter of the enclosing
    `TO` procedure, with fallback to any parameter of the same name).

## Design highlights

- **Error-tolerant parser**: unknown characters become `UNKNOWN` tokens,
  unterminated procedures are still recorded, and the lexer/parser never throw.
- **Split syntax vs semantics**: semantic tokens are driven by the raw token
  stream, so highlighting works even if the AST is incomplete.
- **Double-buffering**: each document keeps a "last known good" AST & symbol
  index, so go-to-definition still works while you're mid-edit with broken code.
- **Relative token positions**: tokens store `(line, column, length)` relative
  to their line; this makes future incremental strategies straightforward.
