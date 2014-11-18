package lang.balaena;


public class BLangMain {

	public static void main(String[] args) {
		// Variáveis referentes aos argumentos
		boolean debug = false;
		boolean recuperacao = false;
		boolean arvore = false;
		boolean intermediario = false;
		String arquivo = "";

		System.out.println("COMPILADOR BALAENA - VERSÃO 0.1 *");
		System.out.println();
		System.out.println("Desenvolvido por:");
		System.out.println(" - João Vitor Retamero");
		System.out.println(" - Guilherme Henrique Perim");
		System.out.println(" - Rafael Fernando Oliveira");
		System.out.println();

		// Trata os argumentos recebidos
		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].toLowerCase().equals("-debug")) {
				debug = true;
			} else if (args[i].toLowerCase().equals("-rec")) {
				recuperacao = true;
			} else if (args[i].toLowerCase().equals("-arvore")) {
				arvore = true;
			} else if (args[i].toLowerCase().equals("-inter")) {
				intermediario = true;
			} else if (args[i].toLowerCase().equals("-f")) {
				if (i + 1 == args.length - 1) {
					arquivo = args[i + 1].toLowerCase();
				} else {
					System.out
							.println("Não foi possível identificar o arquivo");
				}
			}
		}

		// Caso tenha sido informado um arquivo, executa o compilador
		if (arquivo != null && !arquivo.isEmpty()) {
			System.out.println("Analisando arquivo \"" + arquivo + "\"");

			// Obtém uma nova instância do BLangMotor
			BLangMotor parser = BLangMotor.getInstance(arquivo);

			// Debug do analisador sintático
			if (!debug) {
				parser.disable_tracing();
			}

			// Exibição da recuperação de erros
			parser.setDebugRecuperacao(recuperacao);

			// Exibição da árvore sintática
			parser.setDebugArvore(arvore);

			// Exibição do código intermediário
			parser.setIntermediario(intermediario);

			// Definição do código fonte
			parser.setArquivo(arquivo);

			try {
				// Inicia a execução do compilador
				parser.executar();
			} catch (ParseException e) {
				System.out.println(e.getMessage());
			}
		} else {
			System.out.println("Não foi possível obter o arquivo.");
		}
	}

}
