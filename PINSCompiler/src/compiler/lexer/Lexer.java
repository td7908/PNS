/**
 * @Author: turk
 * @Description: Leksikalni analizator.
 */

package compiler.lexer;

import static common.RequireNonNull.requireNonNull;
import static compiler.lexer.TokenType.*;

import common.Report;
import compiler.lexer.Position.Location;

import java.util.*;


public class Lexer {
    /**
     * Izvorna koda.
     */
    private final String source;

    /**
     * Preslikava iz ključnih besed v vrste simbolov.
     */
    private final static Map<String, TokenType> keywordMapping;
    //staticen slovar - hrani preslikavo is stringov v token type -> uporabimo ko preverjamo ali je keyword ali konstanta
    //stet se zacne z 1

    static {
        keywordMapping = new HashMap<>();
        for (var token : TokenType.values()) {
            var str = token.toString();
            if (str.startsWith("KW_")) {
                keywordMapping.put(str.substring("KW_".length()).toLowerCase(), token);
            }
            if (str.startsWith("AT_")) {
                keywordMapping.put(str.substring("AT_".length()).toLowerCase(), token);
            }
        }
    }

    /**
     * Ustvari nov analizator.
     *
     * @param source Izvorna koda programa.
     */
    public Lexer(String source) {
        requireNonNull(source);
        this.source = source;
    }

    /**
     * Izvedi leksikalno analizo.
     *
     * @return seznam leksikalnih simbolov.
     */
    /*
    VRSTA:
    Kaj je enolično -> operatorji -> niso v nobenih kw in ne smejo bit v imenih ali konstantah -> prvo to

    prob: zadnje se ugotovi a je ID al keyword
    SEPRAVI: kaj sklepat glede na prvi znak:
    crka - ime ali ID -> naprej kako ugotovit da ni samo konstanta stringa
    stevilka - NI ime
     */

