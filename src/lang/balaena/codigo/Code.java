package lang.balaena.codigo;

import lang.balaena.simbolos.SimboloEntrada;

/**
 * Classe respons�vel por gerar alguns c�digos intermedi�rios
 */
public class Code {

	/**
	 * M�todo para gerar o c�digo Jasmin correspondente a arrays
	 * 
	 * @param tamanho
	 *            Tamanho do array
	 * @return C�digo Jasmin equivalente ao array desejado
	 */
	public static String descJava(int tamanho) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tamanho; i++) {
			sb.append("[");
		}
		return sb.toString();
	}

	/**
	 * M�todo para gerar c�digo Jasmin correspondente a tipos primitivos
	 * 
	 * @param entrada
	 *            Entrada da tabela de s�mbolo
	 * @return C�digo Jasmin equivalente ao tipo
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
	 * M�todo para gerar c�digo Jasmin correspondente a alguma classe
	 * 
	 * @param clas
	 *            Classe desejada
	 * @return C�digo Jasmin equivalente a uma classe
	 */
	public static String descJava(Class<?> clas) {
		return "L" + clas.getName().replace(".", "/") + ";";
	}

}
