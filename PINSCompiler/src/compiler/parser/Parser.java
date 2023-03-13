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
import compiler.lexer.Symbol;
import compiler.lexer.TokenType;

public class Parser {
    /**
     * Seznam leksikalnih simbolov.
     */
    private final List<Symbol> symbols;
    private int pointer = -1;

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
        if (!check(EOF)) {
            error();
        }
    }

    private void parseSource() {
        dump("source -> definitions");
        parseDefinitions();
    }

    private void parseDefinitions() {
        dump("definitions -> definition definitions2");
        parseDefinition();
        parseDefinitions2();
    }

    private void parseDefinitions2() {
        if (check(OP_SEMICOLON)) {
            dump("definitions2 -> ; definitions");
            skip();
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
        if (check(IDENTIFIER)) {
            dump("type -> identifier");
            skip();
        }
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
                        skip();
                        parseType();
                    } else error();
                } else error();
            } else error();
        } else error();
    }

    private void parseVariableDefinition() {
        if (check(IDENTIFIER)) {
            skip();
            if (check(OP_COLON)) {
                dump("variable_definition -> var identifier : type");
                skip();
                parseType();
            } else error();
        } else error();
    }

    private void parseFunctionDefinition() {
        dump("function_definition -> fun identifier ( parameters ) : type = expression");
        if (check(IDENTIFIER)) {
            skip();
            if (check(OP_LPARENT)) {
                skip();
                parseParameters();
                if (check(OP_RPARENT)) {
                    skip();
                    if (check(OP_COLON)) {
                        skip();
                        parseType();
                        if (check(OP_ASSIGN)) {
                            skip();
                            parseExpression();
                        } else error();
                    } else error();
                } else error();
            } else error();
        } else error();
    }

    private void parseParameters() {
        dump("parameters -> parameter parameters2");
        parseParameter();
        parseParameters2();
    }

    private void parseParameter() {
        if (check(IDENTIFIER)) {
            skip();
            if (check(OP_COLON)) {
                dump("parameter -> identifier : type");
                skip();
                parseType();
            } else error();
        } else error();
    }

    private void parseParameters2() {
        if (check(OP_COMMA)) {
            dump("parameters2 -> , parameters");
            skip();
            parseParameters();
        } else {
            dump("parameters2 -> e");
        }
    }

    private void parseExpression() {
        dump("expression -> logical_ior_expression expression2");
        parseLogicalIOrExpression();
        parseExpression2();
    }

    private void parseExpression2() {
        if (check(OP_LBRACE)) {
            dump("expression2 -> { where definitions }");
            skip();
            if (check(KW_WHERE)) {
                skip();
                parseDefinitions();
                if (check(OP_RBRACE)) {
                    skip();
                } else error();
            } else error();
        } else {
            dump("expression2 -> e");
        }
    }

    private void parseLogicalIOrExpression() {
        dump("logical_ior_expression -> logical_and_expression logical_ior_expression2");
        parseLogicalAndExpression();
        parseLogicalIOrExpression2();
    }

    private void parseLogicalIOrExpression2() {
        if (check(OP_OR)) {
            dump("logical_ior_expression2 -> | logical_ior_expression");
            skip();
            parseLogicalIOrExpression();
        } else {
            dump("logical_ior_expression2 -> e");
        }
    }

    private void parseLogicalAndExpression() {
        dump("logical_and_expression -> compare_expression logical_and_expression2");
        parseCompareExpression();
        parseLogicalAndExpression2();
    }

    private void parseLogicalAndExpression2() {
        if (check(OP_AND)) {
            dump("logical_and_expression2 -> & logical_and_expression");
            skip();
            parseLogicalAndExpression();
        } else {
            dump("logical_and_expression2 -> e");
        }
    }

    private void parseCompareExpression() {
        dump("compare_expression -> additive_expression compare_expression2");
        parseAdditiveExpression();
        parseCompareExpression2();
    }

    private void parseCompareExpression2() {
        if (check(OP_EQ)) {
            dump("compare_expression2 -> == additive_expression");
            skip();
            parseAdditiveExpression();
        } else if (check(OP_NEQ)) {
            dump("compare_expression2 -> != additive_expression");
            skip();
            parseAdditiveExpression();
        } else if (check(OP_LEQ)) {
            dump("compare_expression2 -> <= additive_expression");
            skip();
            parseAdditiveExpression();
        } else if (check(OP_GEQ)) {
            dump("compare_expression2 -> >= additive_expression");
            skip();
            parseAdditiveExpression();
        } else if (check(OP_LT)) {
            dump("compare_expression2 -> < additive_expression");
            skip();
            parseAdditiveExpression();
        } else if (check(OP_GT)) {
            dump("compare_expression2 -> > additive_expression");
            skip();
            parseAdditiveExpression();
        } else {
            dump("compare_expression2 -> e");
        }
    }

    private void parseAdditiveExpression() {
        dump("additive_expression -> multiplicative_expression additive_expression2");
        parseMultiplicativeExpression();
        parseAdditiveExpression2();
    }

    private void parseAdditiveExpression2() {
        if (check(OP_ADD)) {
            dump("additive_expression2 -> + additive_expression");
            skip();
            parseAdditiveExpression();
        } else if (check(OP_SUB)) {
            dump("additive_expression2 -> - additive_expression");
            skip();
            parseAdditiveExpression();
        } else {
            dump("additive_expression2 -> e");
        }
    }

    private void parseMultiplicativeExpression() {
        dump("multiplicative_expression -> prefix_expression multiplicative_expression2");
        parsePrefixExpression();
        parseMultiplicativeExpression2();
    }

    private void parseMultiplicativeExpression2() {
        if (check(OP_MUL)) {
            dump("multiplicative_expression2 -> * multiplicative_expression");
            skip();
            parseMultiplicativeExpression();
        } else if (check(OP_DIV)) {
            dump("multiplicative_expression2 -> / multiplicative_expression");
            skip();
            parseMultiplicativeExpression();
        } else if (check(OP_MOD)) {
            dump("multiplicative_expression2 -> % multiplicative_expression");
            skip();
            parseMultiplicativeExpression();
        } else {
            dump("multiplicative_expression2 -> e");
        }
    }

    private void parsePrefixExpression() {
        if (check(OP_ADD)) {
            dump("prefix_expression -> + prefix_expression");
            skip();
            parsePrefixExpression();
        } else if (check(OP_SUB)) {
            dump("prefix_expression -> - prefix_expression");
            skip();
            parsePrefixExpression();
        } else if (check(OP_NOT)) {
            dump("prefix_expression -> ! prefix_expression");
            skip();
            parsePrefixExpression();
        } else {
            dump("prefix_expression -> postfix_expression");
            parsePostfixExpression();
        }
    }

    private void parsePostfixExpression() {
        dump("postfix_expression -> atom_expression postfix_expression2");
        parseAtomExpression();
        parsePostfixExpression2();
    }

    private void parsePostfixExpression2() {
        if (check(OP_LBRACKET)) {
            dump("postfix_expression2 -> [ expression ] postfix_expression2 ");
            skip();
            parseExpression();
            if (check(OP_RBRACKET)) {
                skip();
                parseAtomExpression2();
            } else error();
        } else {
            dump("postfix_expression2 -> e");
        }
    }

    private void parseAtomExpression() {
        if (check(C_LOGICAL)) {
            dump("atom_expression -> log_constant");
            skip();
        } else if (check(C_INTEGER)) {
            dump("atom_expression -> int_constant");
            skip();
        } else if (check(C_STRING)) {
            dump("atom_expression -> str_constant");
            skip();
        } else if (check(IDENTIFIER)) {
            dump("atom_expression -> identifier atom_expression2");
            skip();
            parseAtomExpression2();
        } else if (check(OP_LPARENT)) {
            dump("atom_expression -> ( expressions )");
            skip();
            parseExpressions();
            if (check(OP_RPARENT)) {
                skip();
            } else error();
        } else if (check(OP_LBRACE)) {
            dump("atom_expression -> { atom_expression4");
            skip();
            parseAtomExpression4();
        } else error();
    }

    private void parseAtomExpression4() {
        if (check(KW_WHILE)) {
            dump("atom_expression4 -> while expression : expression }");
            skip();
            parseExpression();
            if (check(OP_COLON)) {
                skip();
                parseExpression();
            } else error();
            if (check(OP_RBRACE)) {
                skip();
            } else error();
        } else if (check(KW_FOR)) {
            dump("atom_expression4 -> for identifier = expression , expression , expression : expression }");
            skip();
            if (check(IDENTIFIER)) {
                skip();
            } else error();
            if (check(OP_ASSIGN)) {
                skip();
            } else error();
            parseExpression();
            if (check(OP_COMMA)) {
                skip();
            } else error();
            parseExpression();
            if (check(OP_COMMA)) {
                skip();
            } else error();
            parseExpression();
            if (check(OP_COLON)) {
                skip();
            } else error();
            parseExpression();
            if (check(OP_RBRACE)) {
                skip();
            } else error();
        } else if (check(KW_IF)) {
            dump("atom_expression4 -> if expression then expression atom_expression3");
            skip();
            parseExpression();
            if (check(KW_THEN)) {
                skip();
            } else error();
            parseExpression();
            parseAtomExpression3();
        } else {
            dump("atom_expression4 -> expression = expression }");
            parseExpression();
            if (check(OP_ASSIGN)) {
                skip();
            } else error();
            parseExpression();
            if (check(OP_RBRACE)) {
                skip();
            } else error();
        }
    }

    private void parseAtomExpression3() {
        if (check(OP_RBRACE)) {
            dump("atom_expression3 -> }");
            skip();
        } else if (check(KW_ELSE)) {
            dump("atom_expression3 -> else expression }");
            skip();
            parseExpression();
            if (check(OP_RBRACE)) {
                skip();
            } else error();
        } else error();
    }

    private void parseAtomExpression2() {
        if (check(OP_LPARENT)) {
            dump("atom_expression2 -> ( expressions )");
            skip();
            parseExpressions();
            if (check(OP_RPARENT)) {
                skip();
            } else error();
        } else {
            dump("atom_expression2 -> e");
        }
    }

    private void parseExpressions() {
        dump("expressions -> expression expressions2");
        parseExpression();
        parseExpressions2();
    }

    private void parseExpressions2() {
        if (check(OP_COMMA)) {
            dump("expressions2 -> , expressions");
            skip();
            parseExpressions();
        } else {
            dump("expressions2 -> e");
        }
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