    //while template:
//if (i + 1 < characters.length && kajJeChar+1) {
//        while (i < characters.length && kajJeChar+1) {
//        lexem.append(characters[i]);
//        i++;
//        endColumn++;
//        }
//        i--;
//        endColumn--;
//        }
    public List<Symbol> scan() {
        /* implementiraj logiko */
        var symbols = new ArrayList<Symbol>();
        int line = 1;
        int startColumn = 1;
        int endColumn;
        TokenType tokenType = null;
        StringBuilder lexem;

        char[] characters = this.source.toCharArray();
//        characters = new char[]{9, 39, 'd', 'a', 'n', 'e', 's', ' ', 'j', 'e', ' ', 39, 39, 'l', 'e', 'p', 39, 39, ' ', 'd', 'a', 'n', 39};
//        characters = new char[]{'t', 'r', 'u', 'e', 10, 10, ' ', ' ', '<', '=', '&', 10, 9, 10, 9, ' ', 'f', 'a', 'l', 's', 'e'};
        for (int i = 0; i < characters.length; i++) {
            endColumn = startColumn;
            lexem = new StringBuilder();
            boolean addingSymbolNecessity = true;
            if (characters[i] == '+') {
                tokenType = OP_ADD;
                lexem = new StringBuilder("+");
            } else if (characters[i] == '-') {
                tokenType = OP_SUB;
                lexem = new StringBuilder("-");
            } else if (characters[i] == '*') {
                tokenType = OP_MUL;
                lexem = new StringBuilder("*");
            } else if (characters[i] == '/') {
                tokenType = OP_DIV;
                lexem = new StringBuilder("/");
            } else if (characters[i] == '%') {
                tokenType = OP_MOD;
                lexem = new StringBuilder("%");
            } else if (characters[i] == '&') {
                tokenType = OP_AND;
                lexem = new StringBuilder("&");
            } else if (characters[i] == '|') {
                tokenType = OP_OR;
                lexem = new StringBuilder("|");
            } else if (characters[i] == '!') {
                if (i + 1 < characters.length && characters[i + 1] == '=') {
                    tokenType = OP_NEQ;
                    endColumn++;
                    i++;
                    lexem = new StringBuilder("!=");
                } else {
                    tokenType = OP_NOT;
                    lexem = new StringBuilder("!");
                }
            } else if (characters[i] == '=') {
                if (i + 1 < characters.length && characters[i + 1] == '=') {
                    tokenType = OP_EQ;
                    endColumn++;
                    i++;
                    lexem = new StringBuilder("==");
                } else {
                    tokenType = OP_ASSIGN;
                    lexem = new StringBuilder("=");
                }
            } else if (characters[i] == '<') {
                if (i + 1 < characters.length && characters[i + 1] == '=') {
                    tokenType = OP_LEQ;
                    endColumn++;
                    i++;
                    lexem = new StringBuilder("<=");
                } else {
                    tokenType = OP_LT;
                    lexem = new StringBuilder("<");
                }
            } else if (characters[i] == '>') {
                if (i + 1 < characters.length && characters[i + 1] == '=') {
                    tokenType = OP_GEQ;
                    endColumn++;
                    i++;
                    lexem = new StringBuilder(">=");
                } else {
                    tokenType = OP_GT;
                    lexem = new StringBuilder(">");
                }
            } else if (characters[i] == '(') {
                tokenType = OP_LPARENT;
                lexem = new StringBuilder("(");
            } else if (characters[i] == ')') {
                tokenType = OP_RPARENT;
                lexem = new StringBuilder(")");
            } else if (characters[i] == '[') {
                tokenType = OP_LBRACKET;
                lexem = new StringBuilder("[");
            } else if (characters[i] == ']') {
                tokenType = OP_RBRACKET;
                lexem = new StringBuilder("]");
            } else if (characters[i] == '{') {
                tokenType = OP_LBRACE;
                lexem = new StringBuilder("{");
            } else if (characters[i] == '}') {
                tokenType = OP_RBRACE;
                lexem = new StringBuilder("}");
            } else if (characters[i] == ':') {
                tokenType = OP_COLON;
                lexem = new StringBuilder(":");
            } else if (characters[i] == ';') {
                tokenType = OP_SEMICOLON;
                lexem = new StringBuilder(";");
            } else if (characters[i] == '.') {
                tokenType = OP_DOT;
                lexem = new StringBuilder(".");
            } else if (characters[i] == ',') {
                tokenType = OP_COMMA;
                lexem = new StringBuilder(",");
            } else if (characters[i] > 47 && characters[i] < 58) {
                //NUMBERS
                tokenType = C_INTEGER;
                lexem = new StringBuilder();
                if (i + 1 < characters.length && Character.isDigit(characters[i + 1])) {
                    while (i < characters.length && Character.isDigit(characters[i])) {
                        lexem.append(characters[i]);
                        i++;
                        endColumn++;
                    }
                    i--;
                    endColumn--;
                } else {
                    lexem.append(characters[i]);
                }
            } else if (characters[i] == '#') {
                addingSymbolNecessity = false;
                while (i < characters.length && characters[i] != '\n') {
                    i++;
                }
                line++;
                startColumn = 1;
                endColumn = 1;
            } else if (characters[i] == 32) {
                // ' '
                addingSymbolNecessity = false;
                if (i + 1 < characters.length && characters[i + 1] == 32) {
                    while (i < characters.length && characters[i] == 32) {
                        startColumn++;
                        i++;
                    }
                    i--;
                    startColumn--;
                }
                startColumn++;
            } else if (characters[i] == 9) {
                // \t
                addingSymbolNecessity = false;
                if (i + 1 < characters.length && characters[i + 1] == 9) {
                    while (i < characters.length && characters[i] == '\t') {
                        startColumn += 4;
                        i++;
                    }
                    i--;
                    startColumn -= 4;
                }
                startColumn += 4;
            } else if (characters[i] == 10 || characters[i] == 13) {
                // '\n'
                addingSymbolNecessity = false;
                line++;
                startColumn = 1;
                endColumn = 1;
            } else if (characters[i] == 39) {
                // ' -> STRING
                tokenType = C_STRING;
                lexem = new StringBuilder("");
                boolean isEscaped = false;
                i++;
                endColumn++;
                while (i < characters.length && !isEscaped && characters[i] > 31 && characters[i] < 127) {
                    if (characters[i] == 39) {
                        if (i + 1 < characters.length && characters[i + 1] == 39) {
                            i++;
                            endColumn++;
                            lexem.append("'");
                        } else {
                            isEscaped = true;
                        }
                    } else {
                        lexem.append(characters[i]);
                    }
                    i++;
                    endColumn++;
                }
                i--;
                endColumn--;
                if (characters[i] <= 31 || characters[i] >= 127) {
                    Report.error(new Position(line, endColumn, line, endColumn), "Unknown Character: " + characters[i]);
                }
                if (!isEscaped) {
                    Report.error(new Position(line, startColumn, line, endColumn), "String is not escaped");
                }
            } else if ((characters[i] >= 65 && characters[i] <= 90) || (characters[i] >= 97 && characters[i] <= 122) || characters[i] == '_') {
                lexem = new StringBuilder();
                while (i < characters.length && ((characters[i] >= 65 && characters[i] <= 90) || (characters[i] >= 97 && characters[i] <= 122) || characters[i] == '_' || Character.isDigit(characters[i]))) {
                    lexem.append(characters[i]);
                    i++;
                    endColumn++;
                }
                i--;
                endColumn--;
                if (!((characters[i] >= 65 && characters[i] <= 90) || (characters[i] >= 97 && characters[i] <= 122) || characters[i] == '_')) {
                    Report.error(new Position(line, endColumn, line, endColumn), "Unknown Character: " + characters[i]);
                }
                if (keywordMapping.containsKey(lexem.toString())) {
                    tokenType = keywordMapping.get(lexem.toString());
                } else if (lexem.toString().equals("true") || lexem.toString().equals("false")) {
                    tokenType = C_LOGICAL;
                } else {
                    tokenType = IDENTIFIER;
                }
            } else {
                Report.error(new Position(line, endColumn, line, endColumn), "Unknown Character: " + characters[i]);
            }
            if (addingSymbolNecessity) {
                symbols.add(createSymbol(line, startColumn, endColumn + 1, tokenType, lexem.toString()));
                startColumn = endColumn + 1;
            }
        }

        symbols.add(new Symbol(new Position(line, startColumn, line, startColumn), EOF, "$"));
        return symbols;
    }

    private Symbol createSymbol(
            int line,
            int startColumn,
            int endColumn,
            TokenType tokenType,
            String lexeme) {
        Position position = new Position(new Location(line, startColumn), new Location(line, endColumn));
        return new Symbol(position, tokenType, lexeme);
    }
}
