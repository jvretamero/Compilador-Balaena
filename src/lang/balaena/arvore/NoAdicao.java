package lang.balaena.arvore;

import lang.balaena.Token;

public class NoAdicao extends NoExpressao {

	private NoExpressao esquerda;
	private NoExpressao direita;

	public NoAdicao(Token op, NoExpressao esquerda, NoExpressao direita) {
		super(op);
		this.esquerda = esquerda;
		this.direita = direita;
	}

}
