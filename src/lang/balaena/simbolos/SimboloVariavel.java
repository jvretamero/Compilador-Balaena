package lang.balaena.simbolos;

/**
 * Entrada de vari�vel na tabela de s�mbolo
 *
 */
public class SimboloVariavel extends SimboloEntrada {

	// Tipo da lista de vari�veis
	private SimboloEntrada tipo;

	// Tamanho da vari�vel (para arrays)
	private int tamanho;

	// N�mero da variavel local
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

	// Constutor para um novo elemento na lista de vari�veis, colocando-o no
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
