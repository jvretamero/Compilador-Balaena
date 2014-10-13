package lang.balaena.arvore;

import lang.balaena.Token;

public class NoVariavel extends NoExpressao {

	private int tamanho;

	public NoVariavel(Token nome) {
		super(nome);
		this.tamanho = 0;
	}

	public NoVariavel(Token nome, int tamanho) {
		super(nome);
		this.tamanho = tamanho;
	}

}
