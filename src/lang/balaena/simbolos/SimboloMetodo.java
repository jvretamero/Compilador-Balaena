package lang.balaena.simbolos;

public class SimboloMetodo extends SimboloEntrada {

	// Tipo do m�todo (inteiro, decimal, texto ou vazio)
	private SimboloEntrada tipo;

	// Dimens�o do retorno (para arrays)
	private int tamanho;

	// Tipos dos par�metros
	private SimboloParametro parametros;

	// N�mero de vari�veis locais
	private int totalLocal;

	// Tamanho da pilha
	private int tamanhoPilha;

	public SimboloMetodo(SimboloEntrada tipo, String nome, int tamanho,
			SimboloParametro parametros) {
		this.tipo = tipo;
		this.setNome(nome);
		this.tamanho = tamanho;
		this.parametros = parametros;
		this.totalLocal = 0;
		this.tamanhoPilha = 0;
	}

	public SimboloMetodo(SimboloEntrada tipo, String nome) {
		this.tipo = tipo;
		this.setNome(nome);
	}

	public void setTotalLocal(int totalLocal) {
		this.totalLocal = totalLocal;
	}

	public SimboloEntrada getTipo() {
		return tipo;
	}

	public int getTamanho() {
		return tamanho;
	}

	public SimboloParametro getParametros() {
		return parametros;
	}

	public int getTamanhoPilha() {
		return tamanhoPilha;
	}

	public int getTotalLocal() {
		return totalLocal;
	}

}
