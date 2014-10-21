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

	// Raiz da árvore sintática
	private NoLista raiz;

	// Quantidade de erros semânticos
	private int erros;

	// Tabela de símbolos principal (nível mais alto)
	private TabelaSimbolo tabelaPrincipal;

	// Tabela de símbolos atual (alterada durante a mudança de escopo)
	private TabelaSimbolo tabelaAtual;

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
			tabelaPrincipal.adiciona(new SimboloSimples("inteiro"));
			tabelaPrincipal.adiciona(new SimboloSimples("decimal"));
			tabelaPrincipal.adiciona(new SimboloSimples("texto"));
			tabelaPrincipal.adiciona(new SimboloSimples("vazio"));

			tabelaAtual = tabelaPrincipal;

			analisaNoListaMetodoDecl(raiz);

			if (erros != 0) {
				throw new ErroSemanticoException(erros
						+ " erros semânticos encontrados (fase 1)");
			}
		}
	}

	private void analisaNoListaMetodoDecl(NoLista listaMetodos) {

		// Não executa se o nó é nulo (final de lista)
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

		// Continua a análise através dos próximos nos
		analisaNoListaMetodoDecl(listaMetodos.getProximo());
	}

	private void analisaNoMetodoDecl(NoMetodoDecl metodo)
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

}
