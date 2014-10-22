package lang.balaena.simbolos;

public class TabelaSimbolo {

	// Topo da tabela de s�mbolos
	private SimboloEntrada topo;

	// Escopo atual (aninhamento)
	private int escopo;

	// Entrada de m�todo de n�vel superior
	private SimboloMetodo acima;

	// Cria uma tabela totalmente vazia
	public TabelaSimbolo() {
		this.topo = null;
		this.acima = null;
		this.escopo = 0;
	}

	// Cria uma tabela vazia para o n�vel superior
	public TabelaSimbolo(SimboloMetodo metodo) {
		this.topo = null;
		this.acima = metodo;
		this.escopo = 0;
	}

	public void adiciona(SimboloEntrada entrada) {
		// Muda o topo atual para o pr�ximo da nova entrada
		entrada.setProximo(topo);

		// Define a entrada atual como topo
		this.topo = entrada;

		// Sincroniza o escopo
		entrada.setEscopo(escopo);

		// Vincula com a tabela de s�mbolo atual
		entrada.setPai(this);
	}

	public void iniciaEscopo() {
		// Inicia um novo escopo
		escopo++;
	}

	public void terminaEscopo() {
		// Remove todas as entradas do escopo atual
		while (topo != null && topo.getEscopo() == this.getEscopo()) {
			topo = topo.getProximo();
		}
		// Diminui o escopo
		escopo--;
	}

	public SimboloEntrada buscaEntrada(String nome) {
		SimboloEntrada t = topo;

		// Percorre todas entradas na tabela
		while (t != null) {
			// Verifica se � um tipo primitivo
			if (t instanceof SimboloSimples && t.getNome().equals(nome)) {
				return t;
			} else {
				// Se n�o for correspondente, busca na pr�xima entrada
				t = t.getProximo();
			}
		}

		// Se n�o encontrar e estiver no n�vel mais acima, retorna nulo
		if (acima == null) {
			return null;
		}

		// Busca no n�vel mais acima
		return acima.getPai().buscaEntrada(nome);
	}

	// Busca uma vari�vel no escopo
	public SimboloVariavel buscaVariavel(String nome) {
		SimboloEntrada v = topo;

		// Percorre todas as entradas
		while (v != null) {
			// Verifica o tipo correto
			if (v instanceof SimboloVariavel && v.getNome().equals(nome)) {
				return (SimboloVariavel) v;
			} else {
				v = v.getProximo();
			}
		}

		// Se atingir o n�vel mais alto e n�o encontrou, retorna nulo
		if (acima == null) {
			return null;
		}

		// Busca no n�vel mais alto
		return acima.getPai().buscaVariavel(nome);
	}

	// M�todo para buscar um m�todo na tabela de s�mbolos
	public SimboloMetodo buscaMetodo(String metodo, SimboloEntrada parametros) {
		SimboloEntrada t = topo;

		// Percorre todas entradas na tabela
		while (t != null) {
			// Verifica se � uma entrada de m�todo e se o nome � v�lido
			if (t instanceof SimboloMetodo && t.getNome().equals(metodo)) {
				// Converte para uma entrada de m�todo
				SimboloMetodo m = (SimboloMetodo) t;
				// Compara os par�metros
				if (m.getParametros() == null) {
					// Se os dois par�metros s�o nulos, est�o ok
					if (parametros == null) {
						return m;
					}
				} else {
					// Compara os dois parametros
					if (m.getParametros().equals(parametros)) {
						return m;
					}
				}
			}
			t = t.getProximo();
		}

		// S� executa este return se n�o encontrar o m�todo desejado
		return null;
	}

	public SimboloEntrada getTopo() {
		return topo;
	}

	public int getEscopo() {
		return escopo;
	}

	public SimboloMetodo getAcima() {
		return acima;
	}

}