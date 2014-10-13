package lang.balaena.arvore;

import lang.balaena.Token;

public class NoAlocacao extends NoExpressao {

	private Token tipo;
	private NoLista tamanho;

	public NoAlocacao(Token novo, Token tipo, NoLista tamanho) {
		super(novo);
		this.tipo = tipo;
		this.tamanho = tamanho;
	}

}
