package lang.balaena.arvore;

import lang.balaena.Token;

public class NoBloco extends No {

	private NoLista declaracoes;

	public NoBloco(Token ref, NoLista declaracoes) {
		super(ref);
		this.declaracoes = declaracoes;
	}

}
