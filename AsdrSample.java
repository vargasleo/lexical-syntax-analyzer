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
        if (debug) {
            System.out.println("Prog --> ListaDecl");
        }

        ListaDecl();
    }

    private void ListaDecl() {
        if (laToken == FUNC) {
            if (debug) {
                System.out.println("ListaDecl --> DeclFun ListaDecl");
            }

            DeclFun();
            ListaDecl();
        } else if (laToken == Yylex.YYEOF) {
            if (debug) {
                System.out.println("ListaDecl --> vazio");
            }
        } else {
            if (debug) {
                System.out.println("ListaDecl --> DeclVar ListaDecl");
            }

            DeclVar();
            ListaDecl();
        }
    }

    private void DeclVar() {
        if ((laToken == INT) || (laToken == DOUBLE) || (laToken == BOOLEAN)) {
            if (debug) {
                System.out.println("DeclVar --> Tipo ListaIdent ';'");
            }

            Tipo();
            ListaIdent();
            verifica(';');
        } else {
            if (debug) {
                System.out.println("DeclVar --> vazio");
            }
        }
    }

    private void DeclFun() {
        if (laToken == FUNC) {
            if (debug) {
                System.out.println("DeclFun --> FUNC tipoOuVoid IDENT '(' FormalPar ')' '{' DeclVar ListaCmd '}'");
            }

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
            if (debug) {
                System.out.println("DeclFun --> vazio");
            }
        }
    }

    private void TipoOuVoid() {
        if (laToken == VOID) {
            if (debug) {
                System.out.println("TipoOuVoid --> VOID");
            }

            verifica(VOID);
        } else {
            if (debug) {
                System.out.println("TipoOuVoid --> Tipo");
            }

            Tipo();
        }
    }

    private void Tipo() {
        if (laToken == INT) {
            if (debug) {
                System.out.println("Tipo --> int");
            }

            verifica(INT);
        } else if (laToken == DOUBLE) {
            if (debug) {
                System.out.println("Tipo --> double");
            }

            verifica(DOUBLE);
        } else if (laToken == BOOLEAN) {
            if (debug) {
                System.out.println("Tipo --> boolean");
            }

            verifica(BOOLEAN);
        }
    }

    private void FormalPar() {
        if (laToken == ')') {
            if (debug) {
                System.out.println("FormalPar -> vazio");
            }
        } else {
            if (debug) {
                System.out.println("FormalPar -> ParamList");
            }
            ParamList();
        }
    }

    private void ParamList() {
        Tipo();
        verifica(IDENT);

        if (laToken == ',') {
            if (debug) {
                System.out.println("ParamList -> Tipo IDENT , ParamList");
            }

            verifica(',');
            ParamList();
        } else {
            if (debug) {
                System.out.println("ParamList -> Tipo IDENT");
            }
        }
    }

    private void ListaIdent() {
        verifica(IDENT);

        if (laToken == ',') {
            if (debug) {
                System.out.println("ListaIdent --> IDENT , ListaIdent");
            }

            verifica(',');
            ListaIdent();
        } else {
            if (debug) {
                System.out.println("ListaIdent --> IDENT");
            }
        }
    }

    private void Bloco() {
        if (debug) {
            System.out.println("Bloco --> { Cmd }");
        }

        verifica('{');
        ListaCmd();
        verifica('}');
    }

    private void ListaCmd() {
        if ((laToken == '{') || (laToken == WHILE) || (laToken == IDENT) || (laToken == IF)) {
            if (debug) {
                System.out.println("ListaCmd --> Cmd ListaCmd");
            }

            Cmd();
            ListaCmd();
        } else {
            if (debug) {
                System.out.println("ListaCmd --> vazio");
            }
        }
    }

    private void Cmd() {
        if (laToken == '{') {
            if (debug) {
                System.out.println("Cmd --> Bloco");
            }

            Bloco();
        } else if (laToken == WHILE) {
            if (debug) {
                System.out.println("Cmd --> WHILE ( E ) Cmd");
            }

            verifica(WHILE); // laToken = this.yylex();
            verifica('(');
            E();
            verifica(')');
            Cmd();
        } else if (laToken == IDENT) {
            if (debug) {
                System.out.println("Cmd --> IDENT = E ;");
            }

            verifica(IDENT);
            verifica('=');
            E();
            verifica(';');
        } else if (laToken == IF) {
            if (debug) {
                System.out.println("Cmd --> if (E) Cmd RestoIF");
            }

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
            if (debug) {
                System.out.println("RestoIF --> else Cmd");
            }

            verifica(ELSE);
            Cmd();
        } else {
            if (debug) {
                System.out.println("RestoIF --> vazio");
            }
        }
    }

    private void E() {
        if ((laToken == IDENT) || (laToken == NUM) || (laToken == '(')) {
            if (debug) {
                System.out.println("E --> T");
            }

            T();
        } else {
            E();

            if (laToken == '+') {
                if (debug) {
                    System.out.println("E --> E + T");
                }

                verifica('+');
                T();
            } else if (laToken == '-') {
                if (debug) {
                    System.out.println("E --> E - T");
                }

                verifica('-');
                T();
            }
        }
    }

    private void T() {
        if ((laToken == IDENT) || (laToken == NUM) || (laToken == '(')) {
            if (debug) {
                System.out.println("T --> F");
            }

            F();
        } else {
            T();

            if (laToken == '*') {
                if (debug) {
                    System.out.println("T --> T * F");
                }

                verifica('*');
                F();
            } else if (laToken == '/') {
                if (debug) {
                    System.out.println("T --> T / F");
                }

                verifica('/');
                F();
            }
        }
    }

    private void F() {
        if (laToken == IDENT) {
            if (debug) {
                System.out.println("F --> IDENT");
            }

            verifica(IDENT);
        } else if (laToken == NUM) {
            if (debug) {
                System.out.println("F --> NUM");
            }

            verifica(NUM);
        } else if (laToken == '(') {
            if (debug) {
                System.out.println("F --> ( E )");
            }

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
