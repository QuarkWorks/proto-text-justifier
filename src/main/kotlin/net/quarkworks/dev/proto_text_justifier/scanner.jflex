package net.quarkworks.dev.proto_text_justifier;
//scanner
%%
%class Scanner
%unicode


%type String

tok = [^ \t\u000c\r\n\u000b\u0085\u00a0]+
ws  = [ \t\u000c\r\n\u000b\u0085\u00a0]+

%%
{tok}  { return yytext(); }
{ws}  {}
