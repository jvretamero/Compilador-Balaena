package lang.balaena.semantico;

import lang.balaena.Token;

public class ErroSemanticoException extends Exception {

	private static final long serialVersionUID = 1L;

	public ErroSemanticoException(Token token, String msg) {
		super("Linha " + token.beginLine + ", coluna " + token.beginColumn
				+ ": " + msg);
	}

	public ErroSemanticoException(String msg) {
		super(msg);
	}

}
