package lang.balaena.simbolos;

public class Tipo {

	private SimboloEntrada entrada;
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

}
