package lang.balaena.semantico;

import lang.balaena.arvore.NoLista;
import lang.balaena.arvore.NoMetodoDecl;
import lang.balaena.arvore.NoVariavel;
import lang.balaena.arvore.NoVariavelDecl;
import lang.balaena.simbolos.SimboloEntrada;
import lang.balaena.simbolos.SimboloMetodo;
import lang.balaena.simbolos.SimboloParametro;
import lang.balaena.simbolos.SimboloSimples;
import lang.balaena.simbolos.TabelaSimbolo;

public class AnalisadorSemantico {

	// Raiz da �rvore sint�tica
	private NoLista raiz;

	// Quantidade de erros sem�nticos
	private int erros;

	// Tabela de s�mbolos principal (n�vel mais alto)
	private TabelaSimbolo tabelaPrincipal;

	// Tabela de s�mbolos atual (alterada durante a mudan�a de escopo)
	private TabelaSimbolo tabelaAtual;

	// Inicia o analisador sem�ntico a partir do n� raiz da �rvore sint�tica
	public AnalisadorSemantico(NoLista raiz) {
		this.raiz = raiz;
	}

	// Inicia a an�lise sem�ntica
	public void analisa() throws ErroSemanticoException {
		if (raiz != null) {

			erros = 0;
			tabelaPrincipal = new TabelaSimbolo();
			tabelaAtual = new TabelaSimbolo();

			// Adiciona os tipos primitivos na tabela principal
			tabelaPrincipal.adiciona(new SimboloSimples("inteiro"));
			tabelaPrincipal.adiciona(new SimboloSimples("decimal"));
			tabelaPrincipal.adiciona(new SimboloSimples("texto"));
			tabelaPrincipal.adiciona(new SimboloSimples("vazio"));

			tabelaAtual = tabelaPrincipal;

			analisaNoListaMetodoDecl(raiz);

			if (erros != 0) {
				throw new ErroSemanticoException(erros
						+ " erros sem�nticos encontrados (fase 1)");
			}
		}
	}

	private void analisaNoListaMetodoDecl(NoLista listaMetodos) {

		// N�o executa se o n� � nulo (final de lista)
		if (listaMetodos == null) {
			return;
		}

		try {
			// Analisa o NoMetodoDecl
			analisaNoMetodoDecl((NoMetodoDecl) listaMetodos.getNo());
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			erros++;
		}

		// Continua a an�lise atrav�s dos pr�ximos nos
		analisaNoListaMetodoDecl(listaMetodos.getProximo());
	}

	private void analisaNoMetodoDecl(NoMetodoDecl metodo)
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
		SimboloParametro entradaParam = null;

		// Obt�m os par�metros do m�todo
		NoLista parametros = metodo.getCorpo().getParametros();

		// Contagem de par�metros
		int count = 0;

		// Percorre todos os par�metros
		while (parametros != null) {
			count++; // Incrementa 1

			// Obt�m a declara��o da vari�vel do par�metro
			variavelDecl = (NoVariavelDecl) parametros.getNo();
			// Obt�m a vari�vel do par�metro
			variavel = (NoVariavel) variavelDecl.getVariaveis().getNo();

			// Busca o tipo da vari�vel na tabela de s�mbolos
			tipo = tabelaAtual.buscaEntrada(variavelDecl.getToken().image);

			// Se n�o encontrou o tipo primitivo emite um erro
			if (tipo == null) {
				throw new ErroSemanticoException(variavelDecl.getToken(),
						"Tipo " + variavelDecl.getToken().image
								+ " n�o declarado");
			}

			// Cria o elemento da lista de parametros
			entradaParam = new SimboloParametro(tipo, variavel.getTamanho(),
					count, entradaParam);

			// Vai para o pr�ximo n� da �rvore
			parametros = parametros.getProximo();
		}

		// Verifica se criou a lista de par�metros
		if (entradaParam != null) {
			// Inverte a lista de par�metros
			entradaParam.inverte();
		}

		// Busca o tipo de retorno do m�todo na tabela de s�mbolos
		tipo = tabelaAtual.buscaEntrada(metodo.getToken().image);

		// Se o tipo de retorno n�o for encontrado emite um erro
		if (tipo == null) {
			throw new ErroSemanticoException(metodo.getToken(), "Tipo "
					+ metodo.getToken().image + " n�o declarado");
		}

		// Busca o m�todo na tabela de s�mbolos
		m = tabelaAtual.buscaMetodo(metodo.getNome().image, entradaParam);

		// Se n�o declarado ainda, inclui na tabela de s�mbolos
		if (m == null) {
			m = new SimboloMetodo(tipo, metodo.getNome().image, count,
					entradaParam);
			tabelaAtual.adiciona(m);
		} else {
			// Se j� declarado, emite um erro
			throw new ErroSemanticoException(metodo.getNome(), "M�todo "
					+ metodo.getNome().image + " j� declarado");
		}
	}

}
