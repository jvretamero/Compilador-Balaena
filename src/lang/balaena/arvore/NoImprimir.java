package lang.balaena.arvore;

import lang.balaena.Token;

public class NoImprimir extends NoDeclaracao {

	private NoExpressao valor;

	public NoImprimir(Token imprimir, NoExpressao valor) {
		super(imprimir);
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
