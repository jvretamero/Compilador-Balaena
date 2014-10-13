package lang.balaena.arvore;

import lang.balaena.Token;

public class NoAtribuicao extends NoDeclaracao {

	private NoExpressao esquerda;
	private NoExpressao direita;

	public NoAtribuicao(Token igual, NoExpressao esquerda, NoExpressao direita) {
		super(igual);
		this.esquerda = esquerda;
		this.direita = direita;
	}

}
