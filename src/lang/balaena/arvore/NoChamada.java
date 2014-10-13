package lang.balaena.arvore;

import lang.balaena.Token;

public class NoChamada extends NoExpressao {

	private NoLista argumentos;

	public NoChamada(Token metodo, NoLista argumentos) {
		super(metodo);
		this.argumentos = argumentos;
	}

}
