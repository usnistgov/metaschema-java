// This grammar is derived from the XPath 3.1 grammar produced by Ken Domino (https://github.com/antlr/grammars-v4/blob/2b897252c8c3c4bce4ab4886bada62c00a049d90/xpath/xpath31/XPath31.g4).

grammar metapath10;

// [1]
metapath : expr EOF ;
// [5]
expr : exprsingle ( COMMA exprsingle)* ;
exprsingle : orexpr ;
// [10]
// [15]
orexpr : andexpr ( KW_OR andexpr )* ;
andexpr : comparisonexpr ( KW_AND comparisonexpr )* ;
comparisonexpr : stringconcatexpr ( (valuecomp | generalcomp) stringconcatexpr )? ;
stringconcatexpr : additiveexpr ( PP additiveexpr )* ;
// [20]
additiveexpr : multiplicativeexpr ( (PLUS | MINUS) multiplicativeexpr )* ;
multiplicativeexpr : unionexpr ( (STAR | KW_DIV | KW_IDIV | KW_MOD) unionexpr )* ;
unionexpr : intersectexceptexpr ( (KW_UNION | P) intersectexceptexpr )* ;
intersectexceptexpr : arrowexpr ( ( KW_INTERSECT | KW_EXCEPT) arrowexpr )* ;
// [25]
arrowexpr : unaryexpr ( EG arrowfunctionspecifier argumentlist )* ;
// [30]
unaryexpr : ( MINUS | PLUS)* valueexpr ;
//valueexpr : simplemapexpr ;
valueexpr : pathexpr ;
generalcomp : EQ | NE | LT | LE | GT | GE ;
valuecomp : KW_EQ | KW_NE | KW_LT | KW_LE | KW_GT | KW_GE ;
// [35]
//simplemapexpr : pathexpr ( BANG pathexpr)* ;
pathexpr : ( SLASH relativepathexpr?) | ( SS relativepathexpr) | relativepathexpr ;
relativepathexpr : stepexpr (( SLASH | SS) stepexpr)* ;
stepexpr : postfixexpr | axisstep ;
axisstep : forwardstep predicatelist ;
// [40]
forwardstep : AT? nametest ;
// [45]
nametest : eqname | wildcard ;
wildcard : STAR ;
postfixexpr : primaryexpr (predicate)* ;
// [50]
argumentlist : OP (argument ( COMMA argument)*)? CP ;
predicatelist : predicate* ;
predicate : OB expr CB ;
// [55]
arrowfunctionspecifier : eqname | parenthesizedexpr ;
primaryexpr : literal | parenthesizedexpr | contextitemexpr | functioncall ;
literal : numericliteral | StringLiteral ;
numericliteral : IntegerLiteral | DecimalLiteral | DoubleLiteral ;
// [60]
parenthesizedexpr : OP expr? CP ;
contextitemexpr : D ;
functioncall : 
                      { !(
                        getInputStream().LA(1)==KW_EMPTY_SEQUENCE
                        ) }?
                        eqname 
                        argumentlist ;
argument : exprsingle ;
// [65]
// [70]
// [75]
// [80]
// [85]
// [90]
// [95]
// [100]
// [105]
// [110]

// Error in the spec. EQName also includes acceptable keywords.
eqname : LocalName
 | KW_AND
 | KW_DIV
 | KW_EMPTY_SEQUENCE
 | KW_EQ
 | KW_EXCEPT
 | KW_GE
 | KW_GT
 | KW_IDIV
 | KW_INTERSECT
 | KW_LE
 | KW_LT
 | KW_MOD
 | KW_NE
 | KW_OR
 | KW_UNION
 ;

// Not per spec. Specified for testing.
auxilary : (expr SEMI )+ EOF;


AT : '@' ;
BANG : '!' ;
CB : ']' ;
CC : '}' ;
CEQ : ':=' ;
COLON : ':' ;
COLONCOLON : '::' ;
COMMA : ',' ;
CP : ')' ;
CS : ':*' ;
D : '.' ;
DD : '..' ;
DOLLAR : '$' ;
EG : '=>' ;
EQ : '=' ;
GE : '>=' ;
GG : '>>' ;
GT : '>' ;
LE : '<=' ;
LL : '<<' ;
LT : '<' ;
MINUS : '-' ;
NE : '!=' ;
OB : '[' ;
OC : '{' ;
OP : '(' ;
P : '|' ;
PLUS : '+' ;
POUND : '#' ;
PP : '||' ;
QM : '?' ;
SC : '*:' ;
SLASH : '/' ;
SS : '//' ;
STAR : '*' ;

