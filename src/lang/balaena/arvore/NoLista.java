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

}
