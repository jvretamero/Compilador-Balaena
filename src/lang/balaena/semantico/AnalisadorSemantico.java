package lang.balaena.semantico;

import java.awt.font.NumericShaper;

import lang.balaena.BLangMotorConstants;
import lang.balaena.arvore.NoAdicao;
import lang.balaena.arvore.NoAlocacao;
import lang.balaena.arvore.NoArray;
import lang.balaena.arvore.NoAtribuicao;
import lang.balaena.arvore.NoBloco;
import lang.balaena.arvore.NoChamada;
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

	// Raiz da árvore sintática
	private NoLista raiz;

	// Quantidade de erros semânticos
	private int erros;

	// Tabela de símbolos principal (nível mais alto)
	private TabelaSimbolo tabelaPrincipal;

	// Tabela de símbolos atual (alterada durante a mudança de escopo)
	private TabelaSimbolo tabelaAtual;

	// Método atual
	private SimboloMetodo metodoAtual;

	private int totalLocal;
	private Tipo tipoRetorno;
	private final SimboloSimples tipoTexto = new SimboloSimples("texto");
	private final SimboloSimples tipoInteiro = new SimboloSimples("inteiro");
	private final SimboloSimples tipoDecimal = new SimboloSimples("decimal");
	private final SimboloSimples tipoVazio = new SimboloSimples("vazio");
	private final SimboloSimples tipoNulo = new SimboloSimples("nulo");

	// Inicia o analisador semântico a partir do nó raiz da árvore sintática
	public AnalisadorSemantico(NoLista raiz) {
		this.raiz = raiz;
	}

	// Inicia a análise semântica
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

			if (erros != 0) {
				throw new ErroSemanticoException(erros
						+ " erros semânticos encontrados (fase 1)");
			}
		}
	}

	private void simbolosNoListaMetodoDecl(NoLista listaMetodos) {

		// Não executa se o nó é nulo (final de lista)
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

	private void simbolosNoMetodoDecl(NoMetodoDecl metodo)
			throws ErroSemanticoException {

		// Final de lista
		if (metodo == null) {
			return;
		}

		// Declara as variáveis auxiliares
		NoVariavelDecl variavelDecl = null;
		NoVariavel variavel = null;
		SimboloEntrada tipo = null;
		SimboloMetodo m = null;
		SimboloParametro entradaParam = null;

		// Obtém os parâmetros do método
		NoLista parametros = metodo.getCorpo().getParametros();

		// Contagem de parâmetros
		int count = 0;

		// Percorre todos os parâmetros
		while (parametros != null) {
			count++; // Incrementa 1

			// Obtém a declaração da variável do parâmetro
			variavelDecl = (NoVariavelDecl) parametros.getNo();
			// Obtém a variável do parâmetro
			variavel = (NoVariavel) variavelDecl.getVariaveis().getNo();

			// Busca o tipo da variável na tabela de símbolos
			tipo = tabelaAtual.buscaEntrada(variavelDecl.getToken().image);

			// Se não encontrou o tipo primitivo emite um erro
			if (tipo == null) {
				throw new ErroSemanticoException(variavelDecl.getToken(),
						"Tipo " + variavelDecl.getToken().image
								+ " não declarado");
			}

			// Cria o elemento da lista de parametros
			entradaParam = new SimboloParametro(tipo, variavel.getTamanho(),
					count, entradaParam);

			// Vai para o próximo nó da árvore
			parametros = parametros.getProximo();
		}

		// Verifica se criou a lista de parâmetros
		if (entradaParam != null) {
			// Inverte a lista de parâmetros
			entradaParam.inverte();
		}

		// Busca o tipo de retorno do método na tabela de símbolos
		tipo = tabelaAtual.buscaEntrada(metodo.getToken().image);

		// Se o tipo de retorno não for encontrado emite um erro
		if (tipo == null) {
			throw new ErroSemanticoException(metodo.getToken(), "Tipo "
					+ metodo.getToken().image + " não declarado");
		}

		// Busca o método na tabela de símbolos
		m = tabelaAtual.buscaMetodo(metodo.getNome().image, entradaParam);

		// Se não declarado ainda, inclui na tabela de símbolos
		if (m == null) {
			m = new SimboloMetodo(tipo, metodo.getNome().image, count,
					entradaParam);
			tabelaAtual.adiciona(m);
		} else {
			// Se já declarado, emite um erro
			throw new ErroSemanticoException(metodo.getNome(), "Método "
					+ metodo.getNome().image + " já declarado");
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
		analisaNoListaMetodoDecl(metodos);
	}

	private void analisaNoMetodoDecl(NoMetodoDecl metodo)
			throws ErroSemanticoException {
		if (metodo == null) {
			return;
		}

		SimboloEntrada tipo = null;
		SimboloParametro sParam = null;
		SimboloMetodo sMetodo = null;
		NoLista param = null;
		NoVariavelDecl varDecl = null;
		NoVariavel var = null;
		int tam = 0;

		param = metodo.getCorpo().getParametros();

		while (param != null) {
			tam++;

			varDecl = (NoVariavelDecl) param.getNo();
			var = (NoVariavel) varDecl.getVariaveis().getNo();

			tipo = tabelaAtual.buscaEntrada(varDecl.getToken().image);

			sParam = new SimboloParametro(tipo, var.getTamanho(), tam, sParam);

			param = param.getProximo();
		}

		if (sParam != null) {
			sParam.inverte();
		}

		sMetodo = tabelaAtual.buscaMetodo(metodo.getNome().image, sParam);

		metodoAtual = sMetodo;

		tipoRetorno = new Tipo(sMetodo.getTipo(), sMetodo.getTamanho());

		tabelaAtual.iniciaEscopo();
		totalLocal = 0;

		analisaNoCorpoMetodo(metodo.getCorpo());
		sMetodo.setTotalLocal(totalLocal);
		tabelaAtual.terminaEscopo();
	}

	private void analisaNoCorpoMetodo(NoCorpoMetodo corpo) {
		if (corpo == null) {
			return;
		}

		// Trata os parâmetros como variável local
		analisaNoListaVariaveis(corpo.getParametros());

		// Analisa o bloco do método
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
		SimboloEntrada decl = null;
		SimboloEntrada tipo = null;

		// Busca o tipo na tabela local
		tipo = tabelaAtual.buscaEntrada(variavel.getToken().image);

		if (tipo == null) {
			throw new ErroSemanticoException(variavel.getToken(), "Tipo "
					+ variavel.getToken().image + " não encontrado");
		}

		variaveis = variavel.getVariaveis();
		while (variaveis != null) {
			var = (NoVariavel) variaveis.getNo();
			v = tabelaAtual.buscaVariavel(var.getToken().image);

			if (v != null) {
				if (v.getEscopo() == tabelaAtual.getEscopo()) {
					throw new ErroSemanticoException(variaveis.getToken(),
							"Variável " + variaveis.getToken().image
									+ " redeclarada");
				}
			}

			tabelaAtual.adiciona(new SimboloVariavel(tipo,
					variavel.getToken().image, var.getTamanho()));

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
					"Valor esquerdo da atribuição inválido");
		}

		Tipo esquerda = analisaTipoExpressao(atribuicao.getEsquerda());
		Tipo direita = analisaTipoExpressao(atribuicao.getDireita());

		if (esquerda.getTamanho() != direita.getTamanho()) {
			throw new ErroSemanticoException(atribuicao.getToken(),
					"Tamanho de array inválido na atribuição");
		}

		if (esquerda.getEntrada() instanceof SimboloVariavel
				&& direita.getEntrada() == tipoVazio) {
			throw new ErroSemanticoException(atribuicao.getToken(),
					"Atribuição de vazio inválida");
		}

		if (esquerda.getEntrada() != direita.getEntrada()) {
			throw new ErroSemanticoException(atribuicao.getToken(),
					"Tipos incompatíveis para atribuição");
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
					"Tipo texto obrigatório para o comando imprimir");
		}
	}

	private void analisaNoLer(NoLer ler) throws ErroSemanticoException {
		if (ler == null) {
			return;
		}

		if (!(ler.getVariavel() instanceof NoVariavel || ler.getVariavel() instanceof NoArray)) {
			throw new ErroSemanticoException(ler.getToken(),
					"Expressão inválida para o comando ler");
		}

		Tipo expressao = analisaTipoExpressao(ler.getVariavel());

		if (!(expressao.getEntrada() == tipoTexto
				|| expressao.getEntrada() == tipoInteiro || expressao
					.getEntrada() == tipoDecimal)) {
			throw new ErroSemanticoException(ler.getToken(),
					"Tipo inválido para o comando ler");
		}

		if (expressao.getTamanho() != 0) {
			throw new ErroSemanticoException(ler.getToken(),
					"Não é possível ler um vetor");
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
						"Comando retornar sem expressão");
			}
		} else {
			if (tipoRetorno.getEntrada() == tipoVazio) {
				throw new ErroSemanticoException(retornar.getToken(),
						"Método do tipo vazio não pode retornar um valor");
			}
		}

		if (expressao.getEntrada() != tipoRetorno.getEntrada()
				|| expressao.getTamanho() != tipoRetorno.getTamanho()) {
			throw new ErroSemanticoException(retornar.getToken(),
					"Tipo de retorno inválido");
		}
	}

	private void analisaNoSe(NoSe se) {
		if (se == null) {
			return;
		}

		try {
			Tipo expressao = analisaTipoExpressao(se.getCondicao());

			if (expressao.getEntrada() != tipoInteiro
					|| expressao.getTamanho() != 0) {
				throw new ErroSemanticoException(se.getToken(),
						"Condição inválida");
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

			if (expressao.getEntrada() != tipoInteiro
					|| expressao.getTamanho() != 0) {
				throw new ErroSemanticoException(enquanto.getToken(),
						"Condição inválida");
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

	private Tipo analisaTipoNoAlocacao(NoAlocacao alocacao)
			throws ErroSemanticoException {
		if (alocacao == null) {
			return null;
		}

		SimboloEntrada tipo = tabelaAtual
				.buscaEntrada(alocacao.getTipo().image);

		if (tipo == null) {
			throw new ErroSemanticoException(alocacao.getToken(), "Tipo "
					+ alocacao.getTipo().image + " não encontrado");
		}

		NoLista tamanho = alocacao.getTamanho();
		int tam = 0;

		while (tamanho != null) {
			Tipo t = analisaTipoExpressao((NoExpressao) tamanho.getNo());

			if (t.getEntrada() != tipoInteiro || t.getTamanho() != 0) {
				throw new ErroSemanticoException(alocacao.getToken(),
						"Expressão inválida para tamanho de vetor");
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
					"Não é possível comparar objetos de tamanhos diferentes");
		}

		if (operacao != BLangMotorConstants.IGUAL
				&& operacao != BLangMotorConstants.DIFERENTE
				&& esquerda.getTamanho() > 0) {
			throw new ErroSemanticoException(relacional.getToken(),
					"Não é possível usar " + relacional.getToken().image
							+ " para comparar vetores");
		}

		if (esquerda.getEntrada() == direita.getEntrada()
				&& (operacao == BLangMotorConstants.IGUAL && operacao == BLangMotorConstants.DIFERENTE)) {
			return new Tipo(tipoInteiro, 0);
		}

		throw new ErroSemanticoException(relacional.getToken(),
				"Tipos inválidos para a relação");
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
					"Não é possível usar " + adicao.getToken().image
							+ " para vetores");
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
				"Tipos inválidos para a adição/subtração");
	}

	private Tipo analisaTipoNoMultiplicacao(NoMultiplicacao mult)
			throws ErroSemanticoException {
		if (mult == null) {
			return null;
		}

		int operacao = mult.getToken().kind;
		Tipo esquerda = analisaTipoExpressao(mult.getEsquerda());
		Tipo direita = analisaTipoExpressao(mult.getDireita());

		if (esquerda.getTamanho() != 0 || direita.getTamanho() != 0) {
			throw new ErroSemanticoException(mult.getToken(),
					"Não é possível usar " + mult.getToken().image
							+ " para vetores");
		}

		if (esquerda.getEntrada() == tipoInteiro
				&& direita.getEntrada() == tipoInteiro) {
			return new Tipo(tipoInteiro, 0);
		} else if (esquerda.getEntrada() == tipoDecimal
				&& direita.getEntrada() == tipoDecimal) {
			return new Tipo(tipoDecimal, 0);
		} else {
			throw new ErroSemanticoException(mult.getToken(), "Tipos inválidos"
					+ mult.getToken().image);
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
					"Não é possível utilizar termos unários com vetor");
		}

		if (expressao.getEntrada() == tipoInteiro) {
			return new Tipo(tipoInteiro, 0);
		} else if (expressao.getEntrada() == tipoDecimal) {
			return new Tipo(tipoDecimal, 0);
		} else {
			throw new ErroSemanticoException(unario.getToken(), "Tipo inválido");
		}
	}

	private Tipo analisaTipoNoInteiro(NoInteiro inteiro)
			throws ErroSemanticoException {
		if (inteiro == null) {
			return null;
		}

		try {
			int valor = Integer.parseInt(inteiro.getToken().image);
		} catch (NumberFormatException e) {
			throw new ErroSemanticoException(inteiro.getToken(),
					"Constante inteira inválida");
		}

		return new Tipo(tipoInteiro, 0);
	}

	private Tipo analisaTipoNoDecimal(NoDecimal decimal)
			throws ErroSemanticoException {
		if (decimal == null) {
			return null;
		}

		try {
			double valor = Double.parseDouble(decimal.getToken().image);
		} catch (NumberFormatException e) {
			throw new ErroSemanticoException(decimal.getToken(),
					"Constante decimal inválida");
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
			throw new ErroSemanticoException(variavel.getToken(), "Variável "
					+ variavel.getToken().image + " não encontrada");
		}

		return new Tipo(simbolo.getTipo(), simbolo.getTamanho());
	}

	private Tipo analisaTipoNoChamada(NoChamada chamada) {
		return null;
	}

	private Tipo analisaTipoNoArray(NoArray array) {
		return null;
	}

}
