package lang.balaena.recuperacao;

import java.util.HashSet;
import java.util.Iterator;

import lang.balaena.BLangMotor;

/**
 * Conjunto de tokens de sincroniza��o
 *
 */
public class Recuperacao extends HashSet<Integer> {

	private static final long serialVersionUID = -5362872636132935828L;

	public Recuperacao() {
		super();
	}

	/**
	 * Construtor para inicar o conjunto
	 * 
	 * @param token
	 *            Primeiro token do conjunto
	 */
	public Recuperacao(int token) {
		super();
		add(new Integer(token));
	}

	/**
	 * Exibi��o do conjunto
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();

		Iterator<Integer> iterator = this.iterator();

		while (iterator.hasNext()) {
			if (sb.length() > 0) {
				sb.append(", ");
			}

			sb.append(BLangMotor.im(iterator.next()));
		}

		return sb.toString();
	}

}
