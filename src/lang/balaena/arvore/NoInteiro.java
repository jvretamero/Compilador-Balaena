package lang.balaena.arvore;

import lang.balaena.Token;

public class NoInteiro extends NoExpressao {

	public NoInteiro(Token inteiro) {
		super(inteiro);
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getToken(getToken());
	}

}
