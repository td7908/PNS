/**
 * @Author: turk
 * @Description: Vhodna točka prevajalnika.
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import cli.PINS;
import cli.PINS.Phase;
import compiler.lexer.Lexer;

public class Main {
    /**
     * Metoda, ki izvede celotni proces prevajanja.
     * 
     * @param args parametri ukazne vrstice.
     */
    public static void main(String[] args) throws Exception {
        try {
            args = new String[]{"PINS", "src/source.txt", "--dump", "LEX"};
            var cli = PINS.parse(args);
            run(cli);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }


    // -------------------------------------------------------------------


    private static void run(PINS cli) throws IOException {
        var sourceCode = Files.readString(Paths.get(cli.sourceFile));
        run(cli, sourceCode);
    }

    private static void run(PINS cli, String sourceCode) {
        /**
         * Izvedi leksikalno analizo.
         */
        var symbols = new Lexer(sourceCode).scan();
        if (cli.dumpPhases.contains(Phase.LEX)) {
            for (var symbol : symbols) {
                System.out.println(symbol.toString());
            }
        }
        if (cli.execPhase == Phase.LEX) {
            return;
        }
    }
}
