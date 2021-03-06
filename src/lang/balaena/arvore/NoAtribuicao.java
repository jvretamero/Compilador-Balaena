package lang.balaena.arvore;

import lang.balaena.Token;

public class NoAtribuicao extends NoDeclaracao {

	private NoExpressao esquerda;
	private NoExpressao direita;

	public NoAtribuicao(Token igual, NoExpressao esquerda, NoExpressao direita) {
		super(igual);
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

	public NoExpressao getEsquerda() {
		return esquerda;
	}

	public NoExpressao getDireita() {
		return direita;
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getNumero(esquerda) + " "
				+ No.getNumero(direita) + No.toString(esquerda)
				+ No.toString(direita);
	}

}
