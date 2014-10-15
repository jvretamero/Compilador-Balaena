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

	@Override
	public void setNumero(int numero) {
		super.setNumero(numero);
		if (condicao != null) {
			condicao.setNumero(++numero);
		}
		if (bloco != null) {
			bloco.setNumero(++numero);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getNumero(condicao) + " "
				+ No.getNumero(bloco) + No.toString(condicao)
				+ No.toString(bloco);
	}

}
