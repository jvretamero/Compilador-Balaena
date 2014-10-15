package lang.balaena.arvore;

import lang.balaena.Token;

public class NoNulo extends NoExpressao {

	public NoNulo(Token nulo) {
		super(nulo);
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getToken(getToken());
	}

}
