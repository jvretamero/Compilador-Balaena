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
 * Classe responsável por gerar o código intermediário e também por executar o
 * Jasmin (Java Assembler Interface)
 */
public class GeradorCodigo {

	// Classe padrão e classe do runtime
	public static final String CLASSE = "ProgBalaena";
	private static final String RUNTIME = "BalaenaRuntime";

	private AnalisadorSemantico semantico; // Analisador semântico
	private TabelaSimbolo tabelaAtual; // Tabela de símbolos atual durante a
										// geração
	private SimboloMetodo metodoAtual; // Método atual durante a geração
	private boolean intermediario; // Controle se exibirá o código intermediário
									// para depuração
	private NoLista raiz; // Nó raiz da árvore sintática
	private File arquivo; // Arquivo do código fonte
	private File arqInter; // Arquivo do código intermediário
	private File arqFinal; // Arquivo do código final
	private PrintWriter pw; // Classe para escrever nos arquivos
	private int alturaPilha; // Controle de altura da pilha de execução
	private int tamanhoPilha; // Controle do tamanho máximo da pilha de execução
	private boolean armazena; // Controle se irá armazenar ou não um valor
	private int totalLocal; // Total de variáveis locais (para cada método)
	private int labelAtual; // Contador de labels do código intermediário

	// Entradas da tabela de símbolos dos tipos primitivos
	private final SimboloSimples tipoTexto = new SimboloSimples("texto");
	private final SimboloSimples tipoInteiro = new SimboloSimples("inteiro");
	private final SimboloSimples tipoDecimal = new SimboloSimples("decimal");
	private final SimboloSimples tipoVazio = new SimboloSimples("vazio");
	private final SimboloSimples tipoNulo = new SimboloSimples("nulo");

	/**
	 * Constrututor padrão do gerador de código
	 * 
	 * @param semantico
	 *            Analisador semântico
	 * @param raiz
	 *            Nó raiz da árvore sintática
	 * @param arquivo
	 *            Arquivo do código fonte
	 * @param intermediario
	 *            Exibe o código intermediário para depuração?
	 */
	public GeradorCodigo(AnalisadorSemantico semantico, NoLista raiz,
			String arquivo, boolean intermediario) {
		this.semantico = semantico;
		this.raiz = raiz;
		this.arquivo = new File(arquivo);
		this.intermediario = intermediario;
	}

	/**
	 * Método que inicia a geração de código
	 */
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

			// Copia o .class do temp para o local do fonte
			copiaClasse();

