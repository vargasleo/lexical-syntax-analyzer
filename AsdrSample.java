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

    public static final String[] tokenList = {
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

    /* referencia ao objeto Scanner gerado pelo JFLEX */
    private final Yylex lexer;

    private static int laToken;
    private boolean debug;

    /* construtor da classe */
    public AsdrSample(Reader r) {
        lexer = new Yylex(r, this);
    }

    private void Prog() {
        debug("Prog --> ListaDecl");

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

    private void debug(String x) {
        if (debug) {
            System.out.println(x);
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

            check(WHILE); // laToken = this.yylex();
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
            yyError("Esperado {, if, while ou identificador");
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
        T();
        while (laToken == '+' || laToken == '-') {
            if (laToken == '+') {
                check('+');
                T();
            } else {
                check('-');
                T();
            }
        }
    }

    private void T() {
        F();
        while (laToken == '*' || laToken == '/') {
            if (laToken == '*') {
                check('*');
                F();
            } else {
                check('/');
                F();
            }
        }
    }

    private void F() {
        if (laToken == IDENT || laToken == NUM) {
            check(laToken);
        } else if (laToken == '(') {
            check('(');
            E();
            check(')');
        } else {
            yyError("Expected '(', or ,Identifier or Number");
        }
    }

    private void check(int expected) {
        if (laToken == expected) {
            laToken = this.yyLex();
        } else {
            String expStr, laStr;

            expStr = ((expected < BASE_TOKEN_NUM) ? "" + (char) expected : tokenList[expected - BASE_TOKEN_NUM]);

            laStr = ((laToken < BASE_TOKEN_NUM) ? Character.toString(laToken) : tokenList[laToken - BASE_TOKEN_NUM]);

            yyError("Expected: '" + expStr + "', actual: '" + laStr + "'.");
        }
    }

    private int yyLex() {
        int retVal = -1;
        try {
            retVal = lexer.yylex();
        } catch (IOException e) {
            System.err.println("IO Error: " + e);
        }
        return retVal;
    }

    public void yyError(String error) {
        System.err.println("Error: " + error);
        System.err.println("Entry rejected");
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
     * @param args the command line, contains the filenames to run the scanner on.
     */
    public static void main(String[] args) {
        AsdrSample parser;
        try {
            if (args.length == 0) {
                parser = new AsdrSample(new InputStreamReader(System.in));
            } else {
                parser = new AsdrSample(new java.io.FileReader(args[0]));
            }

            parser.setDebug(true);
            laToken = parser.yyLex();

            parser.Prog();

            if (laToken == Yylex.YYEOF) {
                System.out.println("\n\nSUCCESS!");
            } else {
                System.out.println("\n\nFAILED - Expected EOF");
            }
        } catch (java.io.FileNotFoundException e) {
            System.out.println("File not found: \"" + args[0] + "\"");
        }
    }
}
