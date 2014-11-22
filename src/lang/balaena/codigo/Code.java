package lang.balaena.codigo;

import lang.balaena.simbolos.SimboloEntrada;

/**
 * Classe responsável por gerar alguns códigos intermediários
 */
public class Code {

	/**
	 * Método para gerar o código Jasmin correspondente a arrays
	 * 
	 * @param tamanho
	 *            Tamanho do array
	 * @return Código Jasmin equivalente ao array desejado
	 */
	public static String descJava(int tamanho) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tamanho; i++) {
			sb.append("[");
		}
		return sb.toString();
	}

	/**
	 * Método para gerar código Jasmin correspondente a tipos primitivos
	 * 
	 * @param entrada
	 *            Entrada da tabela de símbolo
	 * @return Código Jasmin equivalente ao tipo
	 */
	public static String descJava(SimboloEntrada entrada) {
		String nome = entrada.getNome().toLowerCase();
		if (nome.equals("inteiro")) {
			return "I";
		} else if (nome.equals("decimal")) {
			return "F";
		} else if (nome.equals("texto")) {
			return descJava(String.class);
		} else {
			return "V";
		}
	}

	/**
	 * Método para gerar código Jasmin correspondente a alguma classe
	 * 
	 * @param clas
	 *            Classe desejada
	 * @return Código Jasmin equivalente a uma classe
	 */
	public static String descJava(Class<?> clas) {
		return "L" + clas.getName().replace(".", "/") + ";";
	}

}
