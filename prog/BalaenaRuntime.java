import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BalaenaRuntime {

	private static BufferedReader in;

	public static int inicia() {
		in = new BufferedReader(new InputStreamReader(System.in));
		if (in == null) {
			System.err.println("Não foi possível iniciar o BalaenaRuntime");
			return -1;
		}
		return 0;
	}

	public static int lerInteiro() {
		try {
			String linha = in.readLine();
			return Integer.parseInt(linha);
		} catch (IOException | NumberFormatException e) {
			System.err.println("Ocorreu um problema ao ler o número inteiro");
			System.err.println("Motivo: " + e.getMessage());
			return 0;
		}
	}

	public static Double lerDecimal() {
		try {
			String linha = in.readLine();
			return Double.parseDouble(linha);
		} catch (IOException | NumberFormatException e) {
			System.err.println("Ocorreu um problema ao ler o número decimal");
			System.err.println("Motivo: " + e.getMessage());
			return 0.0;
		}
	}

	public static String lerTexto() {
		try {
			return in.readLine();
		} catch (IOException e) {
			System.err.println("Ocorreu um problema ao ler o texto");
			System.err.println("Motivo: " + e.getMessage());
			return "";
		}
	}

	public static void finaliza() {
		try {
			in.close();
		} catch (IOException e) {
			System.err
					.println("Ocorreu um problema ao finalizar o BalaenaRuntime");
		}
	}

}
