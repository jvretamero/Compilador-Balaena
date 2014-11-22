package lang.balaena.codigo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
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

/**
 * Classe respons�vel por gerar o c�digo intermedi�rio e tamb�m por executar o
 * Jasmin (Java Assembler Interface)
 */
public class GeradorCodigo {

	// Classe padr�o e classe do runtime
	public static final String CLASSE = "ProgBalaena";
	private static final String RUNTIME = "BalaenaRuntime";

	private AnalisadorSemantico semantico; // Analisador sem�ntico
	private TabelaSimbolo tabelaAtual; // Tabela de s�mbolos atual durante a
										// gera��o
	private SimboloMetodo metodoAtual; // M�todo atual durante a gera��o
	private boolean intermediario; // Controle se exibir� o c�digo intermedi�rio
									// para depura��o
	private NoLista raiz; // N� raiz da �rvore sint�tica
	private File arquivo; // Arquivo do c�digo fonte
	private File arqInter; // Arquivo do c�digo intermedi�rio
	private File arqFinal; // Arquivo do c�digo final
	private PrintWriter pw; // Classe para escrever nos arquivos
	private int alturaPilha; // Controle de altura da pilha de execu��o
	private int tamanhoPilha; // Controle do tamanho m�ximo da pilha de execu��o
	private boolean armazena; // Controle se ir� armazenar ou n�o um valor
	private int totalLocal; // Total de vari�veis locais (para cada m�todo)
	private int labelAtual; // Contador de labels do c�digo intermedi�rio

	// Entradas da tabela de s�mbolos dos tipos primitivos
	private final SimboloSimples tipoTexto = new SimboloSimples("texto");
	private final SimboloSimples tipoInteiro = new SimboloSimples("inteiro");
	private final SimboloSimples tipoDecimal = new SimboloSimples("decimal");
	private final SimboloSimples tipoVazio = new SimboloSimples("vazio");
	private final SimboloSimples tipoNulo = new SimboloSimples("nulo");

	/**
	 * Constrututor padr�o do gerador de c�digo
	 * 
	 * @param semantico
	 *            Analisador sem�ntico
	 * @param raiz
	 *            N� raiz da �rvore sint�tica
	 * @param arquivo
	 *            Arquivo do c�digo fonte
	 * @param intermediario
	 *            Exibe o c�digo intermedi�rio para depura��o?
	 */
	public GeradorCodigo(AnalisadorSemantico semantico, NoLista raiz,
			String arquivo, boolean intermediario) {
		this.semantico = semantico;
		this.raiz = raiz;
		this.arquivo = new File(arquivo);
		this.intermediario = intermediario;
	}

	/**
	 * M�todo que inicia a gera��o de c�digo
	 */
	public void gerar() {
		try {
			// Cria o arquivo tempor�rio do Jasmin
			arqInter = File.createTempFile("bln_", "_inter.jas");

			// Cria o arquivo tempor�rio final (ser� o .class)
			arqFinal = File.createTempFile("bln_", "_final.class");

			// Executa a gera��o do c�digo Jasmin
			codigoIntermediario();

			// Executa o Jasmin para gerar o c�digo final aceito pela JVM
			codigoFinal();

			// Copia o .class do temp para o local do fonte
			copiaClasse();

			// Visualiza o arquivo intermedi�rio no console
			if (intermediario) {
				System.out.println("\nC�digo intermedi�rio gerado:\n");
				visualisaIntermediario();
			}

		} catch (IOException e) {
			System.out.println("Ocorreu um erro: " + e.getMessage());
		}
	}

