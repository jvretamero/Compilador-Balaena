package lang.balaena.recuperacao;

/**
 * Exce��o lan�ada ao encontrar o final do arquivo prematuramente
 *
 */
public class ParseEOFException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Construtor padr�o
	 * 
	 * @param mensagem
	 *            Mensagem a ser exibida
	 */
	public ParseEOFException(String mensagem) {
		super(mensagem);
	}

}
