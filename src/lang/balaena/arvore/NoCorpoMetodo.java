package lang.balaena.arvore;

import lang.balaena.Token;

public class NoCorpoMetodo extends No {

	private NoLista parametros;
	private NoBloco bloco;

	public NoCorpoMetodo(Token ref, NoLista parametros, NoBloco bloco) {
		super(ref);
		this.parametros = parametros;
		this.bloco = bloco;
	}

	@Override
	public void setNumero(int numero) {
		super.setNumero(numero);
		if (parametros != null) {
			parametros.setNumero(++numero);
		}
		if (bloco != null) {
			bloco.setNumero(++numero);
		}
	}

	public NoLista getParametros() {
		return parametros;
	}

	public NoBloco getBloco() {
		return bloco;
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getNumero(parametros) + " "
				+ No.getNumero(bloco) + No.toString(parametros)
				+ No.toString(bloco);
	}

}
