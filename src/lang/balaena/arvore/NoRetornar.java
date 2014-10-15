package lang.balaena.arvore;

import lang.balaena.Token;

public class NoRetornar extends NoDeclaracao {

	private NoExpressao valor;

	public NoRetornar(Token retornar, NoExpressao valor) {
		super(retornar);
		this.valor = valor;
	}

	@Override
	public void setNumero(int numero) {
		super.setNumero(numero);
		if (valor != null) {
			valor.setNumero(++numero);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getNumero(valor)
				+ No.toString(valor);
	}

}
