package lang.balaena.semantico;

import lang.balaena.Token;

/**
 * Exceção lançada ao encontrar um erro semântico
 *
 */
public class ErroSemanticoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Construtor padrão
	 * 
	 * @param token
	 *            Token de referência do erro semântico
	 * @param msg
	 *            Mensagem a ser exibida
	 */
	public ErroSemanticoException(Token token, String msg) {
		super("Linha " + token.beginLine + ", coluna " + token.beginColumn
				+ ": " + msg);
	}

}
