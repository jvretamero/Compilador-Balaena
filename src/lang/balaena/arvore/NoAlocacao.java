package lang.balaena.arvore;

import lang.balaena.Token;

public class NoAlocacao extends NoExpressao {

	private Token tipo;
	private NoLista tamanho;

	public NoAlocacao(Token novo, Token tipo, NoLista tamanho) {
		super(novo);
		this.tipo = tipo;
		this.tamanho = tamanho;
	}

	@Override
	public void setNumero(int numero) {
		super.setNumero(numero);
		if (tamanho != null) {
			tamanho.setNumero(++numero);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getToken(tipo) + " "
				+ No.getNumero(tamanho) + No.toString(tamanho);
	}

}
