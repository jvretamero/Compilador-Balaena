package lang.balaena.simbolos;

/**
 * Entrada de variável na tabela de símbolo
 *
 */
public class SimboloVariavel extends SimboloEntrada {

	// Tipo da lista de variáveis
	private SimboloEntrada tipo;

	// Tamanho da variável (para arrays)
	private int tamanho;

	// Número da variavel local
	private int local;

	// Construtor para uma nova lista de variaveis
	public SimboloVariavel(SimboloEntrada tipo, String nome, int tamanho,
			int local) {
		super();
		this.tipo = tipo;
		this.setNome(nome);
		this.tamanho = tamanho;
		this.local = local;
	}

	// Constutor para um novo elemento na lista de variáveis, colocando-o no
	// topo da lista
	public SimboloEntrada getTipo() {
		return tipo;
	}

	public int getTamanho() {
		return tamanho;
	}

	public int getLocal() {
		return local;
	}

}
