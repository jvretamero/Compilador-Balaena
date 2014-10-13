package lang.balaena.arvore;

import lang.balaena.Token;

public class NoVariavelDecl extends NoDeclaracao {

	private NoLista variaveis;

	public NoVariavelDecl(Token tipo, NoLista variaveis) {
		super(tipo);
		this.variaveis = variaveis;
	}
	
	public NoVariavelDecl(Token tipo, NoVariavel variavel) {
		super(tipo);
		this.variaveis = new NoLista(variavel);
	}

}
