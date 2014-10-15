package lang.balaena.arvore;

import lang.balaena.Token;

public class NoDecimal extends NoExpressao {

	public NoDecimal(Token decimal) {
		super(decimal);
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getToken(getToken());
	}

}
