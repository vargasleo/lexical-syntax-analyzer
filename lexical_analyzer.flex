%%

%{
  private SyntaxAnalyzer yyparser;

            public Yylex(java.io.Reader r, SyntaxAnalyzer yyparser) {
              this(r);
              this.yyparser = yyparser;
            }


          %}

%integer
%line
%char

WHITE_SPACE_CHAR=[\n\r\ \t\b\012]

%%

"$TRACE_ON"   { yyparser.setDebug(true); }
"$TRACE_OFF"  { yyparser.setDebug(false); }

"while"	 	{ return SyntaxAnalyzer.WHILE; }
"if"		{ return SyntaxAnalyzer.IF; }
"else"		{ return SyntaxAnalyzer.ELSE; }
"fi"		{ return SyntaxAnalyzer.FI; }
"int"       { return SyntaxAnalyzer.INT; }
"double"    { return SyntaxAnalyzer.DOUBLE; }
"boolean"   { return SyntaxAnalyzer.BOOLEAN; }
"func"      { return SyntaxAnalyzer.FUNC; }
"void"      { return SyntaxAnalyzer.VOID; }

[:jletter:][:jletterdigit:]* { return SyntaxAnalyzer.IDENT; }

[0-9]+ 	{ return SyntaxAnalyzer.NUM; }

"{" |
"}" |
";" |
"(" |
")" |
"+" |
"="    	{ return yytext().charAt(0); } 


{WHITE_SPACE_CHAR}+ { }

. { System.out.println("Erro lexico: caracter invalido: <" + yytext() + ">"); }
