package lang.balaena.arvore;

import lang.balaena.Token;

public class NoRetornar extends NoDeclaracao {

	private NoExpressao valor;

	public NoRetornar(Token retornar, NoExpressao valor) {
		super(retornar);
		this.valor = valor;
	}

}
