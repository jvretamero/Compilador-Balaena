package lang.balaena.semantico;

import lang.balaena.BLangMotorConstants;
import lang.balaena.arvore.NoAdicao;
import lang.balaena.arvore.NoAlocacao;
import lang.balaena.arvore.NoArray;
import lang.balaena.arvore.NoAtribuicao;
import lang.balaena.arvore.NoBloco;
import lang.balaena.arvore.NoChamada;
import lang.balaena.arvore.NoChamadaDecl;
import lang.balaena.arvore.NoCorpoMetodo;
import lang.balaena.arvore.NoDecimal;
import lang.balaena.arvore.NoDeclaracao;
import lang.balaena.arvore.NoEnquanto;
import lang.balaena.arvore.NoExpressao;
import lang.balaena.arvore.NoImprimir;
import lang.balaena.arvore.NoInteiro;
import lang.balaena.arvore.NoLer;
import lang.balaena.arvore.NoLista;
import lang.balaena.arvore.NoMetodoDecl;
import lang.balaena.arvore.NoMultiplicacao;
import lang.balaena.arvore.NoNulo;
import lang.balaena.arvore.NoRelacional;
import lang.balaena.arvore.NoRetornar;
import lang.balaena.arvore.NoSe;
import lang.balaena.arvore.NoTexto;
import lang.balaena.arvore.NoUnario;
import lang.balaena.arvore.NoVariavel;
import lang.balaena.arvore.NoVariavelDecl;
import lang.balaena.simbolos.SimboloEntrada;
import lang.balaena.simbolos.SimboloMetodo;
import lang.balaena.simbolos.SimboloParametro;
import lang.balaena.simbolos.SimboloSimples;
import lang.balaena.simbolos.SimboloVariavel;
import lang.balaena.simbolos.TabelaSimbolo;
import lang.balaena.simbolos.Tipo;

/**
 * Classe responsável por realizar a análise semântica do código fonte
 */
public class AnalisadorSemantico {

	// Raiz da árvore sintática
	private NoLista raiz;

	// Quantidade de erros semânticos
	private int erros;

	// Tabela de símbolos principal (nível mais alto)
	private TabelaSimbolo tabelaPrincipal;

	// Tabela de símbolos atual (alterada durante a mudança de escopo)
	private TabelaSimbolo tabelaAtual;

	// Total de variáveis locais
	private int totalLocal;

	// Tipo de retorno do método atual
	private Tipo tipoRetorno;

	// Entradas da tabela de símbolo dos tipos primitivos
	private final SimboloSimples tipoTexto = new SimboloSimples("texto");
	private final SimboloSimples tipoInteiro = new SimboloSimples("inteiro");
	private final SimboloSimples tipoDecimal = new SimboloSimples("decimal");
	private final SimboloSimples tipoVazio = new SimboloSimples("vazio");
	private final SimboloSimples tipoNulo = new SimboloSimples("nulo");

	// Inicia o analisador semântico a partir do nó raiz da árvore sintática
	public AnalisadorSemantico(NoLista raiz) {
		this.raiz = raiz;
		this.erros = 0;
	}

	/**
	 * @return Quantidade de erros semânticos encontrados
	 */
	public int getErros() {
		return erros;
	}

	/**
	 * @return Tabela de símbolo principal utilizada na análise semântica
	 */
	public TabelaSimbolo getTabela() {
		return tabelaPrincipal;
	}

	/**
	 * Método responsável por iniciar a análise semântica do código fonte
	 * 
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar erros semânticos
	 */
	public void analisa() throws ErroSemanticoException {
		System.out.println("Realizando análise semântica...");

		// Somente analisa se tiver uma árvore sintática válida
		if (raiz != null) {

			// Inicia o contador de erros
			erros = 0;

			// Instancia as tabelas de símbolo
			tabelaPrincipal = new TabelaSimbolo();
			tabelaAtual = new TabelaSimbolo();

			// Adiciona os tipos primitivos na tabela principal
			tabelaPrincipal.adiciona(tipoTexto);
			tabelaPrincipal.adiciona(tipoInteiro);
			tabelaPrincipal.adiciona(tipoDecimal);
			tabelaPrincipal.adiciona(tipoVazio);
			tabelaPrincipal.adiciona(tipoNulo);

			tabelaAtual = tabelaPrincipal;

			// Inicia a análise semântica e geração da tabela de símbolos nos
			// níveis mais altos
			simbolosNoListaMetodoDecl(raiz);

			// Inicia a análise semântica e geração da tabela de símbolos nos
			// níveis mais baixos
			analisaNoListaMetodoDecl(raiz);
		}
	}

	/**
	 * Método que faz a análise semântica e geração da tabela de símbolos nos
	 * níveis mais altos da lista de métodos
	 * 
	 * @param listaMetodos
	 *            Nó da árvore sintática correspondente à lista de métodos
	 */
	private void simbolosNoListaMetodoDecl(NoLista listaMetodos) {

		// Verifica lista de métodos vazia
		if (listaMetodos == null) {
			return;
		}

		try {
			// Analisa o NoMetodoDecl
			simbolosNoMetodoDecl((NoMetodoDecl) listaMetodos.getNo());
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}

		// Continua a análise através dos próximos nos
		simbolosNoListaMetodoDecl(listaMetodos.getProximo());
	}

