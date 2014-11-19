package lang.balaena.simbolos;

import lang.balaena.codigo.Code;

/**
 * Tipo primitivo vinculado a uma entrada na tabela de símbolo
 *
 */
public class Tipo {

	// Entrada na tabela de símbolo
	private SimboloEntrada entrada;

	// Tamanho do array
	public int tamanho;

	public Tipo(SimboloEntrada entrada, int tamanho) {
		this.entrada = entrada;
		this.tamanho = tamanho;
	}

	public SimboloEntrada getEntrada() {
		return entrada;
	}

	public int getTamanho() {
		return tamanho;
	}

	public String descJava() {
		return Code.descJava(tamanho) + entrada.descJava();
	}

}
