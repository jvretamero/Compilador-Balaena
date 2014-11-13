package lang.balaena.recuperacao;

import java.util.HashSet;
import java.util.Iterator;

import lang.balaena.BLangMotor;

public class Recuperacao extends HashSet<Integer> {

	private static final long serialVersionUID = -5362872636132935828L;

	public Recuperacao() {
		super();
	}

	public Recuperacao(int token) {
		super();
		add(new Integer(token));
	}

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
