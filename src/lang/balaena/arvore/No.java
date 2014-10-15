package lang.balaena.arvore;

import lang.balaena.Token;

public class No {

	private Token token;
	private int numero;

	public No(Token ref) {
		this.token = ref;
	}

	public Token getToken() {
		return token;
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	@Override
	public String toString() {
		return String.valueOf(numero) + ": " + this.getClass().getSimpleName();
	}

	public static String getClasse(No no) {
		return no == null ? "nulo" : no.getClass().getSimpleName();
	}

	public static String toString(No no) {
		return no == null ? "" : "\n" + no.toString();
	}

	public static String getNumero(No no) {
		return no == null ? "nulo" : String.valueOf(no.getNumero());
	}

	public static String getToken(Token token) {
		return token == null ? "nulo" : token.image;
	}

}
