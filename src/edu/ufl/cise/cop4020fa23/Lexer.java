/*Copyright 2023 by Beverly A Sanders
* 
* This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
* University of Florida during the fall semester 2023 as part of the course project.  
* 
* No other use is authorized. 
* 
* This code may not be posted on a public web site either during or after the course.  
*/
package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;

import java.util.HashMap;
import java.util.Map;

import static edu.ufl.cise.cop4020fa23.Kind.*;

public class Lexer implements ILexer {

	private enum State {START, HAVE_LSQUARE, HAVE_HASH, COMMENT, HAVE_AND, HAVE_LT, HAVE_EQ, HAVE_MINUS, HAVE_GT, HAVE_COLON, HAVE_STAR, HAVE_OR, IN_IDENT, IN_NUM, IN_STRING}

	private final Map<String, Kind> reserved = new HashMap<>();
	private final Map<String, Kind> constant = new HashMap<>();
	private final Map<String, Kind> bool = new HashMap<>();

	String input;
	char[] chars;
	int pos;
	State state;

	int line;
	int column;

	public Lexer(String input) {
		this.input = input;
		this.pos = 0;

		this.line = 1;
		this.column = 1;

		//Convert input into a char array for easier iteration
		this.chars = input.toCharArray();

		//Reserved words
		reserved.put("image",RES_image);
		reserved.put("pixel",RES_pixel);
		reserved.put("int",RES_int);
		reserved.put("string",RES_string);
		reserved.put("void",RES_void);
		reserved.put("boolean",RES_boolean);
		reserved.put("write",RES_write);
		reserved.put("height",RES_height);
		reserved.put("width",RES_width);
		reserved.put("if",RES_if);
		reserved.put("fi",RES_fi);
		reserved.put("do",RES_do);
		reserved.put("od",RES_od);
		reserved.put("red",RES_red);
		reserved.put("green",RES_green);
		reserved.put("blue",RES_blue);

		//Constants
		constant.put("Z",CONST);
		constant.put("BLACK",CONST);
		constant.put("BLUE",CONST);
		constant.put("CYAN",CONST);
		constant.put("DARK_GRAY",CONST);
		constant.put("GRAY",CONST);
		constant.put("GREEN",CONST);
		constant.put("LIGHT_GRAY",CONST);
		constant.put("MAGENTA",CONST);
		constant.put("ORANGE",CONST);
		constant.put("PINK",CONST);
		constant.put("RED",CONST);
		constant.put("WHITE",CONST);
		constant.put("YELLOW",CONST);

		//Boolean literals
		bool.put("TRUE", BOOLEAN_LIT);
		bool.put("FALSE", BOOLEAN_LIT);
	}