// KEYWORDS

KW_AND : 'and' ;
KW_DIV : 'div' ;
KW_EMPTY_SEQUENCE : 'empty-sequence' ;
KW_EQ : 'eq' ;
KW_EXCEPT : 'except' ;
KW_GE : 'ge' ;
KW_GT : 'gt' ;
KW_IDIV : 'idiv' ;
KW_INTERSECT : 'intersect' ;
KW_LE : 'le' ;
KW_LT : 'lt' ;
KW_MOD : 'mod' ;
KW_NE : 'ne' ;
KW_OR : 'or' ;
KW_UNION : 'union' ;

// A.2.1. TEMINAL SYMBOLS
// This isn't a complete list of tokens in the language.
// Keywords and symbols are terminals.

IntegerLiteral : FragDigits ;
DecimalLiteral : ('.' FragDigits) | (FragDigits '.' [0-9]*) ;
DoubleLiteral : (('.' FragDigits) | (FragDigits ('.' [0-9]*)?)) [eE] [+-]? FragDigits ;
StringLiteral : ('"' (FragEscapeQuot | ~[^"])*? '"') | ('\'' (FragEscapeApos | ~['])*? '\'') ;
//URIQualifiedName : BracedURILiteral NCName ;
//BracedURILiteral : 'Q' '{' [^{}]* '}' ;
// Error in spec: EscapeQuot and EscapeApos are not terminals!
fragment FragEscapeQuot : '""' ; 
fragment FragEscapeApos : '\'';
// Error in spec: Comment isn't really a terminal, but an off-channel object.
Comment : '(:' (Comment | CommentContents)*? ':)' -> skip ;
LocalName  : FragLocalPart ;
NCName : FragmentNCName ;
// Error in spec: Char is not a terminal!
fragment Char : FragChar ;
fragment FragDigits : [0-9]+ ;
fragment CommentContents : Char ;
// https://www.w3.org/TR/REC-xml-names/#NT-QName
//fragment FragQName : FragPrefixedName | FragUnprefixedName ;
//fragment FragQName : FragUnprefixedName ;
//fragment FragPrefixedName : FragPrefix ':' FragLocalPart ;
//fragment FragUnprefixedName : FragLocalPart ;
//fragment FragPrefix : FragmentNCName ;
fragment FragLocalPart : FragmentNCName ;
fragment FragNCNameStartChar
  :  'A'..'Z'
  |  '_'
  | 'a'..'z'
  | '\u00C0'..'\u00D6'
  | '\u00D8'..'\u00F6'
  | '\u00F8'..'\u02FF'
  | '\u0370'..'\u037D'
  | '\u037F'..'\u1FFF'
  | '\u200C'..'\u200D'
  | '\u2070'..'\u218F'
  | '\u2C00'..'\u2FEF'
  | '\u3001'..'\uD7FF'
  | '\uF900'..'\uFDCF'
  | '\uFDF0'..'\uFFFD'
//  | '\u{10000}'..'\u{EFFFF}'
  ;
fragment FragNCNameChar
  :  FragNCNameStartChar | '-' | '.' | '0'..'9'
  |  '\u00B7' | '\u0300'..'\u036F'
  |  '\u203F'..'\u2040'
  ;
fragment FragmentNCName  :  FragNCNameStartChar FragNCNameChar*  ;

// https://www.w3.org/TR/REC-xml/#NT-Char

fragment FragChar : '\u0009' | '\u000a' | '\u000d'
  | '\u0020'..'\ud7ff'
  | '\ue000'..'\ufffd'
//  | '\u{10000}'..'\u{10ffff}'
 ;

// https://github.com/antlr/grammars-v4/blob/17d3db3fd6a8fc319a12176e0bb735b066ec0616/xpath/xpath31/XPath31.g4#L389
Whitespace :  ('\u000d' | '\u000a' | '\u0020' | '\u0009')+ -> skip ;

// Not per spec. Specified for testing.
SEMI : ';' ;