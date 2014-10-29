package lang.balaena.simbolos;

public class SimboloVariavel extends SimboloEntrada {

	// Tipo da lista de vari�veis
	private SimboloEntrada tipo;

	// Tamanho da vari�vel (para arrays)
	private int tamanho;

	// Pr�ximo elemento da lista de vari�veis
	private SimboloVariavel proximo;

	// N�mero da variavel local
	private int local;

	// Construtor para uma nova lista de variaveis
	public SimboloVariavel(SimboloEntrada tipo, String nome, int tamanho,
			int local) {
		super();
		this.tipo = tipo;
		this.setNome(nome);
		this.tamanho = tamanho;
		this.proximo = null;
		this.local = local;
	}

	// Constutor para um novo elemento na lista de vari�veis, colocando-o no
	// topo da lista
	public SimboloVariavel(SimboloEntrada tipo, String nome, int tamanho,
			int local, SimboloVariavel proximo) {
		super();
		this.tipo = tipo;
		this.setNome(nome);
		this.tamanho = tamanho;
		this.proximo = proximo;
		this.local = local;
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

	public int getLocal() {
		return local;
	}

}