	/**
	 * M�todo que exibe o arquivo intermedi�rio no console
	 */
	private void visualisaIntermediario() {
		try {
			// Cria um reader para o arquivo intermedi�rio
			FileReader fr = new FileReader(arqInter);
			BufferedReader buf = new BufferedReader(fr);

			// L� a primeira linha
			String linha = buf.readLine();

			// L� o arquivo at� o final
			while (linha != null) {
				System.out.println(linha);
				linha = buf.readLine();
			}

			// Fecha os readers
			fr.close();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio
	 * 
	 * @throws IOException
	 *             Falha na leitura/grava��o do arquivo
	 */
	private void codigoIntermediario() throws IOException {
		// Cria o PrintWriter para escrever no arquivo
		FileOutputStream out = new FileOutputStream(arqInter);
		pw = new PrintWriter(out);

		try {
			// Gera o c�digo da classe para execu��o na JVM
			classePadrao();

			// Zera o contador de label e define a tabela de s�mbolo atual
			labelAtual = 0;
			tabelaAtual = semantico.getTabela();

			// Gera o c�digo intermedi�rio
			// Come�ando pela lista de m�todos
			geraNoListaMetodoDecl(raiz);

			// Gera o m�todo "main" na classe
			metodoPrincipal();
		} finally {
			pw.close();
			out.close();
		}
	}

	/**
	 * M�todo para copiar arquivos
	 * 
	 * @param origem
	 *            Arquivo de origem
	 * @param destino
	 *            Arquivo de destino
	 * @throws IOException
	 *             Caso n�o seja poss�vel ler/escrever em algum arquivo
	 */
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

	/**
	 * M�todo para gerar o c�digo final atrav�s do Jasmin
	 * 
	 * @throws IOException
	 *             Caso n�o seja poss�vel ler/escrever no arquivo final
	 */
	private void codigoFinal() throws IOException {
		FileInputStream in = null;
		FileOutputStream out = null;

		try {
			in = new FileInputStream(arqInter);
			out = new FileOutputStream(arqFinal);

			// Executa o Jasmin para gerar o c�digo
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

	/**
	 * M�todo para copiar o arquivo ".class" da pasta tempor�ria para a pasta
	 * "prog"
	 */
	private void copiaClasse() {
		// Utiliza o mesmo nome do arquivo do c�digo fonte, apenas renomeando a
		// extens�o
		String nome = arquivo.getName();
		int pos = nome.lastIndexOf(".");
		nome = nome.substring(0, pos) + ".class";

		// Cria o novo arquivo
		File arqClasse = new File(getPath(), CLASSE + ".class");

		try {
			// Copia da pasta tempor�ria para a pasta "prog"
			copia(arqFinal, arqClasse);
		} catch (IOException e) {
			System.err
					.println("Ocorreu um problema ao gerar o arquivo .class do programa");
			System.err.println("Motivo: " + e.getMessage());
		}
	}

	/**
	 * M�todo para obter a pasta "prog"
	 * 
	 * @return Arquivo da pasta "prog"
	 */
	public static File getPath() {
		File path = new File(System.getProperty("user.dir"), "prog");
		if (!path.exists()) {
			// Se n�o existir, cria a pasta
			path.mkdirs();
		}
		return path;
	}

	/**
	 * M�todo para escrever c�digos no arquivo intermedi�rio
	 * 
	 * @param code
	 *            C�digo a ser escrito
	 * @param pilha
	 *            Unidades de mudan�a (acima/abaixo) na pilha
	 */
	private void code(String code, int pilha) {
		// Incrementa/decrementa a altura da pilha
		alturaPilha += pilha;

		// Atualiza o tamanho m�ximo da pilha do m�todo
		if (metodoAtual != null && pilha > 0) {
			metodoAtual.addPilha(pilha);
		}

		// Atualiza o tamanho m�ximo geral da pilha
		if (alturaPilha > tamanhoPilha) {
			tamanhoPilha = alturaPilha;
		}

		// Escreve no arquivo
		if (pw != null) {
			pw.println(code);
		}
	}

	/**
	 * M�todo para escrever c�digos no arquivo intermedi�rio sem altera��o na
	 * pilha
	 * 
	 * @param code
	 *            C�digo a ser escrito
	 */
	private void code(String code) {
		code(code, 0);
	}

	/**
	 * M�todo para pular uma linha no c�digo intermedi�rio
	 */
	private void code() {
		code("");
	}

	/**
	 * M�todo para criar um novo label
	 * 
	 * @return Novo label enumerado
	 */
	private String novoLabel() {
		return "BLN" + Integer.toString(labelAtual++);
	}

	/**
	 * M�todo para retornar a opera��o bin�ria em Jasmin
	 * 
	 * @param op
	 *            Token da opera��o bin�ria
	 * @return C�digo Jasmin da opera��o bin�ria
	 */
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

	/**
	 * M�todo para gerar o c�digo da classe padr�o do programa
	 */
	private void classePadrao() {
		code(";---------------------------------------------");
		code("; Codigo gerado pelo Compilador Balaena");
		code("; Versao 0.1 - 2014");
		code(";---------------------------------------------");
		code(".source " + arquivo.getName());
		code(".class public " + CLASSE);
		code(".super java/lang/Object");
		code();
		code("; Construtor padr�o");
		code(".method public <init>()V");
		code("aload_0");
		code("invokespecial java/lang/Object/<init>()V");
		code("return");
		code(".end method");
	}

	/**
	 * M�todo para gerar o c�digo do m�todo "main" da classe padr�o do programa
	 */
	private void metodoPrincipal() {
		code();
		code("; Metodo principal");
		code(".method static public main([Ljava/lang/String;)V");
		code(".limit locals 1");
		code(".limit stack 1");
		code("invokestatic " + RUNTIME + "/inicia()I"); // Inicia o runtime
		code("ifne end");
		code("invokestatic " + CLASSE + "/principal()V"); // Executa o m�todo
															// principal
		code("end:");
		code("invokestatic " + RUNTIME + "/finaliza()V"); // Finaliza o runtime
		code("return");
		code(".end method");
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio da lista de m�todos
	 * 
	 * @param metodos
	 *            N� da �rvore sint�tica correspondente � lista de m�todo
	 */
	private void geraNoListaMetodoDecl(NoLista metodos) {
		// Verifica lista vazia
		if (metodos == null) {
			return;
		}

		// Gera o c�digo do m�todo
		geraNoMetodoDecl((NoMetodoDecl) metodos.getNo());

		// Gera o c�digo do pr�ximo m�todo
		geraNoListaMetodoDecl(metodos.getProximo());
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio de m�todo
	 * 
	 * @param metodo
	 *            N� da �rvore sint�tica correspondente ao m�todo
	 */
	private void geraNoMetodoDecl(NoMetodoDecl metodo) {
		// Verifica m�todo inexistente
		if (metodo == null) {
			return;
		}

		// Armazena a tabela de s�mbolo atual
		TabelaSimbolo temporaria = tabelaAtual;
		// Entrada dos par�metros do m�todo
		SimboloParametro param = null;
		// Entrada do m�todo
		SimboloMetodo m = null;
		// Entrada do tipo do m�todo
		SimboloEntrada tipo = null;
		// N� da lista de par�metros
		NoLista p = null;
		// N� da declara��o de vari�vel (lista de par�metro)
		NoVariavelDecl varDecl = null;
		// N� da vari�vel (lista de par�metro)
		NoVariavel var = null;

		// Percorre todos os par�metros do m�todo
		for (p = metodo.getCorpo().getParametros(); p != null; p = p
				.getProximo()) {
			// Obt�m as vari�veis
			varDecl = (NoVariavelDecl) p.getNo();
			var = (NoVariavel) varDecl.getVariaveis().getNo();

			// Obt�m os tipos da tabela de s�mbolo
			tipo = tabelaAtual.buscaTipo(varDecl.getToken().image);

			// Cria a lista de par�metros
			if (param == null) {
				param = new SimboloParametro(tipo, var.getTamanho());
			} else {
				param.setProximo(new SimboloParametro(tipo, var.getTamanho()));
			}
		}

		// Busca o m�todo registrado na tabela de s�mbolo
		m = tabelaAtual.buscaMetodo(metodo.getNome().image, param);

		// Atualiza o m�todo atual
		metodoAtual = m;

		// Troca a tabela de s�mbolo atual pela tabela de s�mbolo do escopo do
		// m�todo
		tabelaAtual = m.getTabela();

		// Gera o c�digo do m�todo
		code();
		code("; Declara��o do m�todo " + m.getNome());
		code(".method private static " + m.getNome() + "("
				+ (param == null ? "" : param.descJava()) + ")"
				+ m.getTipo().descJava());
		// Total de vari�veis locais para aloca��o
		code(".limit locals " + (m.getTotalLocal() + 1));

		// Inicia o escopo do m�todo na tabela de s�mbolos
		tabelaAtual.iniciaEscopo();

		// Zera as vari�veis de controle de pilha
		tamanhoPilha = 0;
		alturaPilha = 0;
		totalLocal = 0;

		// Gera o c�digo do corpo do m�todo
		geraNoCorpoMetodo(metodo.getCorpo());

		// Finaliza o escopo do m�todo na tabela de s�mbolos
		tabelaAtual.terminaEscopo();

		// Gera o retorno do m�todo
		if (m.getTipo().equals(tipoInteiro) && m.getTamanho() == 0) {
			code("bipush 0", 1);
			code("ireturn", -1);
		} else if (!m.getTipo().equals(tipoVazio)) {
			code("aconst_null", 1);
			code("areturn", -1);
		}

		code("return");
		// Define o tamanho m�ximo da pilha no m�todo
		code(".limit stack " + (m.getTamanhoPilha() + alturaPilha));
		code(".end method");

		// Retoma a tabela de s�mbolo armazenada
		tabelaAtual = temporaria;
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio do corpo do m�todo
	 * 
	 * @param corpo
	 *            N� da �rvore sint�tica correspondente ao corpo do m�todo
	 */
	private void geraNoCorpoMetodo(NoCorpoMetodo corpo) {
		// Verifica corpo do m�todo vazio
		if (corpo == null) {
			return;
		}

		// Gera o c�digo dos par�metros
		geraNoListaVariavelDecl(corpo.getParametros());

		// Gera o c�digo do bloco do m�todo
		geraNoBloco(corpo.getBloco());
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio da lista de declara��o de
	 * vari�veis
	 * 
	 * @param variaveis
	 *            N� da �rvore sint�tica correspondente a lista de vari�veis
	 *            declaradas
	 */
	private void geraNoListaVariavelDecl(NoLista variaveis) {
		// Verifica lista de vari�veis vazia
		if (variaveis == null) {
			return;
		}

		// Gera o c�digo da vari�vel
		geraNoVariavelDecl((NoVariavelDecl) variaveis.getNo());

		// Gera o c�digo da pr�xima vari�vel
		geraNoListaVariavelDecl(variaveis.getProximo());
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio da declara��o de vari�vel
	 * 
	 * @param variavel
	 *            N� da �rvore sint�tica correspondente a declara��o de vari�vel
	 */
	private void geraNoVariavelDecl(NoVariavelDecl variavel) {
		// Verifica declara��o de vari�vel vazia
		if (variavel == null) {
			return;
		}

		// Tipo das vari�veis
		SimboloEntrada tipo = null;
		// N� da vari�vel
		NoVariavel var = null;
		// N� da lista de vari�veis
		NoLista vars = null;

		// Busca o tipo das vari�veis na tabela de s�mbolo
		tipo = tabelaAtual.buscaTipo(variavel.getToken().image);

		// Percorre todas as vari�veis a serem declaradas
		for (vars = variavel.getVariaveis(); vars != null; vars = vars
				.getProximo()) {
			var = (NoVariavel) vars.getNo();

			// Registra na tabela de s�mbolo do m�todo atual
			tabelaAtual.adiciona(new SimboloVariavel(tipo,
					var.getToken().image, var.getTamanho(), totalLocal++));
		}
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio do bloco
	 * 
	 * @param bloco
	 *            N� da �rvore sint�tica correspondente ao bloco
	 */
	private void geraNoBloco(NoBloco bloco) {
		// Verifica bloco vazio
		if (bloco == null) {
			return;
		}

		// Inicia o escopo do bloco
		tabelaAtual.iniciaEscopo();

		// Gera o c�digo do bloco
		geraNoListaDeclaracao(bloco.getDeclaracoes());

		// Finaliza o escopo do bloco
		tabelaAtual.terminaEscopo();
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio da lista de declara��es
	 * 
	 * @param declaracoes
	 *            N� da �rvore sint�tica correspondente � lista de declara��es
	 */
	private void geraNoListaDeclaracao(NoLista declaracoes) {
		// Verifica lista de declara��es vazia
		if (declaracoes == null) {
			return;
		}

		// Gera o c�digo intermedi�rio da declara��o
		geraNoDeclaracao((NoDeclaracao) declaracoes.getNo());

		// Gera o c�digo intermedi�rio da pr�xima declara��o
		geraNoListaDeclaracao(declaracoes.getProximo());
	}

	/**
	 * Me�todo para gerar o c�digo intermedi�rio de uma declara��o
	 * 
	 * @param declaracao
	 *            N� da �rvore sint�tica correspondente � declara��o
	 */
	private void geraNoDeclaracao(NoDeclaracao declaracao) {
		// Verifica declara��o vazia
		if (declaracao == null) {
			return;
		}

		// Gera o c�digo da declara��o correspondente
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
		} else if (declaracao instanceof NoChamadaDecl) {
			geraNoChamadaDecl((NoChamadaDecl) declaracao);
		}
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio de atribui��o
	 * 
	 * @param atribuicao
	 */
	public void geraNoAtribuicao(NoAtribuicao atribuicao) {
		// Verifica atribui��o vazia
		if (atribuicao == null) {
			return;
		}

		// Controle para somente alocar na pilha
		armazena = false;
		// Gera o c�digo da express�o � direita
		geraNoExpressao(atribuicao.getDireita());

		// Controle para armazenar na pilha
		armazena = true;
		// Gera o c�digo da express�o � esquerda
		geraNoExpressao(atribuicao.getEsquerda());
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio do comando imprimir
	 * 
	 * @param imprimir
	 *            N� da �rvore sint�tica correspondente do comando imprimir
	 */
	private void geraNoImprimir(NoImprimir imprimir) {
		// Verifica comando vazio
		if (imprimir == null) {
			return;
		}

		code();
		code("; Comando de escrita");
		// Coloca System.out na pilha de execu��o
		code("getstatic java/lang/System/out Ljava/io/PrintStream;", 1);

		// Controle para n�o armazenar
		armazena = false;

		// Gera o c�digo da express�o
		Tipo valor = geraNoExpressao(imprimir.getValor());

		// Executa a impress�o no console
		if (valor.getEntrada().equals(tipoInteiro)) {
			code("invokevirtual java/io/PrintStream/print(I)V", -2);
		} else if (valor.getEntrada().equals(tipoDecimal)) {
			code("invokevirtual java/io/PrintStream/print(F)V", -2);
		} else {
			code("invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V",
					-2);
		}
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio do comando ler
	 * 
	 * @param ler
	 *            N� da �rvore sint�tica correspondente ao comando ler
	 */
	private void geraNoLer(NoLer ler) {
		// Verifica comando vazio
		if (ler == null) {
			return;
		}

		Tipo valor = null;

		try {
			// Obt�m o tipo da express�o (no caso vari�vel) usando a tabela de
			// s�mbolo atual
			valor = semantico.analisaTipoNoExpressao(ler.getVariavel(),
					tabelaAtual);
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			return;
		}

		code();
		code("; Comando de leitura");

		// Gera o c�digo do comando correspondente ao tipo de dado
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
			code("invokestatic " + RUNTIME + "/" + comando, 1);
		}

		// Opera��o de armazenagem
		armazena = true;
		// Gera o c�digo da express�o a ser lida
		geraNoExpressao(ler.getVariavel());
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio do comando retornar
	 * 
	 * @param retornar
	 *            N� da �rvore sint�tica correspondente ao comando retornar
	 */
	private void geraNoRetornar(NoRetornar retornar) {
		// Verifica comando vazio
		if (retornar == null) {
			return;
		}

		code();
		code("; Comando de retorno");

		// Controle para n�o armazenar
		armazena = false;
		// Gera o c�digo da express�o (se existir)
		Tipo valor = geraNoExpressao(retornar.getValor());

		// Verifica o tipo da express�o para gerar o c�digo coerente
		if (valor.getEntrada().equals(tipoInteiro) && valor.getTamanho() == 0) {
			code("ireturn", -1);
		} else {
			code("areturn", -1);
		}
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio do controle SE
	 * 
	 * @param se
	 *            N� da �rvrore sint�tica correspondente ao controle SE
	 */
	private void geraNoSe(NoSe se) {
		// Verifica controle vazio
		if (se == null) {
			return;
		}

		// Cria um novo label
		String label = novoLabel();

		code();
		code("; Controle SE " + label);

		armazena = false;
		// C�digo da condi��o, deixando o resultado na pilha
		geraNoExpressao(se.getCondicao());

		// Se o resultado for falso, desvia para o label
		code("ifeq else " + label, -1);

		// Cc�digo para o bloco verdadeiro
		geraNoBloco(se.getVerdadeiro());

		// Verifica se existe "SEN�O"
		if (se.getFalso() != null) {
			// Desvio para o final do SE caso verdadeiro
			code("goto fi " + label);

			// Label para o come�o do bloco falso
			code("else " + label + ":");

			// C�digo do bloco falso
			geraNoBloco(se.getFalso());

			// Final do comando
			code("fi " + label + ":");
		} else {
			// Fim do comando
			code("else " + label + ":");
		}
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio da chamada de m�todo
	 * 
	 * @param chamada
	 *            N� da �rvore sint�tica correspondente � chamada de m�todo
	 * @return Tipo do m�todo chamado
	 */
	private Tipo geraNoChamadaDecl(NoChamadaDecl chamada) {
		// Gera o c�digo da chamada do m�todo
		return geraNoChamada(new NoChamada(chamada.getToken(),
				chamada.getArgumentos()));
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio da chamada de m�todo
	 * 
	 * @param chamada
	 *            N� da �rvore sint�tica correspondente � chamada de m�todo
	 * @return Tipo do m�todo chamado
	 */
	private Tipo geraNoChamada(NoChamada chamada) {
		// Verifica chamada vazia
		if (chamada == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Chamada do metodo " + chamada.getToken().image.toUpperCase());

		// Entrada dos par�metros
		SimboloParametro params = null;
		// Lista de par�metros
		NoLista p = null;

		// Cria os par�metros do m�todo e gera o c�digo dos argumentos
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

		// Busca o m�todo na tabela de s�mbolos
		SimboloMetodo metodo = tabelaAtual.buscaMetodo(
				chamada.getToken().image, params);

		// Prepara o c�digo para os par�metros
		String args = (params == null ? "" : params.descJava());

		// Calcula quanto a pilha ir� diminuir
		int pilha = (params == null ? 0 : params.getElementos());

		// Comando para executar o m�todo
		code("invokestatic " + CLASSE + "/" + metodo.getNome() + "(" + args
				+ ")" + metodo.descJava(), -pilha);

		// Retorna o tipo do m�todo
		return new Tipo(metodo.getTipo(), metodo.getTamanho());
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio de express�es
	 * 
	 * @param expressao
	 *            N� da �rvore sint�tica correspondente a uma express�o
	 * @return Tipo da express�o
	 */
	private Tipo geraNoExpressao(NoExpressao expressao) {
		// Verifica express�o vazia
		if (expressao == null) {
			return new Tipo(tipoNulo, 0);
		}

		// Gera o c�digo conforme o tipo da express�o
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

	/**
	 * M�todo para gerar o c�digo intermedi�rio de uma aloca��o de vetor
	 * 
	 * @param alocacao
	 *            N� da �rvore sint�tica correspondente a uma aloca��o
	 * @return Tipo da aloca��o
	 */
	private Tipo geraNoAlocacao(NoAlocacao alocacao) {
		// Verifica aloca��o vazia
		if (alocacao == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Aloca��o de vetor");

		// Busca o tipo na tabela de s�mbolo
		SimboloEntrada tipo = tabelaAtual.buscaTipo(alocacao.getTipo().image);
		int tamanho = 0;
		NoLista expr = null;

		// Gera o c�digo para cada express�o da aloca��o
		for (expr = alocacao.getTamanho(); expr != null; expr = expr
				.getProximo()) {
			geraNoExpressao((NoExpressao) expr.getNo());
			tamanho++;
		}

		// Vetor dimens�o 1
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

		return new Tipo(tipo, tamanho);
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio de uma rela��o
	 * 
	 * @param relacional
	 *            N� da �rvore sint�tica correspondene a uma rela��o
	 * @return Tipo da rela��o
	 */
	private Tipo geraNoRelacional(NoRelacional relacional) {
		if (relacional == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Rela��o");

		// Cria um novo label
		String label = novoLabel();

		// Gera o c�digo da express�o � esquerda
		int operacao = relacional.getToken().kind;
		Tipo esquerda = geraNoExpressao(relacional.getEsquerda());

		// Gera o c�digo da express�o � direita
		geraNoExpressao(relacional.getDireita());

		// Valida os tipos
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

		// Controla o fluxo
		code("bipush 0", 1);
		code("goto pxeler " + label);
		code("relexpr " + label);
		code("bipush 1");
		code("pxeler " + label + ":");
		code();

		return new Tipo(tipoInteiro, 0);
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio de uma adi��o
	 * 
	 * @param adicao
	 *            N� da �rvore sint�tica correspondente a uma adi��o
	 * @return Tipo da adi��o
	 */
	private Tipo geraNoAdicao(NoAdicao adicao) {
		// Verifica adi��o vazia
		if (adicao == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Adi��o");

		// Obt�m a opera��o
		int operacao = adicao.getToken().kind;

		// Contadores de tipos
		int inteiro = 0;
		int decimal = 0;
		int texto = 0;

		// Gera o c�digo da express�o � esquerda
		Tipo esquerda = geraNoExpressao(adicao.getEsquerda());

		// Gera o c�digo da express�o � direita
		Tipo direita = geraNoExpressao(adicao.getDireita());

		// Conta o tipo � esquerda
		if (esquerda.getEntrada().equals(tipoInteiro)) {
			inteiro++;
		} else if (esquerda.getEntrada().equals(tipoDecimal)) {
			decimal++;
		} else if (esquerda.getEntrada().equals(tipoTexto)) {
			texto++;
		}

		// Conta o tipo � direita
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

		// Dois decimais
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

		// Converte os tipos
		if (esquerda.getEntrada().equals(tipoInteiro)) {
			code("swap");
			code("invokestatic java/lang/Integer/toString(I)Ljava/lang/String;");
			code("swap");
		} else if (esquerda.getEntrada().equals(tipoDecimal)) {
			code("swap");
			code("invokestatic java/lang/Float/toString(F)Ljava/lang/String;");
			code("swap");
		}
		
		if (direita.getEntrada().equals(tipoInteiro)) {
			code("invokestatic java/lang/Integer/toString(I)Ljava/lang/String;");
		} else if (direita.getEntrada().equals(tipoDecimal)) {
			code("invokestatic java/lang/Float/toString(F)Ljava/lang/String;");
		}
		
		// Concatena os tipos
		code("invokevirtual java/lang/String/concat(Ljava/lang/String;)Ljava/lang/String;",
				-1);

		return new Tipo(tipoTexto, 0);
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio de uma multiplica��o
	 * 
	 * @param multiplicacao
	 *            N� da �rvore sint�tica correspondente a uma multiplica��o
	 * @return Tipo da multiplica��o
	 */
	private Tipo geraNoMultiplicacao(NoMultiplicacao multiplicacao) {
		// Verifica multiplica��o vazia
		if (multiplicacao == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Multiplicacao");

		// Obt�m a opera��o
		int operacao = multiplicacao.getToken().kind;

		// Gera o c�digo da express�o � esquerda
		Tipo esquerda = geraNoExpressao(multiplicacao.getEsquerda());

		// Gera o c�digo da express�o � direita
		Tipo direita = geraNoExpressao(multiplicacao.getDireita());

		// Gera o c�digo da opera��o
		code(operacaoBinaria(operacao), -1);

		// Valida o tipo de retorno
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

	/**
	 * M�todo para gerar o c�digo intermedi�rio de um n�mero un�rio
	 * 
	 * @param unario
	 *            N� da �rvore sint�tica correspondente a um n�mero un�rio
	 * @return Tipo do n�mero
	 */
	private Tipo geraNoUnario(NoUnario unario) {
		// Verifica n�mero vazio
		if (unario == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Unario");

		// Opera��o
		int operacao = unario.getToken().kind;

		// Gera o c�digo do fator
		Tipo fator = geraNoExpressao(unario.getFator());

		// Verifica nega��o
		if (operacao == BLangMotorConstants.MENOS) {
			if (fator.getEntrada().equals(tipoInteiro)) {
				code("ineg");
			} else {
				code("aneg");
			}
		}

		// Valida o tipo de retorno
		if (fator.getEntrada().equals(tipoInteiro)) {
			return new Tipo(tipoInteiro, 0);
		} else {
			return new Tipo(tipoDecimal, 0);
		}
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio de um n�mero inteiro
	 * 
	 * @param inteiro
	 *            N� da �rvore sint�tica correspondente a um n�mero inteiro
	 * @return Tipo inteiro
	 */
	private Tipo geraNoInteiro(NoInteiro inteiro) {
		// Verifica n�mero vazio
		if (inteiro == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Constante inteira");
		code("ldc " + inteiro.getToken().image, 1);

		return new Tipo(tipoInteiro, 0);
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio de um n�mero decimal
	 * 
	 * @param decimal
	 *            N� da �rvore sint�tica correspondente a um n�mero decimal
	 * @return Tipo decimal
	 */
	private Tipo geraNoDecimal(NoDecimal decimal) {
		// Verifica n�mero vazio
		if (decimal == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Constante decimal");
		code("ldc " + decimal.getToken().image, 1);

		return new Tipo(tipoDecimal, 0);
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio de um texto
	 * 
	 * @param texto
	 *            N� da �rvore sint�tica correspondente a um texto
	 * @return Tipo texto
	 */
	private Tipo geraNoTexto(NoTexto texto) {
		// Verifica texto vazio
		if (texto == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Constante texto");
		code("ldc " + texto.getToken().image, 1);

		return new Tipo(tipoTexto, 0);
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio do tipo nulo
	 * 
	 * @param nulo
	 *            N� da �rvore sint�tica correspondente ao tipo nulo
	 * @return Tipo nulo
	 */
	private Tipo geraNoNulo(NoNulo nulo) {
		// Verifica tipo vazio
		if (nulo == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Constate nula");
		code("aconst_null", 1);

		return new Tipo(tipoNulo, 0);
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio de um array
	 * 
	 * @param array
	 *            N� da �rvore sint�tica correspondente � um arrau
	 * @return Tipo do array
	 */
	private Tipo geraNoArray(NoArray array) {
		// Verifica array vazio
		if (array == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Array");

		NoLista expr = null;
		boolean a = armazena;
		// Busca a vari�vel do array
		SimboloVariavel var = tabelaAtual.buscaVariavel(array.getToken().image);

		armazena = false;
		// Gera o c�digo de todos os �ndices acessados do array
		for (expr = array.getExpressoes(); expr != null; expr = expr
				.getProximo()) {
			geraNoExpressao((NoExpressao) expr.getNo());
		}

		// Verifica se � operac�o para armazenar ou n�o
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

		armazena = a;

		return new Tipo(var.getTipo(), var.getTamanho());
	}

	/**
	 * M�todo para gerar o c�digo intermedi�rio de uma vari�vel
	 * 
	 * @param variavel
	 *            N� da �rvore sint�tica correspondente a uma vari�vel
	 * @return Tipo da vari�vel
	 */
	private Tipo geraNoVariavel(NoVariavel variavel) {
		// Verifica vari�vel vazia
		if (variavel == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Variavel");

		// Calcula a mudan�a na pilha
		int pilha = (armazena ? -1 : 1);

		// Defina a opera��o de armazenamento ou carregamento
		String ope = (armazena ? "store" : "load");

		// Busca a vari�vel na tabela de s�mbolo
		SimboloVariavel var = tabelaAtual
				.buscaVariavel(variavel.getToken().image);
		
		// Gera o c�digo correspondente
		if (var.getTipo().equals(tipoInteiro) && var.getTamanho() == 0) {
			code("i" + ope + " " + var.getLocal(), pilha);
		} else if (var.getTipo().equals(tipoDecimal) && var.getTamanho() == 0) {
			code("f" + ope + " " + var.getLocal(), pilha);
		} else {
			code("a" + ope + " " + var.getLocal(), pilha);
		}

		return new Tipo(var.getTipo(), var.getTamanho());
	}

}