	/**
	 * Método que faz a análise semântica e geração da tabela de símbolos no
	 * nível mais alto do método
	 * 
	 * @param metodo
	 *            Nó da árvore sintática correspondente ao método
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private void simbolosNoMetodoDecl(NoMetodoDecl metodo)
			throws ErroSemanticoException {

		// Verifica método vazio
		if (metodo == null) {
			return;
		}

		// Nós das variáveis
		NoVariavelDecl variavelDecl = null;
		NoVariavel variavel = null;

		// Entradas da tabela de símbolos
		SimboloEntrada tipo = null;
		SimboloMetodo m = null;
		SimboloParametro listaParametro = null;

		// Obtém os parâmetros do método
		NoLista parametros = metodo.getCorpo().getParametros();

		// Percorre todos os parâmetros
		while (parametros != null) {
			// Obtém a declaração da variável do parâmetro
			variavelDecl = (NoVariavelDecl) parametros.getNo();
			// Obtém a variável do parâmetro
			variavel = (NoVariavel) variavelDecl.getVariaveis().getNo();

			// Busca o tipo da variável na tabela de símbolos
			tipo = tabelaAtual.buscaTipo(variavelDecl.getToken().image);

			// Se não encontrou o tipo primitivo emite um erro
			if (tipo == null) {
				throw new ErroSemanticoException(variavelDecl.getToken(),
						"Tipo " + variavelDecl.getToken().image + " inválido");
			}

			// Cria o elemento da lista de parametros
			if (listaParametro == null) {
				listaParametro = new SimboloParametro(tipo,
						variavel.getTamanho());
			} else {
				listaParametro.setProximo(new SimboloParametro(tipo, variavel
						.getTamanho()));
			}

			// Vai para o próximo nó da árvore
			parametros = parametros.getProximo();
		}

		// Busca o tipo de retorno do método na tabela de símbolos
		tipo = tabelaAtual.buscaTipo(metodo.getToken().image);

		// Se o tipo de retorno não for encontrado emite um erro
		if (tipo == null) {
			throw new ErroSemanticoException(metodo.getToken(), "Tipo "
					+ metodo.getToken().image + " inválido");
		}

		// Busca o método na tabela de símbolos
		m = tabelaAtual.buscaMetodo(metodo.getNome().image, listaParametro);

		// Se não declarado ainda, inclui na tabela de símbolos
		if (m == null) {
			m = new SimboloMetodo(tipo, metodo.getNome().image,
					metodo.getTamanho(), listaParametro);
			tabelaAtual.adiciona(m);
		} else {
			// Se já declarado, emite um erro
			throw new ErroSemanticoException(metodo.getNome(), "Método "
					+ metodo.getNome().image + " já declarado");
		}
	}

	/**
	 * Método que faz a análise semântica e geração da tabela de símbolos nos
	 * níveis mais baixos da lista de métodos
	 * 
	 * @param metodos
	 *            Nó da árvore sintática correspondente à lista de métodos
	 */
	private void analisaNoListaMetodoDecl(NoLista metodos) {
		// Verifica lista vazia
		if (metodos == null) {
			return;
		}

		try {
			// Analisa um método da lista
			analisaNoMetodoDecl((NoMetodoDecl) metodos.getNo());
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}

		// Analisa os próximos métodos
		analisaNoListaMetodoDecl(metodos.getProximo());
	}

