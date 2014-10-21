package lang.balaena.arvore;

public class NoLista extends No {

	private No no;
	private NoLista proximo;

	public NoLista(No no) {
		super(no.getToken());
		this.no = no;
		proximo = null;
	}

	public NoLista(No no, NoLista lista) {
		super(no.getToken());
		this.no = no;
		this.proximo = lista;
	}

	public void adiciona(No no) {
		if (proximo == null) {
			proximo = new NoLista(no);
		} else {
			proximo.adiciona(no);
		}
	}

	@Override
	public void setNumero(int numero) {
		super.setNumero(numero);
		if (no != null) {
			no.setNumero(++numero);
		}
		if (proximo != null) {
			proximo.setNumero(++numero);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " (" + No.getClasse(no) + ")" + " > "
				+ No.getNumero(no) + " " + No.getNumero(proximo)
				+ No.toString(no) + No.toString(proximo);
	}

	public No getNo() {
		return no;
	}

	public NoLista getProximo() {
		return proximo;
	}

}
