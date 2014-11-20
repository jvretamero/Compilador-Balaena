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
 * Classe respons�vel por realizar a an�lise sem�ntica do c�digo fonte
 */
public class AnalisadorSemantico {

	// Raiz da �rvore sint�tica
	private NoLista raiz;

	// Quantidade de erros sem�nticos
	private int erros;

	// Tabela de s�mbolos principal (n�vel mais alto)
	private TabelaSimbolo tabelaPrincipal;

	// Tabela de s�mbolos atual (alterada durante a mudan�a de escopo)
	private TabelaSimbolo tabelaAtual;

	// Total de vari�veis locais
	private int totalLocal;

	// Tipo de retorno do m�todo atual
	private Tipo tipoRetorno;

	// Entradas da tabela de s�mbolo dos tipos primitivos
	private final SimboloSimples tipoTexto = new SimboloSimples("texto");
	private final SimboloSimples tipoInteiro = new SimboloSimples("inteiro");
	private final SimboloSimples tipoDecimal = new SimboloSimples("decimal");
	private final SimboloSimples tipoVazio = new SimboloSimples("vazio");
	private final SimboloSimples tipoNulo = new SimboloSimples("nulo");

	// Inicia o analisador sem�ntico a partir do n� raiz da �rvore sint�tica
	public AnalisadorSemantico(NoLista raiz) {
		this.raiz = raiz;
		this.erros = 0;
	}

	/**
	 * @return Quantidade de erros sem�nticos encontrados
	 */
	public int getErros() {
		return erros;
	}

	/**
	 * @return Tabela de s�mbolo principal utilizada na an�lise sem�ntica
	 */
	public TabelaSimbolo getTabela() {
		return tabelaPrincipal;
	}

