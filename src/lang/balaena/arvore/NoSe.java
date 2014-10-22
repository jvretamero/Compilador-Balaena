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

	@Override
	public void setNumero(int numero) {
		super.setNumero(numero);
		if (condicao != null) {
			condicao.setNumero(++numero);
		}
		if (verdadeiro != null) {
			verdadeiro.setNumero(++numero);
		}
		if (falso != null) {
			falso.setNumero(++numero);
		}
	}

	public NoExpressao getCondicao() {
		return condicao;
	}

	public NoBloco getVerdadeiro() {
		return verdadeiro;
	}

	public NoBloco getFalso() {
		return falso;
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getNumero(condicao) + " "
				+ No.getNumero(verdadeiro) + " " + No.getNumero(falso)
				+ No.toString(condicao) + No.toString(verdadeiro)
				+ No.toString(falso);
	}

}
