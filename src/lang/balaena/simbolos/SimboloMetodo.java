package lang.balaena.simbolos;

import lang.balaena.codigo.Code;

/**
 * Entrada de método na tabela de símbolo
 * 
 */
public class SimboloMetodo extends SimboloEntrada {

	// Tipo do método (inteiro, decimal, texto ou vazio)
	private SimboloEntrada tipo;

	// Dimensão do retorno (para arrays)
	private int tamanho;

	// Tipos dos parâmetros
	private SimboloParametro parametros;

	// Número de variáveis locais
	private int totalLocal;

	// Tamanho da pilha
	private int tamanhoPilha;

	// Tabela de símbolos local do método
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
