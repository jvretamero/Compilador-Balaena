package lang.balaena.simbolos;

import lang.balaena.codigo.Code;

/**
 * Entrada de m�todo na tabela de s�mbolo
 * 
 */
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

	// Tabela de s�mbolos local do m�todo
	private TabelaSimbolo tabela;

	public SimboloMetodo(SimboloEntrada tipo, String nome, int tamanho,
			SimboloParametro parametros) {
		super();
		this.tipo = tipo;
		this.setNome(nome);
		this.tamanho = tamanho;
		this.parametros = parametros;
		this.totalLocal = 0;
		this.tamanhoPilha = 0;
		this.tabela = new TabelaSimbolo(this);
	}

	public SimboloMetodo(SimboloEntrada tipo, String nome) {
		this(tipo, nome, 0, null);
	}

	public void setTotalLocal(int totalLocal) {
		this.totalLocal = totalLocal;
	}

	public void addPilha(int pilha) {
		this.tamanhoPilha += pilha;
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

	public TabelaSimbolo getTabela() {
		return tabela;
	}

	public String descJava() {
		return Code.descJava(tamanho) + tipo.descJava();
	}

}
