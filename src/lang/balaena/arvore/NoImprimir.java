package lang.balaena.arvore;

import lang.balaena.Token;

public class NoImprimir extends NoDeclaracao {

	private NoExpressao valor;

	public NoImprimir(Token imprimir, NoExpressao valor) {
		super(imprimir);
		this.valor = valor;
	}

}