			// Visualiza o arquivo intermediário no console
			if (intermediario) {
				System.out.println("\nCódigo intermediário gerado:\n");
				visualisaIntermediario();
			}

		} catch (IOException e) {
			System.out.println("Ocorreu um erro: " + e.getMessage());
		}
	}

	/**
	 * Método que exibe o arquivo intermediário no console
	 */
	private void visualisaIntermediario() {
		try {
			// Cria um reader para o arquivo intermediário
			FileReader fr = new FileReader(arqInter);
			BufferedReader buf = new BufferedReader(fr);

			// Lê a primeira linha
			String linha = buf.readLine();

			// Lê o arquivo até o final
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
	 * Método para gerar o código intermediário
	 * 
	 * @throws IOException
	 *             Falha na leitura/gravação do arquivo
	 */
	private void codigoIntermediario() throws IOException {
		// Cria o PrintWriter para escrever no arquivo
		FileOutputStream out = new FileOutputStream(arqInter);
		pw = new PrintWriter(out);

		try {
			// Gera o código da classe para execução na JVM
			classePadrao();

			// Zera o contador de label e define a tabela de símbolo atual
			labelAtual = 0;
			tabelaAtual = semantico.getTabela();

			// Gera o código intermediário
			// Começando pela lista de métodos
			geraNoListaMetodoDecl(raiz);

			// Gera o método "main" na classe
			metodoPrincipal();
		} finally {
			pw.close();
			out.close();
		}
	}

	/**
	 * Método para copiar arquivos
	 * 
	 * @param origem
	 *            Arquivo de origem
	 * @param destino
	 *            Arquivo de destino
	 * @throws IOException
	 *             Caso não seja possível ler/escrever em algum arquivo
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
	 * Método para gerar o código final através do Jasmin
	 * 
	 * @throws IOException
	 *             Caso não seja possível ler/escrever no arquivo final
	 */
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

	/**
	 * Método para copiar o arquivo ".class" da pasta temporária para a pasta
	 * "prog"
	 */
	private void copiaClasse() {
		// Utiliza o mesmo nome do arquivo do código fonte, apenas renomeando a
		// extensão
		String nome = arquivo.getName();
		int pos = nome.lastIndexOf(".");
		nome = nome.substring(0, pos) + ".class";

		// Cria o novo arquivo
		File arqClasse = new File(getPath(), CLASSE + ".class");

		try {
			// Copia da pasta temporária para a pasta "prog"
			copia(arqFinal, arqClasse);
		} catch (IOException e) {
			System.err
					.println("Ocorreu um problema ao gerar o arquivo .class do programa");
			System.err.println("Motivo: " + e.getMessage());
		}
	}

	/**
	 * Método para obter a pasta "prog"
	 * 
	 * @return Arquivo da pasta "prog"
	 */
	public static File getPath() {
		File path = new File(System.getProperty("user.dir"), "prog");
		if (!path.exists()) {
			// Se não existir, cria a pasta
			path.mkdirs();
		}
		return path;
	}

	/**
	 * Método para escrever códigos no arquivo intermediário
	 * 
	 * @param code
	 *            Código a ser escrito
	 * @param pilha
	 *            Unidades de mudança (acima/abaixo) na pilha
	 */
	private void code(String code, int pilha) {
		// Incrementa/decrementa a altura da pilha
		alturaPilha += pilha;

		// Atualiza o tamanho máximo da pilha do método
		if (metodoAtual != null && pilha > 0) {
			metodoAtual.addPilha(pilha);
		}

		// Atualiza o tamanho máximo geral da pilha
		if (alturaPilha > tamanhoPilha) {
			tamanhoPilha = alturaPilha;
		}

		// Escreve no arquivo
		if (pw != null) {
			pw.println(code);
		}
	}

	/**
	 * Método para escrever códigos no arquivo intermediário sem alteração na
	 * pilha
	 * 
	 * @param code
	 *            Código a ser escrito
	 */
	private void code(String code) {
		code(code, 0);
	}

	/**
	 * Método para pular uma linha no código intermediário
	 */
	private void code() {
		code("");
	}

	/**
	 * Método para criar um novo label
	 * 
	 * @return Novo label enumerado
	 */
	private String novoLabel() {
		return "BLN" + Integer.toString(labelAtual++);
	}

	/**
	 * Método para retornar a operação binária em Jasmin
	 * 
	 * @param op
	 *            Token da operação binária
	 * @return Código Jasmin da operação binária
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
	 * Método para gerar o código da classe padrão do programa
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
		code("; Construtor padrão");
		code(".method public <init>()V");
		code("aload_0");
		code("invokespecial java/lang/Object/<init>()V");
		code("return");
		code(".end method");
	}

	/**
	 * Método para gerar o código do método "main" da classe padrão do programa
	 */
	private void metodoPrincipal() {
		code();
		code("; Metodo principal");
		code(".method static public main([Ljava/lang/String;)V");
		code(".limit locals 1");
		code(".limit stack 1");
		code("invokestatic " + RUNTIME + "/inicia()I"); // Inicia o runtime
		code("ifne end");
		code("invokestatic " + CLASSE + "/principal()V"); // Executa o método
															// principal
		code("end:");
		code("invokestatic " + RUNTIME + "/finaliza()V"); // Finaliza o runtime
		code("return");
		code(".end method");
	}

	/**
	 * Método para gerar o código intermediário da lista de métodos
	 * 
	 * @param metodos
	 *            Nó da árvore sintática correspondente à lista de método
	 */
	private void geraNoListaMetodoDecl(NoLista metodos) {
		// Verifica lista vazia
		if (metodos == null) {
			return;
		}

		// Gera o código do método
		geraNoMetodoDecl((NoMetodoDecl) metodos.getNo());

		// Gera o código do próximo método
		geraNoListaMetodoDecl(metodos.getProximo());
	}

	/**
	 * Método para gerar o código intermediário de método
	 * 
	 * @param metodo
	 *            Nó da árvore sintática correspondente ao método
	 */
	private void geraNoMetodoDecl(NoMetodoDecl metodo) {
		// Verifica método inexistente
		if (metodo == null) {
			return;
		}

		// Armazena a tabela de símbolo atual
		TabelaSimbolo temporaria = tabelaAtual;
		// Entrada dos parâmetros do método
		SimboloParametro param = null;
		// Entrada do método
		SimboloMetodo m = null;
		// Entrada do tipo do método
		SimboloEntrada tipo = null;
		// Nó da lista de parâmetros
		NoLista p = null;
		// Nó da declaração de variável (lista de parâmetro)
		NoVariavelDecl varDecl = null;
		// Nó da variável (lista de parâmetro)
		NoVariavel var = null;

		// Percorre todos os parâmetros do método
		for (p = metodo.getCorpo().getParametros(); p != null; p = p
				.getProximo()) {
			// Obtém as variáveis
			varDecl = (NoVariavelDecl) p.getNo();
			var = (NoVariavel) varDecl.getVariaveis().getNo();

			// Obtém os tipos da tabela de símbolo
			tipo = tabelaAtual.buscaTipo(varDecl.getToken().image);

			// Cria a lista de parâmetros
			if (param == null) {
				param = new SimboloParametro(tipo, var.getTamanho());
			} else {
				param.setProximo(new SimboloParametro(tipo, var.getTamanho()));
			}
		}

		// Busca o método registrado na tabela de símbolo
		m = tabelaAtual.buscaMetodo(metodo.getNome().image, param);

		// Atualiza o método atual
		metodoAtual = m;

		// Troca a tabela de símbolo atual pela tabela de símbolo do escopo do
		// método
		tabelaAtual = m.getTabela();

		// Gera o código do método
		code();
		code("; Declaração do método " + m.getNome());
		code(".method private static " + m.getNome() + "("
				+ (param == null ? "" : param.descJava()) + ")"
				+ m.getTipo().descJava());
		// Total de variáveis locais para alocação
		code(".limit locals " + (m.getTotalLocal() + 1));

		// Inicia o escopo do método na tabela de símbolos
		tabelaAtual.iniciaEscopo();

		// Zera as variáveis de controle de pilha
		tamanhoPilha = 0;
		alturaPilha = 0;
		totalLocal = 0;

		// Gera o código do corpo do método
		geraNoCorpoMetodo(metodo.getCorpo());

		// Finaliza o escopo do método na tabela de símbolos
		tabelaAtual.terminaEscopo();

		// Gera o retorno do método
		if (m.getTipo().equals(tipoInteiro) && m.getTamanho() == 0) {
			code("bipush 0", 1);
			code("ireturn", -1);
		} else if (!m.getTipo().equals(tipoVazio)) {
			code("aconst_null", 1);
			code("areturn", -1);
		}

		code("return");
		// Define o tamanho máximo da pilha no método
		code(".limit stack " + (m.getTamanhoPilha() + alturaPilha));
		code(".end method");

		// Retoma a tabela de símbolo armazenada
		tabelaAtual = temporaria;
	}

	/**
	 * Método para gerar o código intermediário do corpo do método
	 * 
	 * @param corpo
	 *            Nó da árvore sintática correspondente ao corpo do método
	 */
	private void geraNoCorpoMetodo(NoCorpoMetodo corpo) {
		// Verifica corpo do método vazio
		if (corpo == null) {
			return;
		}

		// Gera o código dos parâmetros
		geraNoListaVariavelDecl(corpo.getParametros());

		// Gera o código do bloco do método
		geraNoBloco(corpo.getBloco());
	}

	/**
	 * Método para gerar o código intermediário da lista de declaração de
	 * variáveis
	 * 
	 * @param variaveis
	 *            Nó da árvore sintática correspondente a lista de variáveis
	 *            declaradas
	 */
	private void geraNoListaVariavelDecl(NoLista variaveis) {
		// Verifica lista de variáveis vazia
		if (variaveis == null) {
			return;
		}

		// Gera o código da variável
		geraNoVariavelDecl((NoVariavelDecl) variaveis.getNo());

		// Gera o código da próxima variável
		geraNoListaVariavelDecl(variaveis.getProximo());
	}

	/**
	 * Método para gerar o código intermediário da declaração de variável
	 * 
	 * @param variavel
	 *            Nó da árvore sintática correspondente a declaração de variável
	 */
	private void geraNoVariavelDecl(NoVariavelDecl variavel) {
		// Verifica declaração de variável vazia
		if (variavel == null) {
			return;
		}

		// Tipo das variáveis
		SimboloEntrada tipo = null;
		// Nó da variável
		NoVariavel var = null;
		// Nó da lista de variáveis
		NoLista vars = null;

		// Busca o tipo das variáveis na tabela de símbolo
		tipo = tabelaAtual.buscaTipo(variavel.getToken().image);

		// Percorre todas as variáveis a serem declaradas
		for (vars = variavel.getVariaveis(); vars != null; vars = vars
				.getProximo()) {
			var = (NoVariavel) vars.getNo();

			// Registra na tabela de símbolo do método atual
			tabelaAtual.adiciona(new SimboloVariavel(tipo,
					var.getToken().image, var.getTamanho(), totalLocal++));
		}
	}

	/**
	 * Método para gerar o código intermediário do bloco
	 * 
	 * @param bloco
	 *            Nó da árvore sintática correspondente ao bloco
	 */
	private void geraNoBloco(NoBloco bloco) {
		// Verifica bloco vazio
		if (bloco == null) {
			return;
		}

		// Inicia o escopo do bloco
		tabelaAtual.iniciaEscopo();

		// Gera o código do bloco
		geraNoListaDeclaracao(bloco.getDeclaracoes());

		// Finaliza o escopo do bloco
		tabelaAtual.terminaEscopo();
	}

	/**
	 * Método para gerar o código intermediário da lista de declarações
	 * 
	 * @param declaracoes
	 *            Nó da árvore sintática correspondente à lista de declarações
	 */
	private void geraNoListaDeclaracao(NoLista declaracoes) {
		// Verifica lista de declarações vazia
		if (declaracoes == null) {
			return;
		}

		// Gera o código intermediário da declaração
		geraNoDeclaracao((NoDeclaracao) declaracoes.getNo());

		// Gera o código intermediário da próxima declaração
		geraNoListaDeclaracao(declaracoes.getProximo());
	}

	/**
	 * Meétodo para gerar o código intermediário de uma declaração
	 * 
	 * @param declaracao
	 *            Nó da árvore sintática correspondente à declaração
	 */
	private void geraNoDeclaracao(NoDeclaracao declaracao) {
		// Verifica declaração vazia
		if (declaracao == null) {
			return;
		}

		// Gera o código da declaração correspondente
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
	 * Método para gerar o código intermediário de atribuição
	 * 
	 * @param atribuicao
	 */
	public void geraNoAtribuicao(NoAtribuicao atribuicao) {
		// Verifica atribuição vazia
		if (atribuicao == null) {
			return;
		}

		// Controle para somente alocar na pilha
		armazena = false;
		// Gera o código da expressão à direita
		geraNoExpressao(atribuicao.getDireita());

		// Controle para armazenar na pilha
		armazena = true;
		// Gera o código da expressão à esquerda
		geraNoExpressao(atribuicao.getEsquerda());
	}

	/**
	 * Método para gerar o código intermediário do comando imprimir
	 * 
	 * @param imprimir
	 *            Nó da árvore sintática correspondente do comando imprimir
	 */
	private void geraNoImprimir(NoImprimir imprimir) {
		// Verifica comando vazio
		if (imprimir == null) {
			return;
		}

		code();
		code("; Comando de escrita");
		// Coloca System.out na pilha de execução
		code("getstatic java/lang/System/out Ljava/io/PrintStream;", 1);

		// Controle para não armazenar
		armazena = false;

		// Gera o código da expressão
		Tipo valor = geraNoExpressao(imprimir.getValor());

		// Executa a impressão no console
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
	 * Método para gerar o código intermediário do comando ler
	 * 
	 * @param ler
	 *            Nó da árvore sintática correspondente ao comando ler
	 */
	private void geraNoLer(NoLer ler) {
		// Verifica comando vazio
		if (ler == null) {
			return;
		}

		Tipo valor = null;

		try {
			// Obtém o tipo da expressão (no caso variável) usando a tabela de
			// símbolo atual
			valor = semantico.analisaTipoNoExpressao(ler.getVariavel(),
					tabelaAtual);
		} catch (ErroSemanticoException e) {
			System.out.println(e.getMessage());
			return;
		}

		code();
		code("; Comando de leitura");

		// Gera o código do comando correspondente ao tipo de dado
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

		// Operação de armazenagem
		armazena = true;
		// Gera o código da expressão a ser lida
		geraNoExpressao(ler.getVariavel());
	}

	/**
	 * Método para gerar o código intermediário do comando retornar
	 * 
	 * @param retornar
	 *            Nó da árvore sintática correspondente ao comando retornar
	 */
	private void geraNoRetornar(NoRetornar retornar) {
		// Verifica comando vazio
		if (retornar == null) {
			return;
		}

		code();
		code("; Comando de retorno");

		// Controle para não armazenar
		armazena = false;
		// Gera o código da expressão (se existir)
		Tipo valor = geraNoExpressao(retornar.getValor());

		// Verifica o tipo da expressão para gerar o código coerente
		if (valor.getEntrada().equals(tipoInteiro) && valor.getTamanho() == 0) {
			code("ireturn", -1);
		} else {
			code("areturn", -1);
		}
	}

	/**
	 * Método para gerar o código intermediário do controle SE
	 * 
	 * @param se
	 *            Nó da árvrore sintática correspondente ao controle SE
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

	/**
	 * Método para gerar o código intermediário da chamada de método
	 * 
	 * @param chamada
	 *            Nó da árvore sintática correspondente à chamada de método
	 * @return Tipo do método chamado
	 */
	private Tipo geraNoChamadaDecl(NoChamadaDecl chamada) {
		// Gera o código da chamada do método
		return geraNoChamada(new NoChamada(chamada.getToken(),
				chamada.getArgumentos()));
	}

	/**
	 * Método para gerar o código intermediário da chamada de método
	 * 
	 * @param chamada
	 *            Nó da árvore sintática correspondente à chamada de método
	 * @return Tipo do método chamado
	 */
	private Tipo geraNoChamada(NoChamada chamada) {
		// Verifica chamada vazia
		if (chamada == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Chamada do metodo " + chamada.getToken().image.toUpperCase());

		// Entrada dos parâmetros
		SimboloParametro params = null;
		// Lista de parâmetros
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
		code("invokestatic " + CLASSE + "/" + metodo.getNome() + "(" + args
				+ ")" + metodo.descJava(), -pilha);

		// Retorna o tipo do método
		return new Tipo(metodo.getTipo(), metodo.getTamanho());
	}

	/**
	 * Método para gerar o código intermediário de expressões
	 * 
	 * @param expressao
	 *            Nó da árvore sintática correspondente a uma expressão
	 * @return Tipo da expressão
	 */
	private Tipo geraNoExpressao(NoExpressao expressao) {
		// Verifica expressão vazia
		if (expressao == null) {
			return new Tipo(tipoNulo, 0);
		}

		// Gera o código conforme o tipo da expressão
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
	 * Método para gerar o código intermediário de uma alocação de vetor
	 * 
	 * @param alocacao
	 *            Nó da árvore sintática correspondente a uma alocação
	 * @return Tipo da alocação
	 */
	private Tipo geraNoAlocacao(NoAlocacao alocacao) {
		// Verifica alocação vazia
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

		return new Tipo(tipo, tamanho);
	}

	/**
	 * Método para gerar o código intermediário de uma relação
	 * 
	 * @param relacional
	 *            Nó da árvore sintática correspondene a uma relação
	 * @return Tipo da relação
	 */
	private Tipo geraNoRelacional(NoRelacional relacional) {
		if (relacional == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Relação");

		// Cria um novo label
		String label = novoLabel();

		// Gera o código da expressão à esquerda
		int operacao = relacional.getToken().kind;
		Tipo esquerda = geraNoExpressao(relacional.getEsquerda());

		// Gera o código da expressão à direita
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
	 * Método para gerar o código intermediário de uma adição
	 * 
	 * @param adicao
	 *            Nó da árvore sintática correspondente a uma adição
	 * @return Tipo da adição
	 */
	private Tipo geraNoAdicao(NoAdicao adicao) {
		// Verifica adição vazia
		if (adicao == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Adição");

		// Obtém a operação
		int operacao = adicao.getToken().kind;

		// Contadores de tipos
		int inteiro = 0;
		int decimal = 0;
		int texto = 0;

		// Gera o código da expressão à esquerda
		Tipo esquerda = geraNoExpressao(adicao.getEsquerda());

		// Gera o código da expressão à direita
		Tipo direita = geraNoExpressao(adicao.getDireita());

		// Conta o tipo à esquerda
		if (esquerda.getEntrada().equals(tipoInteiro)) {
			inteiro++;
		} else if (esquerda.getEntrada().equals(tipoDecimal)) {
			decimal++;
		} else if (esquerda.getEntrada().equals(tipoTexto)) {
			texto++;
		}

		// Conta o tipo à direita
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
	 * Método para gerar o código intermediário de uma multiplicação
	 * 
	 * @param multiplicacao
	 *            Nó da árvore sintática correspondente a uma multiplicação
	 * @return Tipo da multiplicação
	 */
	private Tipo geraNoMultiplicacao(NoMultiplicacao multiplicacao) {
		// Verifica multiplicação vazia
		if (multiplicacao == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Multiplicacao");

		// Obtém a operação
		int operacao = multiplicacao.getToken().kind;

		// Gera o código da expressão à esquerda
		Tipo esquerda = geraNoExpressao(multiplicacao.getEsquerda());

		// Gera o código da expressão à direita
		Tipo direita = geraNoExpressao(multiplicacao.getDireita());

		// Gera o código da operação
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
	 * Método para gerar o código intermediário de um número unário
	 * 
	 * @param unario
	 *            Nó da árvore sintática correspondente a um número unário
	 * @return Tipo do número
	 */
	private Tipo geraNoUnario(NoUnario unario) {
		// Verifica número vazio
		if (unario == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Unario");

		// Operação
		int operacao = unario.getToken().kind;

		// Gera o código do fator
		Tipo fator = geraNoExpressao(unario.getFator());

		// Verifica negação
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
	 * Método para gerar o código intermediário de um número inteiro
	 * 
	 * @param inteiro
	 *            Nó da árvore sintática correspondente a um número inteiro
	 * @return Tipo inteiro
	 */
	private Tipo geraNoInteiro(NoInteiro inteiro) {
		// Verifica número vazio
		if (inteiro == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Constante inteira");
		code("ldc " + inteiro.getToken().image, 1);

		return new Tipo(tipoInteiro, 0);
	}

	/**
	 * Método para gerar o código intermediário de um número decimal
	 * 
	 * @param decimal
	 *            Nó da árvore sintática correspondente a um número decimal
	 * @return Tipo decimal
	 */
	private Tipo geraNoDecimal(NoDecimal decimal) {
		// Verifica número vazio
		if (decimal == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Constante decimal");
		code("ldc " + decimal.getToken().image, 1);

		return new Tipo(tipoDecimal, 0);
	}

	/**
	 * Método para gerar o código intermediário de um texto
	 * 
	 * @param texto
	 *            Nó da árvore sintática correspondente a um texto
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
	 * Método para gerar o código intermediário do tipo nulo
	 * 
	 * @param nulo
	 *            Nó da árvore sintática correspondente ao tipo nulo
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
	 * Método para gerar o código intermediário de um array
	 * 
	 * @param array
	 *            Nó da árvore sintática correspondente à um arrau
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
		// Busca a variável do array
		SimboloVariavel var = tabelaAtual.buscaVariavel(array.getToken().image);

		armazena = false;
		// Gera o código de todos os índices acessados do array
		for (expr = array.getExpressoes(); expr != null; expr = expr
				.getProximo()) {
			geraNoExpressao((NoExpressao) expr.getNo());
		}

		// Verifica se é operacão para armazenar ou não
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
	 * Método para gerar o código intermediário de uma variável
	 * 
	 * @param variavel
	 *            Nó da árvore sintática correspondente a uma variável
	 * @return Tipo da variável
	 */
	private Tipo geraNoVariavel(NoVariavel variavel) {
		// Verifica variável vazia
		if (variavel == null) {
			return new Tipo(tipoNulo, 0);
		}

		code();
		code("; Variavel");

		// Calcula a mudança na pilha
		int pilha = (armazena ? -1 : 1);

		// Defina a operação de armazenamento ou carregamento
		String ope = (armazena ? "store" : "load");

		// Busca a variável na tabela de símbolo
		SimboloVariavel var = tabelaAtual
				.buscaVariavel(variavel.getToken().image);
		
		// Gera o código correspondente
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