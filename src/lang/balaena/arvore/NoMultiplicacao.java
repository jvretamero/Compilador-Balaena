package lang.balaena.arvore;

import lang.balaena.Token;

public class NoMultiplicacao extends NoExpressao {

	private NoExpressao esquerda;
	private NoExpressao direita;

	public NoMultiplicacao(Token op, NoExpressao esquerda, NoExpressao direita) {
		super(op);
		this.esquerda = esquerda;
		this.direita = direita;
	}

}
