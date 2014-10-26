package lang.balaena.arvore;

import lang.balaena.Token;

public class NoChamada extends NoExpressao {

	private NoLista argumentos;

	public NoChamada(Token metodo, NoLista argumentos) {
		super(metodo);
		this.argumentos = argumentos;
	}

	public NoLista getArgumentos() {
		return argumentos;
	}

	@Override
	public void setNumero(int numero) {
		super.setNumero(numero);
		if (argumentos != null) {
			argumentos.setNumero(++numero);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getNumero(argumentos)
				+ No.toString(argumentos);
	}

}
