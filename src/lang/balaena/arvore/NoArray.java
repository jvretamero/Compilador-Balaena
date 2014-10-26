package lang.balaena.arvore;

import lang.balaena.Token;

public class NoArray extends NoExpressao {

	private NoLista expressoes;

	public NoArray(Token nome, NoLista expressoes) {
		super(nome);
		this.expressoes = expressoes;
	}

	public NoLista getExpressoes() {
		return expressoes;
	}

	@Override
	public void setNumero(int numero) {
		super.setNumero(numero);
		if (expressoes != null) {
			expressoes.setNumero(++numero);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getNumero(expressoes)
				+ No.toString(expressoes);
	}

}