	/**
	 * Método que faz a análise semântica e geração da tabela de símbolos nos
	 * níveis mais maixos do método
	 * 
	 * @param metodo
	 *            Nó da árvore sintática correspondente ao método
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private void analisaNoMetodoDecl(NoMetodoDecl metodo)
			throws ErroSemanticoException {
		// Verifica método vazio
		if (metodo == null) {
			return;
		}

		// Salva a tabela de símbolo
		TabelaSimbolo temporaria = tabelaAtual;

		// Entradas da tabela de símbolo
		SimboloEntrada tipo = null;
		SimboloParametro listaParametro = null;
		SimboloMetodo simboloMetodo = null;

		// Nós da árvore sintática
		NoLista param = null;
		NoVariavelDecl varDecl = null;
		NoVariavel var = null;

		// Obtém os parâmetros do método
		param = metodo.getCorpo().getParametros();

		// Monta a entrada da tabela de símbolo dos parâmetros do método
		while (param != null) {

			// Obtém as variáveis
			varDecl = (NoVariavelDecl) param.getNo();
			var = (NoVariavel) varDecl.getVariaveis().getNo();

			// Busca o tipo da variável
			tipo = tabelaAtual.buscaTipo(varDecl.getToken().image);

			// Monta a entrada da tabela de símbolo
			if (listaParametro == null) {
				listaParametro = new SimboloParametro(tipo, var.getTamanho());
			} else {
				listaParametro.setProximo(new SimboloParametro(tipo, var
						.getTamanho()));
			}

			param = param.getProximo();
		}

		// Busca o método com a assinatura correspondente na tabela de símbolo
		simboloMetodo = tabelaAtual.buscaMetodo(metodo.getNome().image,
				listaParametro);

		// Obtém a tabela de símbolo do método
		tabelaAtual = simboloMetodo.getTabela();

		// Armazena o tipo de retorno do método
		tipoRetorno = new Tipo(simboloMetodo.getTipo(),
				simboloMetodo.getTamanho());

		// Variáveis locais
		totalLocal = 0;

		// Faz a análise do corpo do método
		analisaNoCorpoMetodo(metodo.getCorpo());

		// Atualiza o total de variáveis locais do método
		simboloMetodo.setTotalLocal(totalLocal);

		// Retoma a tabela de símbolos
		tabelaAtual = temporaria;
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos do
	 * corpo do método
	 * 
	 * @param corpo
	 *            Nó da árvore sintática correspondente ao corpo do método
	 */
	private void analisaNoCorpoMetodo(NoCorpoMetodo corpo) {
		// Verifica corpo do método vazio
		if (corpo == null) {
			return;
		}

		// Trata os parâmetros como variável local
		analisaNoListaVariaveis(corpo.getParametros());

		// Analisa o bloco do método
		analisaNoBloco(corpo.getBloco());
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos da
	 * lista de variáveis
	 * 
	 * @param variaveis
	 *            Nó da árvore sintática correspondente à lista de variáveis
	 */
	private void analisaNoListaVariaveis(NoLista variaveis) {
		// Verifica lista vazia
		if (variaveis == null) {
			return;
		}

		try {
			// Analisa um nó de declaração de variável
			analisaNoVariavel((NoVariavelDecl) variaveis.getNo());
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}

		// Analisa o próximo nó de declaração de variável
		analisaNoListaVariaveis(variaveis.getProximo());
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos de
	 * uma variável
	 * 
	 * @param variavel
	 *            Nó da árvore sintática correspondente a uma variável
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private void analisaNoVariavel(NoVariavelDecl variavel)
			throws ErroSemanticoException {
		// Nós da árvore sintática
		NoLista variaveis = null;
		NoVariavel var = null;

		// Entradas da tabela de símbolo
		SimboloVariavel v = null;
		SimboloEntrada tipo = null;

		// Busca o tipo na tabela local
		tipo = tabelaAtual.buscaTipo(variavel.getToken().image);

		// Verifica se encontrou o tipo
		if (tipo == null) {
			throw new ErroSemanticoException(variavel.getToken(), "Tipo \""
					+ variavel.getToken().image + "\" não encontrado");
		}

		// Percorre todas as variáveis
		for (variaveis = variavel.getVariaveis(); variaveis != null; variaveis = variaveis
				.getProximo()) {
			// Obtém a variável
			var = (NoVariavel) variaveis.getNo();

			// Busca a variável na tabela de símbolo
			v = tabelaAtual.buscaVariavel(var.getToken().image);

			// Verifica se não está redeclarado
			if (v != null) {
				if (v.getEscopo() == tabelaAtual.getEscopo()) {
					throw new ErroSemanticoException(variaveis.getToken(),
							"Variável \"" + variaveis.getToken().image
									+ "\" redeclarada");
				}
			}

			// Adiciona na tabela de símbolos
			tabelaAtual.adiciona(new SimboloVariavel(tipo,
					var.getToken().image, var.getTamanho(), totalLocal++));
		}
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos do
	 * bloco
	 * 
	 * @param bloco
	 *            Nó da árvore sintática correspondente ao bloco de código
	 */
	private void analisaNoBloco(NoBloco bloco) {
		// Verifica bloco vazio
		if (bloco == null) {
			return;
		}

		// Inicia o escopo do bloco
		tabelaAtual.iniciaEscopo();

		// Analisa o bloco
		analisaNoListaDeclaracao(bloco.getDeclaracoes());

		// Termina o escopo do bloco
		tabelaAtual.terminaEscopo();
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolo da
	 * lista de declarações
	 * 
	 * @param declaracoes
	 *            Nó da árvore sintática correspondente à lista de declaração
	 */
	private void analisaNoListaDeclaracao(NoLista declaracoes) {
		// Verifica lista vazia
		if (declaracoes == null) {
			return;
		}

		try {
			// Analisa a primeira declaração
			analisaNoDeclaracao((NoDeclaracao) declaracoes.getNo());
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}

		// Analisa as próximas declarações
		analisaNoListaDeclaracao(declaracoes.getProximo());
	}

	/**
	 * Método que faz a análise semântica e geração da tabela de símbolos da
	 * declaração
	 * 
	 * @param declaracao
	 *            Nó da árvore sintática correspondente a declaração
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private void analisaNoDeclaracao(NoDeclaracao declaracao)
			throws ErroSemanticoException {
		// Verifica declaração vazia
		if (declaracao == null) {
			return;
		}

		// Analisa a declaração conforme o tipo
		if (declaracao instanceof NoVariavelDecl) {
			analisaNoVariavel((NoVariavelDecl) declaracao);
		} else if (declaracao instanceof NoAtribuicao) {
			analisaNoAtribuicao((NoAtribuicao) declaracao);
		} else if (declaracao instanceof NoImprimir) {
			analisaNoImprimir((NoImprimir) declaracao);
		} else if (declaracao instanceof NoLer) {
			analisaNoLer((NoLer) declaracao);
		} else if (declaracao instanceof NoRetornar) {
			analisaNoRetornar((NoRetornar) declaracao);
		} else if (declaracao instanceof NoSe) {
			analisaNoSe((NoSe) declaracao);
		} else if (declaracao instanceof NoEnquanto) {
			analisaNoEnquanto((NoEnquanto) declaracao);
		} else if (declaracao instanceof NoChamadaDecl) {
			analisaNoChamadaDecl((NoChamadaDecl) declaracao);
		}
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos de
	 * uma atribuição
	 * 
	 * @param atribuicao
	 *            Nó da árvore sintática correspondete a uma atribuição
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private void analisaNoAtribuicao(NoAtribuicao atribuicao)
			throws ErroSemanticoException {
		// Verifica atribuição vazia
		if (atribuicao == null) {
			return;
		}

		// Verifica se o lado esquerdo da atribuição é um array ou uma variável
		if (!(atribuicao.getEsquerda() instanceof NoArray || atribuicao
				.getEsquerda() instanceof NoVariavel)) {
			throw new ErroSemanticoException(atribuicao.getToken(),
					"Valor esquerdo da atribuição inválido");
		}

		// Obtém o tipo da expressão à esquerda
		Tipo esquerda = analisaTipoNoExpressao(atribuicao.getEsquerda());

		// Obtém o tipo da expressão à direita
		Tipo direita = analisaTipoNoExpressao(atribuicao.getDireita());

		// Verifica atribuição de arrays com tamanhos coerentes
		if (esquerda.getTamanho() != direita.getTamanho()) {
			throw new ErroSemanticoException(atribuicao.getToken(),
					"Tamanho de array inválido na atribuição");
		}

		// Verifica se está atribuindo vazio
		if (esquerda.getEntrada() instanceof SimboloVariavel
				&& direita.getEntrada() == tipoVazio) {
			throw new ErroSemanticoException(atribuicao.getToken(),
					"Atribuição de vazio inválida");
		}

		// Verifica os tipos da atribuição
		if (esquerda.getEntrada() != direita.getEntrada()) {
			throw new ErroSemanticoException(atribuicao.getToken(),
					"Tipos incompatíveis para atribuição");
		}
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos do
	 * comando imprimir
	 * 
	 * @param imprimir
	 *            Nó da árvore sintática correspondente ao comando imprimir
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private void analisaNoImprimir(NoImprimir imprimir)
			throws ErroSemanticoException {
		// Verifica comando vazio
		if (imprimir == null) {
			return;
		}

		// Obtém o tipo do valor a ser impresso
		Tipo expressao = analisaTipoNoExpressao(imprimir.getValor());

		// Verifica se não é um array
		if (expressao.getTamanho() != 0) {
			throw new ErroSemanticoException(imprimir.getToken(),
					"Não é permitido o uso de vetor para o comando imprimir");
		}
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos do
	 * comando ler
	 * 
	 * @param ler
	 *            Nó da árvore sintática correspondente ao comando ler
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private void analisaNoLer(NoLer ler) throws ErroSemanticoException {
		// Verifica comando vazio
		if (ler == null) {
			return;
		}

		// Verifica se é uma variável ou um array
		if (!(ler.getVariavel() instanceof NoVariavel || ler.getVariavel() instanceof NoArray)) {
			throw new ErroSemanticoException(ler.getToken(),
					"Expressão inválida para o comando ler");
		}

		// Obtém o tipo da variável
		Tipo expressao = analisaTipoNoExpressao(ler.getVariavel());

		// Verifica se é texto, inteiro ou decimal somente
		if (!(expressao.getEntrada() == tipoTexto
				|| expressao.getEntrada() == tipoInteiro || expressao
					.getEntrada() == tipoDecimal)) {
			throw new ErroSemanticoException(ler.getToken(),
					"Tipo inválido para o comando ler");
		}

		// Verifica se é um vetor, pois não é permitido
		if (expressao.getTamanho() != 0) {
			throw new ErroSemanticoException(ler.getToken(),
					"Não é possível ler um vetor");
		}
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos do
	 * comando retornar
	 * 
	 * @param retornar
	 *            Nó da árvore sintática correspondente ao comando retornar
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private void analisaNoRetornar(NoRetornar retornar)
			throws ErroSemanticoException {
		// Verifica comando vazio
		if (retornar == null) {
			return;
		}

		// Obtém o tipo do valor a retornar
		Tipo expressao = analisaTipoNoExpressao(retornar.getValor());

		// Verifica o tipo do valor
		if (expressao == null) {
			// Se não houver valor para retornar, o método deve ser vazio
			if (tipoRetorno.getEntrada() == tipoVazio) {
				return;
			} else {
				throw new ErroSemanticoException(retornar.getToken(),
						"Comando retornar sem expressão");
			}
		} else {
			// Se houver valor para retornar, deve ser do mesmo tipo do método
			if (tipoRetorno.getEntrada() == tipoVazio) {
				throw new ErroSemanticoException(retornar.getToken(),
						"Método do tipo vazio não pode retornar um valor");
			}
		}

		// Se retornar o array, o método deve ter o mesmo tamanho
		if (expressao.getEntrada() != tipoRetorno.getEntrada()
				|| expressao.getTamanho() != tipoRetorno.getTamanho()) {
			throw new ErroSemanticoException(retornar.getToken(),
					"Tipo de retorno inválido");
		}
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos do
	 * controle SE
	 * 
	 * @param se
	 *            Nó da árvore sintática correspondente ao controle SE
	 */
	private void analisaNoSe(NoSe se) {
		// Verifica controle vazio
		if (se == null) {
			return;
		}

		try {
			// Obtém o tipo da expressão
			Tipo expressao = analisaTipoNoExpressao(se.getCondicao());

			// Verifica se é relacional
			if (!(se.getCondicao() instanceof NoRelacional)) {
				throw new ErroSemanticoException(se.getToken(),
						"Condição inválida");
			}

			// O resultado deve ser inteiro
			if (expressao.getEntrada() != tipoInteiro
					|| expressao.getTamanho() != 0) {
				throw new ErroSemanticoException(se.getToken(),
						"Condição inválida");
			}
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}

		// Analisa o bloco da condição verdadeira
		analisaNoBloco(se.getVerdadeiro());

		// Analisa o bloco da condição falsa
		analisaNoBloco(se.getFalso());
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos do
	 * controle enquanto
	 * 
	 * @param enquanto
	 *            Nó da árvore sintática correspondente ao controle enquanto
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private void analisaNoEnquanto(NoEnquanto enquanto)
			throws ErroSemanticoException {
		// Verifica controle vazio
		if (enquanto == null) {
			return;
		}

		try {
			// Obtém o tipo da expressão
			Tipo expressao = analisaTipoNoExpressao(enquanto.getCondicao());

			// Verfifica se é relacional
			if (!(enquanto.getCondicao() instanceof NoRelacional)) {
				throw new ErroSemanticoException(enquanto.getToken(),
						"Condição inválida");
			}

			// O tipo da expressão deve ser inteiro
			if (expressao.getEntrada() != tipoInteiro
					|| expressao.getTamanho() != 0) {
				throw new ErroSemanticoException(enquanto.getToken(),
						"Condição inválida");
			}
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}

		// Analisa o bloco do controle enquanto
		analisaNoBloco(enquanto.getBloco());
	}

	/**
	 * Método para obter o tipo de uma expressão
	 * 
	 * @param expressao
	 *            Nó da árvore sintática correspondente a uma expressão
	 * @param atual
	 *            Tabela de símbolo atual
	 * @return Tipo da expressão
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	public Tipo analisaTipoNoExpressao(NoExpressao expressao,
			TabelaSimbolo atual) throws ErroSemanticoException {
		// Verifica expressão vazia
		if (expressao == null) {
			return new Tipo(tipoNulo, 0);
		}

		// Salva a tabela atual
		TabelaSimbolo temp = tabelaAtual;

		// Troca a tabela principal
		this.tabelaAtual = atual;

		// Obtém o tipo da expressão
		Tipo resultado = analisaTipoNoExpressao(expressao);

		// Retoma a tabela de símbolo atual
		this.tabelaAtual = temp;

		return resultado;
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos de
	 * uma expressão
	 * 
	 * @param expressao
	 *            Nó da árvore sintática correspondente a uma expressão
	 * @return Tipo da expressão
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private Tipo analisaTipoNoExpressao(NoExpressao expressao)
			throws ErroSemanticoException {
		// Verifica expressão vazia
		if (expressao == null) {
			return new Tipo(tipoNulo, 0);
		}

		// Analisa a expressão de acordo com o tipo da expressão
		if (expressao instanceof NoAlocacao) {
			return analisaTipoNoAlocacao((NoAlocacao) expressao);
		} else if (expressao instanceof NoRelacional) {
			return analisaTipoNoRelacional((NoRelacional) expressao);
		} else if (expressao instanceof NoAdicao) {
			return analisaTipoNoAdicao((NoAdicao) expressao);
		} else if (expressao instanceof NoMultiplicacao) {
			return analisaTipoNoMultiplicacao((NoMultiplicacao) expressao);
		} else if (expressao instanceof NoUnario) {
			return analisaTipoNoUnario((NoUnario) expressao);
		} else if (expressao instanceof NoChamada) {
			return analisaTipoNoChamada((NoChamada) expressao);
		} else if (expressao instanceof NoInteiro) {
			return analisaTipoNoInteiro((NoInteiro) expressao);
		} else if (expressao instanceof NoTexto) {
			return analisaTipoNoTexto((NoTexto) expressao);
		} else if (expressao instanceof NoDecimal) {
			return analisaTipoNoDecimal((NoDecimal) expressao);
		} else if (expressao instanceof NoNulo) {
			return analisaTipoNoNulo((NoNulo) expressao);
		} else if (expressao instanceof NoArray) {
			return analisaTipoNoArray((NoArray) expressao);
		} else if (expressao instanceof NoVariavel) {
			return analisaTipoNoVariavel((NoVariavel) expressao);
		} else {
			return new Tipo(tipoNulo, 0);
		}
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos da
	 * lista de expressões
	 * 
	 * @param lista
	 *            Nó da árvore sintática correspondente à lista de expressões
	 * @return Tipo da lista de expressões
	 */
	private Tipo analisaTipoNoListaExpressao(NoLista lista) {
		// Verifica lista vazia
		if (lista == null) {
			return new Tipo(tipoNulo, 0);
		}

		Tipo primeiro = null;
		try {
			// Analisa a primeira expressão da lista
			primeiro = analisaTipoNoExpressao((NoExpressao) lista.getNo());
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
			return new Tipo(tipoNulo, 0);
		}

		// Analisa a próxima expressão
		Tipo proximo = analisaTipoNoListaExpressao(lista.getProximo());

		// Obtém quantas expressões tem a lista
		int tamanho = 0;
		if (proximo.getEntrada() != null) {
			tamanho = ((SimboloParametro) proximo.getEntrada()).getElementos();
		}

		// Cria a entrada da tabela de símbolos da lista de expressões
		SimboloParametro p = new SimboloParametro(primeiro.getEntrada(),
				tamanho);

		return new Tipo(p, 0);
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos de
	 * uma alocação
	 * 
	 * @param alocacao
	 *            Nó da árvore sintática correspondente a uma alocação
	 * @return Tipo da alocação
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private Tipo analisaTipoNoAlocacao(NoAlocacao alocacao)
			throws ErroSemanticoException {
		// Verifica alocação vazia
		if (alocacao == null) {
			return null;
		}

		// Busca o tipo da variável
		SimboloEntrada tipo = tabelaAtual.buscaTipo(alocacao.getTipo().image);

		// Valida se o tipo existe
		if (tipo == null) {
			throw new ErroSemanticoException(alocacao.getToken(), "Tipo \""
					+ alocacao.getTipo().image + "\" não encontrado");
		}

		// Obtém o tamanho do vetor sendo alocado
		NoLista tamanho = alocacao.getTamanho();
		int tam = 0;

		// Percorre todas as expressões
		while (tamanho != null) {
			// Analisa a expressão atual
			Tipo t = analisaTipoNoExpressao((NoExpressao) tamanho.getNo());

			// Verifica se é inteiro
			if (t.getEntrada() != tipoInteiro || t.getTamanho() != 0) {
				throw new ErroSemanticoException(alocacao.getToken(),
						"Expressão inválida para tamanho de vetor");
			}

			tam++;
			tamanho = tamanho.getProximo();
		}

		return new Tipo(tipo, tam);
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos de
	 * uma relação
	 * 
	 * @param relacional
	 *            Nó da árvore sintática correspondente a uma relação
	 * @return Tipo da relação
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar em erro semântico
	 */
	private Tipo analisaTipoNoRelacional(NoRelacional relacional)
			throws ErroSemanticoException {
		// Verifica relação vazia
		if (relacional == null) {
			return null;
		}

		// Obtém a operação
		int operacao = relacional.getToken().kind;

		// Analisa os dois lados da relação
		Tipo esquerda = analisaTipoNoExpressao(relacional.getEsquerda());
		Tipo direita = analisaTipoNoExpressao(relacional.getDireita());

		// Verifica se os dois lados são inteiros
		if (esquerda.getEntrada() == tipoInteiro
				&& direita.getEntrada() == tipoInteiro) {
			return new Tipo(tipoInteiro, 0);
		}

		// Verifica se os tamanhos são os mesmos
		if (esquerda.getTamanho() != direita.getTamanho()) {
			throw new ErroSemanticoException(relacional.getToken(),
					"Não é possível comparar objetos de tamanhos diferentes");
		}

		// Verifica se é relação com arrays
		if (operacao != BLangMotorConstants.IGUAL
				&& operacao != BLangMotorConstants.DIFERENTE
				&& esquerda.getTamanho() > 0) {
			throw new ErroSemanticoException(relacional.getToken(),
					"Não é possível usar \"" + relacional.getToken().image
							+ "\" para comparar vetores");
		}

		// Verifica se os tipos estão coerentes
		if (esquerda.getEntrada() == direita.getEntrada()
				&& (operacao == BLangMotorConstants.IGUAL && operacao == BLangMotorConstants.DIFERENTE)) {
			return new Tipo(tipoInteiro, 0);
		}

		throw new ErroSemanticoException(relacional.getToken(),
				"Tipos inválidos para a relação");
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos de
	 * uma adição
	 * 
	 * @param adicao
	 *            Nó da árvore sintática correspondente a uma adição
	 * @return Tipo da adição
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private Tipo analisaTipoNoAdicao(NoAdicao adicao)
			throws ErroSemanticoException {
		// Verifica adição vazia
		if (adicao == null) {
			return null;
		}

		// Obtém a operação
		int operacao = adicao.getToken().kind;

		// Obtém os tipos dos dois lados a adição
		Tipo esquerda = analisaTipoNoExpressao(adicao.getEsquerda());
		Tipo direita = analisaTipoNoExpressao(adicao.getDireita());

		// Verifica se é vetor
		if (esquerda.getTamanho() != 0 || direita.getTamanho() != 0) {
			throw new ErroSemanticoException(adicao.getToken(),
					"Não é possível usar \"" + adicao.getToken().image
							+ "\" para vetores");
		}

		// Variáveis para contagem de tipos
		int inteiro = 0;
		int decimal = 0;
		int texto = 0;

		// Conta o tipo do lado esquerdo
		if (esquerda.getEntrada() == tipoInteiro) {
			inteiro++;
		} else if (esquerda.getEntrada() == tipoDecimal) {
			decimal++;
		} else if (esquerda.getEntrada() == tipoTexto) {
			texto++;
		}

		// Conta o tipo do lado direito
		if (direita.getEntrada() == tipoInteiro) {
			inteiro++;
		} else if (direita.getEntrada() == tipoDecimal) {
			decimal++;
		} else if (direita.getEntrada() == tipoTexto) {
			texto++;
		}

		// Verifica se são números
		if (inteiro == 2) {
			return new Tipo(tipoInteiro, 0);
		} else if (decimal == 2) {
			return new Tipo(tipoDecimal, 0);
		}

		// Verifica se é texto, o que não pode
		if (operacao == BLangMotorConstants.MAIS
				&& ((inteiro + texto == 2) || (decimal + texto == 2))) {
			return new Tipo(tipoTexto, 0);
		}

		throw new ErroSemanticoException(adicao.getToken(),
				"Tipos inválidos para a adição/subtração");
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos de
	 * uma multiplicação
	 * 
	 * @param mult
	 *            Nó da árvore sintática correspondente a uma multiplicação
	 * @return Tipo da multiplicação
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private Tipo analisaTipoNoMultiplicacao(NoMultiplicacao mult)
			throws ErroSemanticoException {
		// Verifica multiplicação vazia
		if (mult == null) {
			return null;
		}

		// Obtém os tipos da multiplicação
		Tipo esquerda = analisaTipoNoExpressao(mult.getEsquerda());
		Tipo direita = analisaTipoNoExpressao(mult.getDireita());

		// Verifica se é vetor
		if (esquerda.getTamanho() != 0 || direita.getTamanho() != 0) {
			throw new ErroSemanticoException(mult.getToken(),
					"Não é possível usar \"" + mult.getToken().image
							+ "\" para vetores");
		}

		// Verifica se os tipos estão corretos
		if (esquerda.getEntrada() == tipoInteiro
				&& direita.getEntrada() == tipoInteiro) {
			return new Tipo(tipoInteiro, 0);
		} else if (esquerda.getEntrada() == tipoDecimal
				&& direita.getEntrada() == tipoDecimal) {
			return new Tipo(tipoDecimal, 0);
		} else if (esquerda.getEntrada() == tipoInteiro
				&& direita.getEntrada() == tipoDecimal) {
			return new Tipo(tipoDecimal, 0);
		} else if (esquerda.getEntrada() == tipoDecimal
				&& direita.getEntrada() == tipoInteiro) {
			return new Tipo(tipoDecimal, 0);
		} else {
			throw new ErroSemanticoException(mult.getToken(),
					"Tipos inválidos para \"" + mult.getToken().image + "\"");
		}
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos de
	 * um número unário
	 * 
	 * @param unario
	 *            Nó da árvore sintática correspondete a um número unário
	 * @return Tipo do número unário
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private Tipo analisaTipoNoUnario(NoUnario unario)
			throws ErroSemanticoException {
		// Verifica número vazio
		if (unario == null) {
			return null;
		}

		// Obtém o tipo do fator
		Tipo expressao = analisaTipoNoExpressao(unario.getFator());

		// Verifica se é vetor
		if (expressao.getTamanho() != 0) {
			throw new ErroSemanticoException(unario.getToken(),
					"Não é possível utilizar termos unários com vetor");
		}

		// Retorna o tipo coerente
		if (expressao.getEntrada() == tipoInteiro) {
			return new Tipo(tipoInteiro, 0);
		} else if (expressao.getEntrada() == tipoDecimal) {
			return new Tipo(tipoDecimal, 0);
		} else {
			throw new ErroSemanticoException(unario.getToken(), "Tipo inválido");
		}
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos de
	 * um número inteiro
	 * 
	 * @param inteiro
	 *            Nó da árvore sintática correspondente a um número inteiro
	 * @return Tipo inteiro
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private Tipo analisaTipoNoInteiro(NoInteiro inteiro)
			throws ErroSemanticoException {
		// Verifica número vazio
		if (inteiro == null) {
			return null;
		}

		try {
			// Converte para inteiro
			Integer.parseInt(inteiro.getToken().image);
		} catch (NumberFormatException e) {
			throw new ErroSemanticoException(inteiro.getToken(),
					"Constante inteira inválida");
		}

		return new Tipo(tipoInteiro, 0);
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos de
	 * um número decimal
	 * 
	 * @param decimal
	 *            Nó da árvore sintática correspondente a um número decimal
	 * @return Tipo decimal
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private Tipo analisaTipoNoDecimal(NoDecimal decimal)
			throws ErroSemanticoException {
		// Verifica número vazio
		if (decimal == null) {
			return null;
		}

		try {
			// Converte para decimal
			Double.parseDouble(decimal.getToken().image);
		} catch (NumberFormatException e) {
			throw new ErroSemanticoException(decimal.getToken(),
					"Constante decimal inválida");
		}

		return new Tipo(tipoDecimal, 0);
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos de
	 * um texto
	 * 
	 * @param texto
	 *            Nó da árvore sintática correspondente a um texto
	 * @return Tipo texto
	 */
	private Tipo analisaTipoNoTexto(NoTexto texto) {
		// Verifica texto vazio
		if (texto == null) {
			return null;
		}

		return new Tipo(tipoTexto, 0);
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos do
	 * tipo nulo
	 * 
	 * @param nulo
	 *            Nó da árvore sintática correspondente a um tipo nulo
	 * @return Tipo nulo
	 */
	private Tipo analisaTipoNoNulo(NoNulo nulo) {
		// Verifica tipo vazio
		if (nulo == null) {
			return null;
		}

		return new Tipo(tipoNulo, 0);
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos de
	 * uma variável
	 * 
	 * @param variavel
	 *            Nó da árvore sintática correspondente a uma variável
	 * @return Tipo da variável
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private Tipo analisaTipoNoVariavel(NoVariavel variavel)
			throws ErroSemanticoException {
		// Verifica variável vazia
		if (variavel == null) {
			return null;
		}

		// Busca o tipo da variável
		SimboloVariavel simbolo = tabelaAtual
				.buscaVariavel(variavel.getToken().image);

		// Verifica se o tipo foi encontrado
		if (simbolo == null) {
			throw new ErroSemanticoException(variavel.getToken(), "Variável \""
					+ variavel.getToken().image + "\" não encontrada");
		}

		return new Tipo(simbolo.getTipo(), simbolo.getTamanho());
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos da
	 * chamada de método
	 * 
	 * @param chamada
	 *            Nó da árvore sintática correspondente à chamada de método
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private void analisaNoChamadaDecl(NoChamadaDecl chamada)
			throws ErroSemanticoException {
		// Nó da lista de argumentos
		NoLista arg = null;

		// Entrada na tabela de símbolo da lista de parâmetros
		SimboloParametro parametro = null;

		// Percorre todos os argumentos passados
		for (arg = chamada.getArgumentos(); arg != null; arg = arg.getProximo()) {
			Tipo tipo = analisaTipoNoExpressao((NoExpressao) arg.getNo());

			if (parametro == null) {
				parametro = new SimboloParametro(tipo.getEntrada(),
						tipo.getTamanho());
			} else {
				parametro.setProximo(tipo.getEntrada());
			}
		}

		// Busca o método com a assinatura correspondente
		SimboloMetodo metodo = tabelaAtual.buscaMetodo(
				chamada.getToken().image, parametro);

		// Verifica se o método existe
		if (metodo == null) {
			throw new ErroSemanticoException(chamada.getToken(), "O método \""
					+ chamada.getToken().image + "\" não foi encontrado");
		}
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos da
	 * chamada de método
	 * 
	 * @param chamada
	 *            Nó da árvore sintática correspondente a chamada de método
	 * @return Tipo do método
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private Tipo analisaTipoNoChamada(NoChamada chamada)
			throws ErroSemanticoException {
		// Analisa os argumentos da chamada
		Tipo argumentos = analisaTipoNoListaExpressao(chamada.getArgumentos());

		// Busca o método na tabela de símbolos
		SimboloMetodo metodo = tabelaAtual.buscaMetodo(
				chamada.getToken().image,
				(SimboloParametro) argumentos.getEntrada());

		// Verifica se o método existe
		if (metodo == null) {
			throw new ErroSemanticoException(chamada.getToken(), "Método \""
					+ chamada.getToken().image + "\" não encontrado");
		}

		return new Tipo(metodo.getTipo(), metodo.getTamanho());
	}

	/**
	 * Método que faz a análise semântica e a geração da tabela de símbolos de
	 * um array
	 * 
	 * @param array
	 *            Nó da árvore sintática correspondente a um array
	 * @return Tipo do array
	 * @throws ErroSemanticoException
	 *             Exceção lançada ao encontrar um erro semântico
	 */
	private Tipo analisaTipoNoArray(NoArray array)
			throws ErroSemanticoException {
		// Verifica array vazio
		if (array == null) {
			return new Tipo(tipoNulo, 0);
		}

		// Busca o tipo do array na tabela de símbolo
		SimboloVariavel variavel = tabelaAtual
				.buscaVariavel(array.getToken().image);

		// Verifica se o tipo existe
		if (variavel == null) {
			throw new ErroSemanticoException(array.getToken(), "Vetor \""
					+ array.getToken().image + "\" não encontrado");
		}

		// Verifica se é realmente um array
		if (variavel.getTamanho() <= 0) {
			throw new ErroSemanticoException(array.getToken(), "A variável \""
					+ array.getToken().image
					+ "\" não pode ser acessada como array");
		}

		// Analisa as expressões dos índices do array
		Tipo expressoes = analisaTipoNoListaExpressao(array.getExpressoes());

		// Verifica se existe expressões
		if (expressoes != null) {
			// Verifica se o tamanho do array está correto
			if (variavel.getTamanho() != expressoes.getTamanho()) {
				throw new ErroSemanticoException(array.getToken(),
						"A dimensão vetor \"" + array.getToken().image
								+ "\" difere da declaração");
			}

			// Verifica se as expressões são inteiras
			if (expressoes.getEntrada() != tipoInteiro
					&& expressoes.getTamanho() > 0) {
				throw new ErroSemanticoException(array.getToken(),
						"O índice do vetor deve ser inteiro");
			}
		}

		return new Tipo(variavel.getTipo(), variavel.getTamanho());
	}

}