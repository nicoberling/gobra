// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2011-2020 ETH Zurich.

lexer grammar GobraLexer;
import GoLexer;

@members {boolean escapedOnly = true;}


FLOAT_LIT : (DECIMAL_FLOAT_LIT | HEX_FLOAT_LIT) -> mode(NLSEMI);

// Add lookahead to avoid parsing range expressions like '[1..3]' as two floats '1.' and '.3'
DECIMAL_FLOAT_LIT      : DECIMALS ('.'{_input.LA(1) != '.'}? DECIMALS? EXPONENT? | EXPONENT)
                       | '.'{_input.index() <2 || _input.LA(-2) != '.'}? DECIMALS EXPONENT?
                       ;

// ->mode(NLSEMI) means line breaks directly after this token
// emit a semicolon. (just like after identifiers, literals, ')}]' etc in base Go)

TRUE        : 'true' -> mode(NLSEMI);
FALSE       : 'false' -> mode(NLSEMI);
ASSERT      : 'assert' {!escapedOnly}? | '\\assert';
ASSUME      : 'assume' {!escapedOnly}? | '\\assume';
INHALE      : 'inhale' {!escapedOnly}? | '\\inhale';
EXHALE      : 'exhale' {!escapedOnly}? | '\\exhale';
PRE         : 'requires' {!escapedOnly}? | '\\requires';
PRESERVES   : 'preserves' {!escapedOnly}? | '\\preserves';
POST        : 'ensures' {!escapedOnly}? | '\\ensures';
INV         : 'invariant' {!escapedOnly}? | '\\invariant';
DEC         : ('decreases' {!escapedOnly}? | '\\decreases') -> mode(NLSEMI);
PURE        : ('pure' {!escapedOnly}? | '\\pure') -> mode(NLSEMI);
IMPL        : 'implements' {!escapedOnly}? | '\\implements';
OLD         : 'old'-> mode(NLSEMI);
LHS         : '#lhs';
FORALL      : 'forall' {!escapedOnly}? | '\\forall';
EXISTS      : 'exists' {!escapedOnly}? | '\\exists';
ACCESS      : 'acc' -> mode(NLSEMI);
FOLD        : 'fold' {!escapedOnly}? | '\\fold';
UNFOLD      : 'unfold' {!escapedOnly}? | '\\unfold';
UNFOLDING   : 'unfolding' {!escapedOnly}? | '\\unfolding';
GHOST       : 'ghost' {!escapedOnly}? | '\\ghost';
IN          : 'in' {!escapedOnly}? | '\\in';
MULTI       : '#';
SUBSET      : 'subset' {!escapedOnly}? | '\\subset';
UNION       : 'union' {!escapedOnly}? | '\\union';
INTERSECTION: 'intersection' {!escapedOnly}? | '\\intersection';
SETMINUS    : 'setminus' {!escapedOnly}? | '\\setminus';
IMPLIES     : '==>';
WAND        : '--*';
APPLY       : 'apply' {!escapedOnly}? | '\\apply';
QMARK       : '?';
L_PRED      : '!<';
R_PRED      : '!>' -> mode(NLSEMI);
SEQ         : 'seq'-> mode(NLSEMI);
SET         : 'set'-> mode(NLSEMI);
MSET        : 'mset'-> mode(NLSEMI);
DICT        : 'dict'-> mode(NLSEMI);
OPT         : 'option'-> mode(NLSEMI);
LEN         : 'len'-> mode(NLSEMI);
NEW         : 'new'-> mode(NLSEMI);
MAKE        : 'make'-> mode(NLSEMI);
CAP         : 'cap'-> mode(NLSEMI);
SOME        : 'some'-> mode(NLSEMI);
GET         : 'get'-> mode(NLSEMI);
DOM         : 'domain'-> mode(NLSEMI);
AXIOM       : 'axiom'-> mode(NLSEMI);
NONE        : 'none' -> mode(NLSEMI);
PRED        : 'pred' {!escapedOnly}? | '\\pred';
TYPE_OF      : 'typeOf'-> mode(NLSEMI);
IS_COMPARABLE: 'isComparable'-> mode(NLSEMI);
SHARE       : 'share' {!escapedOnly}? | '\\share';
ADDR_MOD    : '@'-> mode(NLSEMI);
DOT_DOT     : '..';
WRITEPERM   : 'writePerm' -> mode(NLSEMI);
NOPERM      : 'noPerm' -> mode(NLSEMI);
TRUSTED     : ('trusted' {!escapedOnly}? | '\\trusted') -> mode(NLSEMI);