package lang.balaena.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Classe do runtime da Linguagem Balaena
 *
 */
public class BalaenaRuntime {

	// Reader do runtime
	private static BufferedReader in;

	/**
	 * Método para inicia o runtime
	 * 
	 * @return Sucesso da inicialização
	 */
	public static int inicia() {
		// Instancia o reader do System.in (console)
		in = new BufferedReader(new InputStreamReader(System.in));
		if (in == null) {
			System.err.println("Não foi possível iniciar o BalaenaRuntime");
			return -1;
		}
		return 0;
	}

	/**
	 * Método para ler um número inteiro do console
	 * 
	 * @return Número inteiro lido
	 */
	public static int lerInteiro() {
		try {
			// Lê a linha digitada
			String linha = in.readLine();

			// Converte para inteiro
			return Integer.parseInt(linha);
		} catch (IOException e) {
			System.err.println("Ocorreu um problema ao ler o número inteiro");
			System.err.println("Motivo: " + e.getMessage());
			return 0;
		} catch (NumberFormatException e1) {
			return 0;
		}
	}

	/**
	 * Método para ler um número decimal do console
	 * 
	 * @return Número decimal lido
	 */
	public static Double lerDecimal() {
		try {
			// Lê a linha digitada
			String linha = in.readLine();

			// Converte para decimal
			return Double.parseDouble(linha);
		} catch (IOException | NumberFormatException e) {
			System.err.println("Ocorreu um problema ao ler o número decimal");
			System.err.println("Motivo: " + e.getMessage());
			return 0.0;
		}
	}

	/**
	 * Método para ler um texto do console
	 * 
	 * @return Texto lido
	 */
	public static String lerTexto() {
		try {
			// Lê a linha digitada
			return in.readLine();
		} catch (IOException e) {
			System.err.println("Ocorreu um problema ao ler o texto");
			System.err.println("Motivo: " + e.getMessage());
			return "";
		}
	}

	/**
	 * Método para finalizar o runtime
	 */
	public static void finaliza() {
		try {
			// Fecha o reader
			in.close();
		} catch (IOException e) {
			System.err
					.println("Ocorreu um problema ao finalizar o BalaenaRuntime");
		}
	}

}