	/**
	 * M�todo respons�vel por iniciar a an�lise sem�ntica do c�digo fonte
	 * 
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar erros sem�nticos
	 */
	public void analisa() throws ErroSemanticoException {
		System.out.println("Realizando an�lise sem�ntica...");

		// Somente analisa se tiver uma �rvore sint�tica v�lida
		if (raiz != null) {

			// Inicia o contador de erros
			erros = 0;

			// Instancia as tabelas de s�mbolo
			tabelaPrincipal = new TabelaSimbolo();
			tabelaAtual = new TabelaSimbolo();

			// Adiciona os tipos primitivos na tabela principal
			tabelaPrincipal.adiciona(tipoTexto);
			tabelaPrincipal.adiciona(tipoInteiro);
			tabelaPrincipal.adiciona(tipoDecimal);
			tabelaPrincipal.adiciona(tipoVazio);
			tabelaPrincipal.adiciona(tipoNulo);

			tabelaAtual = tabelaPrincipal;

			// Inicia a an�lise sem�ntica e gera��o da tabela de s�mbolos nos
			// n�veis mais altos
			simbolosNoListaMetodoDecl(raiz);

			// Inicia a an�lise sem�ntica e gera��o da tabela de s�mbolos nos
			// n�veis mais baixos
			analisaNoListaMetodoDecl(raiz);
		}
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e gera��o da tabela de s�mbolos nos
	 * n�veis mais altos da lista de m�todos
	 * 
	 * @param listaMetodos
	 *            N� da �rvore sint�tica correspondente � lista de m�todos
	 */
	private void simbolosNoListaMetodoDecl(NoLista listaMetodos) {

		// Verifica lista de m�todos vazia
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

		// Continua a an�lise atrav�s dos pr�ximos nos
		simbolosNoListaMetodoDecl(listaMetodos.getProximo());
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e gera��o da tabela de s�mbolos no
	 * n�vel mais alto do m�todo
	 * 
	 * @param metodo
	 *            N� da �rvore sint�tica correspondente ao m�todo
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private void simbolosNoMetodoDecl(NoMetodoDecl metodo)
			throws ErroSemanticoException {

		// Verifica m�todo vazio
		if (metodo == null) {
			return;
		}

		// N�s das vari�veis
		NoVariavelDecl variavelDecl = null;
		NoVariavel variavel = null;

		// Entradas da tabela de s�mbolos
		SimboloEntrada tipo = null;
		SimboloMetodo m = null;
		SimboloParametro listaParametro = null;

		// Obt�m os par�metros do m�todo
		NoLista parametros = metodo.getCorpo().getParametros();

		// Percorre todos os par�metros
		while (parametros != null) {
			// Obt�m a declara��o da vari�vel do par�metro
			variavelDecl = (NoVariavelDecl) parametros.getNo();
			// Obt�m a vari�vel do par�metro
			variavel = (NoVariavel) variavelDecl.getVariaveis().getNo();

			// Busca o tipo da vari�vel na tabela de s�mbolos
			tipo = tabelaAtual.buscaTipo(variavelDecl.getToken().image);

			// Se n�o encontrou o tipo primitivo emite um erro
			if (tipo == null) {
				throw new ErroSemanticoException(variavelDecl.getToken(),
						"Tipo " + variavelDecl.getToken().image + " inv�lido");
			}

			// Cria o elemento da lista de parametros
			if (listaParametro == null) {
				listaParametro = new SimboloParametro(tipo,
						variavel.getTamanho());
			} else {
				listaParametro.setProximo(new SimboloParametro(tipo, variavel
						.getTamanho()));
			}

			// Vai para o pr�ximo n� da �rvore
			parametros = parametros.getProximo();
		}

		// Busca o tipo de retorno do m�todo na tabela de s�mbolos
		tipo = tabelaAtual.buscaTipo(metodo.getToken().image);

		// Se o tipo de retorno n�o for encontrado emite um erro
		if (tipo == null) {
			throw new ErroSemanticoException(metodo.getToken(), "Tipo "
					+ metodo.getToken().image + " inv�lido");
		}

		// Busca o m�todo na tabela de s�mbolos
		m = tabelaAtual.buscaMetodo(metodo.getNome().image, listaParametro);

		// Se n�o declarado ainda, inclui na tabela de s�mbolos
		if (m == null) {
			m = new SimboloMetodo(tipo, metodo.getNome().image,
					metodo.getTamanho(), listaParametro);
			tabelaAtual.adiciona(m);
		} else {
			// Se j� declarado, emite um erro
			throw new ErroSemanticoException(metodo.getNome(), "M�todo "
					+ metodo.getNome().image + " j� declarado");
		}
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e gera��o da tabela de s�mbolos nos
	 * n�veis mais baixos da lista de m�todos
	 * 
	 * @param metodos
	 *            N� da �rvore sint�tica correspondente � lista de m�todos
	 */
	private void analisaNoListaMetodoDecl(NoLista metodos) {
		// Verifica lista vazia
		if (metodos == null) {
			return;
		}

		try {
			// Analisa um m�todo da lista
			analisaNoMetodoDecl((NoMetodoDecl) metodos.getNo());
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}

		// Analisa os pr�ximos m�todos
		analisaNoListaMetodoDecl(metodos.getProximo());
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e gera��o da tabela de s�mbolos nos
	 * n�veis mais maixos do m�todo
	 * 
	 * @param metodo
	 *            N� da �rvore sint�tica correspondente ao m�todo
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private void analisaNoMetodoDecl(NoMetodoDecl metodo)
			throws ErroSemanticoException {
		// Verifica m�todo vazio
		if (metodo == null) {
			return;
		}

		// Salva a tabela de s�mbolo
		TabelaSimbolo temporaria = tabelaAtual;

		// Entradas da tabela de s�mbolo
		SimboloEntrada tipo = null;
		SimboloParametro listaParametro = null;
		SimboloMetodo simboloMetodo = null;

		// N�s da �rvore sint�tica
		NoLista param = null;
		NoVariavelDecl varDecl = null;
		NoVariavel var = null;

		// Obt�m os par�metros do m�todo
		param = metodo.getCorpo().getParametros();

		// Monta a entrada da tabela de s�mbolo dos par�metros do m�todo
		while (param != null) {

			// Obt�m as vari�veis
			varDecl = (NoVariavelDecl) param.getNo();
			var = (NoVariavel) varDecl.getVariaveis().getNo();

			// Busca o tipo da vari�vel
			tipo = tabelaAtual.buscaTipo(varDecl.getToken().image);

			// Monta a entrada da tabela de s�mbolo
			if (listaParametro == null) {
				listaParametro = new SimboloParametro(tipo, var.getTamanho());
			} else {
				listaParametro.setProximo(new SimboloParametro(tipo, var
						.getTamanho()));
			}

			param = param.getProximo();
		}

		// Busca o m�todo com a assinatura correspondente na tabela de s�mbolo
		simboloMetodo = tabelaAtual.buscaMetodo(metodo.getNome().image,
				listaParametro);

		// Obt�m a tabela de s�mbolo do m�todo
		tabelaAtual = simboloMetodo.getTabela();

		// Armazena o tipo de retorno do m�todo
		tipoRetorno = new Tipo(simboloMetodo.getTipo(),
				simboloMetodo.getTamanho());

		// Vari�veis locais
		totalLocal = 0;

		// Faz a an�lise do corpo do m�todo
		analisaNoCorpoMetodo(metodo.getCorpo());

		// Atualiza o total de vari�veis locais do m�todo
		simboloMetodo.setTotalLocal(totalLocal);

		// Retoma a tabela de s�mbolos
		tabelaAtual = temporaria;
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos do
	 * corpo do m�todo
	 * 
	 * @param corpo
	 *            N� da �rvore sint�tica correspondente ao corpo do m�todo
	 */
	private void analisaNoCorpoMetodo(NoCorpoMetodo corpo) {
		// Verifica corpo do m�todo vazio
		if (corpo == null) {
			return;
		}

		// Trata os par�metros como vari�vel local
		analisaNoListaVariaveis(corpo.getParametros());

		// Analisa o bloco do m�todo
		analisaNoBloco(corpo.getBloco());
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos da
	 * lista de vari�veis
	 * 
	 * @param variaveis
	 *            N� da �rvore sint�tica correspondente � lista de vari�veis
	 */
	private void analisaNoListaVariaveis(NoLista variaveis) {
		// Verifica lista vazia
		if (variaveis == null) {
			return;
		}

		try {
			// Analisa um n� de declara��o de vari�vel
			analisaNoVariavel((NoVariavelDecl) variaveis.getNo());
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}

		// Analisa o pr�ximo n� de declara��o de vari�vel
		analisaNoListaVariaveis(variaveis.getProximo());
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos de
	 * uma vari�vel
	 * 
	 * @param variavel
	 *            N� da �rvore sint�tica correspondente a uma vari�vel
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private void analisaNoVariavel(NoVariavelDecl variavel)
			throws ErroSemanticoException {
		// N�s da �rvore sint�tica
		NoLista variaveis = null;
		NoVariavel var = null;

		// Entradas da tabela de s�mbolo
		SimboloVariavel v = null;
		SimboloEntrada tipo = null;

		// Busca o tipo na tabela local
		tipo = tabelaAtual.buscaTipo(variavel.getToken().image);

		// Verifica se encontrou o tipo
		if (tipo == null) {
			throw new ErroSemanticoException(variavel.getToken(), "Tipo \""
					+ variavel.getToken().image + "\" n�o encontrado");
		}

		// Percorre todas as vari�veis
		for (variaveis = variavel.getVariaveis(); variaveis != null; variaveis = variaveis
				.getProximo()) {
			// Obt�m a vari�vel
			var = (NoVariavel) variaveis.getNo();

			// Busca a vari�vel na tabela de s�mbolo
			v = tabelaAtual.buscaVariavel(var.getToken().image);

			// Verifica se n�o est� redeclarado
			if (v != null) {
				if (v.getEscopo() == tabelaAtual.getEscopo()) {
					throw new ErroSemanticoException(variaveis.getToken(),
							"Vari�vel \"" + variaveis.getToken().image
									+ "\" redeclarada");
				}
			}

			// Adiciona na tabela de s�mbolos
			tabelaAtual.adiciona(new SimboloVariavel(tipo,
					var.getToken().image, var.getTamanho(), totalLocal++));
		}
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos do
	 * bloco
	 * 
	 * @param bloco
	 *            N� da �rvore sint�tica correspondente ao bloco de c�digo
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
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolo da
	 * lista de declara��es
	 * 
	 * @param declaracoes
	 *            N� da �rvore sint�tica correspondente � lista de declara��o
	 */
	private void analisaNoListaDeclaracao(NoLista declaracoes) {
		// Verifica lista vazia
		if (declaracoes == null) {
			return;
		}

		try {
			// Analisa a primeira declara��o
			analisaNoDeclaracao((NoDeclaracao) declaracoes.getNo());
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}

		// Analisa as pr�ximas declara��es
		analisaNoListaDeclaracao(declaracoes.getProximo());
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e gera��o da tabela de s�mbolos da
	 * declara��o
	 * 
	 * @param declaracao
	 *            N� da �rvore sint�tica correspondente a declara��o
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private void analisaNoDeclaracao(NoDeclaracao declaracao)
			throws ErroSemanticoException {
		// Verifica declara��o vazia
		if (declaracao == null) {
			return;
		}

		// Analisa a declara��o conforme o tipo
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
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos de
	 * uma atribui��o
	 * 
	 * @param atribuicao
	 *            N� da �rvore sint�tica correspondete a uma atribui��o
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private void analisaNoAtribuicao(NoAtribuicao atribuicao)
			throws ErroSemanticoException {
		// Verifica atribui��o vazia
		if (atribuicao == null) {
			return;
		}

		// Verifica se o lado esquerdo da atribui��o � um array ou uma vari�vel
		if (!(atribuicao.getEsquerda() instanceof NoArray || atribuicao
				.getEsquerda() instanceof NoVariavel)) {
			throw new ErroSemanticoException(atribuicao.getToken(),
					"Valor esquerdo da atribui��o inv�lido");
		}

		// Obt�m o tipo da express�o � esquerda
		Tipo esquerda = analisaTipoNoExpressao(atribuicao.getEsquerda());

		// Obt�m o tipo da express�o � direita
		Tipo direita = analisaTipoNoExpressao(atribuicao.getDireita());

		// Verifica atribui��o de arrays com tamanhos coerentes
		if (esquerda.getTamanho() != direita.getTamanho()) {
			throw new ErroSemanticoException(atribuicao.getToken(),
					"Tamanho de array inv�lido na atribui��o");
		}

		// Verifica se est� atribuindo vazio
		if (esquerda.getEntrada() instanceof SimboloVariavel
				&& direita.getEntrada() == tipoVazio) {
			throw new ErroSemanticoException(atribuicao.getToken(),
					"Atribui��o de vazio inv�lida");
		}

		// Verifica os tipos da atribui��o
		if (esquerda.getEntrada() != direita.getEntrada()) {
			throw new ErroSemanticoException(atribuicao.getToken(),
					"Tipos incompat�veis para atribui��o");
		}
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos do
	 * comando imprimir
	 * 
	 * @param imprimir
	 *            N� da �rvore sint�tica correspondente ao comando imprimir
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private void analisaNoImprimir(NoImprimir imprimir)
			throws ErroSemanticoException {
		// Verifica comando vazio
		if (imprimir == null) {
			return;
		}

		// Obt�m o tipo do valor a ser impresso
		Tipo expressao = analisaTipoNoExpressao(imprimir.getValor());

		// Verifica se n�o � um array
		if (expressao.getTamanho() != 0) {
			throw new ErroSemanticoException(imprimir.getToken(),
					"N�o � permitido o uso de vetor para o comando imprimir");
		}
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos do
	 * comando ler
	 * 
	 * @param ler
	 *            N� da �rvore sint�tica correspondente ao comando ler
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private void analisaNoLer(NoLer ler) throws ErroSemanticoException {
		// Verifica comando vazio
		if (ler == null) {
			return;
		}

		// Verifica se � uma vari�vel ou um array
		if (!(ler.getVariavel() instanceof NoVariavel || ler.getVariavel() instanceof NoArray)) {
			throw new ErroSemanticoException(ler.getToken(),
					"Express�o inv�lida para o comando ler");
		}

		// Obt�m o tipo da vari�vel
		Tipo expressao = analisaTipoNoExpressao(ler.getVariavel());

		// Verifica se � texto, inteiro ou decimal somente
		if (!(expressao.getEntrada() == tipoTexto
				|| expressao.getEntrada() == tipoInteiro || expressao
					.getEntrada() == tipoDecimal)) {
			throw new ErroSemanticoException(ler.getToken(),
					"Tipo inv�lido para o comando ler");
		}

		// Verifica se � um vetor, pois n�o � permitido
		if (expressao.getTamanho() != 0) {
			throw new ErroSemanticoException(ler.getToken(),
					"N�o � poss�vel ler um vetor");
		}
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos do
	 * comando retornar
	 * 
	 * @param retornar
	 *            N� da �rvore sint�tica correspondente ao comando retornar
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private void analisaNoRetornar(NoRetornar retornar)
			throws ErroSemanticoException {
		// Verifica comando vazio
		if (retornar == null) {
			return;
		}

		// Obt�m o tipo do valor a retornar
		Tipo expressao = analisaTipoNoExpressao(retornar.getValor());

		// Verifica o tipo do valor
		if (expressao == null) {
			// Se n�o houver valor para retornar, o m�todo deve ser vazio
			if (tipoRetorno.getEntrada() == tipoVazio) {
				return;
			} else {
				throw new ErroSemanticoException(retornar.getToken(),
						"Comando retornar sem express�o");
			}
		} else {
			// Se houver valor para retornar, deve ser do mesmo tipo do m�todo
			if (tipoRetorno.getEntrada() == tipoVazio) {
				throw new ErroSemanticoException(retornar.getToken(),
						"M�todo do tipo vazio n�o pode retornar um valor");
			}
		}

		// Se retornar o array, o m�todo deve ter o mesmo tamanho
		if (expressao.getEntrada() != tipoRetorno.getEntrada()
				|| expressao.getTamanho() != tipoRetorno.getTamanho()) {
			throw new ErroSemanticoException(retornar.getToken(),
					"Tipo de retorno inv�lido");
		}
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos do
	 * controle SE
	 * 
	 * @param se
	 *            N� da �rvore sint�tica correspondente ao controle SE
	 */
	private void analisaNoSe(NoSe se) {
		// Verifica controle vazio
		if (se == null) {
			return;
		}

		try {
			// Obt�m o tipo da express�o
			Tipo expressao = analisaTipoNoExpressao(se.getCondicao());

			// Verifica se � relacional
			if (!(se.getCondicao() instanceof NoRelacional)) {
				throw new ErroSemanticoException(se.getToken(),
						"Condi��o inv�lida");
			}

			// O resultado deve ser inteiro
			if (expressao.getEntrada() != tipoInteiro
					|| expressao.getTamanho() != 0) {
				throw new ErroSemanticoException(se.getToken(),
						"Condi��o inv�lida");
			}
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}

		// Analisa o bloco da condi��o verdadeira
		analisaNoBloco(se.getVerdadeiro());

		// Analisa o bloco da condi��o falsa
		analisaNoBloco(se.getFalso());
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos do
	 * controle enquanto
	 * 
	 * @param enquanto
	 *            N� da �rvore sint�tica correspondente ao controle enquanto
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private void analisaNoEnquanto(NoEnquanto enquanto)
			throws ErroSemanticoException {
		// Verifica controle vazio
		if (enquanto == null) {
			return;
		}

		try {
			// Obt�m o tipo da express�o
			Tipo expressao = analisaTipoNoExpressao(enquanto.getCondicao());

			// Verfifica se � relacional
			if (!(enquanto.getCondicao() instanceof NoRelacional)) {
				throw new ErroSemanticoException(enquanto.getToken(),
						"Condi��o inv�lida");
			}

			// O tipo da express�o deve ser inteiro
			if (expressao.getEntrada() != tipoInteiro
					|| expressao.getTamanho() != 0) {
				throw new ErroSemanticoException(enquanto.getToken(),
						"Condi��o inv�lida");
			}
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}

		// Analisa o bloco do controle enquanto
		analisaNoBloco(enquanto.getBloco());
	}

	/**
	 * M�todo para obter o tipo de uma express�o
	 * 
	 * @param expressao
	 *            N� da �rvore sint�tica correspondente a uma express�o
	 * @param atual
	 *            Tabela de s�mbolo atual
	 * @return Tipo da express�o
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	public Tipo analisaTipoNoExpressao(NoExpressao expressao,
			TabelaSimbolo atual) throws ErroSemanticoException {
		// Verifica express�o vazia
		if (expressao == null) {
			return new Tipo(tipoNulo, 0);
		}

		// Salva a tabela atual
		TabelaSimbolo temp = tabelaAtual;

		// Troca a tabela principal
		this.tabelaAtual = atual;

		// Obt�m o tipo da express�o
		Tipo resultado = analisaTipoNoExpressao(expressao);

		// Retoma a tabela de s�mbolo atual
		this.tabelaAtual = temp;

		return resultado;
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos de
	 * uma express�o
	 * 
	 * @param expressao
	 *            N� da �rvore sint�tica correspondente a uma express�o
	 * @return Tipo da express�o
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private Tipo analisaTipoNoExpressao(NoExpressao expressao)
			throws ErroSemanticoException {
		// Verifica express�o vazia
		if (expressao == null) {
			return new Tipo(tipoNulo, 0);
		}

		// Analisa a express�o de acordo com o tipo da express�o
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
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos da
	 * lista de express�es
	 * 
	 * @param lista
	 *            N� da �rvore sint�tica correspondente � lista de express�es
	 * @return Tipo da lista de express�es
	 */
	private Tipo analisaTipoNoListaExpressao(NoLista lista) {
		// Verifica lista vazia
		if (lista == null) {
			return new Tipo(tipoNulo, 0);
		}

		Tipo primeiro = null;
		try {
			// Analisa a primeira express�o da lista
			primeiro = analisaTipoNoExpressao((NoExpressao) lista.getNo());
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
			return new Tipo(tipoNulo, 0);
		}

		// Analisa a pr�xima express�o
		Tipo proximo = analisaTipoNoListaExpressao(lista.getProximo());

		// Obt�m quantas express�es tem a lista
		int tamanho = 0;
		if (proximo.getEntrada() != null) {
			tamanho = ((SimboloParametro) proximo.getEntrada()).getElementos();
		}

		// Cria a entrada da tabela de s�mbolos da lista de express�es
		SimboloParametro p = new SimboloParametro(primeiro.getEntrada(),
				tamanho);

		return new Tipo(p, 0);
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos de
	 * uma aloca��o
	 * 
	 * @param alocacao
	 *            N� da �rvore sint�tica correspondente a uma aloca��o
	 * @return Tipo da aloca��o
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private Tipo analisaTipoNoAlocacao(NoAlocacao alocacao)
			throws ErroSemanticoException {
		// Verifica aloca��o vazia
		if (alocacao == null) {
			return null;
		}

		// Busca o tipo da vari�vel
		SimboloEntrada tipo = tabelaAtual.buscaTipo(alocacao.getTipo().image);

		// Valida se o tipo existe
		if (tipo == null) {
			throw new ErroSemanticoException(alocacao.getToken(), "Tipo \""
					+ alocacao.getTipo().image + "\" n�o encontrado");
		}

		// Obt�m o tamanho do vetor sendo alocado
		NoLista tamanho = alocacao.getTamanho();
		int tam = 0;

		// Percorre todas as express�es
		while (tamanho != null) {
			// Analisa a express�o atual
			Tipo t = analisaTipoNoExpressao((NoExpressao) tamanho.getNo());

			// Verifica se � inteiro
			if (t.getEntrada() != tipoInteiro || t.getTamanho() != 0) {
				throw new ErroSemanticoException(alocacao.getToken(),
						"Express�o inv�lida para tamanho de vetor");
			}

			tam++;
			tamanho = tamanho.getProximo();
		}

		return new Tipo(tipo, tam);
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos de
	 * uma rela��o
	 * 
	 * @param relacional
	 *            N� da �rvore sint�tica correspondente a uma rela��o
	 * @return Tipo da rela��o
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar em erro sem�ntico
	 */
	private Tipo analisaTipoNoRelacional(NoRelacional relacional)
			throws ErroSemanticoException {
		// Verifica rela��o vazia
		if (relacional == null) {
			return null;
		}

		// Obt�m a opera��o
		int operacao = relacional.getToken().kind;

		// Analisa os dois lados da rela��o
		Tipo esquerda = analisaTipoNoExpressao(relacional.getEsquerda());
		Tipo direita = analisaTipoNoExpressao(relacional.getDireita());

		// Verifica se os dois lados s�o inteiros
		if (esquerda.getEntrada() == tipoInteiro
				&& direita.getEntrada() == tipoInteiro) {
			return new Tipo(tipoInteiro, 0);
		}

		// Verifica se os tamanhos s�o os mesmos
		if (esquerda.getTamanho() != direita.getTamanho()) {
			throw new ErroSemanticoException(relacional.getToken(),
					"N�o � poss�vel comparar objetos de tamanhos diferentes");
		}

		// Verifica se � rela��o com arrays
		if (operacao != BLangMotorConstants.IGUAL
				&& operacao != BLangMotorConstants.DIFERENTE
				&& esquerda.getTamanho() > 0) {
			throw new ErroSemanticoException(relacional.getToken(),
					"N�o � poss�vel usar \"" + relacional.getToken().image
							+ "\" para comparar vetores");
		}

		// Verifica se os tipos est�o coerentes
		if (esquerda.getEntrada() == direita.getEntrada()
				&& (operacao == BLangMotorConstants.IGUAL && operacao == BLangMotorConstants.DIFERENTE)) {
			return new Tipo(tipoInteiro, 0);
		}

		throw new ErroSemanticoException(relacional.getToken(),
				"Tipos inv�lidos para a rela��o");
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos de
	 * uma adi��o
	 * 
	 * @param adicao
	 *            N� da �rvore sint�tica correspondente a uma adi��o
	 * @return Tipo da adi��o
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private Tipo analisaTipoNoAdicao(NoAdicao adicao)
			throws ErroSemanticoException {
		// Verifica adi��o vazia
		if (adicao == null) {
			return null;
		}

		// Obt�m a opera��o
		int operacao = adicao.getToken().kind;

		// Obt�m os tipos dos dois lados a adi��o
		Tipo esquerda = analisaTipoNoExpressao(adicao.getEsquerda());
		Tipo direita = analisaTipoNoExpressao(adicao.getDireita());

		// Verifica se � vetor
		if (esquerda.getTamanho() != 0 || direita.getTamanho() != 0) {
			throw new ErroSemanticoException(adicao.getToken(),
					"N�o � poss�vel usar \"" + adicao.getToken().image
							+ "\" para vetores");
		}

		// Vari�veis para contagem de tipos
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

		// Verifica se s�o n�meros
		if (inteiro == 2) {
			return new Tipo(tipoInteiro, 0);
		} else if (decimal == 2) {
			return new Tipo(tipoDecimal, 0);
		}

		// Verifica se � texto, o que n�o pode
		if (operacao == BLangMotorConstants.MAIS
				&& ((inteiro + texto == 2) || (decimal + texto == 2))) {
			return new Tipo(tipoTexto, 0);
		}

		throw new ErroSemanticoException(adicao.getToken(),
				"Tipos inv�lidos para a adi��o/subtra��o");
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos de
	 * uma multiplica��o
	 * 
	 * @param mult
	 *            N� da �rvore sint�tica correspondente a uma multiplica��o
	 * @return Tipo da multiplica��o
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private Tipo analisaTipoNoMultiplicacao(NoMultiplicacao mult)
			throws ErroSemanticoException {
		// Verifica multiplica��o vazia
		if (mult == null) {
			return null;
		}

		// Obt�m os tipos da multiplica��o
		Tipo esquerda = analisaTipoNoExpressao(mult.getEsquerda());
		Tipo direita = analisaTipoNoExpressao(mult.getDireita());

		// Verifica se � vetor
		if (esquerda.getTamanho() != 0 || direita.getTamanho() != 0) {
			throw new ErroSemanticoException(mult.getToken(),
					"N�o � poss�vel usar \"" + mult.getToken().image
							+ "\" para vetores");
		}

		// Verifica se os tipos est�o corretos
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
					"Tipos inv�lidos para \"" + mult.getToken().image + "\"");
		}
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos de
	 * um n�mero un�rio
	 * 
	 * @param unario
	 *            N� da �rvore sint�tica correspondete a um n�mero un�rio
	 * @return Tipo do n�mero un�rio
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private Tipo analisaTipoNoUnario(NoUnario unario)
			throws ErroSemanticoException {
		// Verifica n�mero vazio
		if (unario == null) {
			return null;
		}

		// Obt�m o tipo do fator
		Tipo expressao = analisaTipoNoExpressao(unario.getFator());

		// Verifica se � vetor
		if (expressao.getTamanho() != 0) {
			throw new ErroSemanticoException(unario.getToken(),
					"N�o � poss�vel utilizar termos un�rios com vetor");
		}

		// Retorna o tipo coerente
		if (expressao.getEntrada() == tipoInteiro) {
			return new Tipo(tipoInteiro, 0);
		} else if (expressao.getEntrada() == tipoDecimal) {
			return new Tipo(tipoDecimal, 0);
		} else {
			throw new ErroSemanticoException(unario.getToken(), "Tipo inv�lido");
		}
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos de
	 * um n�mero inteiro
	 * 
	 * @param inteiro
	 *            N� da �rvore sint�tica correspondente a um n�mero inteiro
	 * @return Tipo inteiro
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private Tipo analisaTipoNoInteiro(NoInteiro inteiro)
			throws ErroSemanticoException {
		// Verifica n�mero vazio
		if (inteiro == null) {
			return null;
		}

		try {
			// Converte para inteiro
			Integer.parseInt(inteiro.getToken().image);
		} catch (NumberFormatException e) {
			throw new ErroSemanticoException(inteiro.getToken(),
					"Constante inteira inv�lida");
		}

		return new Tipo(tipoInteiro, 0);
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos de
	 * um n�mero decimal
	 * 
	 * @param decimal
	 *            N� da �rvore sint�tica correspondente a um n�mero decimal
	 * @return Tipo decimal
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private Tipo analisaTipoNoDecimal(NoDecimal decimal)
			throws ErroSemanticoException {
		// Verifica n�mero vazio
		if (decimal == null) {
			return null;
		}

		try {
			// Converte para decimal
			Double.parseDouble(decimal.getToken().image);
		} catch (NumberFormatException e) {
			throw new ErroSemanticoException(decimal.getToken(),
					"Constante decimal inv�lida");
		}

		return new Tipo(tipoDecimal, 0);
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos de
	 * um texto
	 * 
	 * @param texto
	 *            N� da �rvore sint�tica correspondente a um texto
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
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos do
	 * tipo nulo
	 * 
	 * @param nulo
	 *            N� da �rvore sint�tica correspondente a um tipo nulo
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
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos de
	 * uma vari�vel
	 * 
	 * @param variavel
	 *            N� da �rvore sint�tica correspondente a uma vari�vel
	 * @return Tipo da vari�vel
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private Tipo analisaTipoNoVariavel(NoVariavel variavel)
			throws ErroSemanticoException {
		// Verifica vari�vel vazia
		if (variavel == null) {
			return null;
		}

		// Busca o tipo da vari�vel
		SimboloVariavel simbolo = tabelaAtual
				.buscaVariavel(variavel.getToken().image);

		// Verifica se o tipo foi encontrado
		if (simbolo == null) {
			throw new ErroSemanticoException(variavel.getToken(), "Vari�vel \""
					+ variavel.getToken().image + "\" n�o encontrada");
		}

		return new Tipo(simbolo.getTipo(), simbolo.getTamanho());
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos da
	 * chamada de m�todo
	 * 
	 * @param chamada
	 *            N� da �rvore sint�tica correspondente � chamada de m�todo
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private void analisaNoChamadaDecl(NoChamadaDecl chamada)
			throws ErroSemanticoException {
		// N� da lista de argumentos
		NoLista arg = null;

		// Entrada na tabela de s�mbolo da lista de par�metros
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

		// Busca o m�todo com a assinatura correspondente
		SimboloMetodo metodo = tabelaAtual.buscaMetodo(
				chamada.getToken().image, parametro);

		// Verifica se o m�todo existe
		if (metodo == null) {
			throw new ErroSemanticoException(chamada.getToken(), "O m�todo \""
					+ chamada.getToken().image + "\" n�o foi encontrado");
		}
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos da
	 * chamada de m�todo
	 * 
	 * @param chamada
	 *            N� da �rvore sint�tica correspondente a chamada de m�todo
	 * @return Tipo do m�todo
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private Tipo analisaTipoNoChamada(NoChamada chamada)
			throws ErroSemanticoException {
		// Analisa os argumentos da chamada
		Tipo argumentos = analisaTipoNoListaExpressao(chamada.getArgumentos());

		// Busca o m�todo na tabela de s�mbolos
		SimboloMetodo metodo = tabelaAtual.buscaMetodo(
				chamada.getToken().image,
				(SimboloParametro) argumentos.getEntrada());

		// Verifica se o m�todo existe
		if (metodo == null) {
			throw new ErroSemanticoException(chamada.getToken(), "M�todo \""
					+ chamada.getToken().image + "\" n�o encontrado");
		}

		return new Tipo(metodo.getTipo(), metodo.getTamanho());
	}

	/**
	 * M�todo que faz a an�lise sem�ntica e a gera��o da tabela de s�mbolos de
	 * um array
	 * 
	 * @param array
	 *            N� da �rvore sint�tica correspondente a um array
	 * @return Tipo do array
	 * @throws ErroSemanticoException
	 *             Exce��o lan�ada ao encontrar um erro sem�ntico
	 */
	private Tipo analisaTipoNoArray(NoArray array)
			throws ErroSemanticoException {
		// Verifica array vazio
		if (array == null) {
			return new Tipo(tipoNulo, 0);
		}

		// Busca o tipo do array na tabela de s�mbolo
		SimboloVariavel variavel = tabelaAtual
				.buscaVariavel(array.getToken().image);

		// Verifica se o tipo existe
		if (variavel == null) {
			throw new ErroSemanticoException(array.getToken(), "Vetor \""
					+ array.getToken().image + "\" n�o encontrado");
		}

		// Verifica se � realmente um array
		if (variavel.getTamanho() <= 0) {
			throw new ErroSemanticoException(array.getToken(), "A vari�vel \""
					+ array.getToken().image
					+ "\" n�o pode ser acessada como array");
		}

		// Analisa as express�es dos �ndices do array
		Tipo expressoes = analisaTipoNoListaExpressao(array.getExpressoes());

		// Verifica se existe express�es
		if (expressoes != null) {
			// Verifica se o tamanho do array est� correto
			if (variavel.getTamanho() != expressoes.getTamanho()) {
				throw new ErroSemanticoException(array.getToken(),
						"A dimens�o vetor \"" + array.getToken().image
								+ "\" difere da declara��o");
			}

			// Verifica se as express�es s�o inteiras
			if (expressoes.getEntrada() != tipoInteiro
					&& expressoes.getTamanho() > 0) {
				throw new ErroSemanticoException(array.getToken(),
						"O �ndice do vetor deve ser inteiro");
			}
		}

		return new Tipo(variavel.getTipo(), variavel.getTamanho());
	}

}