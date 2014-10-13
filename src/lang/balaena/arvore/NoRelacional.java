package lang.balaena.arvore;

import lang.balaena.Token;

public class NoRelacional extends NoExpressao {

	private NoExpressao esquerda;
	private NoExpressao direita;

	public NoRelacional(Token op, NoExpressao esquerda, NoExpressao direita) {
		super(op);
		this.esquerda = esquerda;
		this.direita = direita;
	}

}
