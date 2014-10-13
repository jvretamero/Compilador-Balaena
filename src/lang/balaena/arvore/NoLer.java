package lang.balaena.arvore;

import lang.balaena.Token;

public class NoLer extends NoDeclaracao {

	private NoExpressao variavel;

	public NoLer(Token ler, NoExpressao variavel) {
		super(ler);
		this.variavel = variavel;
	}

}
