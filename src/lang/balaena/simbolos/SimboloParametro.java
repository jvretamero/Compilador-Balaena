package lang.balaena.simbolos;

public class SimboloParametro extends SimboloEntrada {

	// Tipo do par�metro
	private SimboloEntrada tipo;

	// Tamanho do par�metro (para arrays)
	private int tamanho;

	// Pr�ximo simbolo da lista de par�metros
	private SimboloParametro proximo;

	// Contagem de elementos a partir deste
	private int elementos;

	// Cria o primeiro elemento da lista
	public SimboloParametro(SimboloEntrada tipo, int tamanho, int elementos) {
		this.tipo = tipo;
		this.tamanho = tamanho;
		this.elementos = elementos;
		this.proximo = null;
	}

	// Cria um novo elemento colocando-o no topo da lista
	public SimboloParametro(SimboloEntrada tipo, int tamanho, int elementos,
			SimboloParametro proximo) {
		this.tipo = tipo;
		this.tamanho = tamanho;
		this.elementos = elementos;
		this.proximo = proximo;
	}

	public SimboloParametro inverte(SimboloParametro ant) {
		SimboloParametro invertido = this;

		if (proximo != null) {
			invertido = proximo.inverte(this);
		}

		elementos = ant.elementos + 1;
		proximo = ant;

		return invertido;
	}

	public SimboloParametro inverte() {
		SimboloParametro invertido = this;

		elementos = 1;

		if (proximo != null) {
			invertido = proximo.inverte(this);
		}

		proximo = null;

		return invertido;
	}

	public SimboloEntrada getTipo() {
		return tipo;
	}

	public int getTamanho() {
		return tamanho;
	}

	public SimboloParametro getProximo() {
		return proximo;
	}

	public int getElementos() {
		return elementos;
	}

}
