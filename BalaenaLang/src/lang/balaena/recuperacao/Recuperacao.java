package lang.balaena.recuperacao;

import java.util.HashSet;
import java.util.Iterator;

import lang.balaena.BLang;

public class Recuperacao extends HashSet<Integer> {

	private static final long serialVersionUID = -5362872636132935828L;

	public Recuperacao() {
		super();
	}

	public Recuperacao(int token) {
		super();
		add(new Integer(token));
	}

	public Recuperacao uniao(Recuperacao lista) {
		Recuperacao resultado = null;
		if (lista != null) {
			resultado = (Recuperacao) this.clone();
			resultado.addAll(lista);
		}
		return resultado;
	}

	public Recuperacao remove(int token) {
		Recuperacao resultado = (Recuperacao) this.clone();
		resultado.remove(token);
		return resultado;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		Iterator<Integer> iterator = this.iterator();

		while (iterator.hasNext()) {
			if (sb.length() > 0) {
				sb.append(", ");
			}

			sb.append(BLang.im(iterator.next()));
		}

		return sb.toString();
	}

}
