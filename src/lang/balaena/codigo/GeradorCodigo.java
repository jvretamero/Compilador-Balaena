package lang.balaena.codigo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

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
import lang.balaena.semantico.AnalisadorSemantico;
import lang.balaena.semantico.ErroSemanticoException;
import lang.balaena.simbolos.SimboloEntrada;
import lang.balaena.simbolos.SimboloMetodo;
import lang.balaena.simbolos.SimboloParametro;
import lang.balaena.simbolos.SimboloSimples;
import lang.balaena.simbolos.SimboloVariavel;
import lang.balaena.simbolos.TabelaSimbolo;
import lang.balaena.simbolos.Tipo;

public class GeradorCodigo {

	private static final String classe = "ProgBalaena";

	private AnalisadorSemantico semantico;
	private TabelaSimbolo tabelaAtual;
	private NoLista raiz;
	private File arqInter;
	private File arqFinal;
	private PrintWriter pw;
	private File arquivo;
	private int alturaPilha;
	private int tamanhoPilha;
	private boolean armazena;
	private int totalLocal;
	private int labelAtual;

	private final SimboloSimples tipoTexto = new SimboloSimples("texto");
	private final SimboloSimples tipoInteiro = new SimboloSimples("inteiro");
	private final SimboloSimples tipoDecimal = new SimboloSimples("decimal");
	private final SimboloSimples tipoNulo = new SimboloSimples("nulo");

	public GeradorCodigo(AnalisadorSemantico semantico, NoLista raiz,
			String arquivo) {
		this.semantico = semantico;
		this.raiz = raiz;
		this.arquivo = new File(arquivo);
	}

	public void gerar() {

		try {
			// Cria o arquivo temporário do Jasmin
			arqInter = File.createTempFile("bln_", "_inter.jas");

			// Cria o arquivo temporário final (será o .class)
			arqFinal = File.createTempFile("bln_", "_final.class");

			// Executa a geração do código Jasmin
			codigoIntermediario();

			// Executa o Jasmin para gerar o código final aceito pela JVM
			codigoFinal();

			copiaClasse();
		} catch (IOException e) {
			System.out.println("Ocorreu um erro: " + e.getMessage());
		}
	}

	private void codigoIntermediario() throws IOException {
		FileOutputStream out = new FileOutputStream(arqInter);
		pw = new PrintWriter(out);

		try {
			classePadrao();

			labelAtual = 0;
			tabelaAtual = semantico.getTabela();
			geraNoListaMetodoDecl(raiz);

			metodoPrincipal();
		} finally {
			pw.close();
			out.close();
		}

		copia(arqInter, new File("d:\\inter.jas"));
	}

	private void copia(File origem, File destino) throws IOException {
		InputStream is = null;
		OutputStream os = null;

		try {
			is = new FileInputStream(origem);
			os = new FileOutputStream(destino);

			byte[] buffer = new byte[1024];
			int length;

			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			if (is != null) {
				is.close();
			}
			if (os != null) {
				os.close();
			}
		}
	}

