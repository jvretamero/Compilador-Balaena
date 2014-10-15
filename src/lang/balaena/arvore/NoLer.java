package lang.balaena.arvore;

import lang.balaena.Token;

public class NoLer extends NoDeclaracao {

	private NoExpressao variavel;

	public NoLer(Token ler, NoExpressao variavel) {
		super(ler);
		this.variavel = variavel;
	}

	@Override
	public void setNumero(int numero) {
		super.setNumero(numero);
		if (variavel != null) {
			variavel.setNumero(++numero);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getNumero(variavel)
				+ No.toString(variavel);
	}

}
