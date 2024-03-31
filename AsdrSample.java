// Leonardo Vargas, Lorenzo Windmoller, Osmar Sadi

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

    public static final String tokenList[] = {
            "IDENT",
            "NUM",
            "WHILE",
            "IF",
            "ELSE",
            "FUNC",
            "INT",
            "DOUBLE",
            "BOOLEAN",
            "VOID",
    };

    private final Yylex lexer;
    private static int laToken;
    private boolean debug;

    public AsdrSample(Reader r) {
        lexer = new Yylex(r, this);
    }

    private void debug(String message) {
        if (debug) {
            System.out.println(message);
        }
    }

    private void Prog() {
        System.out.println("Prog --> ListaDecl");

        ListaDecl();
    }

    private void ListaDecl() {
        if (laToken == FUNC) {
            debug("ListaDecl --> DeclFun ListaDecl");

            DeclFun();
            ListaDecl();
        } else if (laToken == Yylex.YYEOF) {
            debug("ListaDecl --> vazio");
        } else {
            debug("ListaDecl --> DeclVar ListaDecl");

            DeclVar();
            ListaDecl();
        }
    }

    private void DeclVar() {
        if ((laToken == INT) || (laToken == DOUBLE) || (laToken == BOOLEAN)) {
            debug("DeclVar --> Tipo ListaIdent ';'");

            Tipo();
            ListaIdent();
            check(';');
        } else {
            debug("DeclVar --> vazio");
        }
    }

    private void DeclFun() {
        if (laToken == FUNC) {
            debug("DeclFun --> FUNC tipoOuVoid IDENT '(' FormalPar ')' '{' DeclVar ListaCmd '}'");

            check(FUNC);
            TipoOuVoid();
            check(IDENT);
            check('(');
            FormalPar();
            check(')');
            check('{');
            DeclVar();
            ListaCmd();
            check('}');
        } else {
            debug("DeclFun --> vazio");
        }
    }

    private void TipoOuVoid() {
        if (laToken == VOID) {
            debug("TipoOuVoid --> VOID");

            check(VOID);
        } else {
            debug("TipoOuVoid --> Tipo");

            Tipo();
        }
    }

    private void Tipo() {
        if (laToken == INT) {
            debug("Tipo --> int");

            check(INT);
        } else if (laToken == DOUBLE) {
            debug("Tipo --> double");

            check(DOUBLE);
        } else if (laToken == BOOLEAN) {
            debug("Tipo --> boolean");

            check(BOOLEAN);
        } else {
            yyError("Esperado int, double ou boolean");
        }
    }

    private void FormalPar() {
        if (laToken == ')') {
            debug("FormalPar -> vazio");
        } else {
            debug("FormalPar -> ParamList");

            ParamList();
        }
    }

    private void ParamList() {
        Tipo();
        check(IDENT);

        if (laToken == ',') {
            debug("ParamList -> Tipo IDENT , ParamList");

            check(',');
            ParamList();
        } else {
            debug("ParamList -> Tipo IDENT");
        }
    }

    private void ListaIdent() {
        check(IDENT);

        if (laToken == ',') {
            debug("ListaIdent --> IDENT , ListaIdent");

            check(',');
            ListaIdent();
        } else {
            debug("ListaIdent --> IDENT");
        }
    }

    private void Bloco() {
        debug("Bloco --> { Cmd }");

        check('{');
        ListaCmd();
        check('}');
    }

    private void ListaCmd() {
        if ((laToken == '{') || (laToken == WHILE) || (laToken == IDENT) || (laToken == IF)) {
            debug("ListaCmd --> Cmd ListaCmd");

            Cmd();
            ListaCmd();
        } else {
            debug("ListaCmd --> vazio");
        }
    }

    private void Cmd() {
        if (laToken == '{') {
            debug("Cmd --> Bloco");

            Bloco();
        } else if (laToken == WHILE) {
            debug("Cmd --> WHILE ( E ) Cmd");

            check(WHILE);
            check('(');
            E();
            check(')');
            Cmd();
        } else if (laToken == IDENT) {
            debug("Cmd --> IDENT = E ;");

            check(IDENT);
            check('=');
            E();
            check(';');
        } else if (laToken == IF) {
            debug("Cmd --> if (E) Cmd RestoIF");

            check(IF);
            check('(');
            E();
            check(')');
            Cmd();
            RestoIF();
        } else {
            yyError("Expected: '{', 'if', 'while' or identifier");
        }
    }

    private void RestoIF() {
        if (laToken == ELSE) {
            debug("RestoIF --> else Cmd");

            check(ELSE);
            Cmd();
        } else {
            debug("RestoIF --> vazio");
        }
    }

    private void E() {
        if ((laToken == IDENT) || (laToken == NUM) || (laToken == '(')) {
            T();

            if ((laToken == '+') || (laToken == '-')) {
                if (laToken == '+') {
                    debug("E --> E + T");

                    check('+');
                    T();
                } else if (laToken == '-') {
                    debug("E --> E - T");

                    check('-');
                    T();
                }
            } else {
                debug("E --> T");
            }
        } else {
            yyError("Expected: '(', identifier or number");
        }
    }

    private void T() {
        if ((laToken == IDENT) || (laToken == NUM) || (laToken == '(')) {
            F();

            if ((laToken == '*') || (laToken == '/')) {
                if (laToken == '*') {
                    debug("T --> T * F");

                    check('*');
                    F();
                } else if (laToken == '/') {
                    debug("T --> T / F");

                    check('/');
                    F();
                }
            } else {
                debug("T --> F");
            }
        } else {
            yyError("Expected: '(', identifier or number");
        }
    }

    private void F() {
        if (laToken == IDENT) {
            debug("F --> IDENT");

            check(IDENT);
        } else if (laToken == NUM) {
            debug("F --> NUM");

            check(NUM);
        } else if (laToken == '(') {
            debug("F --> ( E )");

            check('(');
            E();
            check(')');
        } else {
            yyError("Esperado operando (, identificador ou numero");
        }
    }

    private void check(int expected) {
        if (laToken == expected) {
            laToken = this.yyLex();
        } else {
            String expStr, laStr;

            expStr = ((expected < BASE_TOKEN_NUM) ? "" + (char) expected : tokenList[expected - BASE_TOKEN_NUM]);

            laStr = ((laToken < BASE_TOKEN_NUM) ? Character.toString(laToken) : tokenList[laToken - BASE_TOKEN_NUM]);

            yyError("esperado token: " + expStr + " na entrada: " + laStr);
        }
    }

    private int yyLex() {
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
        System.err.println("Rejected");
        System.out.println("\n\nFAILED!");

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
     * @param args the command line, contains the filenames to run
     *             the scanner on.
     */
    public static void main(String[] args) {
        AsdrSample parser;
        try {
            if (args.length == 0) parser = new AsdrSample(new InputStreamReader(System.in));
            else parser = new AsdrSample(new java.io.FileReader(args[0]));

            parser.setDebug(false);

            laToken = parser.yyLex();

            parser.Prog();

            if (laToken == Yylex.YYEOF) System.out.println("\n\nSUCCESS");
            else System.out.println("\n\nFAILED, expected EOF");
        } catch (java.io.FileNotFoundException e) {
            System.out.println("File not found : \"" + args[0] + "\"");
        }
    }
}