	private void codigoFinal() throws IOException {
		FileInputStream in = null;
		FileOutputStream out = null;

		try {
			in = new FileInputStream(arqInter);
			out = new FileOutputStream(arqFinal);

			// Executa o Jasmin para gerar o código
			jasmin.Main.assemble(in, out, true);
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

	private void copiaClasse() {
		String nome = arquivo.getName();
		int pos = nome.lastIndexOf(".");
		nome = nome.substring(0, pos) + ".class";

		File arqClasse = new File(getPath(), classe + ".class");

		try {
			copia(arqFinal, arqClasse);
		} catch (IOException e) {
			System.err
					.println("Ocorreu um problema ao gerar o arquivo .class do programa");
			System.err.println("Motivo: " + e.getMessage());
		}
	}

	private File getPath() {
		return new File(System.getProperty("user.dir"));
	}

	private void code(String code, int pilha) {
		alturaPilha += pilha;

		if (alturaPilha > tamanhoPilha) {
			tamanhoPilha = alturaPilha;
		}

		if (pw != null) {
			pw.println(code);
		}
	}

	private void code(String code) {
		code(code, 0);
	}

	private void code() {
		code("");
	}

	private String novoLabel() {
		return "BLN" + Integer.toString(labelAtual++);
	}

	private String operacaoBinaria(int op) {
		switch (op) {
		case BLangMotorConstants.MAIS:
			return "iadd";
		case BLangMotorConstants.MENOS:
			return "isub";
		case BLangMotorConstants.MULTIPLICACAO:
			return "imul";
		case BLangMotorConstants.DIVISAO:
			return "idiv";
		case BLangMotorConstants.RESTO:
			return "irem";
		case BLangMotorConstants.IGUAL:
			return "if_icmpeq";
		case BLangMotorConstants.DIFERENTE:
			return "if_icmpne";
		case BLangMotorConstants.MENOR:
			return "if_icmplt";
		case BLangMotorConstants.MAIOR:
			return "if_icmpgt";
		case BLangMotorConstants.MENORIGUAL:
			return "if_icmple";
		case BLangMotorConstants.MAIORIGUAL:
			return "if_icmpge";
		}
		return "";
	}

	private void classePadrao() {
		code(";---------------------------------------------");
		code("; Codigo gerado pelo Compilador Balaena");
		code("; Versao 0.1 - 2014");
		code(";---------------------------------------------");
		code(".source " + arquivo.getName());
		code(".class public " + classe);
		code(".super java/lang/Object");
		code();
		/**
		 * code("; Construtor padrão"); code(".method public <init>()V");
		 * code("aload_0"); code("invokespecial java/lang/Object/<init>()V");
		 * code("return"); code(".end method"); code();
		 **/
	}

	private void metodoPrincipal() {
		code();
		code("; Metodo principal");
		code(".method static public main([Ljava/lang/String;)V");
		code(".limit locals 1");
		code(".limit stack 1");
		code("invokestatic lang/balaena/BalaenaRuntime/inicia()I");
		code("ifne end");
		code("invokestatic " + classe + "/principal()V");
		code("end:");
		code("invokestatic lang/balaena/BalaenaRuntime/finaliza()V");
		code("return");
		code(".end method");
	}

	private void geraNoListaMetodoDecl(NoLista metodos) {
		if (metodos == null) {
			return;
		}

		geraNoMetodoDecl((NoMetodoDecl) metodos.getNo());
		geraNoListaMetodoDecl(metodos.getProximo());
	}

	private void geraNoMetodoDecl(NoMetodoDecl metodo) {
		if (metodo == null) {
			return;
		}

		TabelaSimbolo temporaria = tabelaAtual;
		SimboloParametro param = null;
		SimboloMetodo m = null;
		SimboloEntrada tipo = null;
		NoLista p = null;
		NoVariavelDecl varDecl = null;
		NoVariavel var = null;

		for (p = metodo.getCorpo().getParametros(); p != null; p = p
				.getProximo()) {
			varDecl = (NoVariavelDecl) p.getNo();
			var = (NoVariavel) varDecl.getVariaveis().getNo();

			tipo = tabelaAtual.buscaTipo(varDecl.getToken().image);

			if (param == null) {
				param = new SimboloParametro(tipo, var.getTamanho());
			} else {
				param.setProximo(new SimboloParametro(tipo, var.getTamanho()));
			}
		}

		m = tabelaAtual.buscaMetodo(metodo.getNome().image, param);

		tabelaAtual = m.getTabela();

		code();
		code(".method private static " + m.getNome() + "("
				+ (param == null ? "" : param.descJava()) + ")"
				+ m.getTipo().descJava());
		code(".limit locals " + (m.getTotalLocal() + 1));

		tabelaAtual.iniciaEscopo();

		tamanhoPilha = 0;
		alturaPilha = 0;
		totalLocal = 0;

		geraNoCorpoMetodo(metodo.getCorpo());

		tabelaAtual.terminaEscopo();

		if (m.getTipo().equals(tipoInteiro) && m.getTamanho() == 0) {
			//code("bipush 0", 1);
			code("ireturn", -1);
		} else {
			//code("aconst_null", 1);
			code("areturn", -1);
		}

		code("return");
		code(".limit stack " + (m.getTamanhoPilha() + alturaPilha));
		code(".end method");
		code();

		tabelaAtual = temporaria;
	}

	private void geraNoCorpoMetodo(NoCorpoMetodo corpo) {
		if (corpo == null) {
			return;
		}

		geraNoListaVariavelDecl(corpo.getParametros());
		geraNoBloco(corpo.getBloco());
	}

	private void geraNoListaVariavelDecl(NoLista variaveis) {
		if (variaveis == null) {
			return;
		}

		geraNoVariavelDecl((NoVariavelDecl) variaveis.getNo());
		geraNoListaVariavelDecl(variaveis.getProximo());
	}

	private void geraNoVariavelDecl(NoVariavelDecl variavel) {
		if (variavel == null) {
			return;
		}

		SimboloEntrada tipo = null;
		NoVariavel var = null;
		NoLista vars = null;

		tipo = tabelaAtual.buscaTipo(variavel.getToken().image);

		for (vars = variavel.getVariaveis(); vars != null; vars = vars
				.getProximo()) {
			var = (NoVariavel) vars.getNo();

			tabelaAtual.adiciona(new SimboloVariavel(tipo,
					var.getToken().image, var.getTamanho(), totalLocal++));
		}
	}

	private void geraNoBloco(NoBloco bloco) {
		if (bloco == null) {
			return;
		}
		tabelaAtual.iniciaEscopo();
		geraNoListaDeclaracao(bloco.getDeclaracoes());
		tabelaAtual.terminaEscopo();
	}

	private void geraNoListaDeclaracao(NoLista declaracoes) {
		if (declaracoes == null) {
			return;
		}
		geraNoDeclaracao((NoDeclaracao) declaracoes.getNo());
		geraNoListaDeclaracao(declaracoes.getProximo());
	}

	private void geraNoDeclaracao(NoDeclaracao declaracao) {
		if (declaracao == null) {
			return;
		}

		if (declaracao instanceof NoVariavelDecl) {
			geraNoVariavelDecl((NoVariavelDecl) declaracao);
		} else if (declaracao instanceof NoAtribuicao) {
			geraNoAtribuicao((NoAtribuicao) declaracao);
		} else if (declaracao instanceof NoImprimir) {
			geraNoImprimir((NoImprimir) declaracao);
		} else if (declaracao instanceof NoLer) {
			geraNoLer((NoLer) declaracao);
		} else if (declaracao instanceof NoRetornar) {
			geraNoRetornar((NoRetornar) declaracao);
		} else if (declaracao instanceof NoSe) {
			geraNoSe((NoSe) declaracao);
		} else if (declaracao instanceof NoEnquanto) {
			// geraNoEnquanto((NoEnquanto) declaracao);
		} else if (declaracao instanceof NoChamadaDecl) {
			geraNoChamadaDecl((NoChamadaDecl) declaracao);
		}
	}

	public void geraNoAtribuicao(NoAtribuicao atribuicao) {
		if (atribuicao == null) {
			return;
		}

		armazena = false;
		geraNoExpressao(atribuicao.getDireita());

		armazena = true;
		geraNoExpressao(atribuicao.getEsquerda());
	}

	private void geraNoImprimir(NoImprimir imprimir) {
		if (imprimir == null) {
			return;
		}

		code();
		code("; Comando de escrita");
		// Coloca System.out na pilha de execução
		code("getstatic java/lang/System/out Ljava/io/PrintStream;", 1);

		armazena = false;
		geraNoExpressao(imprimir.getValor());

		code("invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V", -2);
		code();
	}

	private void geraNoLer(NoLer ler) {
		if (ler == null) {
			return;
		}

		Tipo valor = null;

		try {
			valor = semantico.analisaTipoNoExpressao(ler.getVariavel());
		} catch (ErroSemanticoException e) {
			return;
		}

		code();
		code("; Comando de leitura");

		String comando = "";
		if (valor.getEntrada().equals(tipoInteiro)) {
			comando = "lerInteiro()" + Code.descJava(tipoInteiro);
		} else if (valor.getEntrada().equals(tipoDecimal)) {
			comando = "lerDecimal()" + Code.descJava(tipoDecimal);
		} else if (valor.getEntrada().equals(tipoTexto)) {
			comando = "lerTexto()" + Code.descJava(tipoTexto);
		}

		// Executa o comando do BalaenaRuntime
		if (!comando.isEmpty()) {
			code("invokestatic lang/balaena/runtime/BalaenaRuntime/" + comando,
					1);
		}

		// Operação de armazenagem
		armazena = true;
		// Gera o código da expressão a ser lida
		geraNoExpressao(ler.getVariavel());

		code();
	}

	private void geraNoRetornar(NoRetornar retornar) {
		if (retornar == null) {
			return;
		}

		code();
		code("; Comando de retorno");

		armazena = false;
		Tipo valor = geraNoExpressao(retornar.getValor());

		if (valor.getEntrada().equals(tipoInteiro) && valor.getTamanho() == 0) {
			code("ireturn", -1);
		} else {
			code("areturn", -1);
		}
	}

	private void geraNoSe(NoSe se) {
		if (se == null) {
			return;
		}

		String label = novoLabel();

		code();
		code("; Controle SE " + label);

		armazena = false;
		// Código da condição, deixando o resultado na pilha
		geraNoExpressao(se.getCondicao());

		// Se o resultado for falso, desvia para o label
		code("ifeq else " + label, -1);

		// Ccódigo para o bloco verdadeiro
		geraNoBloco(se.getVerdadeiro());

		// Verifica se existe "SENÃO"
		if (se.getFalso() != null) {
			// Desvio para o final do SE caso verdadeiro
			code("goto fi " + label);

			// Label para o começo do bloco falso
			code("else " + label + ":");

			// Código do bloco falso
			geraNoBloco(se.getFalso());

			// Final do comando
			code("fi " + label + ":");
		} else {
			// Fim do comando
			code("else " + label + ":");
		}
	}

	private Tipo geraNoChamadaDecl(NoChamadaDecl chamada) {
		return geraNoChamada(new NoChamada(chamada.getToken(),
				chamada.getArgumentos()));
	}

	private Tipo geraNoChamada(NoChamada chamada) {
		if (chamada == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Chamada do metodo " + chamada.getToken().image.toUpperCase());

		SimboloParametro params = null;
		NoLista p = null;

		// Cria os parâmetros do método e gera o código dos argumentos
		for (p = chamada.getArgumentos(); p != null; p = p.getProximo()) {
			Tipo tipo = geraNoExpressao((NoExpressao) p.getNo());

			if (params == null) {
				params = new SimboloParametro(tipo.getEntrada(),
						tipo.getTamanho());
			} else {
				params.setProximo(new SimboloParametro(tipo.getEntrada(), tipo
						.getTamanho()));
			}
		}

		// Busca o método na tabela de símbolos
		SimboloMetodo metodo = tabelaAtual.buscaMetodo(
				chamada.getToken().image, params);

		// Prepara o código para os parâmetros
		String args = (params == null ? "" : params.descJava());

		// Calcula quanto a pilha irá diminuir
		int pilha = (params == null ? 0 : params.getElementos());

		// Comando para executar o método
		code("invokevirtual " + classe + "/" + metodo.getNome() + "(" + args
				+ ")" + metodo.descJava(), -pilha);
		code();

		return new Tipo(metodo.getTipo(), metodo.getTamanho());
	}

	private Tipo geraNoExpressao(NoExpressao expressao) {
		if (expressao == null) {
			return new Tipo(tipoNulo, 0);
		}

		if (expressao instanceof NoAlocacao) {
			return geraNoAlocacao((NoAlocacao) expressao);
		} else if (expressao instanceof NoRelacional) {
			return geraNoRelacional((NoRelacional) expressao);
		} else if (expressao instanceof NoAdicao) {
			return geraNoAdicao((NoAdicao) expressao);
		} else if (expressao instanceof NoMultiplicacao) {
			return geraNoMultiplicacao((NoMultiplicacao) expressao);
		} else if (expressao instanceof NoUnario) {
			return geraNoUnario((NoUnario) expressao);
		} else if (expressao instanceof NoChamada) {
			return geraNoChamada((NoChamada) expressao);
		} else if (expressao instanceof NoInteiro) {
			return geraNoInteiro((NoInteiro) expressao);
		} else if (expressao instanceof NoTexto) {
			return geraNoTexto((NoTexto) expressao);
		} else if (expressao instanceof NoDecimal) {
			return geraNoDecimal((NoDecimal) expressao);
		} else if (expressao instanceof NoNulo) {
			return geraNoNulo((NoNulo) expressao);
		} else if (expressao instanceof NoArray) {
			return geraNoArray((NoArray) expressao);
		} else if (expressao instanceof NoVariavel) {
			return geraNoVariavel((NoVariavel) expressao);
		} else {
			return new Tipo(tipoNulo, 0);
		}
	}

	private Tipo geraNoAlocacao(NoAlocacao alocacao) {
		if (alocacao == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Alocação de vetor");

		// Busca o tipo na tabela de símbolo
		SimboloEntrada tipo = tabelaAtual.buscaTipo(alocacao.getTipo().image);
		int tamanho = 0;
		NoLista expr = null;

		// Gera o código para cada expressão da alocação
		for (expr = alocacao.getTamanho(); expr != null; expr = expr
				.getProximo()) {
			geraNoExpressao((NoExpressao) expr.getNo());
			tamanho++;
		}

		// Vetor dimensão 1
		if (tamanho == 1) {
			if (tipo.equals(tipoInteiro)) {
				code("newarray I", 1);
			} else {
				code("anewarray " + tipo.descJava(), 1);
			}
		} else {
			// Vetor multidimensional
			code("multianewarray " + Code.descJava(tamanho) + tipo.descJava()
					+ " " + tamanho, 1);
		}

		code();

		return new Tipo(tipo, tamanho);
	}

	private Tipo geraNoRelacional(NoRelacional relacional) {
		if (relacional == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Relação");

		String label = novoLabel();

		int operacao = relacional.getToken().kind;
		Tipo esquerda = geraNoExpressao(relacional.getEsquerda());
		geraNoExpressao(relacional.getDireita());

		if (esquerda.getEntrada().equals(tipoInteiro)
				&& esquerda.getTamanho() == 0) {
			code(operacaoBinaria(operacao) + " relexpr " + label, -2);
		} else {
			if (operacao == BLangMotorConstants.IGUAL) {
				code("if_acompeq relexpr " + label, -2);
			} else {
				code("if_acompne relexpr " + label, -2);
			}
		}

		code("bipush 0", 1);
		code("goto pxeler " + label);
		code("relexpr " + label);
		code("bipush 1");
		code("pxeler " + label + ":");
		code();

		return new Tipo(tipoInteiro, 0);
	}

	private Tipo geraNoAdicao(NoAdicao adicao) {
		if (adicao == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Adição");

		int operacao = adicao.getToken().kind;

		int inteiro = 0;
		int decimal = 0;
		int texto = 0;

		Tipo esquerda = geraNoExpressao(adicao.getEsquerda());
		Tipo direita = geraNoExpressao(adicao.getDireita());

		if (esquerda.getEntrada().equals(tipoInteiro)) {
			inteiro++;
		} else if (esquerda.getEntrada().equals(tipoDecimal)) {
			decimal++;
		} else if (esquerda.getEntrada().equals(tipoTexto)) {
			texto++;
		}

		if (direita.getEntrada().equals(tipoInteiro)) {
			inteiro++;
		} else if (direita.getEntrada().equals(tipoDecimal)) {
			decimal++;
		} else if (direita.getEntrada().equals(tipoTexto)) {
			texto++;
		}

		// Dois inteiros
		if (inteiro == 2) {
			code(operacaoBinaria(operacao), -1);
			return new Tipo(tipoInteiro, 0);
		}

		if (decimal == 2) {
			code(operacaoBinaria(operacao), -1);
			return new Tipo(tipoDecimal, 0);
		}

		// Dois textos
		if (texto == 2) {
			code("invokevirtual java/lang/String/concat(Ljava/lang/String;)Ljava/lang/String;",
					-1);
			return new Tipo(tipoTexto, 0);
		}

		if (esquerda.getEntrada().equals(tipoInteiro)) {
			code("swap");
			code("invokestatic java/lang/Integer/toString(I)Ljava/lang/String;");
			code("swap");
		} else if (esquerda.getEntrada().equals(tipoDecimal)) {
			code("swap");
			code("invokestatic java/lang/Double/toString()Ljava/lang/String;");
			code("swap");
		}

		code("invokevirtual java/lang/String/concat(Ljava/lang/String;)Ljava/lang/String;",
				-1);
		code();

		return new Tipo(tipoTexto, 0);
	}

	private Tipo geraNoMultiplicacao(NoMultiplicacao multiplicacao) {
		if (multiplicacao == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Multiplicacao");

		int operacao = multiplicacao.getToken().kind;
		Tipo esquerda = geraNoExpressao(multiplicacao.getEsquerda());
		Tipo direita = geraNoExpressao(multiplicacao.getDireita());

		code(operacaoBinaria(operacao), -1);
		code();

		if (esquerda.getEntrada().equals(tipoInteiro)
				&& direita.getEntrada().equals(tipoInteiro)) {
			return new Tipo(tipoInteiro, 0);
		} else if (esquerda.getEntrada().equals(tipoDecimal)
				&& direita.getEntrada().equals(tipoDecimal)) {
			return new Tipo(tipoDecimal, 0);
		} else if (esquerda.getEntrada().equals(tipoInteiro)
				&& direita.getEntrada().equals(tipoDecimal)) {
			return new Tipo(tipoDecimal, 0);
		} else if (esquerda.getEntrada().equals(tipoDecimal)
				&& direita.getEntrada().equals(tipoInteiro)) {
			return new Tipo(tipoDecimal, 0);
		} else {
			return new Tipo(tipoNulo, 0);
		}
	}

	private Tipo geraNoUnario(NoUnario unario) {
		if (unario == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Unario");

		int operacao = unario.getToken().kind;
		Tipo fator = geraNoExpressao(unario.getFator());

		if (operacao == BLangMotorConstants.MENOS) {
			if (fator.getEntrada().equals(tipoInteiro)) {
				code("ineg");
			} else {
				// não sei
			}
		}
		code();

		if (fator.getEntrada().equals(tipoInteiro)) {
			return new Tipo(tipoInteiro, 0);
		} else {
			return new Tipo(tipoDecimal, 0);
		}
	}

	private Tipo geraNoInteiro(NoInteiro inteiro) {
		if (inteiro == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Constante inteira");
		code("ldc " + inteiro.getToken().image, 1);
		code();

		return new Tipo(tipoInteiro, 0);
	}

	private Tipo geraNoDecimal(NoDecimal decimal) {
		if (decimal == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Constante decimal");
		code("ldc " + decimal.getToken().image, 1);
		code();

		return new Tipo(tipoDecimal, 0);
	}

	private Tipo geraNoTexto(NoTexto texto) {
		if (texto == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Constante texto");
		code("ldc " + texto.getToken().image, 1);
		code();

		return new Tipo(tipoTexto, 0);
	}

	private Tipo geraNoNulo(NoNulo nulo) {
		if (nulo == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Constate nula");
		code("aconst_null", 1);
		code();

		return new Tipo(tipoNulo, 0);
	}

	private Tipo geraNoArray(NoArray array) {
		if (array == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Array");

		NoLista expr = null;
		boolean a = armazena;
		SimboloVariavel var = tabelaAtual.buscaVariavel(array.getToken().image);

		armazena = false;
		for (expr = array.getExpressoes(); expr != null; expr = expr
				.getProximo()) {
			geraNoExpressao((NoExpressao) expr.getNo());
		}

		if (a) {
			code("swap");
			if (var.getTipo().equals(tipoInteiro) && var.getTamanho() == 1) {
				code("iastore", -3);
			} else {
				code("aastore", -3);
			}
		} else {
			if (var.getTipo().equals(tipoInteiro) && var.getTamanho() == 1) {
				code("iaload", -1);
			} else {
				code("aaload", -1);
			}
		}
		code();

		armazena = a;

		return new Tipo(var.getTipo(), var.getTamanho());
	}

	private Tipo geraNoVariavel(NoVariavel variavel) {
		if (variavel == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Variavel");

		int pilha = (armazena ? -1 : 1);
		String ope = (armazena ? "store" : "load");
		SimboloVariavel var = tabelaAtual
				.buscaVariavel(variavel.getToken().image);
		if (var.getTipo().equals(tipoInteiro) && var.getTamanho() == 0) {
			code("i" + ope + " " + var.getLocal(), pilha);
		} else {
			code("a" + ope + " " + var.getLocal(), pilha);
		}

		code();
		return new Tipo(var.getTipo(), var.getTamanho());
	}

}
