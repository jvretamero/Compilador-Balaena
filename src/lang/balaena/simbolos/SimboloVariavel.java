package lang.balaena.simbolos;

public class SimboloVariavel extends SimboloEntrada {

	// Tipo da lista de variáveis
	private SimboloEntrada tipo;

	// Tamanho da variável (para arrays)
	private int tamanho;

	// Próximo elemento da lista de variáveis
	private SimboloVariavel proximo;

	// Construtor para uma nova lista de variaveis
	public SimboloVariavel(SimboloEntrada tipo, String nome, int tamanho) {
		this.tipo = tipo;
		this.setNome(nome);
		this.tamanho = tamanho;
		this.proximo = null;
	}

	// Constutor para um novo elemento na lista de variáveis, colocando-o no
	// topo da lista
	public SimboloVariavel(SimboloEntrada tipo, String nome, int tamanho,
			SimboloVariavel proximo) {
		this.tipo = tipo;
		this.setNome(nome);
		this.tamanho = tamanho;
		this.proximo = proximo;
	}

	public SimboloEntrada getTipo() {
		return tipo;
	}

	public int getTamanho() {
		return tamanho;
	}

	public SimboloVariavel getProximo() {
		return proximo;
	}

}
