package lang.balaena.arvore;

import lang.balaena.Token;

public class NoVariavelDecl extends NoDeclaracao {

	private NoLista variaveis;

	public NoVariavelDecl(Token tipo, NoLista variaveis) {
		super(tipo);
		this.variaveis = variaveis;
	}

	public NoVariavelDecl(Token tipo, NoVariavel variavel) {
		super(tipo);
		this.variaveis = new NoLista(variavel);
	}

	@Override
	public void setNumero(int numero) {
		super.setNumero(numero);
		if (variaveis != null) {
			variaveis.setNumero(++numero);
		}
	}

	public NoLista getVariaveis() {
		return variaveis;
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getNumero(variaveis)
				+ No.toString(variaveis);
	}

}
