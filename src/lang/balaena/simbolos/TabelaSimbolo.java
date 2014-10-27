package lang.balaena.simbolos;

public class TabelaSimbolo {

	// Topo da tabela de símbolos
	private SimboloEntrada topo;

	// Escopo atual (aninhamento)
	private int escopo;

	// Entrada de método de nível superior
	private SimboloMetodo acima;

	// Cria uma tabela totalmente vazia
	public TabelaSimbolo() {
		this.topo = null;
		this.acima = null;
		this.escopo = 0;
	}

	// Cria uma tabela vazia para o nível superior
	public TabelaSimbolo(SimboloMetodo metodo) {
		this.topo = null;
		this.acima = metodo;
		this.escopo = 0;
	}

	public void adiciona(SimboloEntrada entrada) {
		// Muda o topo atual para o próximo da nova entrada
		entrada.setProximo(topo);

		// Define a entrada atual como topo
		this.topo = entrada;

		// Sincroniza o escopo
		entrada.setEscopo(escopo);

		// Vincula com a tabela de símbolo atual
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

	public SimboloEntrada buscaTipo(String nome) {
		SimboloEntrada t;

		// Percorre todas entradas na tabela
		for (t = topo; t != null; t = t.getProximo()) {
			// Verifica se é um tipo declarado na tabela de símbolo
			if (t instanceof SimboloSimples && t.getNome().equals(nome)) {
				return t;
			}
		}

		// Se não encontrar e estiver no nível mais acima, retorna nulo
		if (acima == null) {
			return null;
		}

		// Busca no nível mais acima
		return acima.getPai().buscaTipo(nome);
	}

	// Busca uma variável no escopo
	public SimboloVariavel buscaVariavel(String nome) {
		SimboloEntrada v;

		// Percorre todas as entradas
		for (v = topo; v != null; v = v.getProximo()) {
			// Verifica o tipo correto
			if (v instanceof SimboloVariavel && v.getNome().equals(nome)) {
				return (SimboloVariavel) v;
			}
		}

		// Se atingir o nível mais alto e não encontrou, retorna nulo
		if (acima == null) {
			return null;
		}

		// Busca no nível mais alto
		return acima.getPai().buscaVariavel(nome);
	}

	// Método para buscar um método na tabela de símbolos
	public SimboloMetodo buscaMetodo(String metodo, SimboloEntrada parametros) {
		SimboloEntrada t = topo;

		// Percorre todas entradas na tabela
		for (t = topo; t != null; t = t.getProximo()) {
			// Verifica se é uma entrada de método e se o nome é válido
			if (t instanceof SimboloMetodo && t.getNome().equals(metodo)) {
				// Converte para uma entrada de método
				SimboloMetodo m = (SimboloMetodo) t;
				// Compara os parâmetros
				if (m.getParametros() == null) {
					// Se os dois parâmetros são nulos, estão ok
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
		}

		if (acima == null) {
			return null;
		}

		return acima.getPai().buscaMetodo(metodo, parametros);
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
