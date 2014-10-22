package lang.balaena.arvore;

import lang.balaena.Token;

public class NoBloco extends No {

	private NoLista declaracoes;

	public NoBloco(Token ref, NoLista declaracoes) {
		super(ref);
		this.declaracoes = declaracoes;
	}

	@Override
	public void setNumero(int numero) {
		super.setNumero(numero);
		if (declaracoes != null) {
			declaracoes.setNumero(++numero);
		}
	}

	public NoLista getDeclaracoes() {
		return declaracoes;
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getNumero(declaracoes)
				+ No.toString(declaracoes);
	}
}
