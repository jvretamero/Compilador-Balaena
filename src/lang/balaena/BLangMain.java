package lang.balaena;

public class BLangMain {

	public static void main(String[] args) {
		boolean debug = false;
		boolean recuperacao = false;
		boolean arvore = false;
		String arquivo = "";

		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].toLowerCase().equals("-debug")) {
				debug = true;
			} else if (args[i].toLowerCase().equals("-recuperacao")) {
				recuperacao = true;
			} else if (args[i].toLowerCase().equals("-arvore")) {
				arvore = true;
			} else if (args[i].toLowerCase().equals("-f")) {
				if (i + 1 == args.length - 1) {
					arquivo = args[i + 1].toLowerCase();
				} else {
					System.out
							.println("Não foi possível identificar o arquivo");
				}
			}
		}

		if (arquivo != null && !arquivo.isEmpty()) {
			BLangMotor parser = BLangMotor.getInstance(arquivo, debug,
					recuperacao, arvore);

			try {
				parser.executar();
			} catch (ParseException e) {
				System.out.println(e.getMessage());
			}
		}
	}

}
