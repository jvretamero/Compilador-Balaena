package lang.balaena.arvore;

import lang.balaena.Token;

public class NoEnquanto extends NoDeclaracao {

	private NoExpressao condicao;
	private NoBloco bloco;

	public NoEnquanto(Token enquanto, NoExpressao condicao, NoBloco bloco) {
		super(enquanto);
		this.condicao = condicao;
		this.bloco = bloco;
	}

}
