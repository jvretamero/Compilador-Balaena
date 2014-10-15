package lang.balaena.arvore;

import lang.balaena.Token;

public class NoTexto extends NoExpressao {

	public NoTexto(Token texto) {
		super(texto);
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getToken(getToken());
	}

}
