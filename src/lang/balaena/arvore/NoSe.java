package lang.balaena.arvore;

import lang.balaena.Token;

public class NoSe extends NoDeclaracao {

	private NoExpressao condicao;
	private NoBloco verdadeiro;
	private NoBloco falso;

	public NoSe(Token ref, NoExpressao condicao, NoBloco verdadeiro,
			NoBloco falso) {
		super(ref);
		this.condicao = condicao;
		this.verdadeiro = verdadeiro;
		this.falso = falso;
	}

}
