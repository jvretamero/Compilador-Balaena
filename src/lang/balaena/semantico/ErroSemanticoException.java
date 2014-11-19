package lang.balaena.semantico;

import lang.balaena.Token;

/**
 * Exce��o lan�ada ao encontrar um erro sem�ntico
 *
 */
public class ErroSemanticoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Construtor padr�o
	 * 
	 * @param token
	 *            Token de refer�ncia do erro sem�ntico
	 * @param msg
	 *            Mensagem a ser exibida
	 */
	public ErroSemanticoException(Token token, String msg) {
		super("Linha " + token.beginLine + ", coluna " + token.beginColumn
				+ ": " + msg);
	}

}
