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

    /* referencia ao objeto Scanner gerado pelo JFLEX */
    private Yylex lexer;

    public Parser yylval;

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
            verifica(';');
        } else {
            debug("DeclVar --> vazio");
        }
    }

    private void DeclFun() {
        if (laToken == FUNC) {
            debug("DeclFun --> FUNC tipoOuVoid IDENT '(' FormalPar ')' '{' DeclVar ListaCmd '}'");

            verifica(FUNC);
            TipoOuVoid();
            verifica(IDENT);
            verifica('(');
            FormalPar();
            verifica(')');
            verifica('{');
            DeclVar();
            ListaCmd();
            verifica('}');
        } else {
            debug("DeclFun --> vazio");
        }
    }

    private void TipoOuVoid() {
        if (laToken == VOID) {
            debug("TipoOuVoid --> VOID");

            verifica(VOID);
        } else {
            debug("TipoOuVoid --> Tipo");

            Tipo();
        }
    }

    private void Tipo() {
        if (laToken == INT) {
            debug("Tipo --> int");

            verifica(INT);
        } else if (laToken == DOUBLE) {
            debug("Tipo --> double");

            verifica(DOUBLE);
        } else if (laToken == BOOLEAN) {
            debug("Tipo --> boolean");

            verifica(BOOLEAN);
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
        verifica(IDENT);

        if (laToken == ',') {
            debug("ParamList -> Tipo IDENT , ParamList");

            verifica(',');
            ParamList();
        } else {
            debug("ParamList -> Tipo IDENT");
        }
    }

    private void ListaIdent() {
        verifica(IDENT);

        if (laToken == ',') {
            debug("ListaIdent --> IDENT , ListaIdent");

            verifica(',');
            ListaIdent();
        } else {
            debug("ListaIdent --> IDENT");
        }
    }

    private void Bloco() {
        debug("Bloco --> { Cmd }");

        verifica('{');
        ListaCmd();
        verifica('}');
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

            verifica(WHILE); // laToken = this.yylex();
            verifica('(');
            E();
            verifica(')');
            Cmd();
        } else if (laToken == IDENT) {
            debug("Cmd --> IDENT = E ;");

            verifica(IDENT);
            verifica('=');
            E();
            verifica(';');
        } else if (laToken == IF) {
            debug("Cmd --> if (E) Cmd RestoIF");

            verifica(IF);
            verifica('(');
            E();
            verifica(')');
            Cmd();
            RestoIF();
        } else {
            yyerror("Esperado {, if, while ou identificador");
        }
    }

    private void RestoIF() {
        if (laToken == ELSE) {
            debug("RestoIF --> else Cmd");

            verifica(ELSE);
            Cmd();
        } else {
            debug("RestoIF --> vazio");
        }
    }

    private void E() {
        if ((laToken == IDENT) || (laToken == NUM) || (laToken == '(')) {
            debug("E --> T");

            T();
        } else {
            E();

            if (laToken == '+') {
                debug("E --> E + T");

                verifica('+');
                T();
            } else if (laToken == '-') {
                debug("E --> E - T");

                verifica('-');
                T();
            }
        }
    }

    private void T() {
        if ((laToken == IDENT) || (laToken == NUM) || (laToken == '(')) {
            debug("T --> F");

            F();
        } else {
            T();

            if (laToken == '*') {
                debug("T --> T * F");

                verifica('*');
                F();
            } else if (laToken == '/') {
                debug("T --> T / F");

                verifica('/');
                F();
            }
        }
    }

    private void F() {
        if (laToken == IDENT) {
            debug("F --> IDENT");

            verifica(IDENT);
        } else if (laToken == NUM) {
            debug("F --> NUM");

            verifica(NUM);
        } else if (laToken == '(') {
            debug("F --> ( E )");

            verifica('(');
            E();
            verifica(')');
        } else {
            yyerror("Esperado operando (, identificador ou numero");
        }
    }

    private void verifica(int expected) {
        if (laToken == expected) {
            laToken = this.yylex();
        } else {
            String expStr, laStr;

            expStr = ((expected < BASE_TOKEN_NUM) ? "" + (char) expected : tokenList[expected - BASE_TOKEN_NUM]);

            laStr = ((laToken < BASE_TOKEN_NUM) ? Character.toString(laToken) : tokenList[laToken - BASE_TOKEN_NUM]);

            yyerror("esperado token: " + expStr + " na entrada: " + laStr);
        }
    }

    /* metodo de acesso ao Scanner gerado pelo JFLEX */
    private int yylex() {
        int retVal = -1;
        try {
            yylval = new Parser(0); //zera o valor do token
            retVal = lexer.yylex(); //le a entrada do arquivo e retorna um token
        } catch (IOException e) {
            System.err.println("IO Error:" + e);
        }

        return retVal; //retorna o token para o Parser
    }

    /* metodo de manipulacao de erros de sintaxe */
    public void yyerror(String error) {
        System.err.println("Erro: " + error);
        System.err.println("Entrada rejeitada");
        System.out.println("\n\nFalhou!!!");

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
        AsdrSample parser = null;
        try {
            if (args.length == 0) {
                parser = new AsdrSample(new InputStreamReader(System.in));
            } else {
                parser = new AsdrSample(new java.io.FileReader(args[0]));
            }

            parser.setDebug(true);
            laToken = parser.yylex();

            parser.Prog();

            if (laToken == Yylex.YYEOF) {
                System.out.println("\n\nSucesso!");
            } else {
                System.out.println("\n\nFalhou - esperado EOF.");
            }
        } catch (java.io.FileNotFoundException e) {
            System.out.println("File not found : \"" + args[0] + "\"");
        }
    }
}
