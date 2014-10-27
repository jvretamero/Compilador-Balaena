package lang.balaena.arvore;

public class NoChamadaDecl extends NoDeclaracao {

	private NoLista argumentos;

	public NoChamadaDecl(NoChamada chamada) {
		super(chamada.getToken());
		this.argumentos = chamada.getArgumentos();
	}

	public NoLista getArgumentos() {
		return argumentos;
	}

	@Override
	public void setNumero(int numero) {
		super.setNumero(numero);
		if (argumentos != null) {
			argumentos.setNumero(++numero);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " > " + No.getNumero(argumentos)
				+ No.toString(argumentos);
	}
}