	@Override
	public IToken next() throws LexicalException {

		//Initialize to Start state for new token
		state = State.START;

		//Start location and length of token
		int startPos = this.pos;
		SourceLocation startLoc = new SourceLocation(this.line,this.column);

		//Character loop
		while(true){

			//Get next char
			char c = ' ';
			if(this.pos < chars.length)
				c = chars[this.pos];

			//EOF, finish the last token
			else{
				switch(this.state){
					case START -> {return new Token(EOF, startPos, 0, this.chars, startLoc);}
					case HAVE_LSQUARE -> {return new Token(LSQUARE, startPos, 1, this.chars, startLoc);}
					case HAVE_GT -> {return new Token(GT, startPos, 1, this.chars, startLoc);}
					case HAVE_LT -> {return new Token(LT, startPos, 1, this.chars, startLoc);}
					case HAVE_MINUS -> {return new Token(MINUS, startPos, 1, this.chars, startLoc);}
					case HAVE_AND -> {return new Token(BITAND, startPos, 1, this.chars, startLoc);}
					case HAVE_OR -> {return new Token(BITOR, startPos, 1, this.chars, startLoc);}
					case HAVE_EQ -> {return new Token(ASSIGN, startPos, 1, this.chars, startLoc);}
					case HAVE_COLON -> {return new Token(COLON, startPos, 1, this.chars, startLoc);}
					case HAVE_STAR -> {return new Token(TIMES, startPos, 1, this.chars, startLoc);}
					case IN_IDENT -> {
						//Temp token to check map keys
						Token token = new Token(IDENT, startPos, this.pos-startPos, this.chars, startLoc);

						//Check for reserved words, constants, and boolean literals
						if(reserved.containsKey(token.text()))
							return new Token(reserved.get(token.text()), startPos, this.pos-startPos, this.chars, startLoc);
						else if(constant.containsKey(token.text()))
							return new Token(CONST, startPos, this.pos-startPos, this.chars, startLoc);
						else if(bool.containsKey(token.text()))
							return new Token(BOOLEAN_LIT, startPos, this.pos-startPos, this.chars, startLoc);

						return new Token(IDENT, startPos, this.pos-startPos, this.chars, startLoc);
					}
					case IN_NUM -> {return new Token(NUM_LIT, startPos, this.pos-startPos, this.chars, startLoc);}
					case HAVE_HASH -> throw new LexicalException(startLoc, "Token not defined by lexical structure");
				}
			}

			this.pos++;
			this.column++;

			switch(this.state){
				case START -> {
					switch (c) {
						case ' ', '\t', '\r' -> {
							startPos = this.pos;
							startLoc = new SourceLocation(this.line,this.column);
						}
						case '\n' -> {
							this.line++;
							this.column = 1;
							startPos = this.pos;
							startLoc = new SourceLocation(this.line,this.column);
						}
						//Operators and Separators
						case ',' -> {return new Token(COMMA, startPos, 1, this.chars, startLoc);}
						case '%' -> {return new Token(MOD, startPos, 1, this.chars, startLoc);}
						case '+' -> {return new Token(PLUS, startPos, 1, this.chars, startLoc);}
						case '[' -> this.state = State.HAVE_LSQUARE;
						case ']' -> {return new Token(RSQUARE, startPos, 1, this.chars, startLoc);}
						case '/' -> {return new Token(DIV, startPos, 1, this.chars, startLoc);}
						case '?' -> {return new Token(QUESTION, startPos, 1, this.chars, startLoc);}
						case '!' -> {return new Token(BANG, startPos, 1, this.chars, startLoc);}
						case ';' -> {return new Token(SEMI, startPos, 1, this.chars, startLoc);}
						case ')' -> {return new Token(RPAREN, startPos, 1, this.chars, startLoc);}
						case '(' -> {return new Token(LPAREN, startPos, 1, this.chars, startLoc);}
						case '^' -> {return new Token(RETURN, startPos, 1, this.chars, startLoc);}
						case '&' -> this.state = State.HAVE_AND;
						case '<' -> this.state = State.HAVE_LT;
						case '>' -> this.state = State.HAVE_GT;
						case '=' -> this.state = State.HAVE_EQ;
						case '-' -> this.state = State.HAVE_MINUS;
						case ':' -> this.state = State.HAVE_COLON;
						case '*' -> this.state = State.HAVE_STAR;
						case '|' -> this.state = State.HAVE_OR;

						//Comments
						case '#' -> this.state = State.HAVE_HASH;

						//Identifiers
						case 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
								'a','b','c','d','e','f','g','h','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','_' -> this.state = State.IN_IDENT;

						//Num literals
						case '1','2','3','4','5','6','7','8','9' -> this.state = State.IN_NUM;
						case '0' -> {return new Token(NUM_LIT, startPos, 1, this.chars, startLoc);}

						//String literals
						case '"' -> this.state = State.IN_STRING;

						default -> throw new LexicalException(startLoc, "Lexical Exception OR Feature not yet implemented");
					}
				}

				case HAVE_LSQUARE -> {
					if(c == ']')
						return new Token(BOX, startPos, 2, this.chars, startLoc);
					this.pos--;
					this.column--;
					this.state = State.START;
					return new Token(LSQUARE, startPos, 1, this.chars, startLoc);
				}

				case HAVE_HASH -> {
					if(c == '#') this.state = State.COMMENT;
					else throw new LexicalException(startLoc, "Token not defined by lexical structure");
				}

				case COMMENT -> {
					if(c == '\n'){
						this.line++;
						this.column = 1;
						startPos = this.pos;
						startLoc = new SourceLocation(this.line,this.column);
						this.state = State.START;
					}
				}

				case HAVE_AND -> {
					if(c == '&')
						return new Token(AND, startPos, 2, this.chars, startLoc);
					this.pos--;
					this.column--;
					this.state = State.START;
					return new Token(BITAND, startPos, 1, this.chars, startLoc);
				}

				case HAVE_LT -> {
					switch(c){
						case '=' -> {return new Token(LE, startPos, 2, this.chars, startLoc);}
						case ':' -> {return new Token(BLOCK_OPEN, startPos, 2, this.chars, startLoc);}
					}
					this.pos--;
					this.column--;
					this.state = State.START;
					return new Token(LT, startPos, 1, this.chars, startLoc);
				}

				case HAVE_EQ -> {
					if(c == '=')
						return new Token(EQ, startPos, 2, this.chars, startLoc);
					this.pos--;
					this.column--;
					this.state = State.START;
					return new Token(ASSIGN, startPos, 1, this.chars, startLoc);
				}

				case HAVE_GT -> {
					if(c == '=')
						return new Token(GE, startPos, 2, this.chars, startLoc);
					this.pos--;
					this.column--;
					this.state = State.START;
					return new Token(GT, startPos, 1, this.chars, startLoc);
				}

				case HAVE_MINUS -> {
					if(c == '>')
						return new Token(RARROW, startPos, 2, this.chars, startLoc);
					this.pos--;
					this.column--;
					this.state = State.START;
					return new Token(MINUS, startPos, 1, this.chars, startLoc);
				}

				case HAVE_COLON -> {
					if(c == '>')
						return new Token(BLOCK_CLOSE, startPos, 2, this.chars, startLoc);
					this.pos--;
					this.column--;
					this.state = State.START;
					return new Token(COLON, startPos, 1, this.chars, startLoc);
				}

				case HAVE_STAR -> {
					if(c == '*')
						return new Token(EXP, startPos, 2, this.chars, startLoc);
					this.pos--;
					this.column--;
					this.state = State.START;
					return new Token(TIMES, startPos, 1, this.chars, startLoc);
				}

				case HAVE_OR -> {
					if(c == '|')
						return new Token(OR, startPos, 2, this.chars, startLoc);
					this.pos--;
					this.column--;
					this.state = State.START;
					return new Token(BITOR, startPos, 1, this.chars, startLoc);
				}

				case IN_IDENT -> {
					switch(c){
						case 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
								'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
								'_','0','1','2','3','4','5','6','7','8','9' -> {}
						default -> {
							this.pos--;
							this.column--;
							this.state = State.START;

							Token token = new Token(IDENT, startPos, this.pos-startPos, this.chars, startLoc);

							//Check for reserved words, constants, and boolean literals
							if(reserved.containsKey(token.text()))
								return new Token(reserved.get(token.text()), startPos, this.pos-startPos, this.chars, startLoc);
							else if(constant.containsKey(token.text()))
								return new Token(CONST, startPos, this.pos-startPos, this.chars, startLoc);
							else if(bool.containsKey(token.text()))
								return new Token(BOOLEAN_LIT, startPos, this.pos-startPos, this.chars, startLoc);

							return token;
						}
					}
				}

				case IN_NUM -> {
					switch(c){
						case '0','1','2','3','4','5','6','7','8','9' -> {}
						default -> {
							this.pos--;
							this.column--;
							this.state = State.START;

							Token token = new Token(NUM_LIT, startPos, this.pos-startPos, this.chars, startLoc);

							//Checking max int size
							try{
								Integer.parseInt(token.text());
							}
							catch (NumberFormatException e){
								throw new LexicalException(new SourceLocation(this.line,this.column), "NumberFormatException: Integer value too large");
							}

							return token;
						}
					}
				}

				case IN_STRING -> {
					if(c == '"'){
						this.state = State.START;
						return new Token(STRING_LIT, startPos, this.pos-startPos, this.chars, startLoc);
					}
					else if((int)c < 32 || (int)c > 126){
						throw new LexicalException(new SourceLocation(this.line,this.column), "Token not defined by lexical structure");
					}
				}

				default ->throw new IllegalStateException("Undefined Lexer Behavior");
			}
		}
	}


}
