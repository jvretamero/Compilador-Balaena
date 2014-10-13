package lang.balaena.arvore;

import lang.balaena.Token;

public class NoUnario extends NoExpressao {

	private NoExpressao fator;

	public NoUnario(Token op, NoExpressao fator) {
		super(op);
		this.fator = fator;
	}

}
