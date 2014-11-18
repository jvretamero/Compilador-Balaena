package lang.balaena.recuperacao;

/**
 * Exceção lançada ao encontrar o final do arquivo prematuramente
 *
 */
public class ParseEOFException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Construtor padrão
	 * 
	 * @param mensagem
	 *            Mensagem a ser exibida
	 */
	public ParseEOFException(String mensagem) {
		super(mensagem);
	}

}
