package lang.balaena;


public class BLangMain {

	public static void main(String[] args) {
		// Vari�veis referentes aos argumentos
		boolean debug = false;
		boolean recuperacao = false;
		boolean arvore = false;
		boolean intermediario = false;
		String arquivo = "";

		System.out.println("COMPILADOR BALAENA - VERS�O 0.1 *");
		System.out.println();
		System.out.println("Desenvolvido por:");
		System.out.println(" - Jo�o Vitor Retamero");
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
							.println("N�o foi poss�vel identificar o arquivo");
				}
			}
		}

		// Caso tenha sido informado um arquivo, executa o compilador
		if (arquivo != null && !arquivo.isEmpty()) {
			System.out.println("Analisando arquivo \"" + arquivo + "\"");

			// Obt�m uma nova inst�ncia do BLangMotor
			BLangMotor parser = BLangMotor.getInstance(arquivo);

			// Debug do analisador sint�tico
			if (!debug) {
				parser.disable_tracing();
			}

			// Exibi��o da recupera��o de erros
			parser.setDebugRecuperacao(recuperacao);

			// Exibi��o da �rvore sint�tica
			parser.setDebugArvore(arvore);

			// Exibi��o do c�digo intermedi�rio
			parser.setIntermediario(intermediario);

			// Defini��o do c�digo fonte
			parser.setArquivo(arquivo);

			try {
				// Inicia a execu��o do compilador
				parser.executar();
			} catch (ParseException e) {
				System.out.println(e.getMessage());
			}
		} else {
			System.out.println("N�o foi poss�vel obter o arquivo.");
		}
	}

}
