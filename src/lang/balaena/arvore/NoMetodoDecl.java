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

	@Override
	public void setNumero(int numero) {
		super.setNumero(numero);
		if (corpo != null) {
			corpo.setNumero(++numero);
		}
	}

	public Token getTipo() {
		return tipo;
	}

	public int getTamanho() {
		return tamanho;
	}

	public Token getNome() {
		return nome;
	}

	public NoCorpoMetodo getCorpo() {
		return corpo;
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getToken(tipo) + " "
				+ String.valueOf(tamanho) + " " + No.getToken(nome) + " "
				+ No.getNumero(corpo) + No.toString(corpo);
	}

}
