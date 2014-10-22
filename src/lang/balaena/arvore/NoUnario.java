package lang.balaena.arvore;

import lang.balaena.Token;

public class NoUnario extends NoExpressao {

	private NoExpressao fator;

	public NoUnario(Token op, NoExpressao fator) {
		super(op);
		this.fator = fator;
	}

	@Override
	public void setNumero(int numero) {
		super.setNumero(numero);
		if (fator != null) {
			fator.setNumero(++numero);
		}
	}

	public NoExpressao getFator() {
		return fator;
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getNumero(fator)
				+ No.toString(fator);
	}

}
