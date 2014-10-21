package lang.balaena.simbolos;

public abstract class SimboloEntrada {

	// Nome do s�mbolo (vari�vel ou m�todo)
	private String nome;

	// Pr�ximo item da lista
	private SimboloEntrada proximo;

	// Escopo da entrada
	private int escopo;

	// Tabela de s�mbolo pai
	private TabelaSimbolo pai;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public SimboloEntrada getProximo() {
		return proximo;
	}

	public void setProximo(SimboloEntrada proximo) {
		this.proximo = proximo;
	}

	public int getEscopo() {
		return escopo;
	}

	public void setEscopo(int escopo) {
		this.escopo = escopo;
	}

	public TabelaSimbolo getPai() {
		return pai;
	}

	public void setPai(TabelaSimbolo pai) {
		this.pai = pai;
	}

}
