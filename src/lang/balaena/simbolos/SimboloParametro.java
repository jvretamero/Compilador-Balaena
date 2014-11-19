package lang.balaena.simbolos;

/**
 * Entrada de par�metro na tabela de s�mbolo
 *
 */
public class SimboloParametro extends SimboloEntrada {

	// Tipo do par�metro
	private SimboloEntrada tipo;

	// Tamanho do par�metro (para arrays)
	private int tamanho;

	// Pr�ximo simbolo da lista de par�metros
	private SimboloParametro proximo;

	// Contagem de elementos a partir deste (contando este)
	private int elementos;

	// Cria o primeiro elemento da lista
	public SimboloParametro(SimboloEntrada tipo, int tamanho) {
		super();
		this.tipo = tipo;
		this.tamanho = tamanho;
		this.elementos = 1;
		this.proximo = null;
	}

	// Define o pr�ximo elemento da lista
	public void setProximo(SimboloParametro prox) {
		if (this.proximo == null) {
			this.elementos++;
			this.proximo = prox;
		} else {
			this.proximo.setProximo(prox);
		}
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

	public String descJava() {
		return tipo.descJava() + (proximo != null ? proximo.descJava() : "");
	}

}
