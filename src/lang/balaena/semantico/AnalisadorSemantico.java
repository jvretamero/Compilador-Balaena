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

	public int getErros() {
		return erros;
	}

	// Inicia a an�lise sem�ntica
	public void analisa() throws ErroSemanticoException {
		if (raiz != null) {

			erros = 0;
			tabelaPrincipal = new TabelaSimbolo();
			tabelaAtual = new TabelaSimbolo();

			// Adiciona os tipos primitivos na tabela principal
			tabelaPrincipal.adiciona(tipoTexto);
			tabelaPrincipal.adiciona(tipoInteiro);
			tabelaPrincipal.adiciona(tipoDecimal);
			tabelaPrincipal.adiciona(tipoVazio);
			tabelaPrincipal.adiciona(tipoNulo);

			tabelaAtual = tabelaPrincipal;

			simbolosNoListaMetodoDecl(raiz);
			analisaNoListaMetodoDecl(raiz);
		}
	}

	private void simbolosNoListaMetodoDecl(NoLista listaMetodos) {

		// N�o executa se a lista estiver vazia
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

	private void simbolosNoMetodoDecl(NoMetodoDecl metodo)
			throws ErroSemanticoException {

		// Final de lista
		if (metodo == null) {
			return;
		}

		// Declara as vari�veis auxiliares
		NoVariavelDecl variavelDecl = null;
		NoVariavel variavel = null;
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

	private void analisaNoListaMetodoDecl(NoLista metodos) {
		if (metodos == null) {
			return;
		}

		try {
			analisaNoMetodoDecl((NoMetodoDecl) metodos.getNo());
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}
		analisaNoListaMetodoDecl(metodos.getProximo());
	}

	private void analisaNoMetodoDecl(NoMetodoDecl metodo)
			throws ErroSemanticoException {
		if (metodo == null) {
			return;
		}

		TabelaSimbolo temporaria = tabelaAtual;
		SimboloEntrada tipo = null;
		SimboloParametro listaParametro = null;
		SimboloMetodo simboloMetodo = null;
		NoLista param = null;
		NoVariavelDecl varDecl = null;
		NoVariavel var = null;

		param = metodo.getCorpo().getParametros();

		while (param != null) {

			varDecl = (NoVariavelDecl) param.getNo();
			var = (NoVariavel) varDecl.getVariaveis().getNo();

			tipo = tabelaAtual.buscaTipo(varDecl.getToken().image);

			if (listaParametro == null) {
				listaParametro = new SimboloParametro(tipo, var.getTamanho());
			} else {
				listaParametro.setProximo(new SimboloParametro(tipo, var
						.getTamanho()));
			}

			param = param.getProximo();
		}

		simboloMetodo = tabelaAtual.buscaMetodo(metodo.getNome().image,
				listaParametro);

		tabelaAtual = simboloMetodo.getTabela();

		tipoRetorno = new Tipo(simboloMetodo.getTipo(),
				simboloMetodo.getTamanho());

		tabelaAtual.iniciaEscopo();
		totalLocal = 0;

		analisaNoCorpoMetodo(metodo.getCorpo());
		simboloMetodo.setTotalLocal(totalLocal);
		tabelaAtual.terminaEscopo();
		tabelaAtual = temporaria;
	}

	private void analisaNoCorpoMetodo(NoCorpoMetodo corpo) {
		if (corpo == null) {
			return;
		}

		// Trata os par�metros como vari�vel local
		analisaNoListaVariaveis(corpo.getParametros());

		// Analisa o bloco do m�todo
		analisaNoBloco(corpo.getBloco());
	}

	private void analisaNoListaVariaveis(NoLista variaveis) {
		if (variaveis == null) {
			return;
		}

		try {
			analisaNoVariavel((NoVariavelDecl) variaveis.getNo());
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}

		analisaNoListaVariaveis(variaveis.getProximo());
	}

	private void analisaNoVariavel(NoVariavelDecl variavel)
			throws ErroSemanticoException {
		NoLista variaveis = null;
		NoVariavel var = null;
		SimboloVariavel v = null;
		SimboloEntrada tipo = null;

		// Busca o tipo na tabela local
		tipo = tabelaAtual.buscaTipo(variavel.getToken().image);

		if (tipo == null) {
			throw new ErroSemanticoException(variavel.getToken(), "Tipo \""
					+ variavel.getToken().image + "\" n�o encontrado");
		}

		variaveis = variavel.getVariaveis();
		while (variaveis != null) {
			var = (NoVariavel) variaveis.getNo();

			v = tabelaAtual.buscaVariavel(var.getToken().image);

			if (v != null) {
				if (v.getEscopo() == tabelaAtual.getEscopo()) {
					throw new ErroSemanticoException(variaveis.getToken(),
							"Vari�vel \"" + variaveis.getToken().image
									+ "\" redeclarada");
				}
			}

			tabelaAtual.adiciona(new SimboloVariavel(tipo,
					var.getToken().image, var.getTamanho()));

			variaveis = variaveis.getProximo();
		}
	}

	private void analisaNoBloco(NoBloco bloco) {
		tabelaAtual.iniciaEscopo();
		analisaNoListaDeclaracao(bloco.getDeclaracoes());
		tabelaAtual.terminaEscopo();
	}

	private void analisaNoListaDeclaracao(NoLista declaracoes) {
		if (declaracoes == null) {
			return;
		}

		try {
			analisaNoDeclaracao((NoDeclaracao) declaracoes.getNo());
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}

		analisaNoListaDeclaracao(declaracoes.getProximo());
	}

	private void analisaNoDeclaracao(NoDeclaracao declaracao)
			throws ErroSemanticoException {
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

	private void analisaNoAtribuicao(NoAtribuicao atribuicao)
			throws ErroSemanticoException {
		if (atribuicao == null) {
			return;
		}

		if (!(atribuicao.getEsquerda() instanceof NoArray || atribuicao
				.getEsquerda() instanceof NoVariavel)) {
			throw new ErroSemanticoException(atribuicao.getToken(),
					"Valor esquerdo da atribui��o inv�lido");
		}

		Tipo esquerda = analisaTipoExpressao(atribuicao.getEsquerda());
		Tipo direita = analisaTipoExpressao(atribuicao.getDireita());

		if (esquerda.getTamanho() != direita.getTamanho()) {
			throw new ErroSemanticoException(atribuicao.getToken(),
					"Tamanho de array inv�lido na atribui��o");
		}

		if (esquerda.getEntrada() instanceof SimboloVariavel
				&& direita.getEntrada() == tipoVazio) {
			throw new ErroSemanticoException(atribuicao.getToken(),
					"Atribui��o de vazio inv�lida");
		}

		if (esquerda.getEntrada() != direita.getEntrada()) {
			throw new ErroSemanticoException(atribuicao.getToken(),
					"Tipos incompat�veis para atribui��o");
		}
	}

	private void analisaNoImprimir(NoImprimir imprimir)
			throws ErroSemanticoException {
		if (imprimir == null) {
			return;
		}

		Tipo expressao = analisaTipoExpressao(imprimir.getValor());

		if (expressao.getEntrada() != tipoTexto || expressao.getTamanho() != 0) {
			throw new ErroSemanticoException(imprimir.getToken(),
					"Tipo texto obrigat�rio para o comando imprimir");
		}
	}

	private void analisaNoLer(NoLer ler) throws ErroSemanticoException {
		if (ler == null) {
			return;
		}

		if (!(ler.getVariavel() instanceof NoVariavel || ler.getVariavel() instanceof NoArray)) {
			throw new ErroSemanticoException(ler.getToken(),
					"Express�o inv�lida para o comando ler");
		}

		Tipo expressao = analisaTipoExpressao(ler.getVariavel());

		if (!(expressao.getEntrada() == tipoTexto
				|| expressao.getEntrada() == tipoInteiro || expressao
					.getEntrada() == tipoDecimal)) {
			throw new ErroSemanticoException(ler.getToken(),
					"Tipo inv�lido para o comando ler");
		}

		if (expressao.getTamanho() != 0) {
			throw new ErroSemanticoException(ler.getToken(),
					"N�o � poss�vel ler um vetor");
		}
	}

	private void analisaNoRetornar(NoRetornar retornar)
			throws ErroSemanticoException {
		if (retornar == null) {
			return;
		}

		Tipo expressao = analisaTipoExpressao(retornar.getValor());

		if (expressao == null) {
			if (tipoRetorno.getEntrada() == tipoVazio) {
				return;
			} else {
				throw new ErroSemanticoException(retornar.getToken(),
						"Comando retornar sem express�o");
			}
		} else {
			if (tipoRetorno.getEntrada() == tipoVazio) {
				throw new ErroSemanticoException(retornar.getToken(),
						"M�todo do tipo vazio n�o pode retornar um valor");
			}
		}

		if (expressao.getEntrada() != tipoRetorno.getEntrada()
				|| expressao.getTamanho() != tipoRetorno.getTamanho()) {
			throw new ErroSemanticoException(retornar.getToken(),
					"Tipo de retorno inv�lido");
		}
	}

	private void analisaNoSe(NoSe se) {
		if (se == null) {
			return;
		}

		try {
			Tipo expressao = analisaTipoExpressao(se.getCondicao());
			if (!(se.getCondicao() instanceof NoRelacional)) {
				throw new ErroSemanticoException(se.getToken(),
						"Condi��o inv�lida");
			}
			if (expressao.getEntrada() != tipoInteiro
					|| expressao.getTamanho() != 0) {
				throw new ErroSemanticoException(se.getToken(),
						"Condi��o inv�lida");
			}
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}

		analisaNoBloco(se.getVerdadeiro());
		analisaNoBloco(se.getFalso());
	}

	private void analisaNoEnquanto(NoEnquanto enquanto)
			throws ErroSemanticoException {
		if (enquanto == null) {
			return;
		}

		try {
			Tipo expressao = analisaTipoExpressao(enquanto.getCondicao());
			if (!(enquanto.getCondicao() instanceof NoRelacional)) {
				throw new ErroSemanticoException(enquanto.getToken(),
						"Condi��o inv�lida");
			}
			if (expressao.getEntrada() != tipoInteiro
					|| expressao.getTamanho() != 0) {
				throw new ErroSemanticoException(enquanto.getToken(),
						"Condi��o inv�lida");
			}
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}

		analisaNoBloco(enquanto.getBloco());
	}

	private Tipo analisaTipoExpressao(NoExpressao expressao)
			throws ErroSemanticoException {
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
			return null;
		}
	}

	private Tipo analisaTipoNoListaExpressao(NoLista lista) {
		if (lista == null) {
			return new Tipo(tipoNulo, 0);
		}

		Tipo primeiro = null;
		try {
			primeiro = analisaTipoExpressao((NoExpressao) lista.getNo());
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
			return new Tipo(tipoNulo, 0);
		}

		Tipo proximo = analisaTipoNoListaExpressao(lista.getProximo());

		int tamanho = 0;
		if (proximo.getEntrada() != null) {
			tamanho = ((SimboloParametro) proximo.getEntrada()).getElementos();
		}

		SimboloParametro p = new SimboloParametro(primeiro.getEntrada(),
				tamanho);

		return new Tipo(p, 0);
	}

	private Tipo analisaTipoNoAlocacao(NoAlocacao alocacao)
			throws ErroSemanticoException {
		if (alocacao == null) {
			return null;
		}

		SimboloEntrada tipo = tabelaAtual.buscaTipo(alocacao.getTipo().image);

		if (tipo == null) {
			throw new ErroSemanticoException(alocacao.getToken(), "Tipo \""
					+ alocacao.getTipo().image + "\" n�o encontrado");
		}

		NoLista tamanho = alocacao.getTamanho();
		int tam = 0;

		while (tamanho != null) {
			Tipo t = analisaTipoExpressao((NoExpressao) tamanho.getNo());

			if (t.getEntrada() != tipoInteiro || t.getTamanho() != 0) {
				throw new ErroSemanticoException(alocacao.getToken(),
						"Express�o inv�lida para tamanho de vetor");
			}

			tam++;
			tamanho = tamanho.getProximo();
		}

		return new Tipo(tipo, tam);
	}

	private Tipo analisaTipoNoRelacional(NoRelacional relacional)
			throws ErroSemanticoException {
		if (relacional == null) {
			return null;
		}

		int operacao = relacional.getToken().kind;
		Tipo esquerda = analisaTipoExpressao(relacional.getEsquerda());
		Tipo direita = analisaTipoExpressao(relacional.getDireita());

		if (esquerda.getEntrada() == tipoInteiro
				&& direita.getEntrada() == tipoInteiro) {
			return new Tipo(tipoInteiro, 0);
		}

		if (esquerda.getTamanho() != direita.getTamanho()) {
			throw new ErroSemanticoException(relacional.getToken(),
					"N�o � poss�vel comparar objetos de tamanhos diferentes");
		}

		if (operacao != BLangMotorConstants.IGUAL
				&& operacao != BLangMotorConstants.DIFERENTE
				&& esquerda.getTamanho() > 0) {
			throw new ErroSemanticoException(relacional.getToken(),
					"N�o � poss�vel usar \"" + relacional.getToken().image
							+ "\" para comparar vetores");
		}

		if (esquerda.getEntrada() == direita.getEntrada()
				&& (operacao == BLangMotorConstants.IGUAL && operacao == BLangMotorConstants.DIFERENTE)) {
			return new Tipo(tipoInteiro, 0);
		}

		throw new ErroSemanticoException(relacional.getToken(),
				"Tipos inv�lidos para a rela��o");
	}

	private Tipo analisaTipoNoAdicao(NoAdicao adicao)
			throws ErroSemanticoException {
		if (adicao == null) {
			return null;
		}

		int operacao = adicao.getToken().kind;
		Tipo esquerda = analisaTipoExpressao(adicao.getEsquerda());
		Tipo direita = analisaTipoExpressao(adicao.getDireita());

		if (esquerda.getTamanho() != 0 || direita.getTamanho() != 0) {
			throw new ErroSemanticoException(adicao.getToken(),
					"N�o � poss�vel usar \"" + adicao.getToken().image
							+ "\" para vetores");
		}

		int inteiro = 0;
		int decimal = 0;
		int texto = 0;

		if (esquerda.getEntrada() == tipoInteiro) {
			inteiro++;
		} else if (esquerda.getEntrada() == tipoDecimal) {
			decimal++;
		} else if (esquerda.getEntrada() == tipoTexto) {
			texto++;
		}

		if (direita.getEntrada() == tipoInteiro) {
			inteiro++;
		} else if (direita.getEntrada() == tipoDecimal) {
			decimal++;
		} else if (direita.getEntrada() == tipoTexto) {
			texto++;
		}

		if (inteiro == 2) {
			return new Tipo(tipoInteiro, 0);
		} else if (decimal == 2) {
			return new Tipo(tipoDecimal, 0);
		}

		if (operacao == BLangMotorConstants.MAIS
				&& ((inteiro + texto == 2) || (decimal + texto == 2))) {
			return new Tipo(tipoTexto, 0);
		}

		throw new ErroSemanticoException(adicao.getToken(),
				"Tipos inv�lidos para a adi��o/subtra��o");
	}

	private Tipo analisaTipoNoMultiplicacao(NoMultiplicacao mult)
			throws ErroSemanticoException {
		if (mult == null) {
			return null;
		}

		Tipo esquerda = analisaTipoExpressao(mult.getEsquerda());
		Tipo direita = analisaTipoExpressao(mult.getDireita());

		if (esquerda.getTamanho() != 0 || direita.getTamanho() != 0) {
			throw new ErroSemanticoException(mult.getToken(),
					"N�o � poss�vel usar \"" + mult.getToken().image
							+ "\" para vetores");
		}

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

	private Tipo analisaTipoNoUnario(NoUnario unario)
			throws ErroSemanticoException {
		if (unario == null) {
			return null;
		}

		Tipo expressao = analisaTipoExpressao(unario.getFator());

		if (expressao.getTamanho() != 0) {
			throw new ErroSemanticoException(unario.getToken(),
					"N�o � poss�vel utilizar termos un�rios com vetor");
		}

		if (expressao.getEntrada() == tipoInteiro) {
			return new Tipo(tipoInteiro, 0);
		} else if (expressao.getEntrada() == tipoDecimal) {
			return new Tipo(tipoDecimal, 0);
		} else {
			throw new ErroSemanticoException(unario.getToken(), "Tipo inv�lido");
		}
	}

	private Tipo analisaTipoNoInteiro(NoInteiro inteiro)
			throws ErroSemanticoException {
		if (inteiro == null) {
			return null;
		}

		try {
			Integer.parseInt(inteiro.getToken().image);
		} catch (NumberFormatException e) {
			throw new ErroSemanticoException(inteiro.getToken(),
					"Constante inteira inv�lida");
		}

		return new Tipo(tipoInteiro, 0);
	}

	private Tipo analisaTipoNoDecimal(NoDecimal decimal)
			throws ErroSemanticoException {
		if (decimal == null) {
			return null;
		}

		try {
			Double.parseDouble(decimal.getToken().image);
		} catch (NumberFormatException e) {
			throw new ErroSemanticoException(decimal.getToken(),
					"Constante decimal inv�lida");
		}

		return new Tipo(tipoDecimal, 0);
	}

	private Tipo analisaTipoNoTexto(NoTexto texto) {
		if (texto == null) {
			return null;
		}

		return new Tipo(tipoTexto, 0);
	}

	private Tipo analisaTipoNoNulo(NoNulo nulo) {
		if (nulo == null) {
			return null;
		}

		return new Tipo(tipoNulo, 0);
	}

	private Tipo analisaTipoNoVariavel(NoVariavel variavel)
			throws ErroSemanticoException {
		if (variavel == null) {
			return null;
		}

		SimboloVariavel simbolo = tabelaAtual
				.buscaVariavel(variavel.getToken().image);

		if (simbolo == null) {
			throw new ErroSemanticoException(variavel.getToken(), "Vari�vel \""
					+ variavel.getToken().image + "\" n�o encontrada");
		}

		return new Tipo(simbolo.getTipo(), simbolo.getTamanho());
	}

	private void analisaNoChamadaDecl(NoChamadaDecl chamada)
			throws ErroSemanticoException {
		NoLista arg = null;
		SimboloParametro parametro = null;

		for (arg = chamada.getArgumentos(); arg != null; arg = arg.getProximo()) {
			Tipo tipo = analisaTipoExpressao((NoExpressao) arg.getNo());

			if (parametro == null) {
				parametro = new SimboloParametro(tipo.getEntrada(),
						tipo.getTamanho());
			} else {
				parametro.setProximo(tipo.getEntrada());
			}
		}

		SimboloMetodo metodo = tabelaAtual.buscaMetodo(
				chamada.getToken().image, parametro);

		if (metodo == null) {
			throw new ErroSemanticoException(chamada.getToken(), "O m�todo \""
					+ chamada.getToken().image + "\" n�o foi encontrado");
		}
	}

	private Tipo analisaTipoNoChamada(NoChamada chamada)
			throws ErroSemanticoException {
		Tipo argumentos = analisaTipoNoListaExpressao(chamada.getArgumentos());

		SimboloMetodo metodo = tabelaAtual.buscaMetodo(
				chamada.getToken().image,
				(SimboloParametro) argumentos.getEntrada());

		if (metodo == null) {
			throw new ErroSemanticoException(chamada.getToken(), "M�todo \""
					+ chamada.getToken().image + "\" n�o encontrado");
		}

		return new Tipo(metodo.getTipo(), metodo.getTamanho());
	}

	private Tipo analisaTipoNoArray(NoArray array)
			throws ErroSemanticoException {
		if (array == null) {
			return new Tipo(tipoNulo, 0);
		}

		SimboloVariavel variavel = tabelaAtual
				.buscaVariavel(array.getToken().image);

		if (variavel == null) {
			throw new ErroSemanticoException(array.getToken(), "Vetor \""
					+ array.getToken().image + "\" n�o encontrado");
		}

		if (variavel.getTamanho() <= 0) {
			throw new ErroSemanticoException(array.getToken(), "A vari�vel \""
					+ array.getToken().image
					+ "\" n�o pode ser acessada como array");
		}

		Tipo expressoes = analisaTipoNoListaExpressao(array.getExpressoes());
		if (expressoes != null) {
			if (variavel.getTamanho() != expressoes.getTamanho()) {
				throw new ErroSemanticoException(array.getToken(),
						"A dimens�o vetor \"" + array.getToken().image
								+ "\" difere da declara��o");
			}

			if (expressoes.getEntrada() != tipoInteiro
					&& expressoes.getTamanho() > 0) {
				throw new ErroSemanticoException(array.getToken(),
						"O �ndice do vetor deve ser inteiro");
			}
		}

		return new Tipo(variavel.getTipo(), variavel.getTamanho());
	}

}
