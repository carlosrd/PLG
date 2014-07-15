package AnalizadorSintactico.TSimbolos;

public class ClaveTS implements Cloneable {

	// ATRIBUTOS
	// ******************************************************************************************

	String id;
	int nivel;
	
	// ATRIBUTOS
	// ******************************************************************************************
	
	public ClaveTS(String id, int nivel){
		this.id = id;
		this.nivel = nivel;
	}
	
	// GETTERS
	// ******************************************************************************************
	
	public String getId() {
		return id;
	}


	public int getNivel() {
		return nivel;
	}

	// EQUALS & HASH CODE
	// ******************************************************************************************
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + nivel;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClaveTS other = (ClaveTS) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (nivel != other.nivel)
			return false;
		return true;
	}
	
	 public Object clone(){
	        Object obj=null;
	        try{
	            obj=super.clone();
	        }catch(CloneNotSupportedException ex){
	            System.out.println(" no se puede duplicar");
	        }
	        return obj;
	    }
}
