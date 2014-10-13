package lang.balaena.arvore;

import lang.balaena.Token;

public class NoArray extends NoExpressao {

	private NoLista tamanho;

	public NoArray(Token nome, NoLista tamanho) {
		super(nome);
		this.tamanho = tamanho;
	}

}
