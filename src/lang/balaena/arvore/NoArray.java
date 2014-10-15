package lang.balaena.arvore;

import lang.balaena.Token;

public class NoArray extends NoExpressao {

	private NoLista tamanho;

	public NoArray(Token nome, NoLista tamanho) {
		super(nome);
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
		return super.toString() + " > " + No.getNumero(tamanho)
				+ No.toString(tamanho);
	}

}
