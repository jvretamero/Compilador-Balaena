package lang.balaena.arvore;

import lang.balaena.Token;

public class NoCorpoMetodo extends No {

	private NoLista parametros;
	private NoBloco bloco;

	public NoCorpoMetodo(Token ref, NoLista parametros, NoBloco bloco) {
		super(ref);
		this.parametros = parametros;
		this.bloco = bloco;
	}

}
