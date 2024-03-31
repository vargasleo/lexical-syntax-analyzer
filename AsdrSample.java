import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class AsdrSample {

    private static final int BASE_TOKEN_NUM = 301;
    public static final int IDENT = 301;
    public static final int NUM = 302;
    public static final int WHILE = 303;
    public static final int IF = 304;
    public static final int ELSE = 305;
    public static final int FUNC = 306;
    public static final int INT = 307;
    public static final int DOUBLE = 308;
    public static final int BOOLEAN = 309;
    public static final int VOID = 310;

    public static final String[] tokenList =
            {
                    "IDENT",
                    "NUM",
                    "WHILE",
                    "IF",
                    "FI",
                    "ELSE",
                    "INT",
                    "DOUBLE",
                    "BOOLEAN",
                    "FUNC",
                    "VOID"
            };

    private final Yylex lexer;

    private static int laToken;
    private boolean debug;


    public AsdrSample(Reader r) {
        lexer = new Yylex(r, this);
    }

    /*
    /////////////////////////////////////////////////
    //      Gramática original, vista em aula      //
    /////////////////////////////////////////////////

    Prog -->  Bloco

    Bloco --> { Cmd }

    Cmd --> Bloco
            | while ( E ) Cmd
            | ident = E ;
            | if ( E ) Cmd RestoIf   // 'fatorada à esquerda'

     RestoIf --> fi
            | else Cmd fi

     E --> IDENT
            | NUM
            | ( E )

    /////////////////////////////////////////////////
    // Gramática a ser considerada para o trabalho //
    /////////////////////////////////////////////////

    Prog -->  ListaDecl

    ListaDecl -->  DeclVar  ListaDecl
            |  DeclFun  ListaDecl
            |  // vazio

    DeclVar --> Tipo ListaIdent ';'
            | // vazio

    Tipo --> int | double | boolean

    ListaIdent --> IDENT , ListaIdent
            | IDENT

    DeclFun --> FUNC tipoOuVoid IDENT '(' FormalPar ')' '{' DeclVar ListaCmd '}'
            | // vazio

    TipoOuVoid --> Tipo | VOID

    FormalPar -> paramList | // vazio

    paramList --> Tipo IDENT , ParamList
            | Tipo IDENT

    Bloco --> { ListaCmd }

    ListaCmd --> Cmd ListaCmd
            |    // vazio

    Cmd --> Bloco
            | while ( E ) Cmd
            | IDENT = E ;
            | if ( E ) Cmd RestoIf

    RestoIf -> else Cmd
            |    // vazio

    E --> E + T
            | E - T
            | T

    T --> T * F
            | T / F
            | F

    F -->  IDENT
            | NUM
            | ( E )
    */

    private void Prog() {
        if (laToken == '{') {
            if (debug) System.out.println("Prog --> Bloco");
            Bloco();
        } else
            yyError("esperado '{'");
    }

    private void Bloco() {
        if (debug) System.out.println("Bloco --> { Cmd }");
        check('{');
        Cmd();
        check('}');
    }

    private void Cmd() {
        if (laToken == '{') {
            if (debug) System.out.println("Cmd --> Bloco");
            Bloco();
        } else if (laToken == WHILE) {
            if (debug) System.out.println("Cmd --> WHILE ( E ) Cmd");
            check(WHILE);    // laToken = this.yylex();
            check('(');
            E();
            check(')');
            Cmd();
        } else if (laToken == IDENT) {
            if (debug) System.out.println("Cmd --> IDENT = E ;");
            check(IDENT);
            check('=');
            E();
            check(';');
        } else if (laToken == IF) {
            if (debug) System.out.println("Cmd --> if (E) Cmd RestoIF");
            check(IF);
            check('(');
            E();
            check(')');
            Cmd();
        } else yyError("Expected '{', 'if', 'while' or identifier");
    }

    private void E() {
        if (laToken == IDENT) {
            if (debug) System.out.println("E --> IDENT");
            check(IDENT);
        } else if (laToken == NUM) {
            if (debug) System.out.println("E --> NUM");
            check(NUM);
        } else if (laToken == '(') {
            if (debug) System.out.println("E --> ( E )");
            check('(');
            E();
            check(')');
        } else yyError("Expected '(', identifier or number");
    }

    private void check(int expected) {
        if (laToken == expected) laToken = this.yylex();
        else {
            String expStr, laStr;

            expStr = ((expected < BASE_TOKEN_NUM)
                    ? "" + (char) expected
                    : tokenList[expected - BASE_TOKEN_NUM]);

            laStr = ((laToken < BASE_TOKEN_NUM)
                    ? Character.toString(laToken)
                    : tokenList[laToken - BASE_TOKEN_NUM]);

            yyError("Expected: " + expStr + " on: " + laStr);
        }
    }

    private int yylex() {
        int retVal = -1;
        try {
            retVal = lexer.yylex();
        } catch (IOException e) {
            System.err.println("IO Error:" + e);
        }
        return retVal;
    }

    public void yyError(String error) {
        System.err.println("Error: " + error);
        System.err.println("Entry rejected");
        System.out.println("\n\nFailed!");
        System.exit(1);
    }

    public void setDebug(boolean trace) {
        debug = trace;
    }

    /**
     * Runs the scanner on input files.
     * <p>
     * This main method is the debugging routine for the scanner.
     * It prints debugging information about each returned token to
     * System.out until the end of file is reached, or an error occured.
     *
     * @param args the command line, contains the filenames to run the scanner on.
     */
    public static void main(String[] args) {
        AsdrSample parser;
        try {
            if (args.length == 0) parser = new AsdrSample(new InputStreamReader(System.in));
            else parser = new AsdrSample(new java.io.FileReader(args[0]));

            parser.setDebug(false);
            laToken = parser.yylex();

            parser.Prog();

            if (laToken == Yylex.YYEOF) System.out.println("\n\nSuccess");
            else System.out.println("\n\nFailed - expected EOF");

        } catch (java.io.FileNotFoundException e) {
            System.out.println("File not found : \"" + args[0] + "\"");
        } catch (Exception e) {
            System.out.println("Unexpected exception:");
            e.printStackTrace();
        }
    }
}
