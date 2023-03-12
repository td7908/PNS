/**
 * @Author: turk
 * @Description: Sintaksni analizator.
 */

package compiler.parser;

import static compiler.lexer.TokenType.*;
import static common.RequireNonNull.requireNonNull;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

import common.Report;
import compiler.lexer.Position;
import compiler.lexer.Symbol;
import compiler.lexer.TokenType;

public class Parser {
    /**
     * Seznam leksikalnih simbolov.
     */
    private final List<Symbol> symbols;
    private int pointer;

    /**
     * Ciljni tok, kamor izpisujemo produkcije. Če produkcij ne želimo izpisovati,
     * vrednost opcijske spremenljivke nastavimo na Optional.empty().
     */
    private final Optional<PrintStream> productionsOutputStream;

    public Parser(List<Symbol> symbols, Optional<PrintStream> productionsOutputStream) {
        requireNonNull(symbols, productionsOutputStream);
        this.symbols = symbols;
        this.productionsOutputStream = productionsOutputStream;
    }

    /**
     * Izvedi sintaksno analizo.
     */
    public void parse() {
        parseSource();
    }

    private void parseSource() {
        dump("source -> definitions");
    }

    private void parseDefinitions() {
        dump("definitions -> definition definitions2");
        parseDefinition();
        parseDefinitions2();
    }

    private void parseDefinitions2() {
        if (check(OP_SEMICOLON)) {
            dump("definitions2 -> ; definitions");
            parseDefinitions();
        } else {
            dump("definitions2 -> e");
        }
    }

    private void parseDefinition() {
        if (check(KW_TYP)) {
            dump("definition -> type_definition");
            skip();
            parseTypeDefinition();
        } else if (check(KW_VAR)) {
            dump("definition -> variable_definition");
            skip();
            parseVariableDefinition();
        } else if (check(KW_FUN)) {
            dump("definition -> function_definition");
            skip();
            parseFunctionDefinition();
        } else error();
    }

    private void parseTypeDefinition() {
        dump("type_definition -> typ identifier : type");
        if (check(IDENTIFIER)) {
            skip();
            if (check(OP_COLON)) {
                skip();
                parseType();
            } else error();
        } else error();
    }

    private void parseType() {
        if (check(AT_LOGICAL)) {
            dump("type -> logical");
            skip();
        } else if (check(AT_INTEGER)) {
            dump("type -> integer");
            skip();
        } else if (check(AT_STRING)) {
            dump("type -> string");
            skip();
        } else if (check(KW_ARR)) {
            if (check(OP_LBRACKET)) {
                skip();
                if (check(C_INTEGER)) {
                    skip();
                    if (check(OP_RBRACKET)) {
                        dump("type -> arr [ int_const ] type");
                        parseType();
                    } else error();
                } else error();
            } else error();
        } else error();
    }

    private void parseVariableDefinition() {
        if (check(KW_VAR)) {
            skip();
            if (check(IDENTIFIER)) {
                skip();
                if (check(OP_COLON)) {
                    dump("variable_definition -> var identifier : type");
                    skip();
                    parseType();
                } else error();
            } else error();
        } else error();
    }

    private void parseFunctionDefinition() {
        if (check(KW_FUN)) {
            skip();
            if (check(IDENTIFIER)) {
                skip();
                if (check(OP_LPARENT)) {
                    dump("function_definition -> fun identifier ( parameters ) : type = expression");
                    skip();
                    parseParameters();
                    if (check(OP_RPARENT)) {
                        skip();
                        if (check(OP_COLON)) {
                            parseType();
                            if (check(OP_EQ)) {
                                skip();
                                parseExpression();
                            } else error();
                        } else error();
                    } else error();
                } else error();
            } else error();
        } else error();
    }

    private void parseParameters() {

    }

    private void parseExpression() {
    }


    /**
     * Izpiše produkcijo na izhodni tok.
     */
    private void dump(String production) {
        if (productionsOutputStream.isPresent()) {
            productionsOutputStream.get().println(production);
        }
    }

    private boolean check(TokenType typeToCheck) {
        if (pointer + 1 < symbols.size()) {
            return symbols.get(pointer + 1).tokenType == typeToCheck;
        }
        return false;
    }

    private void error() {
        Report.error("Error");
    }
    private void skip() {
        pointer++;
    }
}
