package lang.balaena.codigo;

import lang.balaena.simbolos.SimboloEntrada;

public class Code {

	public static String descJava(int tamanho) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tamanho; i++) {
			sb.append("[");
		}
		return sb.toString();
	}

	public static String descJava(SimboloEntrada entrada) {
		String nome = entrada.getNome().toLowerCase();
		if (nome.equals("inteiro")) {
			return "I";
		} else if (nome.equals("decimal")) {
			return descJava(Double.class);
		} else if (nome.equals("texto")) {
			return descJava(String.class);
		} else {
			return "V";
		}
	}

	public static String descJava(Class<?> clas) {
		return clas.getName().replace(".", "/");
	}

}
