package lang.balaena.simbolos;

public class SimboloSimples extends SimboloEntrada {

	public SimboloSimples(String nome) {
		super();
		this.setNome(nome);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimboloEntrada other = (SimboloEntrada) obj;
		if (getNome() == null) {
			if (other.getNome() != null)
				return false;
		} else if (!getNome().equals(other.getNome()))
			return false;
		return true;
	}
}
