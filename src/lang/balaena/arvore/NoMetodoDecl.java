package lang.balaena.arvore;

import lang.balaena.Token;

public class NoMetodoDecl extends No {

	private Token tipo;
	private int tamanho;
	private Token nome;
	private NoCorpoMetodo corpo;

	public NoMetodoDecl(Token tipo, int tamanho, Token nome, NoCorpoMetodo corpo) {
		super(tipo);
		this.tipo = tipo;
		this.tamanho = tamanho;
		this.nome = nome;
		this.corpo = corpo;
	}

}
