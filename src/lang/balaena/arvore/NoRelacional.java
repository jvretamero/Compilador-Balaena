package lang.balaena.arvore;

import lang.balaena.Token;

public class NoRelacional extends NoExpressao {

	private NoExpressao esquerda;
	private NoExpressao direita;

	public NoRelacional(Token op, NoExpressao esquerda, NoExpressao direita) {
		super(op);
		this.esquerda = esquerda;
		this.direita = direita;
	}

	@Override
	public void setNumero(int numero) {
		super.setNumero(numero);
		if (esquerda != null) {
			esquerda.setNumero(++numero);
		}
		if (direita != null) {
			direita.setNumero(++numero);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getNumero(esquerda) + " "
				+ No.getNumero(direita) + No.toString(esquerda)
				+ No.toString(direita);
	}

}
